# DBpedia Spotlight (added models with Wikidata entitites)
[DBpedia Spotlight](https://www.dbpedia-spotlight.org/) is a tool for automatically annotating mentions of [DBpedia](https://www.dbpedia.org/about/)  resources in text, providing a solution for linking unstructured information sources to the Linked Open Data cloud. 

Original DBpedia Spotlight demo can be found [here](https://demo.dbpedia-spotlight.org/). Nevertheless, **this demo also allows annotating mentions of  [Wikidata](https://www.wikidata.org/wiki/Wikidata:Main_Page)  resources**. 


### How
New models have been created in English and Spanish by modifying the original [wikistats](https://databus.dbpedia.org/dbpedia/spotlight/spotlight-wikistats/), using the [wikistatsextractor tool](https://github.com/dbpedia-spotlight/wikistatsextractor). Specifically, *Wikidata's URIs* have been added to **uriCounts** and **pairCounts** files. 

It has also been necessary to add Wikidata's *instance types*, *redirects* and *disambiguations* to the original [DBpedia Spotlight tool](https://github.com/dbpedia-spotlight/dbpedia-spotlight-model).

### APIs
This **demo** uses an statistical *web service* for [English]() and [Spanish](https://www.dbpedia-spotlight.org/api/es).

**Original DBpedia Spotlight** *web services* for [English](https://www.dbpedia-spotlight.org/api/en), [Spanish](https://www.dbpedia-spotlight.org/api/en) and many more languages can also be checked.

### Sources
- Modified [DBpedia Spotlight tool](https://github.com/jmdu99/DBpedia-Spotlight-2.0/tree/main/dbpedia-spotlight) 
- Modified [wikistatextractor tool](https://github.com/jmdu99/DBpedia-Spotlight-2.0/tree/main/wikistatsextractor) 
- Modified [model generator tool](https://github.com/jmdu99/DBpedia-Spotlight-2.0/tree/main/model-quickstarter)
- [Evaluation datasets](https://github.com/jmdu99/DBpedia-Spotlight-2.0/tree/main/datasets)
- [Source code](https://github.com/jmdu99/DBpedia-Spotlight-2.0/tree/main/spotlight-demo) of this demo
- Original models are found [here](https://databus.dbpedia.org/dbpedia/spotlight/spotlight-model/), as well as [redirects](https://databus.dbpedia.org/dbpedia/generic/redirects), [instance types](https://databus.dbpedia.org/dbpedia/mappings/instance-types), and [disambiguations](https://databus.dbpedia.org/dbpedia/generic/disambiguations).
