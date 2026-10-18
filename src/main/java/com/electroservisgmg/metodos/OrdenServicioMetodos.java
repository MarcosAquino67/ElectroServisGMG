package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.OrdenServicio;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdenServicioMetodos {
    public boolean insertar(OrdenServicio o) {
        String sql = "INSERT INTO ordenservicio(idCliente, idEquipo, idUsuario, fechaIngreso, estado, diagnostico, costoReparacion, observaciones) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getIdCliente());
            ps.setInt(2, o.getIdEquipo());
            ps.setInt(3, o.getIdUsuario());
            ps.setDate(4, new java.sql.Date(o.getFechaIngreso().getTime()));
            ps.setString(5, o.getEstado());
            ps.setString(6, o.getDiagnostico());
            ps.setDouble(7, o.getCostoReparacion());
            ps.setString(8, o.getObservaciones());
            boolean ok = ps.executeUpdate() > 0;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) o.setIdOrden(rs.getInt(1));
            }
            return ok;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    public boolean actualizarEstado(int idOrden, String estado) {
        String sql = "UPDATE ordenservicio SET estado=? WHERE idOrden=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idOrden);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }

    /** Marca la orden como ENTREGADA con fecha de hoy (retiro del equipo). */
    public boolean marcarRetirado(int idOrden) {
        String sql = "UPDATE ordenservicio SET estado='ENTREGADO', fechaEntrega=NOW() WHERE idOrden=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }

    public boolean modificar(OrdenServicio o) {
        String sql = "UPDATE ordenservicio SET idCliente=?, idEquipo=?, estado=?, diagnostico=?, costoReparacion=?, observaciones=? WHERE idOrden=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, o.getIdCliente());
            ps.setInt(2, o.getIdEquipo());
            ps.setString(3, o.getEstado());
            ps.setString(4, o.getDiagnostico());
            ps.setDouble(5, o.getCostoReparacion());
            ps.setString(6, o.getObservaciones());
            ps.setInt(7, o.getIdOrden());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    /**
     * Borra la orden. Si tenia repuestos cargados, primero los devuelve al
     * stock y borra esas lineas (si no, la clave foranea no deja borrar).
     * Todo en una transaccion.
     */
    public boolean eliminar(int id) {
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE producto p JOIN orden_repuesto r ON r.idProducto = p.idProducto "
                    + "SET p.stock = p.stock + r.cantidad WHERE r.idOrdenServicio=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException sinTabla) {
                // si todavia no se corrio el script de repuestos, la tabla no existe
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM orden_repuesto WHERE idOrdenServicio=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException sinTabla) {}
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ordenservicio WHERE idOrden=?")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            System.out.println(e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException ignored) {}
        }
    }
    public List<OrdenServicio> listar() {
        List<OrdenServicio> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordenservicio";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                OrdenServicio o = new OrdenServicio();
                o.setIdOrden(rs.getInt("idOrden"));
                o.setIdCliente(rs.getInt("idCliente"));
                o.setIdEquipo(rs.getInt("idEquipo"));
                o.setIdUsuario(rs.getInt("idUsuario"));
                o.setFechaIngreso(rs.getDate("fechaIngreso"));
                o.setFechaEntrega(rs.getDate("fechaEntrega"));
                o.setEstado(rs.getString("estado"));
                o.setDiagnostico(rs.getString("diagnostico"));
                o.setCostoReparacion(rs.getDouble("costoReparacion"));
                o.setObservaciones(rs.getString("observaciones"));
                lista.add(o);
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }

    public int contarPorEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM ordenservicio WHERE estado=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return 0;
    }
}
