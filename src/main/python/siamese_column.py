import numpy as np

from keras.models import Sequential
from keras.optimizers import Adam
from keras.layers import Activation, Input, Dropout
from keras.models import Model

from keras.layers.normalization import BatchNormalization
from keras.layers.merge import Concatenate
from keras.layers.core import Lambda, Flatten, Dense

from keras.engine.topology import Layer
from keras.regularizers import l2
from keras import backend as K

import random

from numpy import genfromtxt


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


def load_data(path, number_of_features):
    
    print("Load data: " + path, flush=True)
    
    train_data_pos1 = genfromtxt(path + 'positive_pairs_1.csv', delimiter=',')  # , dtype=np.dtype('d')
    train_data_pos2 = genfromtxt(path + 'positive_pairs_2.csv', delimiter=',')
    train_data_neg1 = genfromtxt(path + 'negative_pairs_1.csv', delimiter=',')
    train_data_neg2 = genfromtxt(path + 'negative_pairs_2.csv', delimiter=',')

    print(path + 'positive_pairs_1.csv')
    
    no_positive_examples = 0
    no_negative_examples = 0
    # check for nan values
    for i in range(len(train_data_pos1)):
        if(np.isnan(train_data_pos1[i]).any() or np.isnan(train_data_pos2[i]).any()):
            print("NaN value")
            continue
        no_positive_examples += 1
    for i in range(len(train_data_neg1)):
        if(np.isnan(train_data_neg1[i]).any() or np.isnan(train_data_neg2[i]).any()):
            print("NaN value")
            continue
        no_negative_examples += 1
    
    print("Pos: " + str(no_positive_examples))
    print("Neg: " + str(no_negative_examples))
            
    positive_examples = [[[int for i in range(number_of_features)] for i in range(2)] for i in range(no_positive_examples)]
    negative_examples = [[[int for i in range(number_of_features)] for i in range(2)] for i in range(no_negative_examples)]

    # pp = pprint.PrettyPrinter(indent=4)
    # pp.pprint(positive_examples)
                  
    p = 0
    n = 0
    
    for i in range(len(train_data_pos1)):
        if(np.isnan(train_data_pos1[i]).any() or np.isnan(train_data_pos2[i]).any()):
            print("NaN value")
            continue
        positive_examples[p][0] = train_data_pos1[i]
        positive_examples[p][1] = train_data_pos2[i]
        p += 1
    
    for i in range(len(train_data_neg1)):       
        if(np.isnan(train_data_neg1[i]).any() or np.isnan(train_data_neg2[i]).any()):
            print("NaN value")
            continue
        negative_examples[n][0] = train_data_neg1[i]
        negative_examples[n][1] = train_data_neg2[i]
        n += 1 
    
    np.random.shuffle(positive_examples)
    np.random.shuffle(negative_examples)

    print("Finished loading data from: " + path, flush=True)

    return positive_examples, negative_examples


def get_data_balanced(s="train"):
    """Create batch of n pairs, half same class, half different class"""
    if s == 'train':
        Pos = positive_examplesTrain
        Neg = negative_examplesTrain
    else:
        Pos = positive_examplesTest
        Neg = negative_examplesTest
    
    n_examples = min(len(Pos), len(Neg))

    # initialize 2 empty arrays for the input image batch
    pairs = [np.zeros((2 * n_examples, number_of_features)) for i in range(2)]
    
    # initialize vector for the targets
    targets = np.zeros((2 * n_examples,))

    print("Size: " + str(2 * n_examples))

    idxPos = 0
    idxNeg = 0
    i = 0
    while i != 2 * n_examples:
                
        if(idxPos == n_examples):
            pos = False
        elif(idxNeg == n_examples):
            pos = True
        else:
            pos = bool(random.getrandbits(1))     
                
        if(pos):
            values = Pos[idxPos]  
            targets[i] = 1
            idxPos += 1
        else:
            values = Neg[idxNeg]  
            targets[i] = 0
            idxNeg += 1
            
        pairs[0][i] = values[0]
        pairs[1][i] = values[1]
        i += 1

    return pairs, targets

#K.set_floatx('float64')

number_of_features = 83

train_folder = "/home/gottschalk/www21/column_matching/training/"
test_folder = "/home/gottschalk/www21/column_matching/test/"
save_path = '/home/gottschalk/www21/column_matching/model/'

positive_examplesTrain, negative_examplesTrain = load_data(train_folder, number_of_features)
positive_examplesTest, negative_examplesTest = load_data(test_folder, number_of_features)

print("#examples in training: " + str(len(positive_examplesTrain)))
print("#examples in test: " + str(len(positive_examplesTest)))

model = get_siamese_model((number_of_features,))
print(model.summary())

optimizer = Adam(lr=0.00006)
model.compile(loss="binary_crossentropy", optimizer=optimizer, metrics=['accuracy'])

(inputs, targets) = get_data_balanced("train")
print("Number of data points: " + str(len(inputs[0])))

# for i in range(0,1000):
#     if(inputs[0][i][0] != inputs[1][i][0]):
#         print("Target: "+str(targets[i]))
#         print(inputs[0][i])
#         print(inputs[1][i])
#         print("")
 
num_same_t0 = 0
num_diff_t0 = 0
num_same_t1 = 0
num_diff_t1 = 0
print(len(targets))
for i in range(0, len(targets)):
#    print(str(i) + "/" + str(len(targets)))
    same = 0
    diff = 0
    if(inputs[0][i][0] == inputs[1][i][0]):
        same = 1
    else:
        diff = 1
    target = targets[i]

#     if(same == 1):
#         print(str(inputs[0][i][0]) + " - " + str(inputs[1][i][0]) + " -> same")
#     if(diff == 1):
#         print(str(inputs[0][i][0]) + " - " + str(inputs[1][i][0]) + " -> different")
    
    # print("same: " + str(same) + ", diff: " + str(diff))
    if(target == 1):
        # print(" target 1")
        num_same_t1 += same
        num_diff_t1 += diff
    elif(target == 0):
        # print(" target 0")
        num_same_t0 += same
        num_diff_t0 += diff
 
print("num_same_t0: " + str(num_same_t0))
print("num_diff_t0: " + str(num_diff_t0))
print("num_same_t1: " + str(num_same_t1))
print("num_diff_t1: " + str(num_diff_t1))

print("Fit model.", flush=True)
 
model.fit(x=inputs, y=targets, validation_split=0.1, batch_size=50, epochs=1000, shuffle=True)
 
# save model
model.save(save_path + "model.h5")
model.save_weights(save_path + "weights.h5")

# test
 
(input_test, targets_test) = get_data_balanced("test")
predictions = model.predict(input_test)
 
tp = 0
fp = 0
tn = 0
fn = 0
for i in range(0, len(targets_test)):
    if(predictions[i][0] < 0.5 and targets_test[i] == 0):
        tn += 1
    elif(predictions[i][0] < 0.5 and targets_test[i] == 1):
        fn += 1
    elif(predictions[i][0] >= 0.5 and targets_test[i] == 0):
        fp += 1
    elif(predictions[i][0] >= 0.5 and targets_test[i] == 1):
        tp += 1
    print(str(predictions[i][0]) + " " + str(targets_test[i]))

print(tp)
print(fp)
print(tn)
print(fn)
acc = (tp + tn) / (tp + tn + fp + fn)
print(acc)
print("Done.")
