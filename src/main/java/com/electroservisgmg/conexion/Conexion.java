package com.electroservisgmg.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    // BD de la aplicacion (ver sql/crear_bd_app.sql). No usar electroservis_db:
    // esa es otra base con esquema distinto (tablas en plural) y rompe los inserts.
    private static final String URL = "jdbc:mysql://localhost:3306/electroservis_app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Asuncion";
    private static final String USER = "root";
    private static final String PASS = "2307";

    private static Connection conn = null;

    public static Connection getConexion() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error de conexion a electroservis_db: " + e.getMessage());
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
