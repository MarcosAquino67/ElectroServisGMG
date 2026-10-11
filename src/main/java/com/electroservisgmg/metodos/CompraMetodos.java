package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Compra;
import com.electroservisgmg.clases.DetalleCompra;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraMetodos {

    // Registro de compra cabecera + detalles con incremento automatico de stock (transaccion)
    public boolean registrarCompra(Compra compra, List<DetalleCompra> detalles) {
        String sqlCompra = "INSERT INTO compra(idProveedor, idUsuario, fechaCompra, total, estado) VALUES(?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO detallecompra(idCompra, idProducto, cantidad, precioUnitario, subtotal) VALUES(?,?,?,?,?)";
        String sqlStock = "UPDATE producto SET stock = stock + ? WHERE idProducto=?";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, compra.getIdProveedor());
                ps.setInt(2, compra.getIdUsuario());
                ps.setDate(3, new java.sql.Date(compra.getFechaCompra().getTime()));
                ps.setDouble(4, compra.getTotal());
                ps.setString(5, compra.getEstado());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) compra.setIdCompra(rs.getInt(1));
                }
            }
            try (PreparedStatement psDet = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                for (DetalleCompra d : detalles) {
                    psDet.setInt(1, compra.getIdCompra());
                    psDet.setInt(2, d.getIdProducto());
                    psDet.setInt(3, d.getCantidad());
                    psDet.setDouble(4, d.getPrecioUnitario());
                    psDet.setDouble(5, d.getSubtotal());
                    psDet.addBatch();
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getIdProducto());
                    psStock.addBatch();
                }
                psDet.executeBatch();
                psStock.executeBatch();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("Error registrarCompra: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    public List<Compra> listar() {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compra ORDER BY fechaCompra DESC";
        try (Connection con = Conexion.nuevaConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Compra c = new Compra();
                c.setIdCompra(rs.getInt("idCompra"));
                c.setIdProveedor(rs.getInt("idProveedor"));
                c.setIdUsuario(rs.getInt("idUsuario"));
                c.setFechaCompra(rs.getDate("fechaCompra"));
                c.setTotal(rs.getDouble("total"));
                c.setEstado(rs.getString("estado"));
                lista.add(c);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

    public boolean anular(int idCompra) {
        String sql = "UPDATE compra SET estado='ANULADA' WHERE idCompra=?";
        try (Connection con = Conexion.nuevaConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }
}
