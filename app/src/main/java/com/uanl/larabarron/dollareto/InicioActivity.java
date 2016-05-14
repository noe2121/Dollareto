package com.uanl.larabarron.dollareto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class InicioActivity extends AppCompatActivity {

    private TextView textAnuncio;
    Toast mensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        //Cargar Elementos de Pantalla
        textAnuncio = (TextView)findViewById(R.id.textAnuncio);
        mensaje = Toast.makeText(this, "Creado", Toast.LENGTH_SHORT);

        ConnectivityManager cManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();
        if(nInfo!=null && nInfo.isConnected()) {
            //SI EXISTE ARCHIVO
            if (existeArchivoDeVariables()) {
                if(pasaronQuinceMinutos()) {
                    GuardarArchivoDeVariables();
                }
                else {
                    Intent intent = new Intent(InicioActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            } else {
                GuardarArchivoDeVariables();
            }
        } else {
            textAnuncio.setText(R.string.sin_internet);
        }
    }

    public boolean existeArchivoDeVariables() {
        SharedPreferences archivoDeVariables = getSharedPreferences("archivoDeVariables", Context.MODE_APPEND);
        return (archivoDeVariables.getAll().size() > 0);
    }

    public void GuardarArchivoDeVariables() {
        ParseadorHtmlAsyncTask miParseadorHtmlAsyncTask;
        miParseadorHtmlAsyncTask = new ParseadorHtmlAsyncTask();
        miParseadorHtmlAsyncTask.execute();
    }

    public boolean pasaronQuinceMinutos() {
        SharedPreferences archivoDeVariables = getSharedPreferences("archivoDeVariables", Context.MODE_PRIVATE);
        //POR DEFECTO SI NO ENCUENTRA
        long fechaArchivoDeVariables = archivoDeVariables.getLong("fecha", new Date().getTime());
        long fechaActual = new Date().getTime();
        long deltaMilisegundos = fechaActual - fechaArchivoDeVariables;
        if(deltaMilisegundos > 900000L) {
            mensaje.setText("ACTUALIZADO!");
            mensaje.setDuration(Toast.LENGTH_SHORT);
            mensaje.show();
//            Toast.makeText(this, "ACTUALIZADO!" , Toast.LENGTH_SHORT).show();
        } else {
            Date fechaDeUltimaActualizacion = new Date(fechaArchivoDeVariables);
            SimpleDateFormat formatoFecha = new SimpleDateFormat("HH:mm / dd-MMM-yyyy");
            mensaje.setText("Ultima Actualizacion: " + formatoFecha.format(fechaDeUltimaActualizacion));
            mensaje.setDuration(Toast.LENGTH_SHORT);
            mensaje.show();
//            Toast.makeText(this, "Ultima Actualizacion: " + formatoFecha.format(fechaDeUltimaActualizacion), Toast.LENGTH_SHORT).show();
        }
        return (deltaMilisegundos > 900000L);
    }

    Document documentoHtml;
    Elements nodo;
    private String cadenaParseadaNodoHtml;
    private class ParseadorHtmlAsyncTask extends AsyncTask<Void, Void, Void> {

        //Antes de Ejecutarse...
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //Ejecucion en Segundo Plano...
        @Override
        protected Void doInBackground(Void... params) {
            try {
                documentoHtml = Jsoup.connect("http://eldolarenmexico.com/").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        //Despues de Ejecutarse...
        @Override
        protected void onPostExecute(Void result) {
            try {
                SharedPreferences archivoDeVariables = getSharedPreferences("archivoDeVariables", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorArchivoDeVariables = archivoDeVariables.edit();

                /**** VENTA ****/
                //BANCO AZTECA
                nodo = documentoHtml.getElementsByClass("tdventa").eq(3);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("bancoAztecaVenta", Float.valueOf(cadenaParseadaNodoHtml));

                //HSBC
                nodo = documentoHtml.getElementsByClass("tdventa").eq(6);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("hsbcVenta", Float.valueOf(cadenaParseadaNodoHtml));

                //BANCOMER
                nodo = documentoHtml.getElementsByClass("tdventa").eq(5);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("bancomerVenta", Float.valueOf(cadenaParseadaNodoHtml));

                //BANORTE
                nodo = documentoHtml.getElementsByClass("tdventa").eq(8);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("banorteVenta", Float.valueOf(cadenaParseadaNodoHtml));

                //BANAMEX
                nodo = documentoHtml.getElementsByClass("tdventa").eq(12);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("banamexVenta", Float.valueOf(cadenaParseadaNodoHtml));

                /**** COMPRA ****/
                //BANCO AZTECA
                nodo = documentoHtml.getElementsByClass("tdcompra").eq(3);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("bancoAztecaCompra", Float.valueOf(cadenaParseadaNodoHtml));

                //HSBC
                nodo = documentoHtml.getElementsByClass("tdcompra").eq(6);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("hsbcCompra", Float.valueOf(cadenaParseadaNodoHtml));

                //BANCOMER
                nodo = documentoHtml.getElementsByClass("tdcompra").eq(5);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("bancomerCompra", Float.valueOf(cadenaParseadaNodoHtml));

                //BANORTE
                nodo = documentoHtml.getElementsByClass("tdcompra").eq(8);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("banorteCompra", Float.valueOf(cadenaParseadaNodoHtml));

                //BANAMEX
                nodo = documentoHtml.getElementsByClass("tdcompra").eq(12);
                cadenaParseadaNodoHtml = nodo.text().trim().replaceAll("[^\\d.]", "");
                editorArchivoDeVariables.putFloat("banamexCompra", Float.valueOf(cadenaParseadaNodoHtml));

                /**** FECHA ****/
                Date fecha = new Date();
                long milisegundos = fecha.getTime();
                editorArchivoDeVariables.putLong("fecha", milisegundos);

                /**** FAVORITO ****/
                editorArchivoDeVariables.putInt("bancoFavorito", 1);

                /**** CERRAR ****/
                editorArchivoDeVariables.commit();

                Intent intent = new Intent(InicioActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            } catch (Exception e) {
                textAnuncio.setText("Error de Conexion!");
                mensaje.setText("Error, Conexion no Establacida!");
                mensaje.setDuration(Toast.LENGTH_SHORT);
                mensaje.show();
                e.printStackTrace();
            }
        }
    }

}
