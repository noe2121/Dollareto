package com.uanl.larabarron.dollareto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

public class BancosActivity extends AppCompatActivity  implements OnGestureListener {

    private TextView banco1;
    private TextView banco2;
    private TextView banco3;
    private TextView banco4;
    private TextView banco5;
    Toast mensaje;

    private boolean estaEnVenta = false;

    GestureDetector detector = new GestureDetector(this);
    private static final int SWIPE_DISTANCE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bancos);

        //Convertidor
        banco1 = (TextView)findViewById(R.id.banco1);
        banco2 = (TextView)findViewById(R.id.banco2);
        banco3 = (TextView)findViewById(R.id.banco3);
        banco4 = (TextView)findViewById(R.id.banco4);
        banco5 = (TextView)findViewById(R.id.banco5);
        mensaje = Toast.makeText(this, "Creado", Toast.LENGTH_SHORT);

        CambiarAVenta();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoBanco1:
                mensaje.setText("Conversion: Banco Azteca");
                mensaje.setDuration(Toast.LENGTH_SHORT);
                mensaje.show();
                CambiarBancoFavorita(1);
                break;
            case R.id.logoBanco2:
                mensaje.setText("Conversion: HSBC");
                mensaje.setDuration(Toast.LENGTH_SHORT);
                mensaje.show();
                CambiarBancoFavorita(2);
                break;
            case R.id.logoBanco3:
                mensaje.setText("Conversion: Bancomer");
                mensaje.setDuration(Toast.LENGTH_SHORT);
                mensaje.show();
                CambiarBancoFavorita(3);
                break;
            case R.id.logoBanco4:
                mensaje.setText("Conversion: Banorte");
                mensaje.setDuration(Toast.LENGTH_SHORT);
                mensaje.show();
                CambiarBancoFavorita(4);
                break;
            case R.id.logoBanco5:
                mensaje.setText("Conversion: Banamex");
                mensaje.setDuration(Toast.LENGTH_SHORT);
                mensaje.show();
                CambiarBancoFavorita(5);
                break;
            default:
                if(estaEnVenta) {
                    CambiarACompra();
                } else {
                    CambiarAVenta();
                }
                break;
        }
    }

    public void CambiarBancoFavorita(int numeroBanco) {
        SharedPreferences archivoDeVariables = getSharedPreferences("archivoDeVariables", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorArchivoDeVariables = archivoDeVariables.edit();
        editorArchivoDeVariables.putInt("bancoFavorito", numeroBanco);
        editorArchivoDeVariables.commit();
        Intent intent = new Intent(BancosActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    public void CambiarACompra() {
        SharedPreferences archivoDeVariables = getSharedPreferences("archivoDeVariables", Context.MODE_PRIVATE);
        banco1.setText(String.format("$ %.02f", archivoDeVariables.getFloat("bancoAztecaCompra", 10.00f)));
        banco2.setText(String.format("$ %.02f", archivoDeVariables.getFloat("hsbcCompra", 10.00f)));
        banco3.setText(String.format("$ %.02f", archivoDeVariables.getFloat("bancomerCompra", 10.00f)));
        banco4.setText(String.format("$ %.02f", archivoDeVariables.getFloat("banorteCompra", 10.00f)));
        banco5.setText(String.format("$ %.02f", archivoDeVariables.getFloat("banamexCompra", 10.00f)));
        estaEnVenta = false;
        mensaje.setText("Compra");
        mensaje.setDuration(Toast.LENGTH_SHORT);
        mensaje.show();
    }

    public void CambiarAVenta() {
        SharedPreferences archivoDeVariables = getSharedPreferences("archivoDeVariables", Context.MODE_PRIVATE);
        banco1.setText(String.format("$ %.02f", archivoDeVariables.getFloat("bancoAztecaVenta", 10.00f)));
        banco2.setText(String.format("$ %.02f", archivoDeVariables.getFloat("hsbcVenta", 10.00f)));
        banco3.setText(String.format("$ %.02f", archivoDeVariables.getFloat("bancomerVenta", 10.00f)));
        banco4.setText(String.format("$ %.02f", archivoDeVariables.getFloat("banorteVenta", 10.00f)));
        banco5.setText(String.format("$ %.02f", archivoDeVariables.getFloat("banamexVenta", 10.00f)));
        estaEnVenta = true;
        mensaje.setText("Venta");
        mensaje.setDuration(Toast.LENGTH_SHORT);
        mensaje.show();
    }

    public void onSwipeLeft() {
//        Toast.makeText(getApplicationContext(), "Left", Toast.LENGTH_SHORT).show();
    }

    public void onSwipeRight() {
//        Toast.makeText(getApplicationContext(), "Right", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(BancosActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
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
