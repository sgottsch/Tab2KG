nohup java -jar ~/tab2kg/jars/TableGraphPairsFinder.jar SOCCER > nohup_tgpf_soccer.out
nohup java -jar ~/tab2kg/jars/TableGraphPairsFinder.jar WEAPONS > nohup_tgpf_weapons.out
nohup java -jar ~/tab2kg/jars/TableGraphPairsFinder.jar SEMTAB > nohup_tgpf_semtab.out

# start API in another tab

# transform data
python3 ~/tab2kg/ml/data_transformer.py all
python3 ~/tab2kg/ml/data_transformer.py no_distributions
python3 ~/tab2kg/ml/data_transformer.py no_basic_statistics
python3 ~/tab2kg/ml/data_transformer.py no_completeness
python3 ~/tab2kg/ml/data_transformer.py no_datatypes
