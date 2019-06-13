#!/usr/bin/python3
import os
import sys
import csv
import argparse

from xml.dom.minidom import parseString

def parse_args():
	parser = argparse.ArgumentParser(description='Selecciona metodos de una lista pasada como argumento.', add_help=False)
	parser.add_argument('--apk', '-a', help='Nombre del apk', required=True)
	parser.add_argument('--methods', '-m', help='Tipos de metodos buscados, pueden ser camara, localizacion o audio. Si no pasa este argumento se analiza todos.')
	parser.add_argument('--permission', '-p', action='store_true', help='Extrae solo los permisos')
	parser.add_argument('--feature', '-f', action='store_true', help='Extrae solo las caracteristicas')
	parser.add_argument('--service', '-s', action='store_true', help='Extrae solo los servicios')
	parser.add_argument('--receiver', '-r', action='store_true', help='Extrae solo los Broadcast Receiver')

	parser.add_argument('--help', '-h', action='help', default=argparse.SUPPRESS,
                    	help='Muestra este mensaje de ayuda.')
	return parser.parse_args()


### Buscando en el manisfest
def searchPermissions(apk, file):

	data = ''
	pathManifest = '../report/{}/filesExtracted/AndroidManifest.xml'.format(apk)

	with open(pathManifest,'r') as f:
	    data = f.read()
	dom = parseString(data)
	nodes = dom.getElementsByTagName('uses-permission')

	with open(file, 'a', encoding='utf-8') as f:
		w = csv.writer(f)

		w.writerow(["Permisos"])
		# Iterate over all the uses-permission nodes
		for node in nodes:
			w.writerow([node.getAttribute('android:name')])
		
		w.writerow([])

def searchFeatures(apk, file):

	data = ''
	pathManifest = '../report/{}/filesExtracted/AndroidManifest.xml'.format(apk)

	with open(pathManifest,'r') as f:
	    data = f.read()
	dom = parseString(data)
	nodes = dom.getElementsByTagName('uses-feature')

	with open(file, 'a', encoding='utf-8') as f:
		w = csv.writer(f)

		w.writerow(["Caracteristicas"])
		# Iterate over all the uses-feature nodes
		for node in nodes:
			w.writerow([node.getAttribute('android:name')])
		
		w.writerow([])

def searchServices(apk, file):

	data = ''
	pathManifest = '../report/{}/filesExtracted/AndroidManifest.xml'.format(apk)

	with open(pathManifest,'r') as f:
	    data = f.read()
	dom = parseString(data)
	nodes = dom.getElementsByTagName('service')

	with open(file, 'a', encoding='utf-8') as f:
		w = csv.writer(f)

		w.writerow(["Servicios", "Permisos adicionales necesarios para el servicio"])
		# Iterate over all the service nodes
		for node in nodes:
			if node.getAttribute('android:permission'):
				w.writerow([node.getAttribute('android:name'), node.getAttribute('android:permission')])
			else:
				w.writerow([node.getAttribute('android:name'), 'ninguno'])
		
		w.writerow([])


def searchBroadcastReceiver(apk, file):

	data = ''
	#with open('../1 - ExtractedApks/{}/AndroidManifest.xml'.format(apk),'r') as f:
	pathManifest = '../report/{}/filesExtracted/AndroidManifest.xml'.format(apk)
	with open(pathManifest,'r') as f:
	    data = f.read()

	dom = parseString(data)

	nodes = dom.getElementsByTagName('receiver')

	with open(file, 'a', encoding='utf-8') as f:
		w = csv.writer(f)

		w.writerow(["Broadcast Receiver"])
		# Iterate over all the service nodes
		for node in nodes:
				w.writerow([node.getAttribute('android:name')])
		
		w.writerow([])

def extractApk(apk):
	os.system('apktool d ../apks/{}.apk -o ../report/{}/filesExtracted'.format(apk, apk))

def callgraph(apk, file, methods):
	#os.system('source venv-androguard/bin/activate')
	if methods == "localizacion":
		os.system('python callgraphBIEN.py -a /home/static/analysis/apks/{}.apk --methods config-files/METODOS_LOCALIZACION.json --save-csv -c importantMethods'.format(apk))
	elif methods == "camara":
		os.system('python callgraphBIEN.py -a /home/static/analysis/apks/{}.apk --methods config-files/METODOS_CAMARA.json --save-csv -c importantMethods'.format(apk))
	elif methods == "audio":
		os.system('python callgraphBIEN.py -a /home/static/analysis/apks/{}.apk --methods config-files/METODOS_AUDIO.json --save-csv -c importantMethods'.format(apk))
	else:
		os.system('python callgraphBIEN.py -a /home/static/analysis/apks/{}.apk --methods config-files/METODOS.json --save-csv -c importantMethods'.format(apk))

	with open('../report/{}/importantMethods_{}.csv'.format(apk, apk), 'r') as cg:
		reader = csv.reader(cg)

		with open(file, 'a', encoding='utf-8') as f:
			writer = csv.writer(f)

			for line in reader:
				writer.writerow(line)

def init(file):
	with open(file, 'w', encoding='utf-8') as f:
		w = csv.writer(f)

		w.writerow(["Resultado de analisis de la app {}.apk".format(apk)])
		w.writerow([])

def analyze(apk,  methods, permission=False, service=False, receiver=False, feature=False):

	file = "../report/{}/report.csv".format(apk)

	init(file)

	if permission:
		searchPermissions(apk, file)
	elif service:
		searchServices(apk, file)
	elif receiver:
		searchBroadcastReceiver(apk, file)
	elif feature:
		searchFeatures(apk, file)
	else:
		searchPermissions(apk, file)
		searchFeatures(apk, file)
		searchServices(apk, file)
		searchBroadcastReceiver(apk,file)
		callgraph(apk, file, methods)


if __name__ == '__main__':

	args = parse_args()

	apk = args.apk
	methods = args.methods
	
	if not os.path.exists('../report/{}/filesExtracted'.format(apk)):
		extractApk(apk)

	analyze(apk, methods, args.permission, args.service, args.receiver, args.feature)
	