# SistemasDistribuidos-Lab2

Javier Fuentes
Ismael Vicencio

**************************************************************************************

Crear carpetas para las replicas con nombres:
rs0-0
rs0-1
rs0-2

Iniciar mongo
mongod --port 27017 --bind_ip=127.0.0.1 --dbpath rs0-0 --replSet rs0 --smallfiles --oplogSize 128
mongod --port 27018 --bind_ip=127.0.0.1 --dbpath rs0-1 --replSet rs0 --smallfiles --oplogSize 128
mongod --port 27019 --bind_ip=127.0.0.1 --dbpath rs0-2 --replSet rs0 --smallfiles --oplogSize 128

Para iniciar la replica cero

mongo --port 27017

rs.initiate({_id:"rs0", members: [{"_id":1, "host":"127.0.0.1:27017"}]})

rs.add("127.0.0.1:27018")
rs.add("127.0.0.1:27019")

**************************************************************************************

El programa hace uso de los datos contenidos en el archivo datos.ini
El formato de este archivo corresponde a :
	
	nombre base de datos
	nombre documento xml
	nombre coleccion que contiene los documentos
	nombre colecion que continee los indices invertidos

Ambos proyectos hacen uso de este tipo de archivo, un archivo por cada proyecto

**************************************************************************************

Ejemplo de indice invertido
** Se almacena el titulo para aumentar la velocidad de las busquedas, debido a que normalmente se debe
** entregar esta información en la búsqueda e ir a buscar a otra collection lleva un tiempo de costo que se
** justifica con agregar esta nueva variable

{
	"_id": ObjectId('569055d285c8ae07f40183d0'),
	"palabra": "YORK",
	"documento": [
		{
			"id_documento": 3,       -> Id documento donde se encuentra la palaba
			"titulo": "Andorra",	 -> Titulo del documento encontrado
			"frecuencia": 1			 -> Frecuencia en el documento
		},
		{
			"id_documento": 5,
			"titulo": "Batalla de Leipzig",
			"frecuencia": 1
		},
		{
			"id_documento": 10,
			"titulo": "David Irving",
			"frecuencia": 8
		}
	]
}

**************************************************************************************
Ejemplo de documento almacenado
{
	"_id": 7,
	"titulo": "Administradoras de Fondos de Jubilaciones y Pensiones de Argentina",
	"texto": "#redirect [[Administradora de Fondos de Jubilaciones y Pensiones]]"
}

