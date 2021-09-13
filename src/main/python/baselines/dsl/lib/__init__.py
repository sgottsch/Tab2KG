# This is an edited version of https://github.com/minhptx/iswc-2016-semantic-labeling, which was edited to use it as a baseline for Tab2KG (https://github.com/sgottsch/Tab2KG).

from elasticsearch import Elasticsearch

from search.indexer import Indexer
from search.searcher import Searcher

__author__ = 'alse'
elastic_search = Elasticsearch()
print("ES:")
print(elastic_search)
indexer = Indexer(elastic_search)
searcher = Searcher(elastic_search)
