#!/bin/bash

echo "Indexing..."
./index_db.sh -b en/ignore.list wdir en_US en/stopwords.list English models/en
./index_db.sh -b de/ignore.list wdir de_DE de/stopwords.list German  models/de
./index_db.sh -b nl/ignore.list wdir nl_NL nl/stopwords.list Dutch   models/nl
./index_db.sh -b sv/ignore.list wdir sv_SE sv/stopwords.list Swedish models/sv
./index_db.sh -b pt/ignore.list wdir pt_BR pt/stopwords.list Portuguese models/pt
./index_db.sh -b fr/ignore.list wdir fr_FR fr/stopwords.list French models/fr
./index_db.sh -b es/ignore.list wdir es_ES es/stopwords.list Spanish models/es
./index_db.sh -b tr/ignore.list wdir tr_TR tr/stopwords.list Turkish models/tr
./index_db.sh -b no/ignore.list wdir no_NO no/stopwords.list Norwegian models/no
./index_db.sh wdir it_IT it/stopwords.list Italian models/it
./index_db.sh wdir da_DK da/stopwords.list Danish models/da
./index_db.sh wdir ja_JP ja/stopwords.list None models/ja
./index_db.sh wdir cs_CZ cs/stopwords.list None models/cs
./index_db.sh wdir hu_HU hu/stopwords.list Hungarian models/hu
./index_db.sh wdir ru_RU ru/stopwords.list Russian models/ru
./index_db.sh wdir zh_CN zh/stopwords.list None models/zh
