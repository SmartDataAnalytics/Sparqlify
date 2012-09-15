#!/bin/bash

./sp2b_gen -t 50000
./rdf2schema.pl --mysql sp2b.n3
./prepare.sh
./insertRefBagCol.sh
