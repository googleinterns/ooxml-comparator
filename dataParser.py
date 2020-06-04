import os
import json
import xmltodict
import zipfile
import pandas as pd
from pathlib import Path
import pytest
import sys
import filecmp


class ooxml_file:
	"""
	OOXML file class object to load various XMLs from the zip of the OOXML file and 
	saving them in json format in the coresponding path in the generate folder.
	"""
	def __init__(self,filename):
		self.filename = filename
		
	def load_data(self):

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


def prepare_folder(path):
	path = path.strip()
	if path[-1]=='/':
		gen_path = path + 'generated/'
	else:
		gen_path = path + '/generated/'

	if not os.path.exists(gen_path):
		os.mkdir(gen_path)
	
	for cur_path in Path(path).rglob('*.pptx'):
		cur_file = ooxml_file(str(cur_path))
		cur_file.load_data()
		cur_file.save_json_data(gen_path)
	
	for cur_path in Path(path).rglob('*.xlsx'):
		cur_file = ooxml_file(str(cur_path))
		cur_file.load_data()
		cur_file.save_json_data(gen_path)
		
	for cur_path in Path(path).rglob('*.docx'):
		cur_file = ooxml_file(str(cur_path))
		cur_file.load_data()
		cur_file.save_json_data(gen_path)

if '--run' in sys.argv:
	cur_path = sys.argv[-1].strip()
	prepare_folder(cur_path)


#====================================================
# Testing Code
#====================================================

testFiles = "./test/testFiles"

truth_folder = "./test/testFiles/truth_data"

def build_testdata():
	"""
	This method builds the gold directory structure for the files added
	in the test folder of the data path. This created directory structure 
	will be used while comparing the test.
	"""
	os.system("rm -r ./test/testFiles/generated")
	os.system("rm -r ./test/testFiles/truth_data")

	if not os.path.exists(truth_folder):
		os.mkdir(truth_folder)

	prepare_folder("./test/testFiles")
	os.system("cp -r ./test/testFiles/generated/* ./test/testFiles/truth_data")


# --build-test in command line argument to generate the gold test data
if "--build-test" in sys.argv:
	build_testdata()


def are_dir_trees_equal(dir1, dir2):
	"""
	Compare two directories recursively. Files in each directory are
	assumed to be equal if their names and contents are equal.

	@param dir1: First directory path
	@param dir2: Second directory path

	@return: True if the directory trees are the same and 
		there were no errors while accessing the directories or files, 
		False otherwise.
	"""
	dirs_cmp = filecmp.dircmp(dir1, dir2)
	if len(dirs_cmp.left_only)>0 or len(dirs_cmp.right_only)>0 or \
		len(dirs_cmp.funny_files)>0:
		return False

	(_, mismatch, errors) =  filecmp.cmpfiles(
		dir1, dir2, dirs_cmp.common_files, shallow=False)
	
	if len(mismatch)>0 or len(errors)>0:
		return False
	
	for common_dir in dirs_cmp.common_dirs:
		new_dir1 = os.path.join(dir1, common_dir)
		new_dir2 = os.path.join(dir2, common_dir)
		if not are_dir_trees_equal(new_dir1, new_dir2):
			return False
	
	return True


# The pytest testing function
def test_method():
	os.system("rm -r ./test/testFiles/generated/*")
	prepare_folder(testFiles)
	assert(are_dir_trees_equal('./test/testFiles/generated',truth_folder)==True)