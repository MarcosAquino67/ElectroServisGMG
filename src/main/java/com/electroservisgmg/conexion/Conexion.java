package com.electroservisgmg.conexion;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    // BD de la aplicacion (ver sql/crear_bd_app.sql). No usar electroservis_db:
    // esa es otra base con esquema distinto (tablas en plural) y rompe los inserts.
    private static final String URL_DEFECTO =
            "jdbc:mysql://localhost:3306/electroservis_app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Asuncion";
    private static final String USER_DEFECTO = "root";
    private static final String PASS_DEFECTO = "2307";

    // Valores que realmente se usan: salen del conexion.properties si existe
    // (lo escribe el instalador), y si no, quedan los de arriba.
    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
        Properties p = new Properties();
        File cfg = new File(System.getProperty("user.dir"), "conexion.properties");
        if (cfg.exists()) {
            // Ojo: se lee en UTF-8 (Properties.load sin Reader usa ISO-8859-1 y rompe tildes).
            try (Reader r = new InputStreamReader(new FileInputStream(cfg), StandardCharsets.UTF_8)) {
                p.load(r);
            } catch (Exception e) {
                System.err.println("No se pudo leer conexion.properties: " + e.getMessage());
            }
        }

        String url = valor(p, "url");
        if (url == null) {
            String host = valor(p, "host", "servidor");
            String puerto = valor(p, "puerto", "port");
            String base = valor(p, "base", "database", "bd");
            if (host != null || puerto != null || base != null) {
                url = "jdbc:mysql://" + (host == null ? "localhost" : host)
                        + ":" + (puerto == null ? "3306" : puerto)
                        + "/" + (base == null ? "electroservis_app" : base)
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Asuncion";
            }
        }
        URL = url == null ? URL_DEFECTO : url;

        String usuario = valor(p, "usuario", "user");
        USER = usuario == null ? USER_DEFECTO : usuario;

        String clave = valor(p, "clave", "pass", "password", "contrasena", "contraseña");
        PASS = clave == null ? PASS_DEFECTO : clave;
    }

    private static String valor(Properties p, String... claves) {
        for (String c : claves) {
            String v = p.getProperty(c);
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }

    private static Connection conn = null;

    public static Connection getConexion() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error de conexion a electroservis_app: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    public static Connection nuevaConexion() throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException e) { throw new SQLException(e); }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void cerrarConexion() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexion: " + e.getMessage());
        }
    }
}
