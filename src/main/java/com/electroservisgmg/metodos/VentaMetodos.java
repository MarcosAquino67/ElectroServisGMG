package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Caja;
import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.clases.DetalleVenta;
import com.electroservisgmg.clases.Empresa;
import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.clases.Sesion;
import com.electroservisgmg.clases.Usuario;
import com.electroservisgmg.clases.Venta;
import com.electroservisgmg.conexion.Conexion;
import com.electroservisgmg.formularios.UiTheme;
import com.electroservisgmg.reportes.ReportesMetodos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaMetodos {

    // Registra venta + detalles y descuenta stock automaticamente en una transaccion
    public boolean registrarVenta(Venta v, List<DetalleVenta> detalles) {
        String sqlVenta = "INSERT INTO venta(idCliente, idUsuario, fechaVenta, total, formaPago, condicionVenta, tipoTransaccion, tipoDocumento) VALUES(?,?,?,?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO detalleventa(idVenta, idProducto, cantidad, precioUnitario, descuento, iva, subtotal) VALUES(?,?,?,?,?,?,?)";
        String sqlStock = "UPDATE producto SET stock = stock - ? WHERE idProducto=? AND stock >= ?";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, v.getIdCliente());
                ps.setInt(2, v.getIdUsuario());
                ps.setTimestamp(3, new java.sql.Timestamp(v.getFechaVenta().getTime()));
                ps.setDouble(4, v.getTotal());
                ps.setString(5, v.getFormaPago());
                ps.setString(6, v.getCondicionVenta() == null ? "Contado" : v.getCondicionVenta());
                ps.setString(7, v.getTipoTransaccion() == null ? "Venta de mercadería" : v.getTipoTransaccion());
                ps.setString(8, v.getTipoDocumento() == null ? Venta.TICKET : v.getTipoDocumento());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) v.setIdVenta(rs.getInt(1)); }
            }
            try (PreparedStatement psDet = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                for (DetalleVenta d : detalles) {
                    psDet.setInt(1, v.getIdVenta());
                    psDet.setInt(2, d.getIdProducto());
                    psDet.setInt(3, d.getCantidad());
                    psDet.setDouble(4, d.getPrecioUnitario());
                    psDet.setDouble(5, d.getDescuento());
                    psDet.setDouble(6, d.getIva());
                    psDet.setDouble(7, d.getSubtotal());
                    psDet.addBatch();
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getIdProducto());
                    psStock.setInt(3, d.getCantidad());
                    psStock.addBatch();
                }
                psDet.executeBatch();
                int[] res = psStock.executeBatch();
                for (int r : res) if (r == 0) throw new SQLException("Stock insuficiente");
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("Error registrarVenta: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    // Compatibilidad con firma anterior
    public boolean registrarVenta(Venta v) { return registrarVenta(v, new ArrayList<>()); }

    /** Detalle (renglones) de una venta. */
    public List<DetalleVenta> listarDetalles(int idVenta) {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT idDetalleVenta, idProducto, cantidad, precioUnitario, descuento, iva, subtotal "
                + "FROM detalleventa WHERE idVenta=? ORDER BY idDetalleVenta";
        try (Connection con = Conexion.nuevaConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta();
                    d.setIdDetalleVenta(rs.getInt("idDetalleVenta"));
                    d.setIdVenta(idVenta);
                    d.setIdProducto(rs.getInt("idProducto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getDouble("precioUnitario"));
                    try { d.setDescuento(rs.getDouble("descuento")); } catch (SQLException ignored) {}
                    try { d.setIva(rs.getDouble("iva")); } catch (SQLException ignored) {}
                    d.setSubtotal(rs.getDouble("subtotal"));
                    lista.add(d);
                }
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

    public boolean descontarInventario(int idProducto, int cantidad) {
        ProductoMetodos pm = new ProductoMetodos();
        return pm.descontarStock(idProducto, cantidad);
    }

    /** Aviso extra del ultimo guardado (por ejemplo, el movimiento de caja). */
    public String ultimaNota = "";

    /**
     * Lee los controles del formulario, valida, registra la venta (y su ingreso
     * en la caja si esta abierta) y muestra el mensaje. Devuelve la venta
     * guardada, o null si no se pudo, asi el formulario sabe si refrescar.
     */
    public Venta guardar(int idVentaActual, com.toedter.calendar.JDateChooser dcFecha,
                         javax.swing.JComboBox<String> cboCliente, javax.swing.JComboBox<String> cboPago,
                         javax.swing.JComboBox<String> cboCondicion, List<DetalleVenta> detalle) {
        ultimaNota = "";
        if (idVentaActual != -1) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "La venta #" + idVentaActual + " ya está registrada.\n"
                    + "Presione '+ Nueva' para registrar otra venta.");
            return null;
        }
        java.util.Date fecha = dcFecha.getDate();
        if (fecha == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Elija la fecha de la venta");
            return null;
        }
        int idCli = idDelCombo(cboCliente);
        if (idCli < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un cliente");
            return null;
        }
        if (detalle.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Agregue al menos un producto al detalle");
            return null;
        }
        double total = 0;
        for (DetalleVenta d : detalle) total += d.getSubtotal();
        Venta v = new Venta();
        v.setIdCliente(idCli);
        Usuario u = Sesion.getUsuario();
        v.setIdUsuario(u != null ? u.getIdUsuario() : 1);
        v.setFechaVenta(fecha);
        v.setTotal(total);
        String formaPago = String.valueOf(cboPago.getSelectedItem());
        v.setFormaPago(formaPago);
        v.setCondicionVenta(String.valueOf(cboCondicion.getSelectedItem()));
        if (!registrarVenta(v, detalle)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No se pudo registrar la venta (verifique el stock disponible)");
            return null;
        }
        Caja caja = new CajaMetodos().cajaAbierta();
        if (caja != null) {
            new CajaMetodos().registrarMovimiento(caja.getIdCaja(), "INGRESO", total, "Venta #" + v.getIdVenta());
            ultimaNota = "Ingreso asentado en la caja #" + caja.getIdCaja() + ".";
        }
        // en efectivo el formulario abre la ventana del vuelto, asi que ahi no se avisa
        if (!"EFECTIVO".equalsIgnoreCase(formaPago)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Venta #" + v.getIdVenta() + " registrada (stock actualizado)."
                    + (ultimaNota.isEmpty() ? "" : "\n" + ultimaNota));
        }
        return v;
    }

    /**
     * Anula la venta elegida en la tabla: le devuelve el stock a los productos,
     * asienta la salida en la caja (si esta abierta) y muestra el mensaje.
     */
    public boolean anular(javax.swing.JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Seleccione en la lista la venta que quiere anular");
            return false;
        }
        int id = ((Number) tabla.getValueAt(fila, 0)).intValue();
        Venta v = buscar(id);
        if (v == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "No se encontró la venta #" + id);
            return false;
        }
        if (v.estaAnulada()) {
            javax.swing.JOptionPane.showMessageDialog(null, "La venta #" + id + " ya está anulada");
            return false;
        }
        if (!anular(id)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No se pudo anular la venta #" + id + " (¿ya estaba anulada?)");
            return false;
        }
        Caja caja = new CajaMetodos().cajaAbierta();
        if (caja != null) {
            new CajaMetodos().registrarMovimiento(caja.getIdCaja(), "EGRESO", v.getTotal(),
                    "Anulación venta #" + id);
        }
        javax.swing.JOptionPane.showMessageDialog(null,
                "Venta #" + id + " anulada.\nLos productos volvieron al stock."
                + (caja == null ? "" : "\nSalida de " + UiTheme.gs(v.getTotal())
                        + " asentada en la caja #" + caja.getIdCaja() + "."));
        return true;
    }

    /** Genera el ticket de la venta elegida en la tabla y dice donde quedo. */
    public void imprimirTicket(javax.swing.JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Seleccione una venta de la tabla para imprimir su ticket");
            return;
        }
        int id = ((Number) tabla.getValueAt(fila, 0)).intValue();
        Venta anulada = buscar(id);
        if (anulada != null && anulada.estaAnulada()) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "La venta #" + id + " está anulada: no se puede imprimir su ticket.");
            return;
        }
        try {
            java.io.File pdf = ReportesMetodos.ticketVenta(id);
            javax.swing.JOptionPane.showMessageDialog(null, "Ticket generado:\n" + pdf.getAbsolutePath());
            ReportesMetodos.abrir(pdf);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "No se pudo generar el ticket:\n" + e.getMessage());
        }
    }

    /**
     * Emite (o reimprime) la factura electronica de la venta elegida en la tabla,
     * muestra el numero y el CDC, y devuelve true si hay que refrescar la lista.
     */
    public boolean imprimirFactura(javax.swing.JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Seleccione una venta de la tabla para emitir su factura electrónica");
            return false;
        }
        int id = ((Number) tabla.getValueAt(fila, 0)).intValue();
        Venta previa = buscar(id);
        if (previa != null && previa.estaAnulada()) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "La venta #" + id + " está anulada: no se puede emitir su factura.");
            return false;
        }
        try {
            java.io.File pdf = ReportesMetodos.facturaElectronica(id);
            Venta v = buscar(id);
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Factura electrónica emitida.\n"
                    + "N°: " + (v == null ? "" : v.getNroFactura()) + "\n"
                    + "CDC: " + (v == null ? "" : v.getCdc()) + "\n\n" + pdf.getAbsolutePath());
            ReportesMetodos.abrir(pdf);
            return true;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No se pudo emitir la factura:\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Llena la tabla de ventas, opcionalmente filtradas por numero, cliente,
     * fecha, pago, documento o estado. Las ventas anuladas se ven en rojo.
     */
    public void mostrar(javax.swing.JTable tabla, String filtro) {
        java.util.Map<Integer, String> cli = new java.util.HashMap<>();
        for (Cliente c : new ClienteMetodos().listar()) {
            cli.put(c.getIdCliente(), c.getNombre() + " " + (c.getApellido() == null ? "" : c.getApellido()));
        }
        Empresa emp = new EmpresaMetodos().obtener();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String f = filtro == null ? "" : filtro.toLowerCase();
        List<Venta> fil = new ArrayList<>();
        for (Venta v : listar()) {
            String nom = cli.getOrDefault(v.getIdCliente(), "Cliente #" + v.getIdCliente());
            String fecha = v.getFechaVenta() == null ? "" : sdf.format(v.getFechaVenta());
            String pago = v.getFormaPago() == null ? "" : v.getFormaPago();
            String doc = documentoDe(v, emp);
            String estado = v.getEstado() == null ? "" : v.getEstado();
            if (f.isEmpty() || String.valueOf(v.getIdVenta()).contains(f)
                    || nom.toLowerCase().contains(f) || fecha.contains(f)
                    || pago.toLowerCase().contains(f) || doc.toLowerCase().contains(f)
                    || UiTheme.gs(v.getTotal()).toLowerCase().contains(f)
                    || estado.toLowerCase().contains(f)) fil.add(v);
        }
        String[] cols = {"ID", "Cliente", "Fecha", "Total", "Pago", "Documento", "Estado"};
        Object[][] data = new Object[fil.size()][7];
        for (int i = 0; i < fil.size(); i++) {
            Venta v = fil.get(i);
            data[i] = new Object[]{v.getIdVenta(),
                cli.getOrDefault(v.getIdCliente(), "Cliente #" + v.getIdCliente()),
                v.getFechaVenta() == null ? "" : sdf.format(v.getFechaVenta()),
                UiTheme.gs(v.getTotal()), v.getFormaPago(), documentoDe(v, emp), v.getEstado()};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int cc) { return false; }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(230);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(90);
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    boolean anulada = Venta.ANULADA.equalsIgnoreCase(String.valueOf(t.getValueAt(row, 6)));
                    c.setForeground(anulada ? UiTheme.DANGER : t.getForeground());
                    c.setFont(anulada ? t.getFont().deriveFont(java.awt.Font.ITALIC) : t.getFont());
                }
                return c;
            }
        });
    }

    /** Texto de la columna Documento: ticket o factura electronica con su numero. */
    private static String documentoDe(Venta v, Empresa emp) {
        if (v.getCdc() == null || v.getCdc().isEmpty()) return "Ticket";
        return "Factura " + emp.nroFacturaFormateado(v.getNroFactura());
    }

    /**
     * Llena la tabla del detalle con los renglones cargados y actualiza el total
     * que se muestra en la etiqueta.
     */
    public void mostrarDetalle(javax.swing.JTable tabla, List<DetalleVenta> detalle,
                               javax.swing.JLabel lblTotal) {
        java.util.Map<Integer, String> prod = new java.util.HashMap<>();
        for (Producto p : new ProductoMetodos().listar()) prod.put(p.getIdProducto(), p.getNombre());
        String[] cols = {"Producto", "Cant.", "P. Unit.", "IVA", "Subtotal"};
        Object[][] data = new Object[detalle.size()][5];
        double total = 0;
        for (int i = 0; i < detalle.size(); i++) {
            DetalleVenta d = detalle.get(i);
            data[i] = new Object[]{prod.getOrDefault(d.getIdProducto(), "Producto #" + d.getIdProducto()),
                d.getCantidad(), UiTheme.gs(d.getPrecioUnitario()), textoIva(d.getIva()),
                UiTheme.gs(d.getSubtotal())};
            total += d.getSubtotal();
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int cc) { return false; }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(170);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(110);
        if (lblTotal != null) lblTotal.setText("Total: " + UiTheme.gs(total));
    }

    /**
     * Lee el producto, la cantidad y el precio del formulario y lo agrega al
     * detalle (si ya estaba con el mismo precio, le suma la cantidad). Avisa por
     * pantalla y deja pintado el detalle. Devuelve true si agrego.
     */
    public boolean agregar(javax.swing.JComboBox<String> cboProducto, javax.swing.JTextField txtCantidad,
                           javax.swing.JTextField txtPrecioUnit, List<DetalleVenta> detalle,
                           javax.swing.JTable tablaDetalle, javax.swing.JLabel lblTotal) {
        List<Producto> productos = new ProductoMetodos().listar();
        int idx = cboProducto.getSelectedIndex();
        if (productos.isEmpty() || idx < 0 || idx >= productos.size()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un producto");
            return false;
        }
        Producto p = productos.get(idx);
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
        int disponible = p.getStock() - cantidadEnDetalle(detalle, p.getIdProducto());
        if (cant > disponible) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Stock insuficiente de \"" + p.getNombre() + "\".\n"
                    + "Disponible: " + Math.max(0, disponible) + " (stock total " + p.getStock() + ")");
            return false;
        }
        DetalleVenta dv = new DetalleVenta(0, 0, p.getIdProducto(), cant, pu, cant * pu);
        dv.setIva(p.getIva());
        // si el producto ya esta cargado con el mismo precio, se le suma la cantidad
        boolean sumado = false;
        for (DetalleVenta d : detalle) {
            if (d.getIdProducto() == p.getIdProducto() && Math.abs(d.getPrecioUnitario() - pu) < 0.01) {
                d.setCantidad(d.getCantidad() + cant);
                d.setSubtotal(d.getCantidad() * d.getPrecioUnitario());
                sumado = true;
                break;
            }
        }
        if (!sumado) detalle.add(dv);
        mostrarDetalle(tablaDetalle, detalle, lblTotal);
        return true;
    }

    /**
     * Saca del detalle el renglon seleccionado en la tabla y deja pintado el
     * resto. Devuelve true si quito.
     */
    public boolean quitar(javax.swing.JTable tablaDetalle, List<DetalleVenta> detalle,
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

    /** Texto de la tasa de IVA de un renglon. */
    private static String textoIva(double iva) {
        if (iva >= 9.99) return "10%";
        if (iva >= 4.99) return "5%";
        return "Exenta";
    }

    /** Cantidad de ese producto que ya esta cargada en el detalle. */
    private static int cantidadEnDetalle(List<DetalleVenta> detalle, int idProducto) {
        int t = 0;
        for (DetalleVenta d : detalle) if (d.getIdProducto() == idProducto) t += d.getCantidad();
        return t;
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

    public List<Venta> listar() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM venta ORDER BY fechaVenta DESC";
        try (Connection con = Conexion.nuevaConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Venta v = new Venta();
                v.setIdVenta(rs.getInt("idVenta"));
                v.setIdCliente(rs.getInt("idCliente"));
                v.setIdUsuario(rs.getInt("idUsuario"));
                v.setFechaVenta(rs.getTimestamp("fechaVenta"));
                v.setTotal(rs.getDouble("total"));
                v.setFormaPago(rs.getString("formaPago"));
                try { v.setCondicionVenta(rs.getString("condicionVenta")); } catch (SQLException ignored) {}
                try { v.setTipoTransaccion(rs.getString("tipoTransaccion")); } catch (SQLException ignored) {}
                try { v.setTipoDocumento(rs.getString("tipoDocumento")); } catch (SQLException ignored) {}
                try { v.setNroFactura(rs.getInt("nroFactura")); } catch (SQLException ignored) {}
                try { v.setCdc(rs.getString("cdc")); } catch (SQLException ignored) {}
                try { v.setEstado(rs.getString("estado")); } catch (SQLException ignored) {}
                lista.add(v);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

    /** Una venta por id (null si no existe). */
    public Venta buscar(int idVenta) {
        for (Venta v : listar()) if (v.getIdVenta() == idVenta) return v;
        return null;
    }

    /** Proximo numero de factura correlativo (respeta el numero inicial del timbrado). */
    public int siguienteNroFactura(Empresa e) {
        int max = 0;
        String sql = "SELECT COALESCE(MAX(nroFactura), 0) FROM venta WHERE nroFactura IS NOT NULL";
        try (Connection con = Conexion.nuevaConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) max = rs.getInt(1);
        } catch (SQLException ex) { System.err.println("siguienteNroFactura: " + ex.getMessage()); }
        int inicio = (e == null) ? 1 : e.getNroFacturaInicio();
        return Math.max(max + 1, inicio);
    }

    /**
     * Emite la Factura Electronica de una venta: le asigna el numero correlativo
     * y genera su CDC de 44 digitos (que queda guardado en la base).
     * Si la venta ya tenia factura, devuelve la misma sin volver a numerarla.
     */
    public Venta emitirFactura(int idVenta) throws Exception {
        Venta v = buscar(idVenta);
        if (v == null) throw new Exception("No existe la venta #" + idVenta);
        if (v.getCdc() != null && !v.getCdc().isEmpty()) return v;

        Empresa e = new EmpresaMetodos().obtener();
        int nro = siguienteNroFactura(e);
        String cdc = CdcUtil.generar(e, nro, v.getFechaVenta());

        String sql = "UPDATE venta SET tipoDocumento=?, nroFactura=?, cdc=? WHERE idVenta=?";
        try (Connection con = Conexion.nuevaConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Venta.FACTURA);
            ps.setInt(2, nro);
            ps.setString(3, cdc);
            ps.setInt(4, idVenta);
            if (ps.executeUpdate() == 0) throw new Exception("No se encontro la venta #" + idVenta);
        } catch (SQLException ex) {
            throw new Exception("No se pudo emitir la factura: " + ex.getMessage(), ex);
        }
        v.setTipoDocumento(Venta.FACTURA);
        v.setNroFactura(nro);
        v.setCdc(cdc);
        return v;
    }

    /**
     * Anula una venta: la marca como ANULADA y devuelve al stock todo lo que
     * habia descontado. No se borra la fila a proposito: asi el numero de
     * factura no se vuelve a usar.
     * Todo en una transaccion: si algo falla no queda a medias.
     */
    public boolean anular(int idVenta) {
        String sqlEstado = "UPDATE venta SET estado='ANULADA' "
                + "WHERE idVenta=? AND (estado IS NULL OR estado<>'ANULADA')";
        String sqlStock = "UPDATE producto p JOIN detalleventa d ON d.idProducto=p.idProducto "
                + "SET p.stock = p.stock + d.cantidad WHERE d.idVenta=?";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                ps.setInt(1, idVenta);
                if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                ps.setInt(1, idVenta);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("Error anular venta: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}
