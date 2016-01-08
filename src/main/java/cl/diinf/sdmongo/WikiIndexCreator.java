/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.diinf.sdmongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author ismael
 */
public class WikiIndexCreator {

    int ultimoElemento;
    int idDocumento;
    int cantidadPalabras;
    String titulo;
    ArrayList<String> texto;
    ArrayList<String> textoLimpio;
    // Para acceso a la base de datos
    DBCollection indiceInvertido;
    DBObject person;

    // arreglos para almacenar la información
    ArrayList<Integer> cantidadRepeticiones;
    ArrayList<String> palabrasEstudio;
    String palabraActual;

    boolean agregada;

    // stopwords
    ArrayList<String> stopwords;

    public WikiIndexCreator(int idDocumento, String titulo, ArrayList<String> texto, DBCollection indiceInvertido, int cantidadDePalabras) {
        this.idDocumento = idDocumento;
        this.titulo = titulo;
        this.texto = texto;
        this.indiceInvertido = indiceInvertido;
        this.ultimoElemento = 0;
        this.cantidadPalabras = cantidadDePalabras;
        this.cantidadRepeticiones = new ArrayList<Integer>();
        this.palabrasEstudio = new ArrayList<String>();
    }

    // construye el indice invertido
    public void IndiceInvertido() throws FileNotFoundException {
        System.out.println("Comienza la eliminacion de stopwords - palabras actuales : " + this.cantidadPalabras);

        loadStopWords();
        DeleteStopWords();

        cantidadPalabras = this.textoLimpio.size();

        System.out.println("Stopwords eliminadas - nuevo tamaño : " + cantidadPalabras);

        for (int i = 0; i < cantidadPalabras; i++) {
            //palabraActual = texto[i];
            //System.out.println("palabra : " + (i + 1) + " de " + cantidadPalabras);
            palabraActual = this.textoLimpio.get(i);
            //System.out.println("actual: " + palabraActual);
            if (palabrasEstudio.size() == 0) {
                System.out.println("Vacio");
                palabrasEstudio.add(palabraActual);
                cantidadRepeticiones.add(1);

            } else {
                // busco la palabra en el indice actual;
                agregada = false;

                for (int j = 0; j < palabrasEstudio.size(); j++) {
                    if (palabraActual.equals(palabrasEstudio.get(j))) {
                        cantidadRepeticiones.set(j, (cantidadRepeticiones.get(j) + 1));
                        agregada = true;
                        break;
                    }
                }

                if (agregada == false) {
                    palabrasEstudio.add(palabraActual);
                    cantidadRepeticiones.add(1);
                }

            }
        }

        // Indice ya construido a nivel local
        System.out.println("tamaño indice invertido local: " + palabrasEstudio.size());

        ultimoElemento = (int) indiceInvertido.count();
        System.out.println("index: "+ultimoElemento);
        // consulto si la palabra esta en la base de datos
        for (int i = 0; i < palabrasEstudio.size(); i++) {
            //BasicDBObject palabraIngresar = new BasicDBObject("_id", ultimoElemento + i).append("palabra", palabrasEstudio.get(i));
            BasicDBObject palabraIngresar = new BasicDBObject("palabra", palabrasEstudio.get(i));
            DBCursor indexMongo = indiceInvertido.find(palabraIngresar);

            // Verificamos si la palabra no existe en la base de datos
            if (indexMongo.count() == 0) {
                BasicDBList lista = new BasicDBList();
                lista.add(new BasicDBObject("id_documento", idDocumento).append("frecuencia", cantidadRepeticiones.get(i)));
                palabraIngresar.put("documento", lista);
                indiceInvertido.insert(palabraIngresar);
            } else {
                System.out.println("Reemplazando");
                BasicDBObject consultaAgregar = new BasicDBObject();
                BasicDBList listaModificar = new BasicDBList();
                listaModificar.add(new BasicDBObject("id_documento", idDocumento).append("frecuencia", cantidadRepeticiones.get(i)));
                consultaAgregar.put("$push", new BasicDBObject("documento", listaModificar));
                indiceInvertido.update(palabraIngresar, consultaAgregar);
            }

        }
        // si la palabra esta en la base de datos, aplico update
        // si la palabra no existe la ingreso en la ultima posición
        // obtengo la ultima posicion del indice invertido
        //ultimoElemento = (int) indiceInvertido.count();
    }

    public void loadStopWords() throws FileNotFoundException {

        BufferedReader archivoStopwords = new BufferedReader(new FileReader("stopwords.txt"));
        String line;
        this.stopwords = new ArrayList<String>();
        try {

            while ((line = archivoStopwords.readLine()) != null) {
                this.stopwords.add(line.toUpperCase());
                //System.out.println("Stop: " + line);
            }

            archivoStopwords.close();

        } catch (Exception e) {
        }

    }

    public void DeleteStopWords() {

        System.out.println("Cantidad de stopWords " + this.stopwords.size());
        System.out.println("Palabras a estudiar: " + this.texto.size());

        this.textoLimpio = new ArrayList<String>();
        int contadorStop = 0;

        System.out.println("Temaño inicial " + textoLimpio.size());

        for (int i = 0; i < this.texto.size(); i++) {
            //System.out.println("Index de texto: " + i);

            for (int j = 0; j < this.stopwords.size(); j++) {
                if (contadorStop == 0) {
                    if (this.texto.get(i).equals(this.stopwords.get(j))) {
                        contadorStop++;
                    }
                }
            }

            if (contadorStop == 0) {
                this.textoLimpio.add(this.texto.get(i));
            } else {
                System.out.println("palabra eliminada: " + (this.texto.get(i)));
                contadorStop = 0;
            }

            //System.out.println("Cantidad de palabras limpias: " + this.textoLimpio.size());
        }

        System.out.println("Tamaño texto limpio: " + this.textoLimpio.size());
        System.out.println("tamaño texto antiguo: " + this.texto.size());
        this.texto = null;

    }

}
