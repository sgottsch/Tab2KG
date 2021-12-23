import numpy as np
import sys
from numpy import genfromtxt


def load_and_store_data(path):
    
    print("Load data: " + path, flush=True)
    
    train_data_pos1 = genfromtxt(path + 'positive_pairs_1.csv', delimiter=',')
    print("1/4", flush = True)
    train_data_pos2 = genfromtxt(path + 'positive_pairs_2.csv', delimiter=',')
    print("2/4", flush = True)
    train_data_neg1 = genfromtxt(path + 'negative_pairs_1.csv', delimiter=',')
    print("3/4", flush = True)
    train_data_neg2 = genfromtxt(path + 'negative_pairs_2.csv', delimiter=',')
    print("4/4", flush = True)
    
    np.save(path + 'positive_pairs_1.npy', train_data_pos1)
    np.save(path + 'positive_pairs_2.npy', train_data_pos2)
    np.save(path + 'negative_pairs_1.npy', train_data_neg1)
    np.save(path + 'negative_pairs_2.npy', train_data_neg2)

    
def main():
    args = sys.argv[1:]
    
    config = args[0]

    print("Config:", config)

    #K.set_floatx('float64')

    train_folder = "/home/gottschalk/tab2kg_minor_revision/column_matching/training/" + config + "/"
    test_folder = "/home/gottschalk/tab2kg_minor_revision/column_matching/test/" + config + "/"

    load_and_store_data(train_folder)
    load_and_store_data(test_folder)


if __name__ == "__main__":
    main()
