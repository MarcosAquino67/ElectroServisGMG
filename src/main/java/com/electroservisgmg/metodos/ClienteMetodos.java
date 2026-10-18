package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteMetodos {

    /**
     * Lee los campos del formulario, valida, decide si es alta o modificacion,
     * guarda y muestra el mensaje. Devuelve true si guardo, asi el formulario
     * sabe si refrescar la tabla.
     */
    public boolean guardar(javax.swing.JTextField txtId, javax.swing.JTextField txtNombre,
                           javax.swing.JTextField txtApellido, javax.swing.JTextField txtCedula,
                           javax.swing.JTextField txtTelefono, javax.swing.JTextField txtEmail,
                           javax.swing.JTextField txtDireccion) {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "El nombre es obligatorio");
            return false;
        }
        Cliente c = new Cliente();
        int id = txtId.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtId.getText().trim());
        c.setIdCliente(id);
        c.setNombre(nombre);
        c.setApellido(txtApellido.getText().trim());
        c.setCedula(txtCedula.getText().trim());
        c.setTelefono(txtTelefono.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setDireccion(txtDireccion.getText().trim());

        if (existeCedula(c.getCedula(), id)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Ya hay otro cliente cargado con la cédula " + c.getCedula() + ".\n"
                    + "Revise la lista antes de guardar.");
            return false;
        }
        boolean ok = id > 0 ? modificar(c) : insertar(c);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Guardado correctamente" : "Error al guardar");
        return ok;
    }

    public boolean insertar(Cliente c) {
        String sql = "INSERT INTO cliente(nombre, apellido, cedula, telefono, direccion, email) VALUES(?,?,?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getCedula());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getDireccion());
            ps.setString(6, c.getEmail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }

    public boolean modificar(Cliente c) {
        String sql = "UPDATE cliente SET nombre=?, apellido=?, cedula=?, telefono=?, direccion=?, email=? WHERE idCliente=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getCedula());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getDireccion());
            ps.setString(6, c.getEmail());
            ps.setInt(7, c.getIdCliente());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }

    /**
     * Elimina el cliente seleccionado (el id viene del campo txtId) y muestra
     * el mensaje. Devuelve true si elimino.
     */
    public boolean eliminar(javax.swing.JTextField txtId) {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un cliente de la tabla");
            return false;
        }
        boolean ok = eliminarFila(id);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Eliminado"
                : "No se puede eliminar: tiene ventas u órdenes asociadas");
        return ok;
    }

    private boolean eliminarFila(int id) {
        String sql = "DELETE FROM cliente WHERE idCliente=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }

    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("idCliente"));
                c.setNombre(rs.getString("nombre"));
                c.setApellido(rs.getString("apellido"));
                c.setCedula(rs.getString("cedula"));
                c.setTelefono(rs.getString("telefono"));
                c.setDireccion(rs.getString("direccion"));
                c.setEmail(rs.getString("email"));
                lista.add(c);
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }

    /**
     * Llena la tabla con los clientes, opcionalmente filtrados por nombre,
     * apellido o cedula.
     */
    public void mostrar(javax.swing.JTable tabla, String filtro) {
        String f = filtro == null ? "" : filtro.toLowerCase();
        List<Cliente> fil = new ArrayList<>();
        for (Cliente c : listar()) {
            if (f.isEmpty() || c.getNombre().toLowerCase().contains(f)
                    || c.getApellido().toLowerCase().contains(f)
                    || c.getCedula().toLowerCase().contains(f)) fil.add(c);
        }
        String[] cols = {"ID", "Nombre", "Apellido", "Cédula", "Teléfono", "Email"};
        Object[][] data = new Object[fil.size()][6];
        for (int i = 0; i < fil.size(); i++) {
            Cliente c = fil.get(i);
            data[i] = new Object[]{c.getIdCliente(), c.getNombre(), c.getApellido(),
                c.getCedula(), c.getTelefono(), c.getEmail()};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
    }

    /**
     * True si ya hay otro cliente con esa cedula.
     * Se comparan solo los numeros y letras, asi "1.234.567" y "1234567"
     * cuentan como la misma cedula. Sirve para no cargar el mismo cliente
     * dos veces. Si la cedula esta vacia no se controla nada.
     *
     * @param idExcluir el cliente que se esta editando (asi no choca consigo mismo)
     */
    public boolean existeCedula(String cedula, int idExcluir) {
        if (cedula == null || cedula.trim().isEmpty()) return false;
        String limpia = cedula.replaceAll("[^0-9A-Za-z]", "").toUpperCase();
        if (limpia.isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM cliente "
                + "WHERE UPPER(REGEXP_REPLACE(cedula, '[^0-9A-Za-z]', '')) = ? AND idCliente <> ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, limpia);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { System.out.println("existeCedula: " + e.getMessage()); }
        return false;
    }
}
