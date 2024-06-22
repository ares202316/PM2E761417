package com.example.PM1E761417;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.PM1E761417.Configuracion.SQLiteConexion;
import com.example.PM1E761417.Configuracion.Usuarios;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.util.Base64;
public class MainActivity extends AppCompatActivity {

    Button btnContacSalvados, btnSalvarCont;
    Spinner spinnerPais;
    EditText nombre, telefono, nota;
    String selectedValue;
    static final int peticion_captura_imagen = 101;
    static final int peticion_acceso_camara = 102;
    String currentPhotoPath;
    ImageView Objetoimagen;
    ImageButton btnTomarfotografia;
    String pathfoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objetoimagen = (ImageView) findViewById(R.id.imageView);
        btnTomarfotografia = (ImageButton) findViewById(R.id.imageButton);

        btnTomarfotografia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });


        String[] datos = {"GUATEMALA (+502)", "EL SALVADOR (+503)",
                "HONDURAS (+504)", "NICARAGUA (+505)", "COSTA RICA (+506)"};

        nombre = (EditText) findViewById(R.id.nombre);
        telefono = (EditText) findViewById(R.id.telefono);
        nota = (EditText)findViewById(R.id.nota);
        btnContacSalvados = (Button) findViewById(R.id.btnContactoSalvados);
        btnSalvarCont = (Button) findViewById(R.id.btnSalvarContactos);
        spinnerPais = (Spinner) findViewById(R.id.spinnerPaises);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, datos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPais.setAdapter(adapter);



        btnContacSalvados.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentsalvados = new Intent(getApplicationContext(),ActivityContactos.class);
                startActivity(intentsalvados);
            }
        });

        btnSalvarCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()==true){
                    AddUser();
                    Limpiar();
                }

            }
        });

    }

    private void AddUser() {
        try {
            SQLiteConexion conexion = new SQLiteConexion(this, Usuarios.namedb, null, 1);
            SQLiteDatabase db = conexion.getWritableDatabase();
            byte[] photoData = getPhotoData();

            ContentValues valores = new ContentValues();
            valores.put(Usuarios.paises, spinnerPais.getSelectedItem().toString());
            valores.put(Usuarios.nombres, nombre.getText().toString());
            valores.put(Usuarios.telefonos, telefono.getText().toString());
            valores.put(Usuarios.notas, nota.getText().toString());
            if (photoData != null) {
                valores.put("imagen", photoData); // Add image data
                Toast.makeText(this, "FOTO GUARDARA", Toast.LENGTH_SHORT).show();
            }

            Long result = db.insert(Usuarios.Tabla, null, valores); // Pass null instead of Usuarios.id

            if (result != -1) {
                Toast.makeText(this, "Usuario Ingresado Correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al ingresar el usuario", Toast.LENGTH_SHORT).show();
            }

            db.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            Toast.makeText(this, "Error al ingresar el usuario", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to get photo data from ImageView
    private byte[] getPhotoData() {
        try {
            File photoFile = new File(currentPhotoPath);
            FileInputStream file = new FileInputStream(photoFile);
            byte[] photoData = new byte[(int) photoFile.length()];
            file.read(photoData);
            file.close();
            return photoData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean validate(){
        if(nombre.getText().toString().isEmpty()){
            validate_message("nombre");
            return false;
        }else{
            if(telefono.getText().toString().isEmpty()){
                validate_message("telefeno");
                return false;
            }else{
                if(nota.getText().toString().isEmpty()){
                    validate_message("nota");
                    return false;
                }else{
                    return true;
                }
            }
        }
    }

    private void validate_message(String message){
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Debe escribir un "+message);
        builder.setTitle("Alerta");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog=builder.create();
        dialog.show();
    }


    private void permisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},peticion_acceso_camara);
        }
        else
        {
            dispatchTakePictureIntent();
            //TomarFoto();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == peticion_acceso_camara )
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
            {
                dispatchTakePictureIntent();
                //TomarFoto();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Se necesita el permiso para accder a la camara", Toast.LENGTH_LONG).show();
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode  == peticion_captura_imagen)
        {

            try {
                File foto = new File(currentPhotoPath);
                Objetoimagen.setImageURI(Uri.fromFile(foto));
            }
            catch (Exception ex)
            {
                ex.toString();
            }

        }
    }

    private void Limpiar()
    {
        nombre.setText("");
        telefono.setText("");
        nota.setText("");
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.PM1E761417.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, peticion_captura_imagen);
            }
        }
    }



}
