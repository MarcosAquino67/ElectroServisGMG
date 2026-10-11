package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Equipo;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipoMetodos {
    public boolean insertar(Equipo e) {
        String sql = "INSERT INTO equipo(idCliente, tipo, marca, modelo, nroSerie, fallaReportada) VALUES(?,?,?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getIdCliente());
            ps.setString(2, e.getTipo());
            ps.setString(3, e.getMarca());
            ps.setString(4, e.getModelo());
            ps.setString(5, e.getNroSerie());
            ps.setString(6, e.getFallaReportada());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { System.out.println(ex.getMessage()); return false; }
    }
    public boolean modificar(Equipo e) {
        String sql = "UPDATE equipo SET tipo=?, marca=?, modelo=?, nroSerie=?, fallaReportada=? WHERE idEquipo=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getTipo());
            ps.setString(2, e.getMarca());
            ps.setString(3, e.getModelo());
            ps.setString(4, e.getNroSerie());
            ps.setString(5, e.getFallaReportada());
            ps.setInt(6, e.getIdEquipo());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { System.out.println(ex.getMessage()); return false; }
    }
    public boolean eliminar(int id) {
        String sql = "DELETE FROM equipo WHERE idEquipo=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException ex) { System.out.println(ex.getMessage()); return false; }
    }
    public List<Equipo> listar() {
        List<Equipo> lista = new ArrayList<>();
        String sql = "SELECT * FROM equipo";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Equipo e = new Equipo();
                e.setIdEquipo(rs.getInt("idEquipo"));
                e.setIdCliente(rs.getInt("idCliente"));
                e.setTipo(rs.getString("tipo"));
                e.setMarca(rs.getString("marca"));
                e.setModelo(rs.getString("modelo"));
                e.setNroSerie(rs.getString("nroSerie"));
                e.setFallaReportada(rs.getString("fallaReportada"));
                lista.add(e);
            }
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return lista;
    }
}
