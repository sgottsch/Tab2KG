# This is an edited version of https://github.com/minhptx/iswc-2016-semantic-labeling, which was edited to use it as a baseline for Tab2KG (https://github.com/sgottsch/Tab2KG).

import logging

from elasticsearch.exceptions import RequestError
from elasticsearch.helpers import scan, bulk
from lib.utils import get_index_name


__author__ = "minh"


class Indexer:

    def __init__(self, es):
        self.es = es

    def init_analyzers(self, index_config):
        print("init_analyzers")
        print(index_config)
        print(get_index_name(index_config))
        if(self.es.indices.exists(get_index_name(index_config))):
            self.es.indices.delete(index=get_index_name(index_config))
        self.es.indices.create(index=get_index_name(index_config))

    def index_column(self, column, source_name, index_config):
        body = column.to_json()
        body['source'] = source_name
        try:
            self.es.index(index=get_index_name(index_config), doc_type="service",
                body=body)
            return True
        except RequestError:
            print("Error")
            return False

    def index_source(self, source, index_config):
        # self.es.indices.put_mapping(index=get_index_name(index_config), doc_type="service", body={
        #     "service": {
        #         "properties": {
        #             "source": {
        #                 "type": "string",
        #                 "index": "not_analyzed"
        #             }
        #         }
        #     }
        # })

        for column in source.column_map.values():
            if column.semantic_type:
                if len(column.value_list) > 0:
                    successful = self.index_column(column, source.index_name, index_config)
                    if(not successful):
                        return False
                else:
                    logging.warning("Indexer: IGNORE COLUMN `%s` in source `%s` because of empty values", column.name, source.name)
        return True

    def delete_column(self, attr_name, source_name, index_config):
        bulk_deletes = []
        for result in scan(self.es, query={
            "query": {
                "match": {
                    "name": attr_name,
                }
            }
        }, index=get_index_name(index_config), doc_type="service", _source=False,
                track_scores=False, scroll='5m'):
            result['_op_type'] = 'delete'
            bulk_deletes.append(result)
        bulk(self.es, bulk_deletes)
