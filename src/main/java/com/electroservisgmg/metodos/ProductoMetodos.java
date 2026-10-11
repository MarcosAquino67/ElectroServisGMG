package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoMetodos {
    /** Ultimo error de BD (para mostrarlo en pantalla en vez de "Error" generico). */
    public static String ultimoError = "";
    public boolean insertar(Producto p) {
        ultimoError = "";
        String sqlSinProv = "INSERT INTO producto(nombre, descripcion, stock, precioCompra, precioVenta, foto) VALUES(?,?,?,?,?,?)";
        String sqlConProv = "INSERT INTO producto(nombre, descripcion, stock, precioCompra, precioVenta, idProveedor, foto) VALUES(?,?,?,?,?,?,?)";
        boolean conProv = p.getIdProveedor() > 0;
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(conProv ? sqlConProv : sqlSinProv)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrecioCompra());
            ps.setDouble(5, p.getPrecioVenta());
            if (conProv) { ps.setInt(6, p.getIdProveedor()); ps.setString(7, p.getFoto()); }
            else ps.setString(6, p.getFoto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { ultimoError = e.getMessage(); System.out.println("insertar producto: " + e.getMessage()); return false; }
    }
    public boolean modificar(Producto p) {
        ultimoError = "";
        String sql = "UPDATE producto SET nombre=?, descripcion=?, stock=?, precioCompra=?, precioVenta=?, foto=? WHERE idProducto=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrecioCompra());
            ps.setDouble(5, p.getPrecioVenta());
            ps.setString(6, p.getFoto());
            ps.setInt(7, p.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { ultimoError = e.getMessage(); System.out.println("modificar producto: " + e.getMessage()); return false; }
    }
    public boolean eliminar(int id) {
        ultimoError = "";
        String sql = "DELETE FROM producto WHERE idProducto=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { ultimoError = e.getMessage(); System.out.println(e.getMessage()); return false; }
    }
    public boolean descontarStock(int idProducto, int cantidad) {
        String sql = "UPDATE producto SET stock = stock - ? WHERE idProducto=? AND stock >= ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("idProducto"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setStock(rs.getInt("stock"));
                p.setPrecioCompra(rs.getDouble("precioCompra"));
                p.setPrecioVenta(rs.getDouble("precioVenta"));
                p.setIdProveedor(rs.getInt("idProveedor"));
                try { p.setFoto(rs.getString("foto")); } catch (SQLException ignored) {}
                lista.add(p);
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }
}
