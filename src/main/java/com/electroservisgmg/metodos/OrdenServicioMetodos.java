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
    public boolean eliminar(int id) {
        String sql = "DELETE FROM ordenservicio WHERE idOrden=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
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
