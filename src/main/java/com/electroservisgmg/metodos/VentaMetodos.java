package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.DetalleVenta;
import com.electroservisgmg.clases.Empresa;
import com.electroservisgmg.clases.Venta;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaMetodos {

    // Registra venta + detalles y descuenta stock automaticamente en una transaccion
    public boolean registrarVenta(Venta v, List<DetalleVenta> detalles) {
        String sqlVenta = "INSERT INTO venta(idCliente, idUsuario, fechaVenta, total, formaPago, condicionVenta, tipoTransaccion, tipoDocumento) VALUES(?,?,?,?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO detalleventa(idVenta, idProducto, cantidad, precioUnitario, descuento, iva, subtotal) VALUES(?,?,?,?,?,?,?)";
        String sqlStock = "UPDATE producto SET stock = stock - ? WHERE idProducto=? AND stock >= ?";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, v.getIdCliente());
                ps.setInt(2, v.getIdUsuario());
                ps.setTimestamp(3, new java.sql.Timestamp(v.getFechaVenta().getTime()));
                ps.setDouble(4, v.getTotal());
                ps.setString(5, v.getFormaPago());
                ps.setString(6, v.getCondicionVenta() == null ? "Contado" : v.getCondicionVenta());
                ps.setString(7, v.getTipoTransaccion() == null ? "Venta de mercadería" : v.getTipoTransaccion());
                ps.setString(8, v.getTipoDocumento() == null ? Venta.TICKET : v.getTipoDocumento());
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
                    psDet.setDouble(5, d.getDescuento());
                    psDet.setDouble(6, d.getIva());
                    psDet.setDouble(7, d.getSubtotal());
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

    /** Detalle (renglones) de una venta. */
    public List<DetalleVenta> listarDetalles(int idVenta) {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT idDetalleVenta, idProducto, cantidad, precioUnitario, descuento, iva, subtotal "
                + "FROM detalleventa WHERE idVenta=? ORDER BY idDetalleVenta";
        try (Connection con = Conexion.nuevaConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta();
                    d.setIdDetalleVenta(rs.getInt("idDetalleVenta"));
                    d.setIdVenta(idVenta);
                    d.setIdProducto(rs.getInt("idProducto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getDouble("precioUnitario"));
                    try { d.setDescuento(rs.getDouble("descuento")); } catch (SQLException ignored) {}
                    try { d.setIva(rs.getDouble("iva")); } catch (SQLException ignored) {}
                    d.setSubtotal(rs.getDouble("subtotal"));
                    lista.add(d);
                }
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

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
                v.setFechaVenta(rs.getTimestamp("fechaVenta"));
                v.setTotal(rs.getDouble("total"));
                v.setFormaPago(rs.getString("formaPago"));
                try { v.setCondicionVenta(rs.getString("condicionVenta")); } catch (SQLException ignored) {}
                try { v.setTipoTransaccion(rs.getString("tipoTransaccion")); } catch (SQLException ignored) {}
                try { v.setTipoDocumento(rs.getString("tipoDocumento")); } catch (SQLException ignored) {}
                try { v.setNroFactura(rs.getInt("nroFactura")); } catch (SQLException ignored) {}
                try { v.setCdc(rs.getString("cdc")); } catch (SQLException ignored) {}
                try { v.setEstado(rs.getString("estado")); } catch (SQLException ignored) {}
                lista.add(v);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

    /** Una venta por id (null si no existe). */
    public Venta buscar(int idVenta) {
        for (Venta v : listar()) if (v.getIdVenta() == idVenta) return v;
        return null;
    }

    /** Proximo numero de factura correlativo (respeta el numero inicial del timbrado). */
    public int siguienteNroFactura(Empresa e) {
        int max = 0;
        String sql = "SELECT COALESCE(MAX(nroFactura), 0) FROM venta WHERE nroFactura IS NOT NULL";
        try (Connection con = Conexion.nuevaConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) max = rs.getInt(1);
        } catch (SQLException ex) { System.err.println("siguienteNroFactura: " + ex.getMessage()); }
        int inicio = (e == null) ? 1 : e.getNroFacturaInicio();
        return Math.max(max + 1, inicio);
    }

    /**
     * Emite la Factura Electronica de una venta: le asigna el numero correlativo
     * y genera su CDC de 44 digitos (que queda guardado en la base).
     * Si la venta ya tenia factura, devuelve la misma sin volver a numerarla.
     */
    public Venta emitirFactura(int idVenta) throws Exception {
        Venta v = buscar(idVenta);
        if (v == null) throw new Exception("No existe la venta #" + idVenta);
        if (v.getCdc() != null && !v.getCdc().isEmpty()) return v;

        Empresa e = new EmpresaMetodos().obtener();
        int nro = siguienteNroFactura(e);
        String cdc = CdcUtil.generar(e, nro, v.getFechaVenta());

        String sql = "UPDATE venta SET tipoDocumento=?, nroFactura=?, cdc=? WHERE idVenta=?";
        try (Connection con = Conexion.nuevaConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Venta.FACTURA);
            ps.setInt(2, nro);
            ps.setString(3, cdc);
            ps.setInt(4, idVenta);
            if (ps.executeUpdate() == 0) throw new Exception("No se encontro la venta #" + idVenta);
        } catch (SQLException ex) {
            throw new Exception("No se pudo emitir la factura: " + ex.getMessage(), ex);
        }
        v.setTipoDocumento(Venta.FACTURA);
        v.setNroFactura(nro);
        v.setCdc(cdc);
        return v;
    }

    /**
     * Anula una venta: la marca como ANULADA y devuelve al stock todo lo que
     * habia descontado. No se borra la fila a proposito: asi el numero de
     * factura no se vuelve a usar.
     * Todo en una transaccion: si algo falla no queda a medias.
     */
    public boolean anular(int idVenta) {
        String sqlEstado = "UPDATE venta SET estado='ANULADA' "
                + "WHERE idVenta=? AND (estado IS NULL OR estado<>'ANULADA')";
        String sqlStock = "UPDATE producto p JOIN detalleventa d ON d.idProducto=p.idProducto "
                + "SET p.stock = p.stock + d.cantidad WHERE d.idVenta=?";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                ps.setInt(1, idVenta);
                if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                ps.setInt(1, idVenta);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("Error anular venta: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}
