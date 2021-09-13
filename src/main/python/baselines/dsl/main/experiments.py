# This is an edited version of https://github.com/minhptx/iswc-2016-semantic-labeling, which was edited to use it as a baseline for Tab2KG (https://github.com/sgottsch/Tab2KG).

import random

import time

import sys

from semantic_labeler import SemanticLabeler

__author__ = 'alse'


def run_experiments():
    semantic_labeler = SemanticLabeler()
    semantic_labeler.read_data_sources(["soccer"])
    semantic_labeler.train_semantic_types(["soccer"])
    # semantic_labeler.train_random_forest([11], ["soccer"])
    # semantic_labeler.test_semantic_types("dbpedia", [1, 2, 3, 4, 5])
    # semantic_labeler.test_semantic_types("museum", [1, 2, 3, 4, 5])
    # semantic_labeler.test_semantic_types("soccer", [1, 2, 3, 4, 5])
    # semantic_labeler.test_semantic_types("weather", [1, 2, 3])
    # semantic_labeler.write_data_for_transform("soccer")
    # semantic_labeler.train_semantic_types(["dbpedia", "soccer", "museum", "weather"])
    # semantic_labeler.train_semantic_types(["soccer", "memex"])
    # semantic_labeler.train_semantic_types(["weather2"])
    # start_time = time.time()
    # semantic_labeler.train_random_forest([1, 2, 3, 4, 5], ["soccer"])
    # print("--- %s seconds ---" % (time.time() - start_time))
    #
    # semantic_labeler.train_random_forest([1], ["soccer"])
    # semantic_labeler.train_random_forest([1], ["museum"])
    # print("--- %s seconds ---" % (time.time() - start_time))

    # sizes = random.sample(range(1, 12), 2)
    print("semantic_labeler.train_random_forest")
    semantic_labeler.train_random_forest([1], ["soccer"])
    # semantic_labeler.train_random_forest([1], ["museum2"])
    #
    # semantic_labeler.test_semantic_types("museum2", [14])

    # semantic_labeler.test_semantic_types("memex", [1,2,3,4,5,6,7,8,9,10])
    # semantic_labeler.test_semantic_types("museum", [14])
    # semantic_labeler.test_semantic_types("soccer", [6])
    # semantic_labeler.test_semantic_types("weather", [2])
    # semantic_labeler.test_semantic_types("dbpedia", [1, 2, 3, 4, 5])
    # semantic_labeler.test_semantic_types("museum", [1, 2, 3, 4, 5])
    print("semantic_labeler.test_semantic_types")
    semantic_labeler.test_semantic_types("soccer", [1])
    # semantic_labeler.test_semantic_types("weather", [1, 2, 3])
    # semantic_labeler.test_semantic_types("weather", [2])
    # semantic_labeler.test_semantic_types("soccer", [6])
    # semantic_labeler.test_semantic_types("soccer", [6])
    # semantic_labeler.test_semantic_types("soccer", [6])
    # semantic_labeler.test_semantic_types("soccer", [6])
    # semantic_labeler.test_semantic_types("weather", [2])
    # semantic_labeler.test_semantic_types("weather", xrange(1, 3))
    # semantic_labeler.test_semantic_types_from_2_sets("dbpedia_full", "t2d")


if __name__ == "__main__":
    reload(sys)
    sys.setdefaultencoding('utf-8')
    run_experiments()
