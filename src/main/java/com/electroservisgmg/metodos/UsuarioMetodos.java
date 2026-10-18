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
    /**
     * Lee los campos del formulario, valida, decide si es alta o modificacion,
     * guarda y muestra el mensaje. Devuelve true si guardo, asi el formulario
     * sabe si refrescar la tabla.
     */
    public boolean guardar(javax.swing.JTextField txtId, javax.swing.JTextField txtNombre,
                           javax.swing.JTextField txtUsuario, javax.swing.JPasswordField txtClave,
                           javax.swing.JComboBox<String> cboRol, javax.swing.JComboBox<String> cboEstado) {
        String nombre = txtNombre.getText().trim();
        String user = txtUsuario.getText().trim();
        String pass = new String(txtClave.getPassword());
        if (nombre.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Complete nombre, usuario y clave");
            return false;
        }
        if (pass.length() < 4) {
            javax.swing.JOptionPane.showMessageDialog(null, "La clave debe tener al menos 4 caracteres");
            return false;
        }
        String rol = cboRol.getSelectedItem() == null ? "USUARIO" : cboRol.getSelectedItem().toString();
        int id = 0;
        try {
            id = txtId.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException ignored) {}
        // si te estas editando a vos mismo no te podes sacar el rol de administrador:
        // despues no podrias volver a entrar a Usuarios ni a Ajustes
        Usuario sesion = com.electroservisgmg.clases.Sesion.getUsuario();
        if (id > 0 && sesion != null && id == sesion.getIdUsuario()
                && !"ADMINISTRADOR".equalsIgnoreCase(rol)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No te podés quitar a vos mismo el rol de ADMINISTRADOR:\n"
                    + "después no podrías volver a entrar a Usuarios ni a Ajustes.");
            return false;
        }
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setUsuario(user);
        u.setClave(pass);
        u.setRol(rol);
        u.setEstado(cboEstado.getSelectedItem() == null ? "ACTIVO" : cboEstado.getSelectedItem().toString());
        boolean ok;
        if (id > 0) {
            u.setIdUsuario(id);
            ok = modificar(u);
        } else {
            ok = insertar(u);
        }
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Guardado correctamente" : "Error al guardar");
        return ok;
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
    /**
     * Llena la tabla con los usuarios, opcionalmente filtrados por nombre o
     * nombre de usuario.
     */
    public void mostrar(javax.swing.JTable tabla, String filtro) {
        String f = filtro == null ? "" : filtro.toLowerCase();
        List<Usuario> fil = new ArrayList<>();
        for (Usuario u : listar()) {
            if (f.isEmpty() || u.getNombre().toLowerCase().contains(f)
                    || u.getUsuario().toLowerCase().contains(f)) fil.add(u);
        }
        String[] cols = {"ID", "Nombre", "Usuario", "Rol", "Estado"};
        Object[][] data = new Object[fil.size()][5];
        for (int i = 0; i < fil.size(); i++) {
            Usuario u = fil.get(i);
            data[i] = new Object[]{u.getIdUsuario(), u.getNombre(), u.getUsuario(), u.getRol(), u.getEstado()};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
    }

    /**
     * Elimina el usuario seleccionado (el id viene del campo txtId) y muestra el
     * mensaje. Devuelve true si elimino.
     */
    public boolean eliminar(javax.swing.JTextField txtId) {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un usuario de la tabla");
            return false;
        }
        if (id == 1) {
            javax.swing.JOptionPane.showMessageDialog(null, "No se puede eliminar el usuario principal");
            return false;
        }
        // no podes borrarte a vos mismo: seguirias en sesion como un usuario que ya no existe
        Usuario sesion = com.electroservisgmg.clases.Sesion.getUsuario();
        if (sesion != null && id == sesion.getIdUsuario()) {
            javax.swing.JOptionPane.showMessageDialog(null, "No te podés eliminar a vos mismo estando en sesión");
            return false;
        }
        boolean ok = eliminarFila(id);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Eliminado" : "Error al eliminar");
        return ok;
    }

    private boolean eliminarFila(int id) {
        String sql = "DELETE FROM usuario WHERE idUsuario=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
}
