package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteMetodos {

    /** Mensajes que devuelven guardar() y eliminar() para el formulario. */
    public static final String GUARDADO_OK = "Guardado correctamente";
    public static final String ELIMINADO_OK = "Eliminado";

    /**
     * Valida los datos, decide si es alta o modificacion y devuelve el mensaje
     * que el formulario debe mostrar. Si todo sale bien, el cliente queda
     * guardado en la base.
     */
    public String guardar(Cliente c) {
        if (c.getNombre() == null || c.getNombre().trim().isEmpty()) {
            return "El nombre es obligatorio";
        }
        if (existeCedula(c.getCedula(), c.getIdCliente())) {
            return "Ya hay otro cliente cargado con la cédula " + c.getCedula()
                    + ".\nRevise la lista antes de guardar.";
        }
        boolean ok = c.getIdCliente() > 0 ? modificar(c) : insertar(c);
        return ok ? GUARDADO_OK : "Error al guardar";
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
     * Elimina el cliente y devuelve el mensaje que el formulario debe mostrar.
     */
    public String eliminar(int id) {
        return eliminarFila(id) ? ELIMINADO_OK
                : "No se puede eliminar: tiene ventas u órdenes asociadas";
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
