package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Caja;
import com.electroservisgmg.clases.Compra;
import com.electroservisgmg.clases.DetalleCompra;
import com.electroservisgmg.clases.Sesion;
import com.electroservisgmg.clases.Usuario;
import com.electroservisgmg.conexion.Conexion;
import com.electroservisgmg.formularios.UiTheme;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraMetodos {

    // Registro de compra cabecera + detalles con incremento automatico de stock (transaccion)
    public boolean registrarCompra(Compra compra, List<DetalleCompra> detalles) {
        String sqlCompra = "INSERT INTO compra(idProveedor, idUsuario, fechaCompra, total, estado) VALUES(?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO detallecompra(idCompra, idProducto, cantidad, precioUnitario, subtotal) VALUES(?,?,?,?,?)";
        String sqlStock = "UPDATE producto SET stock = stock + ? WHERE idProducto=?";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, compra.getIdProveedor());
                ps.setInt(2, compra.getIdUsuario());
                ps.setDate(3, new java.sql.Date(compra.getFechaCompra().getTime()));
                ps.setDouble(4, compra.getTotal());
                ps.setString(5, compra.getEstado());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) compra.setIdCompra(rs.getInt(1));
                }
            }
            try (PreparedStatement psDet = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                for (DetalleCompra d : detalles) {
                    psDet.setInt(1, compra.getIdCompra());
                    psDet.setInt(2, d.getIdProducto());
                    psDet.setInt(3, d.getCantidad());
                    psDet.setDouble(4, d.getPrecioUnitario());
                    psDet.setDouble(5, d.getSubtotal());
                    psDet.addBatch();
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getIdProducto());
                    psStock.addBatch();
                }
                psDet.executeBatch();
                psStock.executeBatch();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("Error registrarCompra: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    /**
     * Lee los controles del formulario, valida, registra la compra (y su egreso
     * en la caja si esta abierta) y muestra el mensaje. Devuelve true si guardo,
     * asi el formulario sabe si refrescar la tabla.
     */
    public boolean guardar(int idCompraActual, com.toedter.calendar.JDateChooser dcFecha,
                           javax.swing.JComboBox<String> cboProveedor,
                           javax.swing.JComboBox<String> cboEstado,
                           List<DetalleCompra> detalle) {
        if (idCompraActual != -1) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "La compra #" + idCompraActual + " ya está registrada.\n"
                    + "Presione '+ Nueva' para registrar otra compra.");
            return false;
        }
        java.util.Date fecha = dcFecha.getDate();
        if (fecha == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Elija la fecha de la compra");
            return false;
        }
        int idProv = idDelCombo(cboProveedor);
        if (idProv < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un proveedor");
            return false;
        }
        if (detalle.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Agregue al menos un producto al detalle");
            return false;
        }
        double total = 0;
        for (DetalleCompra d : detalle) total += d.getSubtotal();
        Compra c = new Compra();
        c.setIdProveedor(idProv);
        Usuario u = Sesion.getUsuario();
        c.setIdUsuario(u != null ? u.getIdUsuario() : 1);
        c.setFechaCompra(fecha);
        c.setTotal(total);
        c.setEstado(String.valueOf(cboEstado.getSelectedItem()));
        if (!registrarCompra(c, detalle)) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al registrar la compra");
            return false;
        }
        String extra = "";
        if (!"ANULADA".equals(c.getEstado())) {
            Caja caja = new CajaMetodos().cajaAbierta();
            if (caja != null) {
                new CajaMetodos().registrarMovimiento(caja.getIdCaja(), "EGRESO", total, "Compra #" + c.getIdCompra());
                extra = "\nEgreso asentado en la caja #" + caja.getIdCaja() + ".";
            }
        }
        javax.swing.JOptionPane.showMessageDialog(null,
                "Compra #" + c.getIdCompra() + " registrada (stock actualizado)." + extra);
        return true;
    }

    /**
     * Anula la compra seleccionada en la tabla: devuelve el stock, asienta el
     * ingreso en la caja (si esta abierta) y muestra el mensaje.
     */
    public boolean anular(javax.swing.JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione una compra de la tabla");
            return false;
        }
        int id = ((Number) tabla.getValueAt(fila, 0)).intValue();
        if ("ANULADA".equals(String.valueOf(tabla.getValueAt(fila, 4)))) {
            javax.swing.JOptionPane.showMessageDialog(null, "La compra #" + id + " ya está anulada");
            return false;
        }
        if (!anular(id)) {
            javax.swing.JOptionPane.showMessageDialog(null, "No se pudo anular");
            return false;
        }
        for (Compra c : listar()) {
            if (c.getIdCompra() == id) {
                Caja caja = new CajaMetodos().cajaAbierta();
                if (caja != null) {
                    new CajaMetodos().registrarMovimiento(caja.getIdCaja(), "INGRESO", c.getTotal(),
                            "Anulación compra #" + id);
                }
                break;
            }
        }
        javax.swing.JOptionPane.showMessageDialog(null, "Compra anulada (stock devuelto)");
        return true;
    }

    /**
     * Llena la tabla de compras, opcionalmente filtradas por proveedor, estado,
     * fecha o numero de compra.
     */
    public void mostrar(javax.swing.JTable tabla, String filtro) {
        java.util.Map<Integer, String> prov = new java.util.HashMap<>();
        for (com.electroservisgmg.clases.Proveedor p : new ProveedorMetodos().listar()) {
            prov.put(p.getIdProveedor(), p.getNombre());
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String f = filtro == null ? "" : filtro.toLowerCase();
        List<Compra> fil = new ArrayList<>();
        for (Compra c : listar()) {
            String nom = prov.getOrDefault(c.getIdProveedor(), "-");
            String fecha = c.getFechaCompra() == null ? "" : sdf.format(c.getFechaCompra());
            if (f.isEmpty() || String.valueOf(c.getIdCompra()).contains(f)
                    || nom.toLowerCase().contains(f)
                    || (c.getEstado() != null && c.getEstado().toLowerCase().contains(f))
                    || fecha.contains(f)) fil.add(c);
        }
        String[] cols = {"ID", "Proveedor", "Fecha", "Total", "Estado"};
        Object[][] data = new Object[fil.size()][5];
        for (int i = 0; i < fil.size(); i++) {
            Compra c = fil.get(i);
            data[i] = new Object[]{c.getIdCompra(), prov.getOrDefault(c.getIdProveedor(), "-"),
                c.getFechaCompra() == null ? "" : sdf.format(c.getFechaCompra()),
                UiTheme.gs(c.getTotal()), c.getEstado()};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int cc) { return false; }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(260);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);
    }

    /**
     * Llena la tabla del detalle con los renglones cargados y actualiza el total
     * que se muestra en la etiqueta.
     */
    public void mostrarDetalle(javax.swing.JTable tabla, List<DetalleCompra> detalle,
                               javax.swing.JLabel lblTotal) {
        java.util.Map<Integer, String> prod = new java.util.HashMap<>();
        for (com.electroservisgmg.clases.Producto p : new ProductoMetodos().listar()) {
            prod.put(p.getIdProducto(), p.getNombre());
        }
        String[] cols = {"Producto", "Cant.", "P. Unit.", "Subtotal"};
        Object[][] data = new Object[detalle.size()][4];
        double total = 0;
        for (int i = 0; i < detalle.size(); i++) {
            DetalleCompra d = detalle.get(i);
            data[i] = new Object[]{prod.getOrDefault(d.getIdProducto(), "Producto #" + d.getIdProducto()),
                d.getCantidad(), UiTheme.gs(d.getPrecioUnitario()), UiTheme.gs(d.getSubtotal())};
            total += d.getSubtotal();
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int cc) { return false; }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(110);
        if (lblTotal != null) lblTotal.setText("Total: " + UiTheme.gs(total));
    }

    /**
     * Lee el producto, la cantidad y el precio del formulario y lo agrega al
     * detalle (si ya estaba con el mismo precio, le suma la cantidad). Muestra
     * el mensaje y deja pintado el detalle. Devuelve true si agrego.
     */
    public boolean agregar(javax.swing.JComboBox<String> cboProducto, javax.swing.JTextField txtCantidad,
                           javax.swing.JTextField txtPrecioUnit, List<DetalleCompra> detalle,
                           javax.swing.JTable tablaDetalle, javax.swing.JLabel lblTotal) {
        int idProd = idDelCombo(cboProducto);
        if (idProd < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un producto");
            return false;
        }
        int cant;
        double pu;
        try {
            cant = UiTheme.parseGsInt(txtCantidad.getText().trim());
            if (cant < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "La cantidad debe ser un número ≥ 1");
            return false;
        }
        try {
            pu = UiTheme.parseGs(txtPrecioUnit.getText().trim());
            if (pu < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "El precio unitario debe ser un número válido (≥ 0)");
            return false;
        }
        // si el producto ya esta cargado con el mismo precio, se le suma la cantidad
        boolean sumado = false;
        for (DetalleCompra d : detalle) {
            if (d.getIdProducto() == idProd && Math.abs(d.getPrecioUnitario() - pu) < 0.01) {
                d.setCantidad(d.getCantidad() + cant);
                d.setSubtotal(d.getCantidad() * d.getPrecioUnitario());
                sumado = true;
                break;
            }
        }
        if (!sumado) detalle.add(new DetalleCompra(0, 0, idProd, cant, pu, cant * pu));
        mostrarDetalle(tablaDetalle, detalle, lblTotal);
        return true;
    }

    /**
     * Saca del detalle el renglon seleccionado en la tabla y deja pintado el
     * resto. Devuelve true si quito.
     */
    public boolean quitar(javax.swing.JTable tablaDetalle, List<DetalleCompra> detalle,
                          javax.swing.JLabel lblTotal) {
        int fila = tablaDetalle.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione una fila del detalle");
            return false;
        }
        detalle.remove(fila);
        mostrarDetalle(tablaDetalle, detalle, lblTotal);
        return true;
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

    public List<Compra> listar() {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compra ORDER BY fechaCompra DESC";
        try (Connection con = Conexion.nuevaConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Compra c = new Compra();
                c.setIdCompra(rs.getInt("idCompra"));
                c.setIdProveedor(rs.getInt("idProveedor"));
                c.setIdUsuario(rs.getInt("idUsuario"));
                c.setFechaCompra(rs.getDate("fechaCompra"));
                c.setTotal(rs.getDouble("total"));
                c.setEstado(rs.getString("estado"));
                lista.add(c);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

    /** Detalle (renglones) de una compra. */
    public List<DetalleCompra> listarDetalles(int idCompra) {
        List<DetalleCompra> lista = new ArrayList<>();
        String sql = "SELECT idDetalleCompra, idProducto, cantidad, precioUnitario, subtotal "
                + "FROM detallecompra WHERE idCompra=? ORDER BY idDetalleCompra";
        try (Connection con = Conexion.nuevaConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetalleCompra(
                            rs.getInt("idDetalleCompra"), idCompra,
                            rs.getInt("idProducto"), rs.getInt("cantidad"),
                            rs.getDouble("precioUnitario"), rs.getDouble("subtotal")));
                }
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

    /**
     * Anula la compra y devuelve al stock lo que habia ingresado (sin bajar de 0).
     * Todo en una transaccion: si algo falla no queda a medias.
     */
    public boolean anular(int idCompra) {
        String sqlEstado = "UPDATE compra SET estado='ANULADA' WHERE idCompra=? AND estado<>'ANULADA'";
        String sqlStock = "UPDATE producto p JOIN detallecompra d ON d.idProducto=p.idProducto "
                + "SET p.stock = GREATEST(0, p.stock - d.cantidad) WHERE d.idCompra=?";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                ps.setInt(1, idCompra);
                if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                ps.setInt(1, idCompra);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("Error anular compra: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}
