package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Usuario;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioMetodos {
    /**
     * Valida el usuario y la clave, y devuelve el usuario (o null si no coincide).
     * La clave guardada puede estar hasheada o en texto plano (claves viejas):
     * si esta en texto plano, se convierte a hash sola en este mismo inicio.
     */
    public Usuario validarLogin(String usuario, String clave) {
        String sql = "SELECT * FROM usuario WHERE usuario=? AND estado='ACTIVO'";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String guardada = rs.getString("clave");
                    if (!Seguridad.verificar(clave, guardada)) return null;
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("idUsuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setClave(guardada);
                    u.setRol(rs.getString("rol"));
                    u.setEstado(rs.getString("estado"));
                    if (!Seguridad.esHash(guardada)) {
                        // clave vieja en texto plano: la pasamos a hash sin que se note
                        String nuevo = Seguridad.hash(clave);
                        try (PreparedStatement up = con.prepareStatement(
                                "UPDATE usuario SET clave=? WHERE idUsuario=?")) {
                            up.setString(1, nuevo);
                            up.setInt(2, u.getIdUsuario());
                            up.executeUpdate();
                            u.setClave(nuevo);
                        } catch (SQLException ignored) {}
                    }
                    return u;
                }
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return null;
    }
    public boolean insertar(Usuario u) {
        String sql = "INSERT INTO usuario(nombre, usuario, clave, rol, estado) VALUES(?,?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, claveParaGuardar(u.getClave()));
            ps.setString(4, u.getRol());
            ps.setString(5, u.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }

    /** Si la clave viene en texto plano se guarda hasheada; si ya es un hash, se deja igual. */
    private static String claveParaGuardar(String clave) {
        return Seguridad.esHash(clave) ? clave : Seguridad.hash(clave);
    }
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY idUsuario";
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("idUsuario"));
                u.setNombre(rs.getString("nombre"));
                u.setUsuario(rs.getString("usuario"));
                u.setClave(rs.getString("clave"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getString("estado"));
                lista.add(u);
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }
    public boolean modificar(Usuario u) {
        String sql = "UPDATE usuario SET nombre=?, usuario=?, clave=?, rol=?, estado=? WHERE idUsuario=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, claveParaGuardar(u.getClave()));
            ps.setString(4, u.getRol());
            ps.setString(5, u.getEstado());
            ps.setInt(6, u.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuario WHERE idUsuario=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
}
