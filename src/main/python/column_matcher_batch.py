import numpy as np

from keras.models import Sequential
from keras.optimizers import Adam
from keras.layers import Activation, Input, Dropout
from keras.models import Model, load_model
 
from keras.layers.normalization import BatchNormalization
from keras.layers.merge import Concatenate
from keras.layers.core import Lambda, Flatten, Dense
 
from keras.engine.topology import Layer
from keras.regularizers import l2
from keras import backend as K

from numpy import genfromtxt
from io import StringIO

import sys


def get_siamese_model(input_shape):
    """
        Model architecture based on the one provided in: http://www.cs.utoronto.ca/~gkoch/files/msc-thesis.pdf
    """
    
    # Define the tensors for the two input images
    left_input = Input(input_shape)
    right_input = Input(input_shape)
    
    # Neural Network
    model = Sequential()
    model.add(Dense(256, input_shape=input_shape, init='uniform', activation="tanh"))
    #, bias_initializer='he_uniform'))#, kernel_regularizer=l2(1e-3))) # , bias_initializer='he_uniform'))
    model.add(Dropout(0.5))
    model.add(Dense(256, activation='tanh', init='uniform'))
    #, kernel_regularizer=l2(1e-3))#, bias_initializer='he_uniform'))
    model.add(Dropout(0.5))

#     model2 = Sequential()   
#     model2.add(Dense(512, activation='relu', input_shape=input_shape, bias_initializer='he_uniform'))
#     model2.add(Dropout(0.1))
#     model2.add(Dense(512, activation='sigmoid', kernel_regularizer=l2(1e-3)))  # , bias_initializer='he_uniform'))

    # Generate the encodings (feature vectors) for the two images
    encoded_l = model(left_input)
    encoded_r = model(right_input)
    
    # Add a customized layer to compute the absolute difference between the encodings
    L1_layer = Lambda(lambda tensors:K.abs(tensors[0] - tensors[1]))
    L1_distance = L1_layer([encoded_l, encoded_r])
    
    # Add a dense layer with a sigmoid unit to generate the similarity score
    prediction = Dense(1, activation='sigmoid')(L1_distance)  # ,bias_initializer=initialize_bias
    
    # Connect the inputs with the outputs
    
    siamese_net = Model(inputs=[left_input, right_input], outputs=prediction)
    
    # return the model
    return siamese_net


def load_my_model(model_file_name, number_of_features):
    model = get_siamese_model((number_of_features,))
    model.load_weights(model_file_name)

    optimizer = Adam(lr=0.00006)
    model.compile(loss="binary_crossentropy", optimizer=optimizer, metrics=['accuracy'])
    return model

 
def apply_siamese_model(model, number_of_features, left_input_str, right_input_str):
    left_input = genfromtxt(StringIO(left_input_str), delimiter=',')
    right_input = genfromtxt(StringIO(right_input_str), delimiter=',')
    
    if len(left_input.shape) == 1:
        left_input = np.array([left_input])
    if len(right_input.shape) == 1:
        right_input = np.array([right_input])
    
    n_examples = len(left_input)
    
    #print("n_examples: "+str(n_examples))
    pairs = [np.zeros((n_examples, number_of_features)) for i in range(2)]
    
    for i in range(0, n_examples) :
        pairs[0][i] = left_input[i]
        pairs[1][i] = right_input[i]

    #print("#Pairs: ",len(pairs))
    
    #print(pairs)

    predictions = model.predict(pairs)
    
    result = ""
    for i in range(0, n_examples) :
        result = result + str(predictions[i][0]) + " "
    
    return result

# print(sys.argv[3])


model = load_my_model(sys.argv[1], int(sys.argv[2]))

res = apply_siamese_model(model, int(sys.argv[2]), sys.argv[3], sys.argv[4])
print(res)

