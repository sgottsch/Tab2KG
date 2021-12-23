# pair-wise

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SOCCER > ~/tab2kg/evaluation/results_output/soccer_p.txt
nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source WEAPONS > ~/tab2kg/evaluation/results_output/weapons_p.txt
nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source GITHUB > ~/tab2kg/evaluation/results_output/github_p.txt
nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB > ~/tab2kg/evaluation/results_output/semtab_p.txt
nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB_EASY > ~/tab2kg/evaluation/results_output/semtab_easy_p.txt

# complete graph

nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SOCCER -domainOntologies > ~/tab2kg/evaluation/results_output/soccer_d.txt
nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source WEAPONS -domainOntologies > ~/tab2kg/evaluation/results_output/weapons_d.txt
nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB -domainOntologies > ~/tab2kg/evaluation/results_output/semtab_d.txt
nohup java -jar ~/tab2kg/jars/DataSetEvaluation.jar -source SEMTAB_EASY -domainOntologies > ~/tab2kg/evaluation/results_output/semtab_easy_d.txt

