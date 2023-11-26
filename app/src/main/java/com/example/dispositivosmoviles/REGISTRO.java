package com.example.dispositivosmoviles;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class REGISTRO extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private EditText nombre;
    private EditText correo;
    private EditText contrasena;
    private EditText contrasenaconfirmacion;

    private ArrayList<String> tipos;

    private ArrayAdapter<String> tipoAdapter;

    private String seleccion;

    Spinner tipo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mAuth = FirebaseAuth.getInstance();
        nombre = findViewById(R.id.nombre);
        correo = findViewById(R.id.Correo);
        contrasena = findViewById(R.id.Contrasena);
        contrasenaconfirmacion = findViewById(R.id.ContrasenaConfirmacion);
        tipo = findViewById(R.id.tipousuario);
        tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {
                seleccion = tipo.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        // Inicializa base de datos para guardas info del usuario

        tipos = new ArrayList<String>();
        tipos.add("Tipo de registro");
        tipos.add("Grupo");
        tipos.add("Usuario");
        tipoAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, tipos);
        tipo.setAdapter(tipoAdapter);
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    //IF CONTRASENAS COINCIDEN
    public void registrarUsuario(View view){
        if (contrasena.getText().toString().equals(contrasenaconfirmacion.getText().toString())) {
            mAuth.createUserWithEmailAndPassword(correo.getText().toString(), contrasena.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //IF DE LA TAREA (SI NO TIENE INTERNET SE VA AL ELSE
                            if (task.isSuccessful()) {
                                //Registro exitoso se recopilaron los datos y se muestra mensaje
                                Toast.makeText(getApplicationContext(), "Usuario Creado exitosamente", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                //Se devuelve a la pantalla principal
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                //Envia Mail de autenticacion
                                assert user != null;
                                user.sendEmailVerification();
                                Toast.makeText(getApplicationContext(), "E-mail de Verificacion enviado", Toast.LENGTH_SHORT).show();

                                //Agrega datos a base de datos

                                Map<String, Object> data = new HashMap<>();
                                if(Objects.equals(seleccion, "Usuario")){
                                    data.put("nombre", nombre.getText().toString());
                                    data.put("correo", correo.getText().toString());
                                    data.put("banda", false);
                                    data.put("votacion", "");
                                    data.put("admin", false);
                                }else{
                                    data.put("nombre", nombre.getText().toString());
                                    data.put("correo", correo.getText().toString());
                                    data.put("banda", true);
                                    data.put("canciones", new ArrayList<String>());
                                    data.put("evento", false);
                                    data.put("token_evento", "");
                                    data.put("admin", false);
                                }

                                FirebaseFirestore.getInstance().collection("Users")
                                        .add(data)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Timber.tag(TAG).d("DocumentSnapshot written with ID: %s", documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Timber.tag(TAG).w(e, "Error adding document");
                                            }
                                        });//fin de agregar database

                                startActivity(i);


                            }else{
                                Toast.makeText(getApplicationContext(), "Autenticacion Fallida",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

            //ELSE SI NO COINCIDEN
        }else{
            Toast.makeText(this,"Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        }
    }




}//fin activity



