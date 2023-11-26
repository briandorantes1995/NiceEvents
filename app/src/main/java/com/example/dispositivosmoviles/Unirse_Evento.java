package com.example.dispositivosmoviles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
public class Unirse_Evento extends AppCompatActivity {

    private EditText evento;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_evento);
        evento = findViewById(R.id.codigo);
        android.widget.Button button = findViewById(R.id.unirse);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unirseevento(evento.getText().toString());
            }
        });

    }

    public void onStart() {
        super.onStart();
    }

    public void unirseevento(String evento) {
        saveQRDataToPreferences(evento);
        Intent i = new Intent(getApplicationContext(), LogeadoUser.class);
        startActivity(i);
    }

    private void saveQRDataToPreferences(String data) {
        SharedPreferences preferences = getSharedPreferences("QR_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("qr_data", data);
        editor.apply();
    }

    }
