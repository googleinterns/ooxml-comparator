# Data Parser

To use the zipped OOXML files in JAVA, the files are first converted to JSON format for enhanced readablity and manual inspections for Diffs.


## Requirements

Python 3 has been used for the development.
To install the python libarary for the dataparser use the following command

```bash
pip3 install -r ./requirements.txt
```

## Usage

To convert a folder containing various file of .docx, .pptx, .xlsx files, use 

```bash
python3 dataparser /path/to/the/folder
```

a 'generated' subfolder is created with execution log saved in the exec_log.txt and the JSON files for all OOXML files successfully converted.
