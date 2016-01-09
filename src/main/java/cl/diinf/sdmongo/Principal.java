/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.diinf.sdmongo;

import com.mongodb.BasicDBList;
import org.bson.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.DBCollection;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 *
 * @author ismael
 */
public class Principal {

    public static void main(String[] args) throws IOException {

        String nombreDB;
        String nombreCollection;
        String nombreIndice;
        String fileName;

        try {
            BufferedReader entrada =   new BufferedReader(new FileReader("datos.ini"));
            
            nombreDB = null;
            try {
                nombreDB = entrada.readLine();
                fileName = entrada.readLine();
                nombreCollection = entrada.readLine();
                nombreIndice = entrada.readLine();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                fileName = "ismael.xml";
                nombreDB = "labsd";
                nombreCollection = "documentos";
                nombreIndice = "indiceInvertido";
            }
            entrada.close();
        
            BasicDBObject ObjDocumento = new BasicDBObject();
            DBCollection documentos;
            DBCollection indiceInvertido;
            MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
            //MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://127.0.0.1:27017,127.0.0.1:27018,127.0.01:27019/?replicaSet=rs0"));
            /*MongoClient mongoClient = new MongoClient(Arrays.asList(
                    new ServerAddress("localhost", 27017),
                    new ServerAddress("localhost", 27018),
                    new ServerAddress("localhost", 27019)));*/
            mongoClient.slaveOk();
            mongoClient.setWriteConcern(WriteConcern.SAFE);
            DB database = mongoClient.getDB(nombreDB);
            System.out.println("Conexión a base de datos " + nombreDB + "realizada con éxito");
            System.out.println("Detectando la existencia de coleciones requeridas...");
            System.out.print("Coleccion de documentos...");
            if (!database.collectionExists(nombreCollection)) {
                System.out.println("No existe coleccion documentos");
                System.out.println("Creando coleccion documentos");
                documentos = database.createCollection(nombreCollection, new BasicDBObject());

            } else {
                System.out.println("La coleccion documentos existe");
            }
            if (!database.collectionExists(nombreIndice)) {
                System.out.println("No existe la coleccion de indices...");
                System.out.println("creando coleccion de indices invertidos");
                indiceInvertido = database.createCollection(nombreIndice, new BasicDBObject());
            } else {
                System.out.println("La coleccion de indice invertido existe");
            }
            documentos = database.getCollection(nombreCollection);
            indiceInvertido = database.getCollection(nombreIndice);
            /*     DBObject person;
            for (int i = 1; i <= 10000; i++) {
                person = new BasicDBObject("_id", i).append("titulo", "Javier").append("texto", "Chino");

                WriteResult writeResult = documentos.insert(person);
                System.out.println(writeResult);
                System.out.println("Insertado");
            }*/
            //Procede a parsear el documento xml y almacenando esta información en mongo
            // almacena un id unico para cada documento, el titulo y el texto correspondiente
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            WikiXMLReader wikiXML = new WikiXMLReader(documentos, database, indiceInvertido);
            parser.parse(new File(fileName), wikiXML);

            // Muestra los documentos
            System.out.println("=======================================");
            System.out.println("=======================================");
            System.out.println("=======================================");
            System.out.println("=======================================");

            MongoClient mongoClient2 = new MongoClient();
            MongoDatabase db = mongoClient.getDatabase("labsd");
            FindIterable<Document> iterable = db.getCollection("documentos").find();
            iterable.forEach(new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    //System.out.println(document);
                }
            });

            System.out.println("=======================================");
            System.out.println("=======================================");
            System.out.println("=======================================");

            System.out.println("Cantidad de elementos: " + db.getCollection(nombreCollection).count());

            // Ciera la conexion al cluster
            mongoClient.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
