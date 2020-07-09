import json
import xmltodict
import zipfile
import pandas as pd
from pathlib import Path
import pytest
import os
import sys
import filecmp
import dataParser

testFiles = "./testFiles"
truth_folder = "./testFiles/truth_data"
sys.path.append("../../DataParser")

__TESTS__ = 4 

def build_testdata():
	"""
	This method builds the gold directory structure for the files added
	in the test folder of the data path. This created directory structure 
	will be used while comparing the test.
	"""
	os.system("rm -rf ./testFiles/truth_data/*")
	for i in range(1,__TESTS__+1):
		print("Preparing Test :",i)
		os.system("rm -rf ./testFiles/test"+str(i)+"/generated")
		dataParser.prepare_folder("./testFiles/test"+str(i))
		os.system("cp -rf ./testFiles/test"+str(i)+" ./testFiles/truth_data")
		os.system("rm -rf ./testFiles/test"+str(i)+"/generated")


# --build-test in command line argument to generate the gold test data
if "--build" in sys.argv:
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

# @pytest.mark.set1
def test_method1():
	""" 
	Test for conversion of Word Data
	"""
	dataParser.prepare_folder("./testFiles/test1")
	assert(are_dir_trees_equal("./testFiles/truth_data/test1","./testFiles/test1")==True)
	os.system("rm -rf ./testFiles/test1/generated")

# @pytest.mark.set2
def test_method2():
	""" 
	Test for conversion of Excel Data
	"""
	dataParser.prepare_folder("./testFiles/test2")
	assert(are_dir_trees_equal("./testFiles/truth_data/test2","./testFiles/test2")==True)
	os.system("rm -rf ./testFiles/test2/generated")


# @pytest.mark.set3
def test_method3():
	""" 
	Test for conversion of PPT Data
	"""
	dataParser.prepare_folder("./testFiles/test3")
	assert(are_dir_trees_equal("./testFiles/truth_data/test3","./testFiles/test3")==True)
	os.system("rm -rf ./testFiles/test3/generated")


# @pytest.mark.set4
def test_method4():
	""" 
	Test for Empty Directory
	"""
	dataParser.prepare_folder("./testFiles/test4")
	assert(are_dir_trees_equal("./testFiles/truth_data/test4","./testFiles/test4")==True)
	os.system("rm -rf ./testFiles/test4/generated")

# @pytest.mark.set5
def test_method5():
	""" 
	Test for Invalid Path
	"""
	dataParser.prepare_folder("./testFiles/test5")==False
	