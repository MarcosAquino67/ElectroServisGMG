package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.TipoEquipo;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoEquipoMetodos {
    public List<TipoEquipo> listar() {
        List<TipoEquipo> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipo_equipo ORDER BY nombre";
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                TipoEquipo t = new TipoEquipo();
                t.setIdTipo(rs.getInt("idTipo"));
                t.setNombre(rs.getString("nombre"));
                lista.add(t);
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }
    public boolean insertar(String nombre) {
        String sql = "INSERT IGNORE INTO tipo_equipo(nombre) VALUES(?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
    public boolean eliminar(int id) {
        String sql = "DELETE FROM tipo_equipo WHERE idTipo=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }
}
