from flask import Flask
from flask_restful import Resource, Api, reqparse
import pandas as pd
import ast
from column_matcher import ColumnMatcher

file_name = '/home/gottschalk/tab2kg_minor_revision/column_matching/model/CONFIG/weights_NUM_DIM.h5'

app = Flask(__name__)
api = Api(app)

class Matcher(Resource):
    def post(self):
        print("match")
        parser = reqparse.RequestParser()
        parser.add_argument('config', required=True)
        parser.add_argument('left_input', required=True)
        parser.add_argument('right_input', required=True)
        args = parser.parse_args()
        res = column_matchers[args['config']].apply_siamese_model(number_of_features_dict[args['config']], args['left_input'], args['right_input'])
        print("res ready")
        return {'result': res}, 200  # return result and 200 OK code


api.add_resource(Matcher, '/match')  # '/match' is our entry point

if __name__ == '__main__':
    print("Load model.")
    
    # configs = [ "all", "no_embeddings", "no_distributions", "no_basic_statistics" ]
    configs = [ "all" ]

    number_of_features_dict = { "all": 85 }
    number_of_hidden_layers = 1
    hidden_layer_sizes = 256

    column_matchers = dict()
    for config in configs:    
        column_matcher = ColumnMatcher()
        column_matcher.load_model(file_name.replace("CONFIG", config).replace("NUM", str(number_of_hidden_layers)).replace("DIM", str(hidden_layer_sizes)), number_of_features_dict[config], number_of_hidden_layers, hidden_layer_sizes)
        column_matchers[config] = column_matcher
    
    print("Model loaded.")
    app.run(port=5012)  # run the Flask app
