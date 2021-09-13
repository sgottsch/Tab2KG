import numpy as np

from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.layers import Activation, Input, Dropout
from tensorflow.keras.models import Model, load_model

from tensorflow.keras.layers import BatchNormalization, Concatenate, Lambda, Flatten, Dense

from tensorflow.keras.regularizers import l2
from tensorflow.keras import backend as K

from numpy import genfromtxt
from io import StringIO

import sys

import tensorflow as tf
import logging

logger = logging.getLogger('root')
file_name = '~/tab2kg/column_matching/model/weights.h5'

class ColumnMatcher:

    def __init__(self):
    
        # avoid error: https://stackoverflow.com/questions/51127344/tensor-is-not-an-element-of-this-graph-deploying-keras-model
    
        self.session = tf.Session()
        self.graph = tf.get_default_graph()

        with self.graph.as_default():
            with self.session.as_default():
                logging.info("column matcher initialised")

    def get_siamese_model(self, input_shape, number_of_hidden_layers, hidden_layer_dim):
        """
            Model architecture based on the one provided in: http://www.cs.utoronto.ca/~gkoch/files/msc-thesis.pdf
        """

        # Define the tensors for the two input images
        left_input = Input(input_shape)
        right_input = Input(input_shape)

        # Neural Network
        model = Sequential()
        
        for i in range(0,number_of_hidden_layers):
            print("ADD LAYER",i)
            model.add(Dense(hidden_layer_dim, input_shape=input_shape, activation="tanh", kernel_regularizer=l2(1e-3)))
            # , bias_initializer='he_uniform'))#, kernel_regularizer=l2(1e-3))) # , bias_initializer='he_uniform'))
            #model.add(Dropout(0.5))
            #model.add(Dense(hidden_layer_dim, activation='tanh'))
            # , kernel_regularizer=l2(1e-3))#, bias_initializer='he_uniform'))
            model.add(Dropout(0.2))

        #     model2 = Sequential()
        #     model2.add(Dense(512, activation='relu', input_shape=input_shape, bias_initializer='he_uniform'))
        #     model2.add(Dropout(0.1))
        #     model2.add(Dense(512, activation='sigmoid', kernel_regularizer=l2(1e-3)))  # , bias_initializer='he_uniform'))

        # Generate the encodings (feature vectors) for the two images
        encoded_l = model(left_input)
        encoded_r = model(right_input)

        # Add a customized layer to compute the absolute difference between the encodings
        L1_layer = Lambda(lambda tensors: K.abs(tensors[0] - tensors[1]))
        L1_distance = L1_layer([encoded_l, encoded_r])

        # Add a dense layer with a sigmoid unit to generate the similarity score
        prediction = Dense(1, activation='sigmoid')(L1_distance)  # ,bias_initializer=initialize_bias

        # Connect the inputs with the outputs

        siamese_net = Model(inputs=[left_input, right_input], outputs=prediction)

        # return the model
        return siamese_net

    def load_model(self, model_file_name, number_of_features, number_of_hidden_layers, hidden_layer_dim):
        with self.graph.as_default():
            with self.session.as_default():
                print("number_of_features:",number_of_features)
                self.model = self.get_siamese_model((number_of_features,), number_of_hidden_layers, hidden_layer_dim)
                self.model.load_weights(model_file_name)

    def apply_siamese_model(self, number_of_features, left_input_str, right_input_str):

        print("apply_siamese_model")
        left_input = genfromtxt(StringIO(left_input_str), delimiter=',')
        right_input = genfromtxt(StringIO(right_input_str), delimiter=',')

        print("next")

        if len(left_input.shape) == 1:
            left_input = np.array([left_input])
        if len(right_input.shape) == 1:
            right_input = np.array([right_input])

        n_examples = len(left_input)

        print("n_examples: "+str(n_examples))
        pairs = [np.zeros((n_examples, number_of_features)) for i in range(2)]

        for i in range(0, n_examples):
            pairs[0][i] = left_input[i]
            pairs[1][i] = right_input[i]

        print("#Pairs: ",len(pairs))

        with self.graph.as_default():
            with self.session.as_default():
                predictions = self.model.predict(pairs)

        result = ""
        for i in range(0, n_examples):
            result = result + str(predictions[i][0]) + " "

        return result