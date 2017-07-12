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
#include <omp.h>
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
    char inputFile_path[100], outputFile_path[100];
    const int n=1024;
    const int L=2000;
    int t[n];

    double start, finish, global_start, global_finish;
    double tps_enc = 0;
    double tps_sum = 0;
    double tps_dec = 0;
    double tps_key = 0;
    double tps_adfunc = 0;

    paillier_plaintext_t pt[nbOp], tab_pt[nbOp];
    paillier_ciphertext_t tab_enc[nbOp];
    paillier_ciphertext_t tab_enc0[nbOp];
    paillier_plaintext_t tt[n], SS[n];
    paillier_ciphertext_t et[n], S[n];
    paillier_pubkey_t * pub = NULL;
    paillier_pubkey_t * pub0[nbOp];
    paillier_prvkey_t * prv = NULL;
    strcpy(inputFile_path,  "./data/"); strcpy(outputFile_path,  "./data/");
    FILE * inputFile = fopen( strcat(inputFile_path, argv[1]), "r" );
    FILE * outputFile = fopen( strcat(outputFile_path, argv[2]), "w" );

    global_start = omp_get_wtime();

    // LECTURE FICHIER ENTREE
    printf( "\n\n=> Lecture du fichier en entrée..." );
    
    #pragma omp parallel for
    for (i=0 ; i<nbOp ; i++)
    {
    	fscanf( inputFile, "%d", &(tab[i]) );
    	tab_pt[i] = *(paillier_plaintext_from_ui( tab[i] ));
    }

    srand((int)time(0));
    #pragma omp parallel for
    for (i=0; i<n; i++)
    {
        t[i] = rand()%20000 + 200;
    	tt[i] = *(paillier_plaintext_from_ui( t[i] ));
        SS[i] = *(paillier_plaintext_from_ui( t[n-1-i] ));
    }
    
    /*void * s;
    int ss = 1;
    s = &ss;
    #pragma omp parallel for
    for (i=0; i<n; i++) { SS[i] = *(paillier_plaintext_from_ui( t[n-1-i] ));  }*/

    // GENERATION DE LA PAIRE DE CLEFS
    printf("\n=> Generation de la paire de clefs...");
    start = omp_get_wtime();
    paillier_keygen( modulusbit, &pub, &prv, paillier_get_rand_devurandom );
    finish = omp_get_wtime();
    tps_key = finish-start;

    // CHIFFREMENT DE TOUS LES NOMBRES DU FICHIER EN ENTREE
    printf( "\n=> Chiffrement de tous les nombres du fichier en entrée..." );
    start = omp_get_wtime();

    //#pragma omp parallel for
    //for (i=0 ; i<nbOp ; i++) { tab_enc[i] = *(paillier_enc( NULL, pub, &(tab_pt[i]), paillier_get_rand_devurandom )); }
   paillier_para_enc(nbOp, pub, tab_pt, tab_enc);
   finish = omp_get_wtime();
   tps_enc = finish-start;

    start = omp_get_wtime();
    /*#pragma omp parallel for
    for (i=0; i<n; i++)
    {
    	et[i] = *(paillier_enc( NULL, pub, &(tt[i]), paillier_get_rand_devurandom));
        S[i] = *(paillier_enc( NULL, pub, &(SS[i]), paillier_get_rand_devurandom ));
    }*/
    paillier_para_enc(n, pub, tt, et);
    paillier_para_enc(n, pub, SS, S);
    finish = omp_get_wtime();
    start = omp_get_wtime();
 /*   for (j=0; j<n; j+=8){
        #pragma omp parallel sections
        {
            #pragma omp section
            {
                if(j < n)
                {
                    for (i=0; i<L; i++)
                    {
                        paillier_mul( pub, &(S[j]), &(S[j]), &(et[j]) );
                    }
                }
            }
        
            #pragma omp section
            {
                if(j+1 < n)
                {
                    for (i=0; i<L; i++)
                    {
                        paillier_mul( pub, &(S[j+1]), &(S[j+1]), &(et[j+1]) );
                    }
                }
            }

            #pragma omp section
            {
                if(j+2 < n)
                {
                    for (i=0; i<L; i++)
                    {
                        paillier_mul( pub, &(S[j+2]), &(S[j+2]), &(et[j+2]) );
                    }
                } 
            }

            #pragma omp section
            {
                if(j+3 < n)
                {
                    for (i=0; i<L; i++)
                    {
                        paillier_mul( pub, &(S[j+3]), &(S[j+3]), &(et[j+3]) );
                    }
                }
            }

            #pragma omp section
            {
                if(j+4 < n)
                {
                    for (i=0; i<L; i++)
                    {
                        paillier_mul( pub, &(S[j+4]), &(S[j+4]), &(et[j+4]) );
                    }
                }
            }

            #pragma omp section
            {
                if(j+5 < n)
                {
                    for( i=0; i<L; i++)
                    {
                        paillier_mul( pub, &(S[j+5]), &(S[j+5]), &(et[j+5]) );
                    }
                }
            }

            #pragma omp section
            {
                if(j+6 < n)
                {
                    for( i=0; i<L; i++ )
                    {
                        paillier_mul( pub, &(S[j+6]), &(S[j+6]), &(et[j+6]) );
                    }
                }
            }

            #pragma omp section
            {
                if(j+7 < n)
                {
                    for( i=0; i<L; i++)
                    {
                        paillier_mul( pub, &(S[j+7]), &(S[j+7]), &(et[j+7]) );
                    }
                }
            }

        }
    }

    //paillier_mul(pub, &(S[1]), &(S[1]), &(S[2]) );
    //paillier_mul(pub, &(S[0]), &(S[0]), &(S[1]) );
    
    int length = n;
    int offset = (length - 1)/2 + 1;
    while(length >= 2){
        #pragma omp parallel for
        for(i=0; i<length/2; i++)
        {
            paillier_mul(pub, &(S[i]), &(S[i]), &(S[i+offset]));
        }

        length = (length - 1)/2 + 1;
        offset = (offset - 1)/2 + 1;
    }
*/
    paillier_cipheradd(n, L, pub, et, S);
    finish = omp_get_wtime();
    tps_adfunc = finish-start;


    // ECRITURE DES NOMBRES CHIFFRES EN SORTIE
    printf( "\n=> Generation du fichier sortie : %s", outputFile_path );
    

    //#pragma omp parallel for
    for (i=0 ; i<nbOp ; i++)
    {
	    nbBytes = nbBytes + fprintf( outputFile, "%s\n", mpz_get_str(0, 16, tab_enc[i].c) );
    }

    // ADDITIONS
    printf( "\n=> Additions homomorphiques..." );
    start = omp_get_wtime();

    //#pragma omp parallel for
    for (i=1 ; i<nbOp ; i++) { paillier_mul( pub, &(tab_enc[0]), &(tab_enc[0]), &(tab_enc[i]) ); }

    finish = omp_get_wtime();
    tps_sum = finish-start;


    // DECHIFFREMENT DU RESULTAT
    printf( "\n=> Dechiffrement du resultat... " );
    start = omp_get_wtime();
    //#pragma omp parallel for
    //for (i=0; i<nbOp; i++) {  pt[i] = *(paillier_dec( NULL, pub, prv, &(tab_enc[i]) ));}
    paillier_para_dec(nbOp, pub, prv, tab_enc, pt);
    finish = omp_get_wtime();
    tps_dec = finish-start;
    gmp_printf("La somme est : %Zd", &pt[0]);


    // DESALOCCATION DES RESSOURCES
    printf( "\n=> Liberation des ressources...\n\n" );
    paillier_freepubkey( pub );
    paillier_freeprvkey( prv );
    fclose( inputFile );
    fclose( outputFile );

    global_finish = omp_get_wtime();


    // AFFICHAGE DES STATS
    printf( "\n-- Statistiques :");
    printf( "\n\t-- Time to generate key : %lfsec", tps_key );
    printf( "\n\t-- Temps encryption %d entiers : %lfsec ", nbOp, tps_enc );
    printf( "\n\t-- Temps pour faire %d sum : %lfsec ", nbOp, tps_sum );
    printf( "\n\t-- Temps pour dechiffrer %d entiers : %lfsec", nbOp, tps_dec );
    printf( "\n\t-- Poids fichier avec %d nombres encryptés : %d octets", nbOp, nbBytes );
    printf( "\n\t-- Time to run additional funtion : %lfsec", tps_adfunc );
    printf( "\n\t-- Whole running rime: %lfsec\n", global_finish - global_start );
    return 0;
}
