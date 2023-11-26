package com.example.dispositivosmoviles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collection;
import timber.log.Timber;


public class LogeadoUser extends AppCompatActivity {
    public static final String TAG = "YOUR-TAG-NAME";
    private FirebaseAuth mAuth;
    private String Correo;

    private String Banda;

    private String NombreUsuario;
    public ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView lvItems;
    TextView tvInfoUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logeado_user);
        SharedPreferences preferences = getSharedPreferences("QR_PREFERENCES", Context.MODE_PRIVATE);
        Banda = preferences.getString("qr_data", "briandorantes@gmail.com");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        tvInfoUser = findViewById(R.id.tv_info_user);
        lvItems = (ListView) findViewById(R.id.lvItems);
        // Lista de Tareas
        items = new ArrayList<String>();
        itemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        initviews(user);
        receiveItems();
        setupListViewListener();
    }



    //Borra Tarea si se deja Presionado
    private void setupListViewListener() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter,
                                                   View item, int pos, long id) {
                        // Obtiene el elemento que se va a eliminar
                        String elementoEliminado = items.get(pos);

                        // Remueve el elemento de la lista
                        items.remove(pos);

                        // Actualiza la base de datos con el elemento eliminado
                        updatedatabase(elementoEliminado);

                        // Refresca el adaptador
                        itemsAdapter.notifyDataSetChanged();

                        // Muestra un Toast indicando que se eliminó y se votó por el elemento
                        Toast.makeText(LogeadoUser.this, "Has votado por: " + elementoEliminado, Toast.LENGTH_SHORT).show();

                        // Devuelve true para consumir el evento de clic largo (lo marca como manejado)
                        return true;
                    }
                });
    }



 //Revisa Si existe el usuario y muestra el correo
    private void initviews(FirebaseUser user) {
        if (user!= null) {
            Correo = user.getEmail();
            getUserName();
        }
        else{
            tvInfoUser.setText("--");
        }
    }


// cerrar cesion de usuario
    public void clickCerrarSesion2(View view) {
        mAuth.signOut();
        Intent i=new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        Toast.makeText(getApplicationContext(), "Sesion Cerrada Vuelve Pronto!", Toast.LENGTH_SHORT).show();
        finish();

    }


    private void receiveItems(){
        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("token_evento",Banda)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                               items.addAll((Collection<? extends String>) document.get("canciones"));
                                itemsAdapter.notifyDataSetChanged();
                                Timber.tag(TAG).d("Tareas%s", items);
                            }
                        } else {
                            Timber.tag(TAG).d(task.getException(), "Error getting documents: ");
                        }
                    }
                });
    }//fin de items recibidos


    // Actualiza la base de datos con el elemento eliminado
    private void updatedatabase(String elementoEliminado) {
        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("correo", Correo)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Actualiza la base de datos con el elemento eliminado
                                document.getReference().update("votacion", elementoEliminado);

                            }
                        } else {
                            Timber.tag(TAG).e(task.getException(), "Error obteniendo documentos");
                        }
                    }
                });
    }


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

    public void getUserName(){
        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("correo",Correo)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                NombreUsuario = document.getString("nombre");
                                tvInfoUser.setText(NombreUsuario);
                            }
                        } else {
                            Timber.tag(TAG).d(task.getException(), "Error getting documents: ");
                        }
                    }
                });

    }//Obtener datos del usuario actual
}//Fin Activity