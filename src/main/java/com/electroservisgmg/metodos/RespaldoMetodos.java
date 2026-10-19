package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Sesion;
import com.electroservisgmg.conexion.Conexion;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Respaldo y restauracion de la base de datos, sin depender de mysqldump:
 * el respaldo se arma leyendo las tablas y escribiendo los INSERT en un
 * archivo .sql de texto.
 *
 * El archivo que genera es un volcado de DATOS: primero borra el contenido de
 * cada tabla y despues lo vuelve a cargar. O sea que restaurar deja la base
 * igualita a como estaba cuando se hizo el respaldo (el esquema no se toca).
 *
 * @author Aurelio
 */
public class RespaldoMetodos {

    /** Cantidad de tablas y de filas del ultimo respaldo hecho. */
    public int ultimasTablas;
    public long ultimasFilas;

    // ==================== respaldar ====================

    /** Carpeta donde se guardan los respaldos (se crea si no existe). */
    public static File carpetaRespaldos() {
        File dir = new File("respaldos");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** Nombre de archivo sugerido, con la fecha y la hora adentro. */
    public static String nombreSugerido() {
        return "electroservis_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".sql";
    }

    /** El respaldo mas nuevo de la carpeta de respaldos, o null si no hay ninguno. */
    public static File ultimoRespaldo() {
        File[] archivos = carpetaRespaldos().listFiles((dir, nombre) -> nombre.toLowerCase().endsWith(".sql"));
        if (archivos == null) return null;
        File nuevo = null;
        for (File f : archivos) {
            if (nuevo == null || f.lastModified() > nuevo.lastModified()) nuevo = f;
        }
        return nuevo;
    }

    /** Cuantos respaldos automaticos se conservan; los mas viejos se borran. */
    public static final int RESPALDOS_A_CONSERVAR = 10;

    /**
     * Borra los respaldos mas viejos de la carpeta y deja solo los ultimos
     * {@code mantener} archivos .sql (por fecha de modificacion). Nunca borra el
     * que se acaba de crear, porque es el mas nuevo.
     *
     * @return cuantos archivos borro
     */
    public static int limpiarViejos(int mantener) {
        File[] archivos = carpetaRespaldos().listFiles((dir, nombre) -> nombre.toLowerCase().endsWith(".sql"));
        if (archivos == null || archivos.length <= mantener) return 0;
        java.util.Arrays.sort(archivos, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        int borrados = 0;
        for (int i = Math.max(mantener, 0); i < archivos.length; i++) {
            if (archivos[i].delete()) borrados++;
        }
        return borrados;
    }

    /**
     * Escribe en el archivo indicado el respaldo completo de la base.
     *
     * @return el mismo archivo, ya escrito
     */
    public File respaldar(File destino) throws Exception {
        if (destino == null) throw new Exception("No se eligió dónde guardar el respaldo");
        File padre = destino.getParentFile();
        if (padre != null && !padre.exists()) padre.mkdirs();

        List<String> tablas = new ArrayList<>();
        try (Connection con = Conexion.nuevaConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'")) {
            while (rs.next()) tablas.add(rs.getString(1));
        } catch (SQLException e) {
            throw new Exception("No se pudo leer la lista de tablas: " + e.getMessage(), e);
        }
        if (tablas.isEmpty()) throw new Exception("La base no tiene tablas");
        java.util.Collections.sort(tablas);

        ultimasTablas = tablas.size();
        ultimasFilas = 0;
        SimpleDateFormat fh = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        try (Connection con = Conexion.nuevaConexion();
             PrintWriter out = new PrintWriter(new OutputStreamWriter(
                     new FileOutputStream(destino), StandardCharsets.UTF_8));
             Statement st = con.createStatement()) {

            out.println("-- ============================================================");
            out.println("--  Respaldo de la base de datos - ElectroServis G.M.G.");
            out.println("--  Generado: " + fh.format(new Date()));
            out.println("--");
            out.println("--  Para restaurarlo: Ajustes -> Respaldo de la base -> Restaurar.");
            out.println("--  Ojo: restaurar borra los datos actuales y deja los de este archivo.");
            out.println("-- ============================================================");
            out.println();
            out.println("SET FOREIGN_KEY_CHECKS=0;");

            for (String tabla : tablas) {
                out.println();
                out.println("-- ------------------------------ " + tabla + " ------------------------------");
                out.println("DELETE FROM `" + tabla + "`;");
                try (ResultSet rs = st.executeQuery("SELECT * FROM `" + tabla + "`")) {
                    ResultSetMetaData md = rs.getMetaData();
                    int nc = md.getColumnCount();
                    while (rs.next()) {
                        StringBuilder sb = new StringBuilder("INSERT INTO `").append(tabla).append("` (");
                        for (int i = 1; i <= nc; i++) {
                            if (i > 1) sb.append(", ");
                            sb.append('`').append(md.getColumnName(i)).append('`');
                        }
                        sb.append(") VALUES (");
                        for (int i = 1; i <= nc; i++) {
                            if (i > 1) sb.append(", ");
                            sb.append(valor(rs, md, i));
                        }
                        sb.append(");");
                        out.println(sb.toString());
                        ultimasFilas++;
                    }
                }
            }

            out.println();
            out.println("SET FOREIGN_KEY_CHECKS=1;");
            out.flush();
            if (out.checkError()) throw new Exception("No se pudo escribir el archivo (¿está en uso?)");
        } catch (SQLException e) {
            throw new Exception("No se pudo leer la base: " + e.getMessage(), e);
        }
        return destino;
    }

    /**
     * Respaldo automatico que corre al cerrar el sistema. Solo hace algo si hay
     * un usuario logueado: en ese caso guarda una copia con fecha y hora en la
     * carpeta de respaldos, deja solo los ultimos {@link #RESPALDOS_A_CONSERVAR}
     * y avisa con el resultado. Si no hay sesion no respalda ni muestra nada.
     *
     * @param dueno ventana sobre la que se muestra el aviso (puede ser null)
     * @return true si guardo el respaldo
     */
    public boolean respaldarAlCerrar(java.awt.Window dueno) {
        if (Sesion.getUsuario() == null) return false;
        File destino = new File(carpetaRespaldos(), nombreSugerido());
        try {
            respaldar(destino);
            limpiarViejos(RESPALDOS_A_CONSERVAR);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(dueno,
                    "No se pudo guardar el respaldo automático:\n" + e.getMessage(),
                    "Respaldo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        javax.swing.JOptionPane.showMessageDialog(dueno,
                "Respaldo automático guardado en:\n" + destino.getAbsolutePath()
                + "\n(" + ultimasTablas + " tablas)");
        return true;
    }

    // ==================== restaurar ====================

    /**
     * Restaura un respaldo hecho con {@link #respaldar(File)}.
     * Va todo en una transaccion: si algo sale mal no se cambia nada.
     *
     * @return cuantas sentencias SQL se ejecutaron
     */
    public int restaurar(File archivo) throws Exception {
        if (archivo == null || !archivo.isFile()) throw new Exception("No se encontró el archivo de respaldo");
        String sql = new String(Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8);
        List<String> sentencias = partir(sql);
        if (sentencias.isEmpty()) throw new Exception("El archivo no tiene sentencias SQL");

        int hechas = 0;
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (Statement st = con.createStatement()) {
                for (String s : sentencias) {
                    st.execute(s);
                    hechas++;
                }
            }
            con.commit();
            return hechas;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            throw new Exception("Se canceló la restauración y no se cambió nada:\n" + e.getMessage(), e);
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException ignored) {}
        }
    }

    /**
     * Corta el texto de un archivo .sql en sentencias, respetando las comillas
     * (no corta por los punto y coma que estan adentro de un texto) y salteando
     * los comentarios.
     */
    public static List<String> partir(String sql) {
        List<String> partes = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char cita = 0;          // 0 = afuera de comillas, si no ' " o `
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (cita != 0) {
                sb.append(c);
                if (c == '\\' && cita != '`') {
                    if (i + 1 < sql.length()) { sb.append(sql.charAt(i + 1)); i++; }
                } else if (c == cita) {
                    if (cita == '\'' && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                        sb.append('\''); i++;              // '' es una comilla escapada
                    } else if (cita == '"' && i + 1 < sql.length() && sql.charAt(i + 1) == '"') {
                        sb.append('"'); i++;
                    } else {
                        cita = 0;
                    }
                }
                continue;
            }
            if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                while (i < sql.length() && sql.charAt(i) != '\n') i++;
                sb.append('\n');
                continue;
            }
            if (c == '#') {
                while (i < sql.length() && sql.charAt(i) != '\n') i++;
                sb.append('\n');
                continue;
            }
            if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < sql.length() && !(sql.charAt(i) == '*' && sql.charAt(i + 1) == '/')) i++;
                i++;
                continue;
            }
            if (c == '\'' || c == '"' || c == '`') {
                cita = c;
                sb.append(c);
            } else if (c == ';') {
                String s = sb.toString().trim();
                if (!s.isEmpty()) partes.add(s);
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        String s = sb.toString().trim();
        if (!s.isEmpty()) partes.add(s);
        return partes;
    }

    // ==================== armado de los valores ====================

    /** Texto del valor de una celda, listo para meter en un INSERT. */
    private static String valor(ResultSet rs, ResultSetMetaData md, int i) throws SQLException {
        int tipo = md.getColumnType(i);
        if (esNumero(tipo)) {
            String s = rs.getString(i);            // string para no perder decimales
            return s == null ? "NULL" : s;
        }
        if (tipo == Types.BIT) {
            int v = rs.getInt(i);
            return rs.wasNull() ? "NULL" : String.valueOf(v);
        }
        if (tipo == Types.DATE) {
            java.sql.Date d = rs.getDate(i);
            return d == null ? "NULL" : "'" + d + "'";
        }
        if (tipo == Types.TIME) {
            java.sql.Time t = rs.getTime(i);
            return t == null ? "NULL" : "'" + t + "'";
        }
        if (tipo == Types.TIMESTAMP || tipo == Types.TIMESTAMP_WITH_TIMEZONE) {
            java.sql.Timestamp ts = rs.getTimestamp(i);
            return ts == null ? "NULL" : "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts) + "'";
        }
        String s = rs.getString(i);
        return s == null ? "NULL" : "'" + escapar(s) + "'";
    }

    private static boolean esNumero(int tipo) {
        switch (tipo) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                return true;
            default:
                return false;
        }
    }

    /** Escapa el texto para que entre en un INSERT sin romper nada. */
    private static String escapar(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '\'': sb.append("''"); break;      // '' funciona siempre
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\0': sb.append("\\0"); break;
                case '\u001a': sb.append("\\Z"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
