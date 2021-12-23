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
import sys

from keras.callbacks import EarlyStopping
from keras.callbacks import ModelCheckpoint

from numpy import genfromtxt
from keras.models import load_model

def get_siamese_model(input_shape, number_of_hidden_layers, hidden_layer_dim):
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


def load_data(path):
    
    print("Load data: " + path, flush=True)
    
    train_data_pos1 = np.load(path + 'positive_pairs_1.npy')
    #train_data_pos1 = pd.read_csv(path + 'positive_pairs_1.csv', delimiter=',', header=None, low_memory=False)  # , dtype=np.dtype('d')
    print("1/4", flush = True)
    train_data_pos2 = np.load(path + 'positive_pairs_2.npy')
    print("2/4", flush = True)
    train_data_neg1 = np.load(path + 'negative_pairs_1.npy')
    print("3/4", flush = True)
    train_data_neg2 = np.load(path + 'negative_pairs_2.npy')
    print("4/4", flush = True)

    print(path + 'positive_pairs_1.npy', flush = True)
    
    no_positive_examples = 0
    no_negative_examples = 0
    number_of_features = -1
    # check for nan values
    for i in range(len(train_data_pos1)):
        if(np.isnan(train_data_pos1[i]).any() or np.isnan(train_data_pos2[i]).any()):
            print("NaN value")
            continue
        if number_of_features == -1:
            number_of_features = len(train_data_pos1[i])
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

    return positive_examples, negative_examples, number_of_features


def get_data_balanced(Pos, Neg, number_of_features):
    """Create batch of n pairs, half same class, half different class"""
    
    n_examples = min(len(Pos), len(Neg))

    # initialize 2 empty arrays for the input batch
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
    

def main():
    args = sys.argv[1:]
    
    config = args[0]
    #number_of_features = int(args[1])
    number_of_hidden_layers = int(args[1])
    hidden_layer_dim = int(args[2])

    print("Config:", config)
    print("Hidden layers:",number_of_hidden_layers)
    print("Hidden layers size:",hidden_layer_dim)

    if config == "no_datatypes":
        K.set_floatx('float64')

    # number_of_features = 1109 # 83

    train_folder = "/home/gottschalk/tab2kg_minor_revision/column_matching/training/" + config + "/"
    test_folder = "/home/gottschalk/tab2kg_minor_revision/column_matching/test/" + config + "/"
    save_path = "/home/gottschalk/tab2kg_minor_revision/column_matching/model/" + config + "/"

    positive_examplesTrain, negative_examplesTrain, number_of_features = load_data(train_folder)
    print("#examples in training: " + str(len(positive_examplesTrain)))
    print("Number of features:",number_of_features)

    model = get_siamese_model((number_of_features,), number_of_hidden_layers, hidden_layer_dim)
    print(model.summary())

    optimizer = Adam(lr=0.0001)
    model.compile(loss="binary_crossentropy", optimizer=optimizer, metrics=['accuracy'])

    (inputs, targets) = get_data_balanced(positive_examplesTrain, negative_examplesTrain, number_of_features)
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
    file_name = "weights_"+str(number_of_hidden_layers)+"_"+str(hidden_layer_dim)+".h5";
     
    es = EarlyStopping(monitor='val_loss', mode='min', verbose=1, patience=100)
    mc = ModelCheckpoint(filepath = save_path + file_name,save_weights_only=True, monitor='val_loss', mode='min', save_best_only=True, verbose=1)
    
    model.fit(x=inputs, y=targets, validation_split=0.1, batch_size=50, epochs=1000, shuffle=True, callbacks=[es, mc], verbose = 2)
     
    # save model
    # model.save(save_path + "model.h5")
    # model.save_weights(save_path + "weights.h5")
    # model = load_model(save_path + 'model.h5')
    model.load_weights(save_path + file_name)

    # test
    positive_examplesTest, negative_examplesTest, _ = load_data(test_folder)
    print("#examples in test: " + str(len(positive_examplesTest)))

    (input_test, targets_test) = get_data_balanced(positive_examplesTest, negative_examplesTest, number_of_features)
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
        # print(str(predictions[i][0]) + " " + str(targets_test[i]))

    print(tp)
    print(fp)
    print(tn)
    print(fn)
    acc = (tp + tn) / (tp + tn + fp + fn)
    print(acc)
    print("Done.")
    
    print("Res:", number_of_hidden_layers, hidden_layer_dim, acc)
    
    model.evaluate(input_test, targets_test)




if __name__ == "__main__":
    main()
