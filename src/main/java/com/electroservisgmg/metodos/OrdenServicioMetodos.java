package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.OrdenServicio;
import com.electroservisgmg.conexion.Conexion;
import com.electroservisgmg.formularios.UiTheme;
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
     * Elimina la orden seleccionada (el id viene del campo txtId) y muestra el
     * mensaje. Devuelve true si elimino, asi el formulario sabe si refrescar.
     */
    public boolean eliminar(javax.swing.JTextField txtId) {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione una orden de la tabla");
            return false;
        }
        boolean ok = eliminarFila(id);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Eliminada" : "Error al eliminar");
        return ok;
    }

    /**
     * Borra la orden. Si tenia repuestos cargados, primero los devuelve al
     * stock y borra esas lineas (si no, la clave foranea no deja borrar).
     * Todo en una transaccion.
     */
    private boolean eliminarFila(int id) {
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
    /**
     * Lee los controles del formulario, valida, decide si es alta o modificacion,
     * guarda y muestra el mensaje. Devuelve true si guardo, asi el formulario
     * sabe si refrescar la tabla.
     */
    public boolean guardar(javax.swing.JTextField txtId, javax.swing.JComboBox<String> cboCliente,
                           javax.swing.JComboBox<String> cboEquipo, javax.swing.JComboBox<String> cboEstado,
                           javax.swing.JTextField txtCosto, javax.swing.JTextField txtDiagnostico,
                           javax.swing.JTextField txtObservaciones) {
        if (cboCliente.getItemCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Primero registre un cliente");
            return false;
        }
        int idCli = idDelCombo(cboCliente);
        List<com.electroservisgmg.clases.Equipo> equipos = equiposDelCliente(idCli);
        int idxEq = cboEquipo.getSelectedIndex();
        int idEq = (idxEq >= 0 && idxEq < equipos.size()) ? equipos.get(idxEq).getIdEquipo() : -1;
        if (idCli < 0 || idEq < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione cliente y equipo");
            return false;
        }
        double costo = 0;
        if (!txtCosto.getText().trim().isEmpty()) {
            try {
                costo = UiTheme.parseGs(txtCosto.getText().trim());
                if (costo < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                javax.swing.JOptionPane.showMessageDialog(null, "El costo debe ser un número válido (≥ 0)");
                return false;
            }
        }
        OrdenServicio o = new OrdenServicio();
        o.setIdCliente(idCli);
        o.setIdEquipo(idEq);
        o.setEstado(String.valueOf(cboEstado.getSelectedItem()));
        o.setDiagnostico(txtDiagnostico.getText().trim());
        o.setCostoReparacion(costo);
        o.setObservaciones(txtObservaciones.getText().trim());
        boolean ok;
        String msg;
        int idOrden = 0;
        try { idOrden = Integer.parseInt(txtId.getText().trim()); } catch (NumberFormatException ignored) { }
        if (idOrden <= 0) {
            o.setFechaIngreso(new java.util.Date());
            com.electroservisgmg.clases.Usuario u = com.electroservisgmg.clases.Sesion.getUsuario();
            o.setIdUsuario(u == null ? 1 : u.getIdUsuario());
            ok = insertar(o);
            msg = ok ? "Orden creada correctamente" : "Error al crear la orden";
        } else {
            o.setIdOrden(idOrden);
            ok = modificar(o);
            msg = ok ? "Orden actualizada" : "Error al guardar";
        }
        javax.swing.JOptionPane.showMessageDialog(null, msg);
        return ok;
    }

    /**
     * Marca como ENTREGADA la orden seleccionada en la tabla (con fecha de hoy)
     * y muestra el mensaje. Devuelve true si actualizo.
     */
    public boolean retirar(javax.swing.JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione una orden de la tabla");
            return false;
        }
        if ("ENTREGADO".equals(String.valueOf(tabla.getValueAt(fila, 4)))) {
            javax.swing.JOptionPane.showMessageDialog(null, "Esta orden ya fue retirada");
            return false;
        }
        int id = ((Number) tabla.getValueAt(fila, 0)).intValue();
        boolean ok = marcarRetirado(id);
        javax.swing.JOptionPane.showMessageDialog(null,
                ok ? "Equipo retirado. Orden entregada." : "Error al actualizar");
        return ok;
    }

    /**
     * Llena la tabla de ordenes, opcionalmente filtradas por cliente, equipo o
     * estado.
     */
    public void mostrar(javax.swing.JTable tabla, String filtro) {
        java.util.Map<Integer, String> cli = new java.util.HashMap<>();
        for (com.electroservisgmg.clases.Cliente c : new ClienteMetodos().listar()) {
            cli.put(c.getIdCliente(), c.getNombre() + " " + c.getApellido());
        }
        java.util.Map<Integer, String> eq = new java.util.HashMap<>();
        for (com.electroservisgmg.clases.Equipo e : new EquipoMetodos().listar()) {
            eq.put(e.getIdEquipo(), e.getMarca() + " " + e.getModelo() + " (" + e.getNroSerie() + ")");
        }
        String f = filtro == null ? "" : filtro.toLowerCase();
        List<OrdenServicio> fil = new ArrayList<>();
        for (OrdenServicio o : listar()) {
            String nomCli = cli.getOrDefault(o.getIdCliente(), "");
            String nomEq = eq.getOrDefault(o.getIdEquipo(), "");
            if (f.isEmpty() || nomCli.toLowerCase().contains(f)
                    || nomEq.toLowerCase().contains(f)
                    || (o.getEstado() != null && o.getEstado().toLowerCase().contains(f))) fil.add(o);
        }
        String[] cols = {"ID", "Cliente", "Equipo", "Ingreso", "Estado", "Costo"};
        Object[][] data = new Object[fil.size()][6];
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy");
        for (int i = 0; i < fil.size(); i++) {
            OrdenServicio o = fil.get(i);
            data[i] = new Object[]{o.getIdOrden(),
                cli.getOrDefault(o.getIdCliente(), "ID " + o.getIdCliente()),
                eq.getOrDefault(o.getIdEquipo(), "ID " + o.getIdEquipo()),
                o.getFechaIngreso() == null ? "" : df.format(o.getFechaIngreso()),
                o.getEstado(), UiTheme.gs(o.getCostoReparacion())};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
    }

    /** Id guardado en el texto del combo ("id - nombre"). */
    private static int idDelCombo(javax.swing.JComboBox<String> combo) {
        Object sel = combo.getSelectedItem();
        if (sel == null) return -1;
        String s = sel.toString();
        int guion = s.indexOf(" -");
        if (guion <= 0) return -1;
        try { return Integer.parseInt(s.substring(0, guion).trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    /** Equipos del cliente, en el mismo orden en que se lleno el combo. */
    private static List<com.electroservisgmg.clases.Equipo> equiposDelCliente(int idCliente) {
        List<com.electroservisgmg.clases.Equipo> lista = new ArrayList<>();
        for (com.electroservisgmg.clases.Equipo e : new EquipoMetodos().listar()) {
            if (idCliente < 0 || e.getIdCliente() == idCliente) lista.add(e);
        }
        return lista;
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
