# Commandes
# make : compilation
# make clean : Supprime les .o et executables

# Répertoire de travail :
SRC = ./src
BIN = ./bin

# Nom des exécutables a générer :
BINPAILLIER = $(BIN)/paillier

# Compilateur
CC = gcc

$(BINPAILLIER): $(BIN)/demo.o
	$(CC) -o $(BINPAILLIER) $(BIN)/demo.o /usr/local/lib/libpaillier.a /usr/lib/x86_64-linux-gnu/libgmp.a -fopenmp
	rm -rf $(BIN)/*.o

$(BIN)/demo.o: $(SRC)/demo.c
	$(CC) -o $(BIN)/demo.o -c $(SRC)/demo.c -fopenmp

clean:
	rm -rf $(BIN)/*
