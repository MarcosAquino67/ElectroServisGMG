package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Proveedor;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorMetodos {
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
    public boolean eliminar(int id) {
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
