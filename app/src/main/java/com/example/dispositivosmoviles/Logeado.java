package com.example.dispositivosmoviles;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import timber.log.Timber;


public class Logeado extends AppCompatActivity {
    public static final String TAG = "YOUR-TAG-NAME";
    private FirebaseAuth mAuth;
    private Configuration config = new Configuration();
    private ArrayList<String> Users;
    private String Tareas;

    private String Correo;

    private String NombreUsuario;

    private String compartirToken;
    private ArrayAdapter<String> itemsAdapter;

    private ListView lvUsers;
    TextView tvInfoUser;

    TextView Token;

    TextView Cancion;
    EditText sendText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logeado);
        sendText = findViewById(R.id.etNewItem2);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        tvInfoUser = findViewById(R.id.tv_info_user);
        Token = findViewById(R.id.token);
        Cancion = findViewById(R.id.cancionmasvotada);
        lvUsers = (ListView) findViewById(R.id.users);
        Users = new ArrayList<String>();
        itemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Users);
        lvUsers.setAdapter(itemsAdapter);
        initviews(user);
        getUsers();
        obtenerCancionMasVotada();
    }//fin onCreate


    //Verifica si existe el usuario y muestra el mail
    private void initviews(FirebaseUser user) {

        if (user!= null) {
            Correo = user.getEmail();
            getUserName();
        }
        else{
            tvInfoUser.setText("--");
        }
    }

//destruye la sesion
    public void clickCerrarSesion2(View view) {
        mAuth.signOut();
        Intent i=new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        Toast.makeText(getApplicationContext(), "Sesion Cerrada Vuelve Pronto!", Toast.LENGTH_SHORT).show();
        finish();
    }//fin cerrar sesion


    //Inicia el chat
    public void chat(View view) {
        Intent i = new Intent(getApplicationContext(), Chat.class);
        startActivity(i);
    }//fin chat


    //Inicia la videollmada
    public void Videollamada(View view) {
        Intent i = new Intent(getApplicationContext(), Videollamada.class);
        startActivity(i);
    }//fin videollamada

//agrega cancion
    public void asignartarea(View view) {
      FirebaseFirestore.getInstance().collection("Users").whereEqualTo("correo",Correo)
              .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Tareas = sendText.getText().toString();
                        document.getReference().update("canciones", FieldValue.arrayUnion(Tareas));
                        Timber.tag(TAG).d("Error updating: %s", Tareas);
                    }
                } else {
                    Timber.tag(TAG).d(task.getException(), "Error getting documents: ");
                }
            }

        });//fin oncomplete
        itemsAdapter.notifyDataSetChanged();
    }//fin agregar cancion



    //muestra canciones
    public void getUsers(){
        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("correo",Correo)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Users.addAll((Collection<? extends String>) Objects.requireNonNull(document.get("canciones")));
                                itemsAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Timber.tag(TAG).d(task.getException(), "Error getting documents: ");
                        }
                    }
                });

    }//fin getUsers

//fin getUsers

    public void getUserName(){
        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("correo",Correo)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                NombreUsuario = document.getString("nombre");
                                compartirToken = document.getString("token_evento");
                                tvInfoUser.setText(NombreUsuario);
                                Token.setText("Token: " + compartirToken);
                            }
                        } else {
                            Timber.tag(TAG).d(task.getException(), "Error getting documents: ");
                        }
                    }
                });

    }//Obtener datos del usuario actual

    private void obtenerCancionMasVotada() {
        FirebaseFirestore.getInstance().collection("Users")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Integer> conteoVotos = new HashMap<>();

                            // Itera sobre todos los usuarios
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Obtiene la cadena de votación del usuario
                                String votacion = document.getString("votacion");

                                // Maneja el caso en el que votacion es null
                                if (votacion != null) {
                                    // Descompone la cadena de votación en elementos individuales
                                    String[] votos = votacion.split(","); // Ajusta el delimitador según tu estructura

                                    // Incrementa el contador para cada voto en la cadena
                                    for (String voto : votos) {
                                        if (!voto.isEmpty()) {  // Asegúrate de que no estás contando elementos vacíos
                                            if (conteoVotos.containsKey(voto)) {
                                                conteoVotos.put(voto, conteoVotos.get(voto) + 1);
                                            } else {
                                                conteoVotos.put(voto, 1);
                                            }
                                        }
                                    }
                                } else {
                                    Timber.tag(TAG).d("La votación es nula para este usuario. Ignorando...");
                                }
                            }

                            String cancionMasVotada = encontrarMaximo(conteoVotos);
                            Cancion.setText("Mas Votada: " + cancionMasVotada);
                            Timber.tag(TAG).d("La canción más votada es: %s", cancionMasVotada);
                        } else {
                            Timber.tag(TAG).e(task.getException(), "Error obteniendo documentos");
                        }
                    }
                });
    }


    // Método auxiliar para encontrar la clave con el mayor valor en un mapa
    private String encontrarMaximo(Map<String, Integer> mapa) {
        String maximaClave = null;
        int maximoValor = Integer.MIN_VALUE;

        for (Map.Entry<String, Integer> entry : mapa.entrySet()) {
            if (entry.getValue() > maximoValor) {
                maximoValor = entry.getValue();
                maximaClave = entry.getKey();
            }
        }

        return maximaClave;
    }


}//fin activity