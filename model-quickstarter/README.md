Quickstarter for DBpedia Spotlight models
===================================================

[![Gitter](https://badges.gitter.im/dbpedia-spotlight/model-quickstarter.svg)](https://gitter.im/dbpedia-spotlight/model-quickstarter?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

## Update, January 2016

This tool now uses the wikistatsextractor by the great folks over at [DiffBot](https://www.diffbot.com/). This means: no more Hadoop and Pig! Running the biggest model (English) takes around 2h on a single machine with around 32GB of RAM. We recommend running this script on an SSD with around 100GB of free space.

### Requirements

- Git
- Maven 3

## Spotlight model creation

You can use this tool for creating models of DBpedia Spotlight in your language.


* docker run -it dbpediaspotlight/model-quickstarter bash
* cd model-quickstarter/
* ./prepare.sh

and then the command to the target model

EN - ./index_db.sh -b en/ignore.list wdir en_US en/stopwords.list English models/en

DE - ./index_db.sh -b de/ignore.list wdir de_DE de/stopwords.list German models/de

NL -./index_db.sh -b nl/ignore.list wdir nl_NL nl/stopwords.list Dutch models/nl

SV - ./index_db.sh -b sv/ignore.list wdir sv_SE sv/stopwords.list Swedish models/sv

PT - ./index_db.sh -b pt/ignore.list wdir pt_BR pt/stopwords.list Portuguese models/pt

FR - ./index_db.sh -b fr/ignore.list wdir fr_FR fr/stopwords.list French models/fr

ES - ./index_db.sh -b es/ignore.list wdir es_ES es/stopwords.list Spanish models/es

TR - ./index_db.sh -b tr/ignore.list wdir tr_TR tr/stopwords.list Turkish models/tr

NO - ./index_db.sh -b no/ignore.list wdir no_NO no/stopwords.list Norwegian models/no

IT -./index_db.sh wdir it_IT it/stopwords.list Italian models/it

DA -./index_db.sh wdir da_DK da/stopwords.list Danish models/da

JA - ./index_db.sh wdir ja_JP ja/stopwords.list None models/ja

CS - ./index_db.sh wdir cs_CZ cs/stopwords.list None models/cs

HU - ./index_db.sh wdir hu_HU hu/stopwords.list Hungarian models/hu

RU - ./index_db.sh wdir ru_RU ru/stopwords.list Russian models/ru

ZH - ./index_db.sh wdir zh_CN zh/stopwords.list None models/zh


If you want to generate the models outside the container, just map volumes for the folders /model-quickstarter/wdir, /model-quickstarter/data and /model-quickstarter/models.

E.g:

```
docker run -v /home/user/data/model/wdir:/model-quickstarter/wdir -v /home/user/data/model/data:/model-quickstarter/data -v /home/user/data/model/models:/model-quickstarter/models -it dbpediaspotlight/model-quickstarter bash
```


## Datasets

You can find pre-built datasets created using the model-quickstarter here:

- [Pre-built Spotlight models for English](http://downloads.dbpedia-spotlight.org/en/2016-04/model/)
- [Raw counts for English](http://downloads.dbpedia-spotlight.org/en/2016-04/model/raw/)

For other languages, just visit <http://downloads.dbpedia-spotlight.org>

## Citation

If you use the current (statistical version) of DBpedia Spotlight or the data/models created using this repository, please cite the following paper.

```bibtex
@inproceedings{isem2013daiber,
  title = {Improving Efficiency and Accuracy in Multilingual Entity Extraction},
  author = {Joachim Daiber and Max Jakob and Chris Hokamp and Pablo N. Mendes},
  year = {2013},
  booktitle = {Proceedings of the 9th International Conference on Semantic Systems (I-Semantics)}
}
```
