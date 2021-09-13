# update elasticsearch settings:
## config/jvm.options -> -Xms12g, -Xmx12g
## config/elasticsearch.yml -> indices.query.bool.max_clause_count : 100000, cluster.max_shards_per_node: 10000 

# activate conda environment dsl_env first
source ~/miniconda/etc/profile.d/conda.sh
conda activate dsl_env

# Start and clean elasticsearch
cd elasticsearch-7.6.2
bin/elasticsearch &
curl -XDELETE 'http://localhost:9200/*'
curl -X PUT localhost:9200/_cluster/settings -H "Content-Type: application/json" -d '{ "persistent": { "cluster.max_shards_per_node": "10000" } }'

# Preparation

nohup java -jar ~/tab2kg/jars/TableGraphPairsFinder.jar SOCCER > ~/tab2kg/jars/nohup_tgpf_soccer.out
nohup java -jar ~/tab2kg/jars/TableGraphPairsFinder.jar WEAPONS > ~/tab2kg/jars/nohup_tgpf_weapons.out
nohup java -jar ~/tab2kg/jars/TableGraphPairsFinder.jar SEMTAB > ~/tab2kg/jars/nohup_tgpf_semtab.out

rm -r ~/tab2kg/dsl/logs/*
rm -r ~/tab2kg/dsl/data/datasets/*
rm -r ~/tab2kg/dsl/output/*
rm -r ~/tab2kg/dsl/outputs_final/*

### Soccer_P

nohup java -jar ~/tab2kg/jars/DSLTrainTestSetsCreator.jar ~/tab2kg/dsl/data/datasets/ SOCCER false > ~/tab2kg/dsl/logs/dsl_soccer_p_creation.out

# Soccer_P (less)

cp ~/tab2kg/dsl/model/lr_less.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_less.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset soccer_p --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_soccer_p.txt > ~/tab2kg/dsl/logs/soccer_p_less_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/soccer_p_less
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/soccer_p_less
cp ~/tab2kg/dsl/data/datasets/info_soccer_p.txt ~/tab2kg/dsl/outputs_final/soccer_p_less/info_soccer_p.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SOCCER -dslPairsFile ~/tab2kg/dsl/outputs_final/soccer_p_less/info_soccer_p.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/soccer_p_less > ~/tab2kg/dsl/results/soccer_p_less.out

# Soccer_P (all)

cp ~/tab2kg/dsl/model/lr_all.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_all.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset soccer_p --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_soccer_p.txt > ~/tab2kg/dsl/logs/soccer_p_all_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/soccer_p_all
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/soccer_p_all
cp ~/tab2kg/dsl/data/datasets/info_soccer_p.txt ~/tab2kg/dsl/outputs_final/soccer_p_all/info_soccer_p.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SOCCER -dslPairsFile ~/tab2kg/dsl/outputs_final/soccer_p_all/info_soccer_p.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/soccer_p_all > ~/tab2kg/dsl/results/soccer_p_all.out

### Soccer_D

nohup java -jar ~/tab2kg/jars/DSLTrainTestSetsCreator.jar ~/tab2kg/dsl/data/datasets/ SOCCER true > ~/tab2kg/dsl/logs/dsl_soccer_d_creation.out

# Soccer_D (less)

cp ~/tab2kg/dsl/model/lr_less.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_less.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset soccer_d --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_soccer_d.txt > ~/tab2kg/dsl/logs/soccer_d_less_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/soccer_d_less
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/soccer_d_less
cp ~/tab2kg/dsl/data/datasets/info_soccer_d.txt ~/tab2kg/dsl/outputs_final/soccer_d_less/info_soccer_d.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SOCCER -domainOntologies -dslPairsFile ~/tab2kg/dsl/outputs_final/soccer_d_less/info_soccer_d.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/soccer_d_less > ~/tab2kg/dsl/results/soccer_d_less.out

# Soccer_D (all)

cp ~/tab2kg/dsl/model/lr_all.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_all.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset soccer_d --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_soccer_d.txt > ~/tab2kg/dsl/logs/soccer_d_all_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/soccer_d_all
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/soccer_d_all
cp ~/tab2kg/dsl/data/datasets/info_soccer_d.txt ~/tab2kg/dsl/outputs_final/soccer_d_all/info_soccer_d.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SOCCER -domainOntologies -dslPairsFile ~/tab2kg/dsl/outputs_final/soccer_d_all/info_soccer_d.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/soccer_d_all > ~/tab2kg/dsl/results/soccer_d_all.out

### Weapons_P

nohup java -jar ~/tab2kg/jars/DSLTrainTestSetsCreator.jar ~/tab2kg/dsl/data/datasets/ WEAPONS false > ~/tab2kg/dsl/logs/dsl_weapons_p_creation.out

# Weapons_P (less)

cp ~/tab2kg/dsl/model/lr_less.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_less.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset weapons_p --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_weapons_p.txt > ~/tab2kg/dsl/logs/weapons_p_less_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/weapons_p_less
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/weapons_p_less
cp ~/tab2kg/dsl/data/datasets/info_weapons_p.txt ~/tab2kg/dsl/outputs_final/weapons_p_less/info_weapons_p.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source WEAPONS -dslPairsFile ~/tab2kg/dsl/outputs_final/weapons_p_less/info_weapons_p.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/weapons_p_less > ~/tab2kg/dsl/results/weapons_p_less.out

# Weapons_P (all)

cp ~/tab2kg/dsl/model/lr_all.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_all.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset weapons_p --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_weapons_p.txt > ~/tab2kg/dsl/logs/weapons_p_all_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/weapons_p_all
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/weapons_p_all
cp ~/tab2kg/dsl/data/datasets/info_weapons_p.txt ~/tab2kg/dsl/outputs_final/weapons_p_all/info_weapons_p.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source WEAPONS -dslPairsFile ~/tab2kg/dsl/outputs_final/weapons_p_all/info_weapons_p.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/weapons_p_all > ~/tab2kg/dsl/results/weapons_p_all.out

### Weapons_D

nohup java -jar ~/tab2kg/jars/DSLTrainTestSetsCreator.jar ~/tab2kg/dsl/data/datasets/ WEAPONS true > ~/tab2kg/dsl/logs/dsl_weapons_d_creation.out

# Weapons_D (less)

cp ~/tab2kg/dsl/model/lr_less.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_less.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset weapons_d --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_weapons_d.txt > ~/tab2kg/dsl/logs/weapons_d_less_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/weapons_d_less
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/weapons_d_less
cp ~/tab2kg/dsl/data/datasets/info_weapons_d.txt ~/tab2kg/dsl/outputs_final/weapons_d_less/info_weapons_d.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source WEAPONS -domainOntologies -dslPairsFile ~/tab2kg/dsl/outputs_final/weapons_d_less/info_weapons_d.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/weapons_d_less > ~/tab2kg/dsl/results/weapons_d_less.out

# Weapons_D (all)

cp ~/tab2kg/dsl/model/lr_all.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_all.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset weapons_d --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_weapons_d.txt > ~/tab2kg/dsl/logs/weapons_d_all_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/weapons_d_all
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/weapons_d_all
cp ~/tab2kg/dsl/data/datasets/info_weapons_d.txt ~/tab2kg/dsl/outputs_final/weapons_d_all/info_weapons_d.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source WEAPONS -domainOntologies -dslPairsFile ~/tab2kg/dsl/outputs_final/weapons_d_all/info_weapons_d.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/weapons_d_all > ~/tab2kg/dsl/results/weapons_d_all.out

### SemTab_P

nohup java -jar ~/tab2kg/jars/DSLTrainTestSetsCreator.jar ~/tab2kg/dsl/data/datasets/ SEMTAB false > ~/tab2kg/dsl/logs/dsl_semtab_p_creation.out

# SemTab_P (less)

cp ~/tab2kg/dsl/model/lr_less.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_less.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset semtab_p --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_semtab_p.txt > ~/tab2kg/dsl/logs/semtab_p_less_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/semtab_p_less
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/semtab_p_less
cp ~/tab2kg/dsl/data/datasets/info_semtab_p.txt ~/tab2kg/dsl/outputs_final/semtab_p_less/info_semtab_p.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB -dslPairsFile ~/tab2kg/dsl/outputs_final/semtab_p_less/info_semtab_p.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/semtab_p_less > ~/tab2kg/dsl/results/semtab_p_less.out

# SemTab_P (all)

cp ~/tab2kg/dsl/model/lr_all.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_all.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset semtab_p --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_semtab_p.txt > ~/tab2kg/dsl/logs/semtab_p_all_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/semtab_p_all
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/semtab_p_all
cp ~/tab2kg/dsl/data/datasets/info_semtab_p.txt ~/tab2kg/dsl/outputs_final/semtab_p_all/info_semtab_p.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB -dslPairsFile ~/tab2kg/dsl/outputs_final/semtab_p_all/info_semtab_p.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/semtab_p_all > ~/tab2kg/dsl/results/semtab_p_all.out

### SemTab_D

nohup java -jar ~/tab2kg/jars/DSLTrainTestSetsCreator.jar ~/tab2kg/dsl/data/datasets/ SEMTAB true > ~/tab2kg/dsl/logs/dsl_semtab_d_creation.out

# SemTab_D (less)

cp ~/tab2kg/dsl/model/lr_less.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_less.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset semtab_d --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_semtab_d.txt > ~/tab2kg/dsl/logs/semtab_d_less_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/semtab_d_less
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/semtab_d_less
cp ~/tab2kg/dsl/data/datasets/info_semtab_d.txt ~/tab2kg/dsl/outputs_final/semtab_d_less/info_semtab_d.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB -domainOntologies -dslPairsFile ~/tab2kg/dsl/outputs_final/semtab_d_less/info_semtab_d.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/semtab_d_less > ~/tab2kg/dsl/results/semtab_d_less.out

# SemTab_D (all)

cp ~/tab2kg/dsl/model/lr_all.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_all.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset semtab_d --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_semtab_d.txt > ~/tab2kg/dsl/logs/semtab_d_all_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/semtab_d_all
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/semtab_d_all
cp ~/tab2kg/dsl/data/datasets/info_semtab_d.txt ~/tab2kg/dsl/outputs_final/semtab_d_all/info_semtab_d.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB -domainOntologies -dslPairsFile ~/tab2kg/dsl/outputs_final/semtab_d_all/info_semtab_d.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/semtab_d_all > ~/tab2kg/dsl/results/semtab_d_all.out

### SemTabEasy_P

nohup java -jar ~/tab2kg/jars/DSLTrainTestSetsCreator.jar ~/tab2kg/dsl/data/datasets/ SEMTAB_EASY false > ~/tab2kg/dsl/logs/dsl_semtab_easy_p_creation.out

# SemTabEasy_P (less)

cp ~/tab2kg/dsl/model/lr_less.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_less.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset semtab_easy_p --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_semtab_easy_p.txt > ~/tab2kg/dsl/logs/semtab_easy_p_less_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/semtab_easy_p_less
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/semtab_easy_p_less
cp ~/tab2kg/dsl/data/datasets/info_semtab_easy_p.txt ~/tab2kg/dsl/outputs_final/semtab_easy_p_less/info_semtab_easy_p.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB_EASY -dslPairsFile ~/tab2kg/dsl/outputs_final/semtab_easy_p_less/info_semtab_easy_p.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/semtab_easy_p_less > ~/tab2kg/dsl/results/semtab_easy_p_less.out

# SemTabEasy_P (all)

cp ~/tab2kg/dsl/model/lr_all.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_all.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset semtab_easy_p --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_semtab_easy_p.txt > ~/tab2kg/dsl/logs/semtab_easy_p_all_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/semtab_easy_p_all
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/semtab_easy_p_all
cp ~/tab2kg/dsl/data/datasets/info_semtab_easy_p.txt ~/tab2kg/dsl/outputs_final/semtab_easy_p_all/info_semtab_easy_p.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB_EASY -dslPairsFile ~/tab2kg/dsl/outputs_final/semtab_easy_p_all/info_semtab_easy_p.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/semtab_easy_p_all > ~/tab2kg/dsl/results/semtab_easy_p_all.out

### SemTabEasy_D

nohup java -jar ~/tab2kg/jars/DSLTrainTestSetsCreator.jar ~/tab2kg/dsl/data/datasets/ SEMTAB_EASY true > ~/tab2kg/dsl/logs/dsl_semtab_easy_d_creation.out

# SemTabEasy_D (less)

cp ~/tab2kg/dsl/model/lr_less.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_less.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset semtab_easy_d --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_semtab_easy_d.txt > ~/tab2kg/dsl/logs/semtab_easy_d_less_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/semtab_easy_d_less
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/semtab_easy_d_less
cp ~/tab2kg/dsl/data/datasets/info_semtab_easy_d.txt ~/tab2kg/dsl/outputs_final/semtab_easy_d_less/info_semtab_easy_d.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB_EASY -domainOntologies -dslPairsFile ~/tab2kg/dsl/outputs_final/semtab_easy_d_less/info_semtab_easy_d.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/semtab_easy_d_less > ~/tab2kg/dsl/results/semtab_easy_d_less.out

# SemTabEasy_D (all)

cp ~/tab2kg/dsl/model/lr_all.pkl ~/tab2kg/dsl/model/lr.pkl
cp ~/tab2kg/dsl/tests/integrated_all.py ~/tab2kg/dsl/tests/integrated.py

cd ~/tab2kg/dsl
nohup python2.7 -m main.api --train_dataset github_training --test_dataset semtab_easy_d --reuse_rf_model True --pairs_file ~/tab2kg/dsl/data/datasets/info_semtab_easy_d.txt > ~/tab2kg/dsl/logs/semtab_easy_d_all_dsl.out

mkdir ~/tab2kg/dsl/outputs_final/semtab_easy_d_all
mv  ~/tab2kg/dsl/output/* ~/tab2kg/dsl/outputs_final/semtab_easy_d_all
cp ~/tab2kg/dsl/data/datasets/info_semtab_easy_d.txt ~/tab2kg/dsl/outputs_final/semtab_easy_d_all/info_semtab_easy_d.txt

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB_EASY -domainOntologies -dslPairsFile ~/tab2kg/dsl/outputs_final/semtab_easy_d_all/info_semtab_easy_d.txt -dslOutputFolder ~/tab2kg/dsl/outputs_final/semtab_easy_d_all > ~/tab2kg/dsl/results/semtab_easy_d_all.out

