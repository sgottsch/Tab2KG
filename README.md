# Tab2KG

Tab2KG is a method to automatically infer tabular data semantics and transform such data into a semantic data graph.

## Semantic Profiles: Schema

You can find Tab2KG's extension of the DCAT and SEAS vocabulary for representing semantic profiles in RDF [here](https://github.com/sgottsch/Tab2KG/blob/main/data/data_catalog_schema_tab2kg.ttl).

## Configuration

Create a directory for Tab2KG in your file system (e.g. "/Documents/Tab2KG/"). Insert that path in de.l3s.simpleml.tab2kg.util.Config. Optionally, you can also distinguish between a local and a server path there.

## Datasets

For training Tab2KG and for evaluation, we use a dataset created from GitHub as well other well-known datasets. To create triples of domain profiles, data tables and column mappings, you need to transform these datasets into the required formats. A dataset is transformed into a directory with four sub-folders and a file as follows:

- graphs: `.ttl` knowledge graph files.
- mappings: `.rml` RDF Mapping language definitions to transform tables into knowledge graphs.
- models: `.json` definitions to transform tables into knowledge graphs.
- tables: `.csv` data tables.
- pairs.tsv: A tab-separated file denoting pairs of tables and knowledge graphs.

The corresponding directories for the Soccer and the Weapons dataset are in the [data folder](https://github.com/sgottsch/Tab2KG/tree/main/data/datasets). Unfortunately, the GitHub default license does not allow distribution of public repositories (https://help.github.com/en/github/creating-cloning-and-archiving-repositories/licensing-a-repository\#choosing-the-right-license). Thus, we can not share our GitHub dataset. As we can not share this, we provide the scripts for creating them yourself.

To create datasets yourself, run the following processes:

1. Soccer
	1.1 Download and unzip the following folders into a folder: https://github.com/minhptx/iswc-2016-semantic-labeling/tree/master/data/datasets/soccer
		For example, through the following commands:
			wget https://github.com/minhptx/iswc-2016-semantic-labeling/archive/master.zip
			unzip master.zip
			mv iswc-2016-semantic-labeling-master/data/datasets/soccer/ original_data
			rm -r iswc-2016-semantic-labeling-master/
	1.2 Run de.l3s.simpleml.tab2kg.data.ModelsDataSetTableCreator arguments "SOCCER" and the paths to the downloaded folders "data" and "model"
2. Weapons
	2.1 Download and unzip the following file into a folder: https://github.com/taheriyan/iswc-2016/raw/master/weapon-ads.zip
	2.2 Run de.l3s.simpleml.tab2kg.data.ModelsDataSetTableCreator with arguments "WEAPONS" and the paths to the downloaded folders "sources" and "models_json"
3. GitHub
	3.1 Run de.l3s.simpleml.tab2kg.data.github.GitHubFilesDownloader
	3.2 Run de.l3s.simpleml.tab2kg.data.github.GitHubTablesCreator
4. SemTab
	4.1 Download and unzip the following file into a folder: https://zenodo.org/record/3518539/files/semtab2019.zip?download=1 
	4.2 Run de.l3s.simpleml.tab2kg.data.semtab.SemTabTableCreator with the folder as argument
5. SemTab Easy
	5.1 Copy the file in resources/data/gold_standard_classes.csv to your data folder.

For each of the five data sets GITHUB, SEMTAB, WEAPONS, SOCCER and SEMTAB_EASY, run the de.l3s.simpleml.tab2kg.data.TableGraphPairsFinder (with the dataset identifier as argument).
	 
## Profile Generation

With Tab2KG, you can create data type relation profiles and column profiles.

Example usage: Get the normalized column and data type profiles between a data table and a graph from the soccer domain using de.l3s.simpleml.tab2kg.profiles.ProfilePairNormaliser.

> java -jar ProfilePairNormaliser
> soccer/tables/all_world_cup_players.csv
> soccer/graphs/world_cup_player_ages.csv.ttl

    Output:
	--- Team ---
	http://schema.org/Player http://schema.org/playPosition
	[0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ... , 0.0231917 , ...]
	[0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ... , 0.5661216 , ...]
    ...

## Semantic Table Interpretation

## Evaluation

Run de.l3s.simpleml.tab2kg.evaluation.DataSetEvaluation with the required parameters to evaluate the semantic table interpretation performance for a single dataset.



## Profile Similarity Computation

* siamese_column.py: The Python script to learn profile similarity from a set of positive and negative profile pairs.
