/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.diinf.sdmongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.math.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ismael
 */
public class WikiXMLReader extends DefaultHandler {

    //Almacena el identificador de el elemento leido
    int contador;
    WikiIndexCreator wikiIndexCreator;
    private StringBuilder content;
    DBCollection documentos;
    DBCollection indiceInvertido;
    DB database;
    DBObject person;
    // Variables que contiene lso datos del tag
    String titulo;
    StringBuilder texto;
    // StringTokenizer para contar las palabras de un texto
    StringTokenizer textoEstudio;
    String[] textoParser;
    ArrayList<String> textoParser2;
    int contadorPalabras;

    WriteResult writeResult;

    boolean isTitulo;
    boolean isTexto;

    int contadorWikipedia = 0;

    public WikiXMLReader(DBCollection documentos, DB database, DBCollection indiceInvertido) {
        this.documentos = documentos;
        this.database = database;
        this.contador = 1;
        this.indiceInvertido = indiceInvertido;
        content = new StringBuilder();
        isTitulo = false;
        isTexto = false;
        BasicDBObject ObjDocumento = new BasicDBObject();
    }

    public WikiXMLReader() {

        this.contadorWikipedia = 0;
        this.contador = 1;
        content = new StringBuilder();
        isTitulo = false;
        isTexto = false;
        BasicDBObject ObjDocumento = new BasicDBObject();
    }

    @Override
    public void startDocument() throws SAXException {
        //System.out.println("Se detecto el comeinzo del documento");
    }

    @Override
    public void endDocument() throws SAXException {
        //System.out.println("Fin del documento generado");
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        content = new StringBuilder();
        //System.out.println("comienza tag :" + qName);
        if (qName.equalsIgnoreCase("title")) {
            System.out.println("Iniciando un Titulo");
            titulo = new String();
            isTitulo = true;
        } else if (qName.equalsIgnoreCase("text")) {
            //System.out.println("Finalizando un Texto");
            texto = new StringBuilder();
            isTexto = true;

        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // System.out.println("Cerrando el tag");
        if (isTitulo) {
            //   System.out.println("Finalizando un titulo");
            titulo = content.toString();
            isTitulo = false;
        } else if (isTexto) {

                texto = content;

                // Muestro por pantalla
                //System.out.println("Finalizando un Texto");
                //System.out.println("largo content: " + content.length());
                //System.out.println("Titulo: " + titulo);
                //System.out.println("Texto: " + texto);
                // Comienza el almacenamiento
                person = new BasicDBObject("_id", contador++).append("titulo", titulo).append("texto", texto.toString());
                this.writeResult = documentos.insert(person);
                System.out.println(writeResult);
                //   System.out.println("Insertado");
                // cuenta la cantidad de palabras que existen
                textoEstudio = new StringTokenizer(texto.toString().replaceAll("(?s)", ""));
                textoParser2 = new ArrayList<String>();
                while (textoEstudio.hasMoreElements()) {

                    String normalizado = Normalizer.normalize(textoEstudio.nextElement().toString(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", " ");
                    Object nextElement = normalizado.replaceAll("[^a-zA-Z0-9]+", " ");
                    if (nextElement.toString().equals(" ") == true) {

                    } else {
                        StringTokenizer Modificado = new StringTokenizer(nextElement.toString(), " ");
                        while (Modificado.hasMoreElements()) {
                            Object nextElement1 = Modificado.nextElement().toString();
                            if (nextElement1.toString().equals(" ") == true || nextElement1.toString().equals("") == true || nextElement1.toString().length() >= 12 || nextElement1.toString().length() <= 2 || (NumberUtils.isNumber(nextElement1.toString()) && nextElement1.toString().length() != 4 ) ) {

                            } else {
                                textoParser2.add(nextElement1.toString().toUpperCase());
                                System.out.println("palabra: " + nextElement1.toString() + " size: " + nextElement1.toString().length());

                            }

                        }

                    }

                }

                //textoParser = textoEstudio.toString().replaceAll("[^a-zA-Z0-9]+", "").split(arrayBasura);
                //contadorPalabras = textoEstudio.countTokens();
                contadorPalabras = textoParser2.size();

                System.out.println("cantidad de palabras en parser: " + textoParser2.size());
                System.out.println("cantidad de palabras: " + contadorPalabras);

                // Genero el indice invertido
                wikiIndexCreator = new WikiIndexCreator(contador, titulo, textoParser2, indiceInvertido, contadorPalabras);
                try {
                    wikiIndexCreator.IndiceInvertido();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(WikiXMLReader.class.getName()).log(Level.SEVERE, null, ex);
                }

                //Al finalizar el almacenamiento
                isTexto = false;
            

        }

    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (isTitulo || isTexto) {
            content.append(ch, start, length);
        }

    }

}
