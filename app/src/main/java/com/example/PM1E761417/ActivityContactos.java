package com.example.PM1E761417;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.PM1E761417.Configuracion.SQLiteConexion;
import com.example.PM1E761417.Configuracion.Usuarios;
import com.example.PM1E761417.Models.Contactos;

import java.util.ArrayList;

public class ActivityContactos extends AppCompatActivity {

    SQLiteConexion conexion;
    ListView listView;
    ArrayList<Contactos> listUser;
    EditText id;
    ArrayList<String> ArregloUser;

    Button btnAtras, btnimg, btneliminar, btnactualizar, btncompartir;
    private String telefono;
    private static final int REQUEST_CALL = 1;
    private boolean Selected = false;
    private Contactos selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);

        // Establecer la conexión a la base de datos
        conexion = new SQLiteConexion(this, Usuarios.namedb, null, 1);
        listView = findViewById(R.id.listUsuario);
        id = findViewById(R.id.txtcid);

        btnAtras = findViewById(R.id.btnAtras);
        btneliminar = findViewById(R.id.btneliminar);
        btnimg = findViewById(R.id.btnVerimg);
        btnactualizar = findViewById(R.id.btnActualizar);
        btncompartir = findViewById(R.id.btnCompartir);

        // Listener para el botón Atras
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Listener para el botón Actualizar
        btnactualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetContactos(); // Actualizar lista de contactos
            }
        });

        // Listener para el botón Ver Imagen
        btnimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Selected) {
                    // Obtener el ID del contacto seleccionado
                    int selectedId = selectedContact.getId();

                    // Obtener la imagen codificada del contacto seleccionado
                    String encodedImage = selectedContact.getImagen();

                    // Iniciar ActivityViewImage con la imagen
                    Intent intent = new Intent(ActivityContactos.this, ActivityViewImage.class);
                    intent.putExtra("image", encodedImage);
                    startActivity(intent);
                } else {
                    Toast.makeText(ActivityContactos.this, "Seleccione un contacto", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Obtener los contactos y llenar la lista al iniciar la actividad
        GetContactos();

        // Configurar el adaptador para la lista de contactos
        ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, ArregloUser);
        listView.setAdapter(adp);

        // Configurar modo de selección de lista y manejar eventos de clic
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                telefono = "" + listUser.get(position).getTelefono();
                Selected = true;
                selectedContact = listUser.get(position);

                // Listener para el botón Eliminar
                btneliminar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        eliminarContacto(position);
                    }
                });

                // Listener para el botón Compartir
                btncompartir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        compartirContacto(position);
                    }
                });

                // Mostrar diálogo de confirmación para realizar una llamada
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityContactos.this);
                builder.setMessage("¿Quiere realizar una llamada?");
                builder.setTitle("LLAMADA");

                builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mostrarnumero();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ActivityContactos.this, "Llamada no realizada", Toast.LENGTH_LONG).show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Configurar filtro de texto en el EditText
        id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adp.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Método para obtener los contactos desde la base de datos
    private void GetContactos() {
        SQLiteDatabase db = conexion.getReadableDatabase();
        Contactos contact;
        listUser = new ArrayList<>();

        Cursor cursor = db.rawQuery(Usuarios.SelectTableUsuarios, null);
        while (cursor.moveToNext()) {
            contact = new Contactos();
            contact.setId(cursor.getInt(0));
            contact.setPais(cursor.getString(1));
            contact.setNombre(cursor.getString(2));
            contact.setTelefono(cursor.getInt(3));
            contact.setNota(cursor.getString(4));
            byte[] imageBytes = cursor.getBlob(5); // Obtener el BLOB
            if (imageBytes != null) {
                // Codificar la imagen a Base64
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                contact.setImagen(encodedImage); // Establecer la imagen codificada
            }
            listUser.add(contact);
        }

        cursor.close();
        FillList();
    }

    // Método para llenar la lista de contactos en formato legible
    private void FillList() {
        ArregloUser = new ArrayList<>();

        for (int i = 0; i < listUser.size(); i++) {
            ArregloUser.add(listUser.get(i).getId() + " | " + listUser.get(i).getNombre() + " | " + listUser.get(i).getTelefono());
        }
    }

    // Método para eliminar un contacto
    private void eliminarContacto(int position) {
        SQLiteDatabase db = conexion.getWritableDatabase();
        String sql = "DELETE FROM usuarios WHERE id=" + listUser.get(position).getId();
        db.execSQL(sql);
        Intent i = new Intent(ActivityContactos.this, ActivityContactos.class);
        startActivity(i);
        finish();
    }

    // Método para compartir la información de un contacto
    private void compartirContacto(int position) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, listUser.get(position).getId() + ": " + listUser.get(position).getTelefono());
        share.putExtra(Intent.EXTRA_TEXT, listUser.get(position).getTelefono());
        startActivity(Intent.createChooser(share, "COMPARTIR"));
    }

    // Método para mostrar el número de teléfono seleccionado en una llamada
    private void mostrarnumero() {
        String numero = telefono;
        if (Selected) {
            if (ContextCompat.checkSelfPermission(ActivityContactos.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ActivityContactos.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String n = "tel:" + numero;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(n)));
            }
        } else {
            Toast.makeText(ActivityContactos.this, "Seleccione un contacto", Toast.LENGTH_LONG).show();
        }
    }

    // Manejar el resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarnumero();
            } else {
                Toast.makeText(this, "No se concedió el permiso para realizar llamadas", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
