#!/bin/bash

#JAR="~/tab2kg/T2KMatch/t2kmatch-2.1-jar-with-dependencies.jar"
JAR="~/tab2kg/T2KMatch/T2KMatch.jar"
CLS="de.uni_mannheim.informatik.dws.t2k.match.T2KMatch"

# required parameters: Knowledge Base, Web Tables and Ontology (class hierarchy)
KB="~/tab2kg/T2KMatch/data/dbpedia/"
ONT="~/tab2kg/T2KMatch/data/OntologyDBpedia"
WEB="/storage~/tab2kg/data/semtab_easy/t2kmatch/p/tables"

# optional parameters: Index location (will be created if it does not exist), Surface Forms and Redirects
IDX="~/tab2kg/T2KMatch/data/index/"
SF="~/tab2kg/T2KMatch/data/surfaceforms.txt"
RD="~/tab2kg/T2KMatch/data/redirects.txt"

# evaluation parameters (optional): gold standards for class, property and instance mapping
GS_CLS="~/tab2kg/data/semtab_easy/t2kmatch/p/gs_class.csv"
GS_PROP="~/tab2kg/data/semtab_easy/t2kmatch/p/gs_property.csv"
#GS_INST="/storage~/www21/data/semtab_easy/t2kmatch/gs_instance.csv"

# output location
RES="~/tab2kg/data/semtab_easy/t2kmatch/p/output/"

mkdir -p $RES

java -Xmx15G -cp $JAR $CLS -sf $SF -kb $KB -ontology $ONT -web $WEB -index $IDX -classGS $GS_CLS -schemaGS $GS_PROP -results $RES -verbose > $RES/log.txt 2>&1

# -identityGS $GS_INST 