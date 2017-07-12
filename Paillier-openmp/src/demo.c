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
        printf( "\tUSAGE : ./bin/paillier inputFile_name outputFile_name nbLignes keylength\n\n" );
        printf( "\t- nbLignes is the number of data to be encrypted.\n" );
        printf( "\t- keylength: (128, 256, 512, 1024, 2048, ...)\n\n" );
        return 0;
    }

    printf("\n\n=============");
    printf("\n== PAILLIER =="  );
    printf("\n=============");
	
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

    //read data from inputFile
    printf( "\n\n=> Reading integers from inputFile..." );
    for (i=0 ; i<nbOp ; i++)
    {
        fscanf( inputFile, "%d", &(tab[i]) );
        tab_pt[i] = *(paillier_plaintext_from_ui( tab[i] ));
    }
	
	printf("\n=> Generate key...");
    start = omp_get_wtime();
    paillier_keygen( modulusbit, &pub, &prv, paillier_get_rand_devurandom );
    finish = omp_get_wtime();
    tps_key = finish-start;
	
	printf( "\n=> Parallel encryption..." );
    start = omp_get_wtime();
	paillier_para_enc(nbOp, pub, tab_pt, tab_enc);
	finish = omp_get_wtime();
	tps_enc = finish-start;
	
	printf( "\n=> Write ciphertext into outputFile : %s", outputFile_path );
    for (i=0 ; i<nbOp ; i++)
    {
            nbBytes = nbBytes + fprintf( outputFile, "%s\n", mpz_get_str(0, 16, tab_enc[i].c) );
    }
	
	// ADDITIONS
    printf( "\n=> Additions homomorphiques..." );
    start = omp_get_wtime();
    for (i=1 ; i<nbOp ; i++) { paillier_mul( pub, &(tab_enc[0]), &(tab_enc[0]), &(tab_enc[i]) ); }
    finish = omp_get_wtime();
    tps_sum = finish-start;
	
	// PARALLEL DECHIFFREMENT
    printf( "\n=> Parallel dechiffrement... " );
    start = omp_get_wtime();
    paillier_para_dec(nbOp, pub, prv, tab_enc, pt);
    finish = omp_get_wtime();
    tps_dec = finish-start;
    gmp_printf("Result : %Zd", &pt[0]);
	
    srand((int)time(0));
    for (i=0; i<n; i++)
    {
        t[i] = rand()%20000 + 200;
        tt[i] = *(paillier_plaintext_from_ui( t[i] ));
        SS[i] = *(paillier_plaintext_from_ui( t[n-1-i] ));
    }
	//cipher add
	printf("\n=> Cipher add...");
	start = omp_get_wtime();
	paillier_para_enc(n, pub, tt, et);
    paillier_para_enc(n, pub, SS, S);
    //finish = omp_get_wtime();
    //start = omp_get_wtime();
	paillier_cipheradd(n, L, pub, et, S);
    finish = omp_get_wtime();
    tps_adfunc = finish-start;
	
	global_finish = omp_get_wtime();
    // RELEASE RESOURCES
    printf("\n=> Release resources...\n");
    paillier_freepubkey(pub);
    paillier_freeprvkey(prv);
    fclose(inputFile);
    fclose(outputFile);

    // RESULTS
    printf( "\n-- Statistical result :");
    printf( "\n-- Time to generate key : %lfsec", tps_key );
    printf( "\n-- Encrypte %d integers : %lfsec ", nbOp, tps_enc );
    printf( "\n-- Sum of %d ciphertexts : %lfsec ", nbOp, tps_sum );
    printf( "\n-- Dechiffrer %d integers : %lfsec", nbOp, tps_dec );
    //printf( "\n-- Number of integers %d and length of key : %d", nbOp, nbBytes );
    printf( "\n-- Time to do cipher add : %lfsec", tps_adfunc );
    printf( "\n-- Whole running rime: %lfsec\n", global_finish - global_start );
    return 0;
}
