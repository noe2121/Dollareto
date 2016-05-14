package com.uanl.larabarron.dollareto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnGestureListener{

    GestureDetector detector = new GestureDetector(this);
    private static final int SWIPE_DISTANCE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private TextView textViewPrecioDolar;
    private TextView textConversionADolares;
    private TextView textEtiquetaPesos;
    private TextView textEtiquetaDolares;
    private TextView textBancoFavorito;
    private EditText editTextPesos;
    private Button botonActualizar;
    Toast mensaje;

    private Float precioDolarActual;
    private Float pesosAConvertir;
    private Float conversion;

    private boolean estaEnVenta = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Cargar Elementos de Pantalla
        textViewPrecioDolar = (TextView)findViewById(R.id.textPrecioDolar);
        editTextPesos  = (EditText)findViewById(R.id.editTextPesos);
        textConversionADolares = (TextView)findViewById(R.id.textConversionADolares);
        textEtiquetaPesos = (TextView)findViewById(R.id.textEtiquetaPesos);
        textEtiquetaDolares = (TextView)findViewById(R.id.textEtiquetaDolares);
        textBancoFavorito = (TextView)findViewById(R.id.textBancoFavorito);
        botonActualizar = (Button)findViewById(R.id.botonActualizar);
        mensaje = Toast.makeText(this, "Creado", Toast.LENGTH_SHORT);

        //CargarVenta
        CambiarAVenta();

        textViewPrecioDolar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (estaEnVenta) {
                    CambiarACompra();
                    editTextPesos.setText("");
                    textConversionADolares.setText(R.string.conversion_default);
                    textEtiquetaPesos.setText(R.string.etiqueta_dolares);
                    textEtiquetaDolares.setText(R.string.etiqueta_pesos_mexicanos);
                } else {
                    CambiarAVenta();
                    editTextPesos.setText("");
                    textConversionADolares.setText(R.string.conversion_default);
                    textEtiquetaPesos.setText(R.string.etiqueta_pesos_mexicanos);
                    textEtiquetaDolares.setText(R.string.etiqueta_dolares);
                }
            }
        });

    botonActualizar.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConnectivityManager cManager = (ConnectivityManager) getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cManager.getActiveNetworkInfo();
            if(nInfo!=null && nInfo.isConnected()) {
                //SI EXISTE ARCHIVO
                if(pasaronQuinceMinutos()) {
                    GuardarArchivoDeVariables();
                }
            } else {
                mensaje.setText("No hay Internet");
                mensaje.setDuration(Toast.LENGTH_SHORT);
                mensaje.show();
//                Toast.makeText(MainActivity.this, "No hay Internet", Toast.LENGTH_SHORT).show();
            }
        }
    });

        editTextPesos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if(estaEnVenta) {
                        pesosAConvertir = Float.valueOf(s.toString());
                        conversion = pesosAConvertir / precioDolarActual;
                        textConversionADolares.setText(String.format("%.02f", conversion));
                    } else {
                        pesosAConvertir = Float.valueOf(s.toString());
                        conversion = pesosAConvertir * precioDolarActual;
                        textConversionADolares.setText(String.format("%.02f", conversion));
                    }

                } catch (NumberFormatException e) {
                    textConversionADolares.setText(R.string.conversion_default);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public void GuardarArchivoDeVariables() {
        ParseadorHtmlAsyncTask miParseadorHtmlAsyncTask;
        miParseadorHtmlAsyncTask = new ParseadorHtmlAsyncTask();
        miParseadorHtmlAsyncTask.execute();
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

                /**** CERRAR ****/
                editorArchivoDeVariables.commit();

                CambiarAVenta();

            } catch (Exception e) {
                mensaje.setText("Error, Conexion no Establacida!");
                mensaje.setDuration(Toast.LENGTH_SHORT);
                mensaje.show();
//                Toast.makeText(MainActivity.this, "Error, Conexion no Establacida!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
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

    public void CambiarACompra() {
        SharedPreferences archivoDeVariables = getSharedPreferences("archivoDeVariables", Context.MODE_PRIVATE);
        int bancoFavorito = archivoDeVariables.getInt("bancoFavorito", 1);
        switch (bancoFavorito) {
            case 1:
                precioDolarActual = archivoDeVariables.getFloat("bancoAztecaCompra", 10.00f);
                textBancoFavorito.setText("Banco Azteca - COMPRA");
                break;
            case 2:
                precioDolarActual = archivoDeVariables.getFloat("hsbcCompra", 10.00f);
                textBancoFavorito.setText("HSBC - COMPRA");
                break;
            case 3:
                precioDolarActual = archivoDeVariables.getFloat("bancomerCompra", 10.00f);
                textBancoFavorito.setText("Bancomer - COMPRA");
                break;
            case 4:
                precioDolarActual = archivoDeVariables.getFloat("banorteCompra", 10.00f);
                textBancoFavorito.setText("Banorte - COMPRA");
                break;
            case 5:
                precioDolarActual = archivoDeVariables.getFloat("banamexCompra", 10.00f);
                textBancoFavorito.setText("Banamex - COMPRA");
                break;
        }
        textViewPrecioDolar.setText(String.format("$ %.02f", precioDolarActual));
        estaEnVenta = false;
        mensaje.setText("Compra");
        mensaje.setDuration(Toast.LENGTH_SHORT);
        mensaje.show();
    }

    public void CambiarAVenta() {
        SharedPreferences archivoDeVariables = getSharedPreferences("archivoDeVariables", Context.MODE_PRIVATE);
        int bancoFavorito = archivoDeVariables.getInt("bancoFavorito", 1);
        switch (bancoFavorito) {
            case 1:
                precioDolarActual = archivoDeVariables.getFloat("bancoAztecaVenta", 10.00f);
                textBancoFavorito.setText("Banco Azteca - VENTA");
                break;
            case 2:
                precioDolarActual = archivoDeVariables.getFloat("hsbcVenta", 10.00f);
                textBancoFavorito.setText("HSBC - VENTA");
                break;
            case 3:
                precioDolarActual = archivoDeVariables.getFloat("bancomerVenta", 10.00f);
                textBancoFavorito.setText("Bancomer - VENTA");
                break;
            case 4:
                precioDolarActual = archivoDeVariables.getFloat("banorteVenta", 10.00f);
                textBancoFavorito.setText("Banorte - VENTA");
                break;
            case 5:
                precioDolarActual = archivoDeVariables.getFloat("banamexVenta", 10.00f);
                textBancoFavorito.setText("Banamex - VENTA");
                break;
        }
        textViewPrecioDolar.setText(String.format("$ %.02f", precioDolarActual));
        estaEnVenta = true;
        mensaje.setText("Venta");
        mensaje.setDuration(Toast.LENGTH_SHORT);
        mensaje.show();
    }

    public void onSwipeLeft() {
//        Toast.makeText(getApplicationContext(), "Left", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, BancosActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    public void onSwipeRight() {
//        Toast.makeText(getApplicationContext(), "Right", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float distanceX = e2.getX() - e1.getX();
        float distanceY = e2.getY() - e1.getY();
        if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (distanceX > 0)
                onSwipeRight();
            else
                onSwipeLeft();
            return true;
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
//        Toast.makeText(getApplicationContext(), "Down", Toast.LENGTH_SHORT).show();
        return false;
    }
    @Override
    public void onShowPress(MotionEvent e) {
//        Toast.makeText(getApplicationContext(), "Show Press", Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
//        Toast.makeText(getApplicationContext(), "Single Tap Up", Toast.LENGTH_SHORT).show();
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        Toast.makeText(getApplicationContext(), "On Scroll", Toast.LENGTH_SHORT).show();
        return false;
    }
    @Override
    public void onLongPress(MotionEvent e) {
//        Toast.makeText(getApplicationContext(), "Long Press", Toast.LENGTH_SHORT).show();
    }
}
