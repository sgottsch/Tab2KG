# Tab2KG

## Datasets

To create datasets, run the following processes:

1. GitHub
	1.1 Run GitHubFilesDownloader
	1.2 Run GitHubTablesCreator
2. SemTab
	2.1 Download and unzip the following file into a folder: https://zenodo.org/record/3518539/files/semtab2019.zip?download=1 
	2.2 Run SemTabTableCreator with the folder as argument
3. Weapons
	3.1 Download and unzip the following file into a folder: https://github.com/taheriyan/iswc-2016/raw/master/weapon-ads.zip
	3.2 Run ModelFilesCreatorwith arguments "WEAPONS" and the paths to the downloaded folders "sources" and "models_json"
4. Soccer
	4.1 Download and unzip the following folders into a folder: https://github.com/minhptx/iswc-2016-semantic-labeling/tree/master/data/datasets/soccer
		For example, through the following commands:
			wget https://github.com/minhptx/iswc-2016-semantic-labeling/archive/master.zip
			unzip master.zip
			mv iswc-2016-semantic-labeling-master/data/datasets/soccer/ original_data
			rm -r iswc-2016-semantic-labeling-master/
	4.2 Run ModelFilesCreatorwith arguments "SOCCER" and the paths to the downloaded folders "data" and "model"
5. SemTab Easy
	5.1 Copy the file in resources/data/gold_standard_classes.csv to your data folder.

For each of the five data sets GITHUB, SEMTAB, WEAPONS, SOCCER and SEMTAB_EASY, run the TableGraphPairsFinder (with the dataset identifier as argument).
	
Unfortunately, the GitHub default license does not allow distribution of public repositories (https://help.github.com/en/github/creating-cloning-and-archiving-repositories/licensing-a-repository\#choosing-the-right-license). Thus, we can not share our GitHub dataset.
 
## Profile Generation and GitHub Download

TabKG.zip contains the Java code for the creation of data type relation and column profiles.

Example usage: Get all the normalised profiles between a data table and a graph from the soccer domain.

> java -jar ProfilePairNormaliser
> .../data/evaluation/soccer/data_cleaned/all_world_cup_players.csv
> .../data/evaluation/soccer/graphs/world_cup_player_ages.csv.ttl

    Output ("c1": column identifier):
    c1
    http://schema.org/Player http://schema.org/tag
    [0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.014190405591443397, ...]
    c1
    http://schema.org/Player http://schema.org/playPosition
    [0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.014190405591443397, ...]

* GithubFilesDownloader and opendataportalwatch.DataSetsCollector can be used to download RDF files found in GitHub and the Open Data Portal Watch.
* The "data" package contains scripts to transform soccer, weapons and SemTab data into the required format (RML and model format used by the Domain-independent semantic labeler).
    
## Profile Similarity Computation

* siamese_column.py: The Python script to learn profile similarity from a set of positive and negative profile pairs.
