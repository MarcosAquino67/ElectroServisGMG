package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Caja;
import com.electroservisgmg.clases.CajaMovimiento;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CajaMetodos {
    public boolean apertura(Caja c) {
        String sql = "INSERT INTO caja(idUsuario, fechaApertura, montoInicial, estado) VALUES(?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getIdUsuario());
            ps.setTimestamp(2, new Timestamp(c.getFechaApertura().getTime()));
            ps.setDouble(3, c.getMontoInicial());
            ps.setString(4, "ABIERTA");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    public boolean cierre(int idCaja, double montoFinal) {
        String sql = "UPDATE caja SET fechaCierre=?, montoFinal=?, estado='CERRADA' WHERE idCaja=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setDouble(2, montoFinal);
            ps.setInt(3, idCaja);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    public boolean registrarMovimiento(int idCaja, String tipo, double monto, String descripcion) {
        String sql = "INSERT INTO caja_movimiento(idCaja, tipo, monto, descripcion, fecha) VALUES(?,?,?,?,NOW())";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            ps.setString(2, tipo);
            ps.setDouble(3, monto);
            ps.setString(4, descripcion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }

    public List<CajaMovimiento> listarMovimientos(int idCaja) {
        List<CajaMovimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM caja_movimiento WHERE idCaja=? ORDER BY fecha DESC";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CajaMovimiento m = new CajaMovimiento();
                    m.setIdMovimiento(rs.getInt("idMovimiento"));
                    m.setIdCaja(rs.getInt("idCaja"));
                    m.setTipo(rs.getString("tipo"));
                    m.setMonto(rs.getDouble("monto"));
                    m.setDescripcion(rs.getString("descripcion"));
                    m.setFecha(rs.getTimestamp("fecha"));
                    lista.add(m);
                }
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }
}
