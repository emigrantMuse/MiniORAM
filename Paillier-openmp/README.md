Performance Paillier
=============
Ce programme test les performances de l'algorithme Paillier (chiffrement, somme homomorphic, dechiffrement).

Basé sur les librairies GMP, et [Paillier][1].

Getting started
----------------
1. Install Paillier:
   ```bash
   cd path_to_Paillier/
   git clone https://github.com/isnaj/Paillier.git Paillier
   ```

2. Lancez le .sh en root (root)
   ```bash
   cd path_to_Paillier/Paillier
   ./install_dependances.sh
   ```

3. Utilisez le Makefile (root).
   ```bash
   make
   ```

4. Lancez le programme (root)
   ```bash
   ./bin/paillier inputFile_name outputFile_name nbLignes modulusbit
   ```

Arguments
----------------------

Parametres requis:

`inputFile_name` et `outputFile_name` sont le nom des fichiers entrée et sortie

`nbLignes` est le nombre de ligne du fichier entrée

`modulusbit` (128, 256, 512, 1024, 2048, ...)

Notes importantes
----------------------
1. Le program doit etre lancé depuis le repertoire racine (celui du makefile).

2. Le fichier entrée doit se trouver dans le repertoire ./data/

3. Le fichier sortie se trouve dans le repertoire ./data

4. Le format du fichier entrée doit etre :
	A1
	A2
	A3
	...

Auteurs
---------
Lydéric LEFEBVRE - lyderic.lefebvre [at] live [dot] fr

[1]: http://acsc.cs.utexas.edu/libpaillier/
