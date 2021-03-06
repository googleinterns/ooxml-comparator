# OOXML COMPARATOR - Intern Project 2020

Whenever an Office file is imported into Google docs and exported back, there are significant differences between the OOXML of the original document and the exported document. It would be helpful to compare the OOXMLs of the two documents so as to understand the similarities and differences.


The OOXML Comparator can be a standalone script/project that can take an input of original Office files and roundtripped files (imported/exported in Docs) and compare all the OOXML between those and provide the differences in them.

The project comprises of two main parts:
1. Data Parser - To convert OOXML files to Folders of JSON
2. Java Comparator - To Run Comparision across all the files in a directory serially.

# Data Parser

To use the zipped OOXML files in JAVA, the files are first converted to JSON format for enhanced readability and manual inspections for Diffs.


## Requirements

Python 3 has been used for the development.
To install the python libarary for the dataparser use the following command

```bash
pip3 install -r ./requirements.txt
```

## Usage

To convert a folder containing various file of .docx, .pptx, .xlsx files, use 

```bash
python3 dataparser.py /path/to/the/folder
```

a 'generated' subfolder is created in the folder provided, with execution log saved in the exec_log.txt and the JSON files for all OOXML files successfully converted.


# Java Comparator

Once the OOXML files are converted to JSON files by the Data Parser, they are treated as the input for the JAVA Comparator.

Java Comparator takes in folder for each of the OOXML files converted, loads the necessary JSON, Compares them, and Generates the Report and Diffs to given path.

## Setup and Usage

Once the Dataparser has been run on Folders containing the OOXML files, We now have two generated folders containing all the folders of OOXML files for their respective types. The Java Comparator takes the Path to these folders as input through a config file.

Java 11.0.7 was used for development of the project.

To prepare the input, a config file looks something like this:

```bash
path/to/the/original/files/generated/folder
path/to/the/roundtripped/files/generated/folder
path/to/the/output/folder
```

The Generated folders are created by Data Parser. in the folder containing the original files itselt. If they are not present, please check the exec_log.txt file generated by the Data Parser for the path.

Save the above shown 3 path line in a file, like 'comparator.config'.

To run the project in a dependency free manner, run it directly from the project jar. Execute the following:

```bash
java -jar ./javacomparator/project_jar/OOXMLcomparator.jar ./path/to/comparator.config
```

To run the comparator on the files using the source, execute the following to build from source (you will need to configure the jar in ./javacomparator/externals first):

```bash
javac ./javacomparator/*.java
java RunComparator ./javacomparator/comparator.config
```

This will run the comparator and produce various reports in the output path provided in the Config File. If not, please check the Status log Created.
