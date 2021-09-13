# prerequisite: column matching API is running

# ablation study
nohup python3 ~/tab2kg/ml/siamese_column.py all 1 256 > ~/tab2kg/ml/logs/logs_all.txt
nohup python3 ~/tab2kg/ml/siamese_column.py no_distributions 1 256 > ~/tab2kg/ml/logs/logs_no_distributions.txt
nohup python3 ~/tab2kg/ml/siamese_column.py no_basic_statistics 1 256 > ~/tab2kg/ml/logs/logs_no_basic_statistics.txt
nohup python3 ~/tab2kg/ml/siamese_column.py no_completeness 1 256 > ~/tab2kg/ml/logs/logs_no_completeness.txt
nohup python3 ~/tab2kg/ml/siamese_column.py no_datatypes 1 256 > ~/tab2kg/ml/logs/logs_no_datatypes.txt