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

$(BINPAILLIER): $(BIN)/main.o
	$(CC) -o $(BINPAILLIER) $(BIN)/main.o /usr/local/lib/libpaillier.a /usr/lib/x86_64-linux-gnu/libgmp.a
	rm -rf $(BIN)/*.o

$(BIN)/main.o: $(SRC)/main.c
	$(CC) -o $(BIN)/main.o -c $(SRC)/main.c

clean:
	rm -rf $(BIN)/*
