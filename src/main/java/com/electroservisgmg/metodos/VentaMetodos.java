package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.DetalleVenta;
import com.electroservisgmg.clases.Venta;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaMetodos {

    // Registra venta + detalles y descuenta stock automaticamente en una transaccion
    public boolean registrarVenta(Venta v, List<DetalleVenta> detalles) {
        String sqlVenta = "INSERT INTO venta(idCliente, idUsuario, fechaVenta, total, formaPago) VALUES(?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO detalleventa(idVenta, idProducto, cantidad, precioUnitario, subtotal) VALUES(?,?,?,?,?)";
        String sqlStock = "UPDATE producto SET stock = stock - ? WHERE idProducto=? AND stock >= ?";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, v.getIdCliente());
                ps.setInt(2, v.getIdUsuario());
                ps.setDate(3, new java.sql.Date(v.getFechaVenta().getTime()));
                ps.setDouble(4, v.getTotal());
                ps.setString(5, v.getFormaPago());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) v.setIdVenta(rs.getInt(1)); }
            }
            try (PreparedStatement psDet = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                for (DetalleVenta d : detalles) {
                    psDet.setInt(1, v.getIdVenta());
                    psDet.setInt(2, d.getIdProducto());
                    psDet.setInt(3, d.getCantidad());
                    psDet.setDouble(4, d.getPrecioUnitario());
                    psDet.setDouble(5, d.getSubtotal());
                    psDet.addBatch();
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getIdProducto());
                    psStock.setInt(3, d.getCantidad());
                    psStock.addBatch();
                }
                psDet.executeBatch();
                int[] res = psStock.executeBatch();
                for (int r : res) if (r == 0) throw new SQLException("Stock insuficiente");
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("Error registrarVenta: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    // Compatibilidad con firma anterior
    public boolean registrarVenta(Venta v) { return registrarVenta(v, new ArrayList<>()); }

    public boolean descontarInventario(int idProducto, int cantidad) {
        ProductoMetodos pm = new ProductoMetodos();
        return pm.descontarStock(idProducto, cantidad);
    }

    public List<Venta> listar() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM venta ORDER BY fechaVenta DESC";
        try (Connection con = Conexion.nuevaConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Venta v = new Venta();
                v.setIdVenta(rs.getInt("idVenta"));
                v.setIdCliente(rs.getInt("idCliente"));
                v.setIdUsuario(rs.getInt("idUsuario"));
                v.setFechaVenta(rs.getDate("fechaVenta"));
                v.setTotal(rs.getDouble("total"));
                v.setFormaPago(rs.getString("formaPago"));
                lista.add(v);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }
}
