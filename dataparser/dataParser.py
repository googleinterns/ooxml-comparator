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
		
	def loadData(self):
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
	
	def saveJsonData(self,out_path):
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

		print("Processing : ",dir_name,base_name)

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


def isValidPath(path):
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
	if not isValidPath(path):
		return False 	

	path = path.strip()
	if path[-1]=='/':
		gen_path = path + 'generated/'
	else:
		gen_path = path + '/generated/'

	converted_files = 0
	failed_files = 0
	if not os.path.exists(gen_path):
		os.mkdir(gen_path)
	
	execLogger = open(gen_path+'exec_log.txt','w+')

	for cur_path in Path(path).rglob('*.pptx'):
		try:
			execLogger.write
			cur_file = ooxml_file(str(cur_path))
			cur_file.load_data()
			cur_file.save_json_data(gen_path)
			converted_files += 1
		except:
			failed_files += 1
			execLogger.write("Failed to convert : "+str(cur_path)+"\n")
	
	for cur_path in Path(path).rglob('*.xlsx'):
		try:
			cur_file = ooxml_file(str(cur_path))
			cur_file.load_data()
			cur_file.save_json_data(gen_path)
			converted_files += 1
		except:
			failed_files += 1
			execLogger.write("Failed to convert : "+str(cur_path)+"\n")

	for cur_path in Path(path).rglob('*.docx'):
		try:
			cur_file = ooxml_file(str(cur_path))
			cur_file.load_data()
			cur_file.save_json_data(gen_path)
			converted_files += 1
		except:
			failed_files += 1
			execLogger.write("Failed to convert : "+str(cur_path)+"\n")

	execLogger.close()
	print("Number of files processed :",converted_files)
	print("Number of conversion Failed :",failed_files)
	return True

if __name__ == "__main__": 
	
	cur_path = sys.argv[-1].strip()
	success = prepare_folder(cur_path)
	if success:
		print("All files in the path converted Successfully and the Json data is stored in the generated folder")
	else:
		print("Please enter a valid path")