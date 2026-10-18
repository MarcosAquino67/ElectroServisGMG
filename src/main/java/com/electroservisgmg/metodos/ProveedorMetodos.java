package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Proveedor;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorMetodos {

    /**
     * Lee los campos del formulario, valida, decide si es alta o modificacion,
     * guarda y muestra el mensaje. Devuelve true si guardo, asi el formulario
     * sabe si refrescar la tabla.
     */
    public boolean guardar(javax.swing.JTextField txtId, javax.swing.JTextField txtNombre,
                           javax.swing.JTextField txtRuc, javax.swing.JTextField txtTelefono,
                           javax.swing.JTextField txtEmail, javax.swing.JTextField txtDireccion) {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "El nombre es obligatorio");
            return false;
        }
        Proveedor p = new Proveedor();
        int id = txtId.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtId.getText().trim());
        p.setIdProveedor(id);
        p.setNombre(nombre);
        p.setRuc(txtRuc.getText().trim());
        p.setTelefono(txtTelefono.getText().trim());
        p.setEmail(txtEmail.getText().trim());
        p.setDireccion(txtDireccion.getText().trim());

        if (existeRuc(p.getRuc(), id)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Ya hay otro proveedor cargado con el RUC " + p.getRuc() + ".\n"
                    + "Revise la lista antes de guardar.");
            return false;
        }
        boolean ok = id > 0 ? modificar(p) : insertar(p);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Guardado correctamente" : "Error al guardar");
        return ok;
    }

    public boolean insertar(Proveedor p) {
        String sql = "INSERT INTO proveedor(nombre, ruc, telefono, direccion, email) VALUES(?,?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getRuc());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            ps.setString(5, p.getEmail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    public boolean modificar(Proveedor p) {
        String sql = "UPDATE proveedor SET nombre=?, ruc=?, telefono=?, direccion=?, email=? WHERE idProveedor=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getRuc());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            ps.setString(5, p.getEmail());
            ps.setInt(6, p.getIdProveedor());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    /**
     * Elimina el proveedor seleccionado (el id viene del campo txtId) y muestra
     * el mensaje. Devuelve true si elimino.
     */
    public boolean eliminar(javax.swing.JTextField txtId) {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un proveedor de la tabla");
            return false;
        }
        boolean ok = eliminarFila(id);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Eliminado"
                : "No se puede eliminar: tiene compras o productos asociados");
        return ok;
    }

    private boolean eliminarFila(int id) {
        String sql = "DELETE FROM proveedor WHERE idProveedor=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedor";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("idProveedor"));
                p.setNombre(rs.getString("nombre"));
                p.setRuc(rs.getString("ruc"));
                p.setTelefono(rs.getString("telefono"));
                p.setDireccion(rs.getString("direccion"));
                p.setEmail(rs.getString("email"));
                lista.add(p);
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }

    /**
     * Llena la tabla con los proveedores, opcionalmente filtrados por nombre o
     * RUC.
     */
    public void mostrar(javax.swing.JTable tabla, String filtro) {
        String f = filtro == null ? "" : filtro.toLowerCase();
        List<Proveedor> fil = new ArrayList<>();
        for (Proveedor p : listar()) {
            if (f.isEmpty() || p.getNombre().toLowerCase().contains(f)
                    || p.getRuc().toLowerCase().contains(f)) fil.add(p);
        }
        String[] cols = {"ID", "Nombre", "RUC", "Teléfono", "Email"};
        Object[][] data = new Object[fil.size()][5];
        for (int i = 0; i < fil.size(); i++) {
            Proveedor p = fil.get(i);
            data[i] = new Object[]{p.getIdProveedor(), p.getNombre(), p.getRuc(),
                p.getTelefono(), p.getEmail()};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
    }

    /**
     * True si ya hay otro proveedor con ese RUC (se comparan solo los numeros,
     * asi "80012345-6" y "800123456" cuentan como el mismo). Si viene vacio no
     * se controla nada.
     *
     * @param idExcluir el proveedor que se esta editando
     */
    public boolean existeRuc(String ruc, int idExcluir) {
        if (ruc == null || ruc.trim().isEmpty()) return false;
        String limpio = ruc.replaceAll("[^0-9A-Za-z]", "").toUpperCase();
        if (limpio.isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM proveedor "
                + "WHERE UPPER(REGEXP_REPLACE(ruc, '[^0-9A-Za-z]', '')) = ? AND idProveedor <> ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, limpio);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { System.out.println("existeRuc: " + e.getMessage()); }
        return false;
    }
}
