/*********************************************
* PROGRAMME DE TEST DE PERFORMANCE DE PAILLIER
**********************************************
*   0. AUTEURS :
*       Lyderic L. <lyderic.lefebvre@live.fr>
*       Jordan DLV.
*       Yannick H.
*
*   1. INSTALLATION DES DEPENDANCES (libpaillier, GMP) :
*       Tapez ./install-dependances.sh
*
*   2. COMPILATION DU programme de test :
*        Tapez make
*
*   3. USAGE :
*       ./bin/paillier inputFile_name outputFile_name nbLignes modulusbit
*	Ex : ./bin/paillier in.csv out.csv 1000 2048
*
*       - inputFile_name et outputFile_name sont le nom des fichiers entrée et sortie
*       - nbLignes est le nombre de ligne du fichier entrée
*       - modulusbit (128, 256, 512, 1024, 2048, ...)
*
*	Le program doit etre lancé depuis le repertoire racine (celui du makefile).
*	Le fichier sortie se trouve dans le repertoire ./data
*	Le fichier entrée doit se trouver dans le repertoire ./data/
*       Le format doit etre :
*	A1
*	A2
*	A3
*	...
**********************************************/

#include <stdio.h>
#include <stdlib.h>
#include <gmp.h>
#include <paillier.h>
#include <time.h>
#include <string.h>

int main(int argc, char *argv[])
{
    if (argc != 5)
    {
        printf( "\nIl manque des arguments.\n" );
        printf( "\tUSAGE : ./bin/paillier inputFile_name outputFile_name nbLignes modulusbit\n\n" );
        printf( "\t- inputFile_name et outputFile_name sont les noms des fichiers entrée et sortie\n" );
        printf( "\t- nbLignes est le nombre de ligne du fichier entrée.\n" );
        printf( "\t- modulusbit (128, 256, 512, 1024, 2048, ...)\n\n" );
        printf( "\tLe fichier entrée doit se trouver dans le repertoire data\n\n" );
        printf( "\tLe format doit etre :\n\tA1\n\tA2\n\tA3\n\t...\n\n" );
        return 0;
    }

    printf("\n\n=============");
    printf("\n== PAILLIER =="  );
    printf("\n=============");


    // DECLARATIONS DES VARIABLES
    int i = 0, j = 0, nbBytes = 0, nbOp = atoi(argv[3]), modulusbit = atoi(argv[4]), tab[nbOp];
    int t[3];
    char inputFile_path[100], outputFile_path[100];

    const int n = 1024;
    const int m = 2000;

    clock_t start, finish;
    double tps_enc = 0;
    double tps_sum = 0;
    double tps_dec = 0;
    double tps_adfunc = 0;

    paillier_plaintext_t tt[n], SS[n];
    paillier_ciphertext_t et[n], S[n];
    paillier_plaintext_t pt, tab_pt[nbOp];
    paillier_ciphertext_t tab_enc[nbOp];
    paillier_pubkey_t * pub = NULL;
    paillier_prvkey_t * prv = NULL;
    strcpy(inputFile_path,  "./data/"); strcpy(outputFile_path,  "./data/");
    FILE * inputFile = fopen( strcat(inputFile_path, argv[1]), "r" );
    FILE * outputFile = fopen( strcat(outputFile_path, argv[2]), "w" );


    // LECTURE FICHIER ENTREE
    printf( "\n\n=> Lecture du fichier en entrée..." );
    for (i=0 ; i<nbOp ; i++)
    {
        fscanf( inputFile, "%d", &(tab[i]) );
        tab_pt[i] = *(paillier_plaintext_from_ui( tab[i] ));
    }

    srand((int)time(0));
    for (i=0; i<n; i++)
    {
        t[i] = rand()%20000 + 200;
    	tt[i] = *(paillier_plaintext_from_ui( t[i]  ));
        SS[i] = *(paillier_plaintext_from_ui( t[n-1-i] ));
    }

    void *s;
    int ss = 1;
    s = &ss;


    // GENERATION DE LA PAIRE DE CLEFS
    printf("\n=> Generation de la paire de clefs...");
    paillier_keygen( modulusbit, &pub, &prv, paillier_get_rand_devurandom );


    // CHIFFREMENT DE TOUS LES NOMBRES DU FICHIER EN ENTREE
    printf( "\n=> Chiffrement de tous les nombres du fichier en entrée..." );
    start = clock();
    for (i=0 ; i<nbOp ; i++) { tab_enc[i] = *(paillier_enc( NULL, pub, &(tab_pt[i]), paillier_get_rand_devurandom )); }
    finish = clock();
    tps_enc = ((float)finish-start)/CLOCKS_PER_SEC;


    //additonal function
    start = clock();
    for (i=0; i<n; i++)
    {
    	et[i] = *(paillier_enc(NULL, pub, &(tt[i]), paillier_get_rand_devurandom));
        S[i] = *(paillier_enc(NULL, pub, &(SS[i]), paillier_get_rand_devurandom));
    }
    //finish = clock();
    //start = clock();
    for(j=0;j<n;j++){
        for (i=0; i<m; i++){
            paillier_mul(pub, &(S[j]), &(S[j]), &(et[j]));
        }

    }

   int length = n;
   int offset = (length - 1)/2 + 1;
   while(offset >= 2){
       for(i=0; i<length/2; i++){
           paillier_mul(pub, &(S[i]), &(S[i]), &(S[i+offset]));
       }

       length = (length - 1)/2 + 1;
       offset = (offset - 1)/2 + 1;
   }

    finish = clock();
    tps_adfunc = ((float)finish - start)/CLOCKS_PER_SEC;

    // ECRITURE DES NOMBRES CHIFFRES EN SORTIE
    printf( "\n=> Generation du fichier sortie : %s", outputFile_path );
    for (i=0 ; i<nbOp ; i++)
    {
	nbBytes = nbBytes + fprintf( outputFile, "%s\n", mpz_get_str(0, 16, tab_enc[i].c) );
    }


    // ADDITIONS
    printf( "\n=> Additions homomorphiques..." );
    start = clock();
    for (i=1 ; i<nbOp ; i++) { paillier_mul( pub, &(tab_enc[0]), &(tab_enc[0]), &(tab_enc[i]) ); }
    finish = clock();
    tps_sum = ((float)finish-start)/CLOCKS_PER_SEC;


    // DECHIFFREMENT DU RESULTAT
    printf( "\n=> Dechiffrement du resultat... " );
    start = clock();
    for (i=0; i<nbOp; i++)
        pt = *(paillier_dec( NULL, pub, prv, &(tab_enc[0]) ));
    
    finish = clock();
    tps_dec = ((float)finish-start)/CLOCKS_PER_SEC;
    gmp_printf("La somme est : %Zd", &pt);


    // DESALOCCATION DES RESSOURCES
    printf( "\n=> Liberation des ressources...\n\n" );
    paillier_freepubkey( pub );
    paillier_freeprvkey( prv );
    fclose( inputFile );
    fclose( outputFile );


    // AFFICHAGE DES STATS
    printf( "\n-- Statistiques :");
    printf( "\n\t-- Temps encryption %d entiers : %.5fsec ", nbOp, tps_enc );
    printf( "\n\t-- Temps pour faire %d sum : %.5fsec ", nbOp, tps_sum );
    printf( "\n\t-- Temps pour dechiffrer %d entiers : %.5fsec", nbOp, tps_dec );
    printf( "\n\t-- Time for additional funciton : %.5fsec", tps_adfunc );
    printf( "\n\t-- Poids fichier avec %d nombres encryptés : %d octets\n", nbOp, nbBytes );

    return 0;
}
