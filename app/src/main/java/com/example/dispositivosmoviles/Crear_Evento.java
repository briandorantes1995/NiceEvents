package com.example.dispositivosmoviles;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import java.security.SecureRandom;
import java.util.Base64;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import timber.log.Timber;

public class Crear_Evento extends AppCompatActivity {
    public static final String TAG = "YOUR-TAG-NAME";
    private FirebaseAuth mAuth;
    private String Correo;

    private String token_evento;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        initviews(user);
        android.widget.Button button = findViewById(R.id.crear);
        String tokenAleatorio = generarTokenAleatorio();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearEvento(tokenAleatorio);
            }
        });


    }

    private void initviews(FirebaseUser user) {
        if (user!= null) {
            Correo = user.getEmail();
            checarEvento();
        }
    }


    public void checarEvento(){
        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("correo",Correo)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                token_evento = document.getString("token_evento");
                            }
                            assert token_evento != null;
                            if(!token_evento.equals("")){
                                Intent i = new Intent(getApplicationContext(), Logeado.class);
                                startActivity(i);
                            }
                        } else {
                            Timber.tag(TAG).d(task.getException(), "Error getting documents: ");
                        }
                    }
                });

    }


    private void crearEvento(String token) {
        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("correo", Correo)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Actualiza la base de datos con el elemento eliminado
                                document.getReference().update("token_evento", token);
                            }
                            Intent i = new Intent(getApplicationContext(), Logeado.class);
                            startActivity(i);
                        } else {
                            Timber.tag(TAG).e(task.getException(), "Error obteniendo documentos");
                        }
                    }
                });
    }


    private String generarTokenAleatorio() {
        int longitudToken = 8;
        byte[] bytesAleatorios = new byte[longitudToken];
        new SecureRandom().nextBytes(bytesAleatorios);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(bytesAleatorios);
        } else {
            return android.util.Base64.encodeToString(bytesAleatorios, android.util.Base64.DEFAULT);
        }
    }

}