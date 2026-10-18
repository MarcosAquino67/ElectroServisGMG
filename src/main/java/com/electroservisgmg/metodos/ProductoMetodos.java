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
        String sql = "INSERT INTO producto(codigo, codigoBarras, nombre, descripcion, stock, precioCompra, precioVenta, iva, idProveedor, foto, stockMinimo)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            textoONull(ps, 2, p.getCodigoBarras());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getDescripcion());
            ps.setInt(5, p.getStock());
            ps.setDouble(6, p.getPrecioCompra());
            ps.setDouble(7, p.getPrecioVenta());
            ps.setDouble(8, p.getIva());
            if (p.getIdProveedor() > 0) ps.setInt(9, p.getIdProveedor());
            else ps.setNull(9, Types.INTEGER);
            textoONull(ps, 10, p.getFoto());
            ps.setInt(11, p.getStockMinimo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { ultimoError = e.getMessage(); System.out.println("insertar producto: " + e.getMessage()); return false; }
    }

    public boolean modificar(Producto p) {
        ultimoError = "";
        String sql = "UPDATE producto SET codigo=?, codigoBarras=?, nombre=?, descripcion=?, stock=?, precioCompra=?, precioVenta=?, iva=?, idProveedor=?, foto=?, stockMinimo=? WHERE idProducto=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            textoONull(ps, 2, p.getCodigoBarras());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getDescripcion());
            ps.setInt(5, p.getStock());
            ps.setDouble(6, p.getPrecioCompra());
            ps.setDouble(7, p.getPrecioVenta());
            ps.setDouble(8, p.getIva());
            if (p.getIdProveedor() > 0) ps.setInt(9, p.getIdProveedor());
            else ps.setNull(9, Types.INTEGER);
            textoONull(ps, 10, p.getFoto());
            ps.setInt(11, p.getStockMinimo());
            ps.setInt(12, p.getIdProducto());
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
            while (rs.next()) lista.add(armar(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }

    /**
     * Productos cuyo stock ya llego al minimo cargado (o mas abajo).
     * Los que tienen el minimo en 0 no entran: quiere decir "no avisar".
     */
    public List<Producto> listarBajoMinimo() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE stockMinimo > 0 AND stock <= stockMinimo "
                + "ORDER BY stock - stockMinimo ASC";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(armar(rs));
        } catch (SQLException e) { System.out.println("listarBajoMinimo: " + e.getMessage()); }
        return lista;
    }

    // ---- busqueda por codigo ----

    /**
     * Busca un producto por codigo de barras y, si no aparece, por el codigo
     * interno. Es lo que usa el lector al escanear.
     */
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;
        Producto p = buscarUno("codigoBarras", codigo.trim());
        if (p == null) p = buscarUno("codigo", codigo.trim());
        return p;
    }

    /** Busca por codigo de barras exacto. */
    public Producto buscarPorCodigoBarras(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;
        return buscarUno("codigoBarras", codigo.trim());
    }

    private Producto buscarUno(String columna, String valor) {
        String sql = "SELECT * FROM producto WHERE " + columna + " = ? LIMIT 1";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return armar(rs);
            }
        } catch (SQLException e) { System.out.println("buscar producto: " + e.getMessage()); }
        return null;
    }

    /**
     * Devuelve un EAN-13 libre para ese producto: parte del id y, si ese ya
     * esta usado, prueba con el siguiente. Evita chocar con el indice unico.
     */
    public String generarCodigoBarrasLibre(int idProducto) {
        for (int i = 0; i < 50; i++) {
            String candidato = CodigoBarras.generarEan13((long) idProducto + i * 1000003L);
            if (buscarPorCodigoBarras(candidato) == null) return candidato;
        }
        return CodigoBarras.generarEan13(System.currentTimeMillis());
    }

    // ---- auxiliares ----

    private static Producto armar(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("idProducto"));
        try { p.setCodigo(rs.getString("codigo")); } catch (SQLException ignored) {}
        try { p.setCodigoBarras(rs.getString("codigoBarras")); } catch (SQLException ignored) {}
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setStock(rs.getInt("stock"));
        try { p.setStockMinimo(rs.getInt("stockMinimo")); } catch (SQLException ignored) {}
        p.setPrecioCompra(rs.getDouble("precioCompra"));
        p.setPrecioVenta(rs.getDouble("precioVenta"));
        try { p.setIva(rs.getDouble("iva")); } catch (SQLException ignored) {}
        p.setIdProveedor(rs.getInt("idProveedor"));
        try { p.setFoto(rs.getString("foto")); } catch (SQLException ignored) {}
        return p;
    }

    /** Los campos de texto vacios se guardan como NULL (el codigo de barras es unico). */
    private static void textoONull(PreparedStatement ps, int pos, String valor) throws SQLException {
        if (valor == null || valor.trim().isEmpty()) ps.setNull(pos, Types.VARCHAR);
        else ps.setString(pos, valor.trim());
    }
}
