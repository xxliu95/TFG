#!/usr/bin/python3
import os
import sys
import csv
import argparse

from xml.dom.minidom import parseString

def parse_args():
	parser = argparse.ArgumentParser(description='Busca metodos de una lista pasada como argumento.', add_help=False)
	parser.add_argument('--apk', '-a', help='Nombre del apk', required=True)
	parser.add_argument('--methods', '-m', help='Ruta de un fichero JSON con metodos que se quiere buscar. Si no pasa este argumento se analiza todos.')
	parser.add_argument('--permission', '-p', action='store_true', help='Extrae los permisos')
	parser.add_argument('--feature', '-f', action='store_true', help='Extrae las caracteristicas')

	parser.add_argument('--help', '-h', action='help', default=argparse.SUPPRESS,
                    	help='Muestra este mensaje de ayuda.')
	return parser.parse_args()


### Buscando en el manisfest
def searchPermissions(apk):

	data = ''
	pathManifest = '../report/{}/filesExtracted/AndroidManifest.xml'.format(apk)
	file = "../report/{}/permissions.csv".format(apk)

	with open(pathManifest,'r') as f:
	    data = f.read()
	dom = parseString(data)
	nodes = dom.getElementsByTagName('uses-permission')

	with open(file, 'w', encoding='utf-8') as f:
		w = csv.writer(f)

		w.writerow(["Permisos"])
		# Iterate over all the uses-permission nodes
		for node in nodes:
			w.writerow([node.getAttribute('android:name')])

		w.writerow([])

def searchFeatures(apk):

	data = ''
	pathManifest = '../report/{}/filesExtracted/AndroidManifest.xml'.format(apk)
	file = "../report/{}/features.csv".format(apk)

	with open(pathManifest,'r') as f:
	    data = f.read()
	dom = parseString(data)
	nodes = dom.getElementsByTagName('uses-feature')

	with open(file, 'w', encoding='utf-8') as f:
		w = csv.writer(f)

		w.writerow(["Caracteristicas"])
		# Iterate over all the uses-feature nodes
		for node in nodes:
			w.writerow([node.getAttribute('android:name')])

		w.writerow([])

def searchServices(apk):

	data = ''
	pathManifest = '../report/{}/filesExtracted/AndroidManifest.xml'.format(apk)
	file = "../report/{}/ServicesAndReceivers.csv".format(apk)

	with open(pathManifest,'r') as f:
	    data = f.read()


	dom = parseString(data)
	nodes = dom.getElementsByTagName('service')

	with open(file, 'w', encoding='utf-8') as f:
		w = csv.writer(f)

		# Iterate over all the service nodes
		for node in nodes:
			if node.getAttribute('android:permission') == 'android.permission.BIND_JOB_SERVICE':
				w.writerow(["Job service", node.getAttribute('android:name')])
			else:
				w.writerow(["Service", node.getAttribute('android:name')])


def searchBroadcastReceiver(apk):

	data = ''
	pathManifest = '../report/{}/filesExtracted/AndroidManifest.xml'.format(apk)
	file = "../report/{}/ServicesAndReceivers.csv".format(apk)

	with open(pathManifest,'r') as f:
	    data = f.read()

	dom = parseString(data)

	nodes = dom.getElementsByTagName('receiver')

	with open(file, 'a', encoding='utf-8') as f:
		w = csv.writer(f)

		# Iterate over all the service nodes
		for node in nodes:
				w.writerow(["Receiver", node.getAttribute('android:name')])

def extractApk(apk):
	os.system('apktool d ../apks/{}.apk -o ../report/{}/filesExtracted'.format(apk, apk))

def callgraph(apk, file, methods):
	#os.system('source venv-androguard/bin/activate')
	os.system('python callgraphBIEN.py -a /home/static/analysis/apks/{}.apk --methods {} --save-csv -c importantMethods'.format(apk, methods))
	#os.system('python callgraphBIEN.py -a /home/static/analysis/apks/{}.apk --methods {} --save-csv --verbose -c importantMethods'.format(apk, methods))

	with open('../report/{}/importantMethods_{}.csv'.format(apk, apk), 'r') as cg:
		reader = csv.reader(cg)
		next(reader)
		lista_metodos = []
		for line in reader:
			metodo = line[0].strip().replace("/",".").replace(";","")[3:-1]
			utilizado = line[1].strip().replace("/",".").replace(";","")[3:-1]
			lista_metodos.append([metodo, utilizado])

	with open('../report/{}/ServicesAndReceivers.csv'.format(apk), 'r') as sr:
		readersr = csv.reader(sr)
		with open(file, 'w', encoding='utf-8') as f:
			writer = csv.writer(f)
			writer.writerow(["Tipo", "Clase", "Metodo"])

			# Busca si en la lista de metodos si esta en un servicio
			for l in readersr:
				for linea in lista_metodos:
					if linea[1] == l[1]:
						writer.writerow([l[0], l[1], linea[0]])

def analyze(apk,  methods, permission=False, feature=False):

	file = "../report/{}/report.csv".format(apk)

	if permission:
		searchPermissions(apk)
	if feature:
		searchFeatures(apk)

	searchServices(apk)
	searchBroadcastReceiver(apk)
	callgraph(apk, file, methods)

if __name__ == '__main__':

	args = parse_args()

	apk = args.apk
	methods = args.methods

	if args.methods is not None:
		methods = args.methods
	else:
		methods = "config-files/METODOS.json"

	if not os.path.exists('../report/{}/filesExtracted'.format(apk)):
		extractApk(apk)

	analyze(apk, methods, args.permission, args.feature)
