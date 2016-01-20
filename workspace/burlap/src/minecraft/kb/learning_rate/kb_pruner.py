import sys
import re
from os import listdir, rename
from os.path import isfile, join

path = "."

all_kbs = [f for f in listdir(path) if isfile(join(path,f)) ]
kb_files = []

# Get kb files
for f in all_kbs:
	kb_files.append(f)


for kb in kb_files:
	oldfract = re.sub("[^0-9]", "", kb)
	if(len(oldfract) < 1):
		continue
	newfract = float(oldfract) / 20.0 - 0.05
	new_kb_name = "lr_" + "{0:.2f}".format(newfract) + ".kb"
	out_file = file(new_kb_name, "w+")
	old_file = file(kb,"r")
	for line in old_file.readlines():
		if len(line) <= 1:
			continue
		elif ";" in line:
			continue
		else:
			out_file.write(line)
