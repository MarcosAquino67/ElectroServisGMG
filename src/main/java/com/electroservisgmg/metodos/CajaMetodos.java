package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Caja;
import com.electroservisgmg.clases.CajaMovimiento;
import com.electroservisgmg.clases.Sesion;
import com.electroservisgmg.clases.Usuario;
import com.electroservisgmg.conexion.Conexion;
import com.electroservisgmg.formularios.UiTheme;
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

    /**
     * Refresca el estado de la caja en el formulario: etiquetas de resumen,
     * tabla de movimientos y habilitacion de los controles.
     */
    public void cargar(javax.swing.JTable tabla,
                       javax.swing.JLabel lblEstado, javax.swing.JLabel lblIniV,
                       javax.swing.JLabel lblIngV, javax.swing.JLabel lblEgrV,
                       javax.swing.JLabel lblSaldoV,
                       javax.swing.JButton btnAbrir, javax.swing.JButton btnCerrar,
                       javax.swing.JButton btnRegistrar,
                       javax.swing.JTextField txtConcepto, javax.swing.JTextField txtMonto,
                       javax.swing.JComboBox<String> cboTipo) {
        Caja c = cajaAbierta();
        if (c == null) {
            lblEstado.setText("Caja CERRADA · presione \"Abrir caja\" para comenzar");
            lblEstado.setForeground(UiTheme.DANGER);
            lblIniV.setText(UiTheme.gs(0));
            lblIngV.setText(UiTheme.gs(0));
            lblEgrV.setText(UiTheme.gs(0));
            lblSaldoV.setText(UiTheme.gs(0));
            mostrarMovimientos(tabla, new ArrayList<CajaMovimiento>());
            btnAbrir.setEnabled(true);
            btnCerrar.setEnabled(false);
            btnRegistrar.setEnabled(false);
            txtConcepto.setEnabled(false);
            txtMonto.setEnabled(false);
            cboTipo.setEnabled(false);
            return;
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String desde = c.getFechaApertura() == null ? "" : sdf.format(c.getFechaApertura());
        lblEstado.setText("Caja ABIERTA (id " + c.getIdCaja() + ") · desde " + desde);
        lblEstado.setForeground(UiTheme.SUCCESS);
        lblIniV.setText(UiTheme.gs(c.getMontoInicial()));
        lblIngV.setText(UiTheme.gs(sumarMovimientos(c.getIdCaja(), "INGRESO")));
        lblEgrV.setText(UiTheme.gs(sumarMovimientos(c.getIdCaja(), "EGRESO")));
        lblSaldoV.setText(UiTheme.gs(saldoDe(c)));
        mostrarMovimientos(tabla, listarMovimientos(c.getIdCaja()));
        btnAbrir.setEnabled(false);
        btnCerrar.setEnabled(true);
        btnRegistrar.setEnabled(true);
        txtConcepto.setEnabled(true);
        txtMonto.setEnabled(true);
        cboTipo.setEnabled(true);
    }

    /** Llena la tabla con los movimientos indicados. */
    public void mostrarMovimientos(javax.swing.JTable tabla, List<CajaMovimiento> movs) {
        String[] cols = {"ID", "Fecha", "Tipo", "Concepto", "Monto"};
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        Object[][] data = new Object[movs.size()][5];
        for (int i = 0; i < movs.size(); i++) {
            CajaMovimiento m = movs.get(i);
            data[i] = new Object[]{m.getIdMovimiento(),
                m.getFecha() == null ? "" : sdf.format(m.getFecha()),
                m.getTipo(), m.getDescripcion(), UiTheme.gs(m.getMonto())};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int cc) { return false; }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(420);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(140);
    }

    /** Saldo actual de una caja: monto inicial + ingresos - egresos. */
    public double saldoDe(Caja c) {
        if (c == null) return 0;
        return c.getMontoInicial() + sumarMovimientos(c.getIdCaja(), "INGRESO")
                - sumarMovimientos(c.getIdCaja(), "EGRESO");
    }

    /**
     * Pregunta el monto inicial, abre la caja y avisa. Devuelve true si abrio.
     */
    public boolean abrirCaja() {
        if (cajaAbierta() != null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Ya hay una caja abierta");
            return false;
        }
        String r = javax.swing.JOptionPane.showInputDialog(null, "Monto inicial en caja (₲):", "0");
        if (r == null) return false;
        double monto;
        try {
            monto = UiTheme.parseGs(r.trim().isEmpty() ? "0" : r.trim());
            if (monto < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Monto inicial inválido");
            return false;
        }
        Caja c = new Caja();
        Usuario u = Sesion.getUsuario();
        c.setIdUsuario(u != null ? u.getIdUsuario() : 1);
        c.setFechaApertura(new java.util.Date());
        c.setMontoInicial(monto);
        boolean ok = apertura(c);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Caja abierta" : "No se pudo abrir la caja");
        return ok;
    }

    /**
     * Cierra la caja abierta con el saldo actual y avisa. Devuelve true si cerro.
     */
    public boolean cerrarCaja() {
        Caja c = cajaAbierta();
        if (c == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay ninguna caja abierta");
            return false;
        }
        boolean ok = cierre(c.getIdCaja(), saldoDe(c));
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Caja cerrada" : "No se pudo cerrar la caja");
        return ok;
    }

    /**
     * Lee el concepto, el monto y el tipo, registra el movimiento, limpia el
     * formulario y avisa. Devuelve true si registro.
     */
    public boolean registrar(javax.swing.JTextField txtConcepto, javax.swing.JTextField txtMonto,
                             javax.swing.JComboBox<String> cboTipo) {
        Caja c = cajaAbierta();
        if (c == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Primero abra la caja");
            return false;
        }
        String concepto = txtConcepto.getText().trim();
        if (concepto.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Escriba el concepto del movimiento");
            return false;
        }
        double monto;
        try {
            monto = UiTheme.parseGs(txtMonto.getText().trim());
            if (monto <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "El monto debe ser un número mayor a 0");
            return false;
        }
        String tipo = String.valueOf(cboTipo.getSelectedItem());
        boolean ok = registrarMovimiento(c.getIdCaja(), tipo, monto, concepto);
        if (ok) {
            txtConcepto.setText("");
            txtMonto.setText("");
            cboTipo.setSelectedIndex(0);
        }
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Movimiento registrado" : "No se pudo registrar");
        return ok;
    }

    /** Caja que esta abierta en este momento (o null si no hay ninguna). */
    public Caja cajaAbierta() {
        String sql = "SELECT * FROM caja WHERE estado='ABIERTA' ORDER BY idCaja DESC LIMIT 1";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                Caja c = new Caja();
                c.setIdCaja(rs.getInt("idCaja"));
                c.setIdUsuario(rs.getInt("idUsuario"));
                c.setFechaApertura(rs.getTimestamp("fechaApertura"));
                c.setFechaCierre(rs.getTimestamp("fechaCierre"));
                c.setMontoInicial(rs.getDouble("montoInicial"));
                c.setMontoFinal(rs.getDouble("montoFinal"));
                c.setEstado(rs.getString("estado"));
                return c;
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return null;
    }

    /** Suma de todos los movimientos de un tipo (INGRESO / EGRESO) de una caja. */
    public double sumarMovimientos(int idCaja, String tipo) {
        String sql = "SELECT COALESCE(SUM(monto),0) t FROM caja_movimiento WHERE idCaja=? AND tipo=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            ps.setString(2, tipo);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getDouble("t"); }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return 0;
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
