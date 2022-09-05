"""
Tool to convert non-prefixed RDF triples file to a file with prefixes extracted. Only works
on prefixes defined in this script
"""

import os
import re
import sys


prefixes = dict(
	ga="https://data.goldenagents.org/ontology/",
	rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#",
	rdfs="http://www.w3.org/2000/01/rdf-schema#",
	owl="http://www.w3.org/2002/07/owl#",
	schema="http://schema.org/",
	skos="http://www.w3.org/2004/02/skos/core#",
	void="http://rdfs.org/ns/void#",
	fabio="http://purl.org/spar/fabio/"
)

prefixes_by_url = dict([(v,k) for (k,v) in prefixes.items()])

unknown_urls = list()

prefixes_used = list()
output = []

with open(sys.argv[1]) as rdf_in:
	for line in rdf_in:
		matches = re.findall(r'<(https?:\/\/.*?[\/#])(\w+)>', line)
		for m in matches:
			if m[0] in prefixes_by_url:
				line = line.replace(f'<{m[0]}{m[1]}>', f"{prefixes_by_url[m[0]]}:{m[1]}")
				if prefixes_by_url[m[0]] not in prefixes_used:
					prefixes_used.append(prefixes_by_url[m[0]])
			else:
				if m[0] not in unknown_urls:
					unknown_urls.append(m[0])
				line = line.replace(f'<{m[0]}{m[1]}>', f'ns{unknown_urls.index(m[0])}:{m[1]}')
		
		output.append(line)

path = (os.sep).join(sys.argv[1].split(os.sep)[:-1])
file_name = sys.argv[1].split(os.sep)[-1]
name, extension = file_name[:file_name.rfind('.')], file_name[file_name.rfind('.'):]

outfile = os.path.join(path, name + "_prefixed" + extension)

with open(outfile, 'w') as rdf_out:
	for p in prefixes_used:
		rdf_out.write(f"@prefix {p} <{prefixes[p]}> .\n")

	for i, u in enumerate(unknown_urls):
		rdf_out.write(f"@prefix ns{i}: <{u}> .\n")

	if len(prefixes_used) or len(unknown_urls):
		rdf_out.write("\n")

	for o in output:
		rdf_out.write(o)

print(f"Converted {sys.argv[1]} to {outfile}")


