#!/bin/bash

## Script d'installation de GMP et Paillier pour tests avec machines type Debian
## Auteurs : Lyderic L., Yannick H., Jordan DLV.

sudo apt-get install libgmp3-dev

sudo mkdir /tmp/paillier
sudo wget -P /tmp/paillier http://acsc.cs.utexas.edu/libpaillier/libpaillier-0.8.tar.gz
sudo tar -xzvf /tmp/paillier/libpaillier-0.8.tar.gz -C /tmp/paillier/
cd /tmp/paillier/libpaillier-0.8/
sudo ./configure
sudo make
sudo make install
