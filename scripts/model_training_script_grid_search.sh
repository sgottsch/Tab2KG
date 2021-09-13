#!/bin/bash

for hidden_dim in 16 32 64 128 256 512; do
	for number_of_layers in 1 2 3; do
		python3 ~/tab2kg/ml/siamese_column.py all "$number_of_layers" "$hidden_dim"
	done
done