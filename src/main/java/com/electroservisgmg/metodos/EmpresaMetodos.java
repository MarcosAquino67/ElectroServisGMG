package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Empresa;
import com.electroservisgmg.conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Lectura y grabado de los datos del emisor (tabla empresa, una sola fila).
 */
public class EmpresaMetodos {

    /** Ultimo error de BD, para mostrarlo en pantalla. */
    public static String ultimoError = "";

    /** Datos del emisor guardados; si no hay fila devuelve una empresa en blanco. */
    public Empresa obtener() {
        Empresa e = new Empresa();
        String sql = "SELECT * FROM empresa WHERE idEmpresa=1";
        try (Connection con = Conexion.nuevaConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                e.setIdEmpresa(rs.getInt("idEmpresa"));
                e.setRazonSocial(nvl(rs.getString("razonSocial")));
                e.setNombreComercial(nvl(rs.getString("nombreComercial")));
                e.setRuc(nvl(rs.getString("ruc")));
                e.setDvRuc(nvl(rs.getString("dvRuc")));
                e.setDireccion(nvl(rs.getString("direccion")));
                e.setCiudad(nvl(rs.getString("ciudad")));
                e.setTelefono(nvl(rs.getString("telefono")));
                e.setEmail(nvl(rs.getString("email")));
                e.setActividades(nvl(rs.getString("actividades")));
                e.setTimbrado(nvl(rs.getString("timbrado")));
                e.setTimbradoVigenciaDesde(rs.getDate("timbradoVigenciaDesde"));
                e.setEstablecimiento(nvl(rs.getString("establecimiento")));
                e.setPuntoExpedicion(nvl(rs.getString("puntoExpedicion")));
                e.setNroFacturaInicio(rs.getInt("nroFacturaInicio"));
                e.setNroFacturaFin(rs.getInt("nroFacturaFin"));
                e.setTipoContribuyente(nvl(rs.getString("tipoContribuyente")));
                e.setLogo(rs.getString("logo"));
            }
        } catch (SQLException ex) {
            System.err.println("obtener empresa: " + ex.getMessage());
        }
        return e;
    }

    public boolean guardar(Empresa e) {
        ultimoError = "";
        String sql = "UPDATE empresa SET razonSocial=?, nombreComercial=?, ruc=?, dvRuc=?, direccion=?, ciudad=?, "
                + "telefono=?, email=?, actividades=?, timbrado=?, timbradoVigenciaDesde=?, establecimiento=?, "
                + "puntoExpedicion=?, nroFacturaInicio=?, nroFacturaFin=?, tipoContribuyente=?, logo=? "
                + "WHERE idEmpresa=1";
        try (Connection con = Conexion.nuevaConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getRazonSocial());
            ps.setString(2, e.getNombreComercial());
            ps.setString(3, e.getRuc());
            ps.setString(4, e.getDvRuc());
            ps.setString(5, e.getDireccion());
            ps.setString(6, e.getCiudad());
            ps.setString(7, e.getTelefono());
            ps.setString(8, e.getEmail());
            ps.setString(9, e.getActividades());
            ps.setString(10, e.getTimbrado());
            if (e.getTimbradoVigenciaDesde() == null) ps.setNull(11, Types.DATE);
            else ps.setDate(11, new java.sql.Date(e.getTimbradoVigenciaDesde().getTime()));
            ps.setString(12, e.getEstablecimiento());
            ps.setString(13, e.getPuntoExpedicion());
            ps.setInt(14, e.getNroFacturaInicio());
            ps.setInt(15, e.getNroFacturaFin());
            ps.setString(16, e.getTipoContribuyente());
            ps.setString(17, e.getLogo());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ultimoError = ex.getMessage();
            System.err.println("guardar empresa: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Bytes del logo del emisor: usa el archivo elegido en Ajustes y, si no hay,
     * el logo que viene con el sistema (iconos/logo_empresa.jpg).
     * Devuelve null si no se encontro ninguno.
     */
    public static byte[] logoBytes(Empresa e) {
        String ruta = e == null ? null : e.getLogo();
        if (ruta != null && !ruta.trim().isEmpty()) {
            try {
                java.io.File f = new java.io.File(ruta.trim());
                if (f.isFile()) return java.nio.file.Files.readAllBytes(f.toPath());
            } catch (Exception ex) {
                System.err.println("logo elegido: " + ex.getMessage());
            }
        }
        try (java.io.InputStream is = EmpresaMetodos.class.getResourceAsStream("/iconos/logo_empresa.jpg")) {
            if (is != null) return is.readAllBytes();
        } catch (Exception ex) {
            System.err.println("logo del sistema: " + ex.getMessage());
        }
        return null;
    }

    private static String nvl(String s) { return s == null ? "" : s; }
}
