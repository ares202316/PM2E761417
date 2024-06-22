package com.example.PM1E761417.Configuracion;

public class Usuarios {

    // Nombre de la base de datos
    public static final String namedb = "PM1-E1-0031-0101";

    //Tablas de la base de datos
    public static final String Tabla  = "usuarios";

    // Campos de la tabla
    public static final String id = "id";
    public static final String paises = "pais";
    public static final String nombres = "nombre" ;
    public static final String telefonos = "telefono";
    public static final String notas = "nota";

    public static final String imagen = "imagen";



    // Consultas de Base de datos
    //ddl
    public static final String CreateTableUsuarios = "CREATE TABLE usuarios " +
            "( id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "pais TEXT NOT NULL, " +
            "nombre TEXT NOT NULL, " +
            "telefono INTEGER NOT NULL, " +
            "nota TEXT NOT NULL, " +
            "imagen BLOB)";

    public static final String DropTableUsuarios = "DROP TABLE IF EXISTS usuarios";

    //dml
    // Consulta de la base de datos
    public static final String SelectTableUsuarios = "SELECT id, pais, nombre, telefono, nota, imagen FROM " + Usuarios.Tabla;



}
