import os
import json
import xmltodict
import zipfile
import pandas as pd
from pathlib import Path
import pytest
import sys
import filecmp


class OoxmlFile:
	"""
	OOXML file class object to load various XMLs from the zip of the OOXML file and 
	saving them in json format in the coresponding path in the generate folder.
	"""
	def __init__(self,filename):
		self.filename = filename
		
	def load_data(self):
		"""
		This function loads the OOXML files, Unzips then and saves their data 
		in a dictionary format for all the XMLs present in the code.
		"""
		archive = zipfile.ZipFile(self.filename, 'r')

		self.xml_files = {}
		self.rel_files = {}
		self.other_data_files = {}
		
		for zip_obj in archive.infolist():
			
			extension = str(zip_obj.filename).strip().split('.')[-1]
			
			if extension=='xml':
				with archive.open(zip_obj.filename) as myfile:
					data = myfile.read()
					self.xml_files[zip_obj.filename]= xmltodict.parse(data)
			
			elif extension=='rels':
				with archive.open(zip_obj.filename) as myfile:
					data = myfile.read()
					self.rel_files[zip_obj.filename]=xmltodict.parse(data)
			
			else:
				self.other_data_files[zip_obj.filename]=zip_obj.filename
	
	def save_json_data(self,out_path):
		"""
		This saves the loaded XML file's dictionaries into the path provides 
		in JSON format.

		@Param out_path: path to save all the XML files by creating a folder 
						for the XML file.
		"""
		path_name_decomp = self.filename.split('/')
		dir_name = self.filename[:-len(path_name_decomp[-1])]
		
		base_name = path_name_decomp[-1].replace('.','_')
		dir_name = out_path

		dir_name = dir_name+base_name

		if not os.path.exists(dir_name):
			os.mkdir(dir_name)
		
		for xml_file in list(self.xml_files.keys()):

			json_file_name = dir_name+'/'+xml_file.replace('/','_')+'.json'

			with open(json_file_name,'w') as outfile:
				jData = json.dumps(self.xml_files[xml_file],indent=4)
				outfile.write(json.dumps(self.xml_files[xml_file],indent=4))
		

		for xml_file in list(self.rel_files.keys()):

			json_file_name = dir_name+'/'+xml_file.replace('/','_')+'.json'

			with open(json_file_name,'w') as outfile:
				jData = json.dumps(self.rel_files[xml_file],indent=4)
				outfile.write(jData)


def is_valid_path(path):
	"""
	Function to check if a path is valid

	@Param path: Path to be checked for validity or existence.
	"""
	isDirectory = os.path.isdir(path)
	if not isDirectory:
		return False
	else:
		return True


def prepare_folder(path):
	"""
	Function to create a new generate folder and 
	create all the json data for all files in the generated folder.

	@param path: path to the folder whose all files has to be converted to json data 
	"""
	path = path.strip()
	if path[-1]=='/':
		gen_path = path + 'generated/'
	else:
		gen_path = path + '/generated/'

	converted_files = 0
	failed_files = 0

	if not os.path.exists(gen_path):
		os.mkdir(gen_path)

	exec_logger = open(gen_path+'exec_log.txt','w+')

	for cur_path in Path(path).rglob('*.pptx'):
		try:
			exec_logger.write("Processing : "+str(cur_path)+"\n")

			cur_file = OoxmlFile(str(cur_path))
			cur_file.load_data()
			cur_file.save_json_data(gen_path)

			converted_files += 1
		except:
			failed_files += 1
			exec_logger.write("Failed to convert : "+str(cur_path)+"\n")
	
	for cur_path in Path(path).rglob('*.xlsx'):
		try:
			exec_logger.write("Processing : "+str(cur_path)+"\n")

			cur_file = OoxmlFile(str(cur_path))
			cur_file.load_data()
			cur_file.save_json_data(gen_path)

			converted_files += 1
		except:
			failed_files += 1
			exec_logger.write("Failed to convert : "+str(cur_path)+"\n")

	for cur_path in Path(path).rglob('*.docx'):
		try:
			exec_logger.write("Processing : "+str(cur_path)+"\n")

			cur_file = OoxmlFile(str(cur_path))
			cur_file.load_data()
			cur_file.save_json_data(gen_path)

			converted_files += 1
		except:
			failed_files += 1
			exec_logger.write("Failed to convert : "+str(cur_path)+"\n")

	exec_logger.write("Number of files processed :"+str(converted_files)+"\n")
	exec_logger.write("Number of conversion Failed :"+str(failed_files)+"\n")

	exec_logger.write("All files in the path converted Successfully and the Json data is stored in the generated folder\n")
	exec_logger.write("Location saved at : "+str(gen_path))

	exec_logger.close()


if __name__ == "__main__": 
	
	cur_path = sys.argv[-1].strip()
	status_logger = open('status.txt','w+')

	if not is_valid_path(cur_path):
		status_logger.write("Please enter a valid path\n")
	else:
		try:
			prepare_folder(cur_path)
			status_logger.write("File conversions Successful")
		except:
			status_logger.wrte("File conversions failed.")

	status_logger.close()
		