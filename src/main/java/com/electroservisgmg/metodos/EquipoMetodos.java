package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Equipo;
import com.electroservisgmg.conexion.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipoMetodos {
    /**
     * Lee los campos del formulario, valida, decide si es alta o modificacion,
     * guarda y muestra el mensaje. Devuelve true si guardo, asi el formulario
     * sabe si refrescar la tabla.
     */
    public boolean guardar(javax.swing.JTextField txtId, javax.swing.JComboBox<String> cboCliente,
                           javax.swing.JComboBox<String> cboTipo, javax.swing.JTextField txtMarca,
                           javax.swing.JTextField txtModelo, javax.swing.JTextField txtSerie,
                           javax.swing.JTextField txtFalla) {
        int idCli = idClienteDelCombo(cboCliente);
        if (idCli < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Registre un cliente primero");
            return false;
        }
        Object tipoSel = cboTipo.getSelectedItem();
        if (tipoSel == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un tipo");
            return false;
        }
        Equipo e = new Equipo();
        e.setIdCliente(idCli);
        e.setTipo(tipoSel.toString());
        e.setMarca(txtMarca.getText().trim());
        e.setModelo(txtModelo.getText().trim());
        e.setNroSerie(txtSerie.getText().trim());
        e.setFallaReportada(txtFalla.getText().trim());
        int id = 0;
        try {
            id = txtId.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException ignored) {}
        boolean ok;
        if (id > 0) {
            e.setIdEquipo(id);
            ok = modificar(e);
        } else {
            ok = insertar(e);
        }
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Guardado correctamente" : "Error al guardar");
        return ok;
    }

    /** Saca el id del cliente del texto del combo ("id - nombre apellido"). */
    private int idClienteDelCombo(javax.swing.JComboBox<String> cboCliente) {
        Object sel = cboCliente.getSelectedItem();
        if (sel == null) return -1;
        String s = sel.toString();
        int guion = s.indexOf(" -");
        if (guion <= 0) return -1;
        try { return Integer.parseInt(s.substring(0, guion).trim()); }
        catch (NumberFormatException e) { return -1; }
    }

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
    /**
     * Elimina el equipo seleccionado (el id viene del campo txtId) y muestra el
     * mensaje. Devuelve true si elimino.
     */
    public boolean eliminar(javax.swing.JTextField txtId) {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un equipo de la tabla");
            return false;
        }
        boolean ok = eliminarFila(id);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Eliminado" : "Error al eliminar");
        return ok;
    }

    private boolean eliminarFila(int id) {
        String sql = "DELETE FROM equipo WHERE idEquipo=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException ex) { System.out.println(ex.getMessage()); return false; }
    }

    /**
     * Llena la tabla con los equipos, opcionalmente filtrados por marca, modelo,
     * numero de serie o nombre del cliente.
     */
    public void mostrar(javax.swing.JTable tabla, String filtro) {
        java.util.Map<Integer, String> cli = new java.util.HashMap<>();
        for (com.electroservisgmg.clases.Cliente c : new ClienteMetodos().listar()) {
            cli.put(c.getIdCliente(), c.getNombre() + " " + c.getApellido());
        }
        String f = filtro == null ? "" : filtro.toLowerCase();
        List<Equipo> fil = new ArrayList<>();
        for (Equipo e : listar()) {
            String nomCli = cli.getOrDefault(e.getIdCliente(), "");
            if (f.isEmpty() || e.getMarca().toLowerCase().contains(f)
                    || e.getModelo().toLowerCase().contains(f)
                    || e.getNroSerie().toLowerCase().contains(f)
                    || nomCli.toLowerCase().contains(f)) fil.add(e);
        }
        String[] cols = {"ID", "Cliente", "Tipo", "Marca", "Modelo", "N° Serie"};
        Object[][] data = new Object[fil.size()][6];
        for (int i = 0; i < fil.size(); i++) {
            Equipo e = fil.get(i);
            data[i] = new Object[]{e.getIdEquipo(), cli.getOrDefault(e.getIdCliente(), "ID " + e.getIdCliente()),
                e.getTipo(), e.getMarca(), e.getModelo(), e.getNroSerie()};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
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
