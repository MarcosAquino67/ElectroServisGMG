/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Caja;
import com.electroservisgmg.clases.Compra;
import com.electroservisgmg.clases.DetalleCompra;
import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.clases.Proveedor;
import com.electroservisgmg.clases.Sesion;
import com.electroservisgmg.clases.Usuario;
import com.electroservisgmg.metodos.CajaMetodos;
import com.electroservisgmg.metodos.CompraMetodos;
import com.electroservisgmg.metodos.ProductoMetodos;
import com.electroservisgmg.metodos.ProveedorMetodos;

/**
 *
 * @author Aurelio
 */
public class FormCompras extends javax.swing.JPanel {

    private java.util.List<Proveedor> listaProveedores = new java.util.ArrayList<>();
    private java.util.List<Producto> listaProductos = new java.util.ArrayList<>();
    private final java.util.List<DetalleCompra> detalle = new java.util.ArrayList<>();
    private com.toedter.calendar.JDateChooser dcFecha;
    /** -1 = compra nueva; otro valor = compra ya registrada (solo vista). */
    private int idCompraActual = -1;

    /** Campo donde escribe el lector de codigos de barras (se crea en runtime). */
    private javax.swing.JTextField txtCodigoBarras;

    /**
     * Creates new form FormCompras
     */
    public FormCompras() {
        initComponents();
        txtBuscar.setText("");
        crearDateChooser();
        crearCampoCodigoBarras();
        UiTheme.styleTable(tblCompras);
        UiTheme.styleTable(tblDetalle);
        UiTheme.miles(txtCantidad);
        UiTheme.miles(txtPrecioUnit, true);
        cargarProveedores();
        cargarProductos();
        cargarEstados();
        cargarTabla("");
        pintarDetalle();
        btnBuscar.addActionListener(e -> buscar());
        btnNuevo.addActionListener(e -> limpiar());
        btnAnular.addActionListener(e -> anular());
        btnAgregar.addActionListener(e -> agregar());
        btnQuitar.addActionListener(e -> quitarFila());
        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
        txtCodigoBarras.addActionListener(e -> agregarPorCodigo());
        txtBuscar.addActionListener(e -> buscar());
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
        });
        tblCompras.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) llenarDesdeFila();
        });
    }

    // ---- JDateChooser (fuera del GEN: reemplaza al txtFecha placeholder) ----
    private void crearDateChooser() {
        panelForm.remove(txtFecha);
        dcFecha = new com.toedter.calendar.JDateChooser();
        dcFecha.setDateFormatString("dd/MM/yyyy");
        dcFecha.setDate(new java.util.Date());
        dcFecha.setFont(new java.awt.Font("Segoe UI", 0, 13));
        dcFecha.setBackground(new java.awt.Color(15, 23, 42));
        panelForm.add(dcFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 40, 140, 35));
        javax.swing.JTextField ed = (javax.swing.JTextField) dcFecha.getDateEditor().getUiComponent();
        ed.setBackground(new java.awt.Color(15, 23, 42));
        ed.setForeground(new java.awt.Color(243, 244, 246));
        ed.setCaretColor(new java.awt.Color(243, 244, 246));
        ed.setFont(new java.awt.Font("Segoe UI", 0, 13));
        estilizarCalendario(dcFecha.getJCalendar());
    }

    /** Oscurece los paneles blancos del calendario (encabezado de días y semana). */
    private void estilizarCalendario(java.awt.Container c) {
        for (java.awt.Component ch : c.getComponents()) {
            if (ch instanceof javax.swing.JComponent jc) {
                java.awt.Color bg = jc.getBackground();
                if (bg != null && bg.getRed() > 235 && bg.getGreen() > 235 && bg.getBlue() > 235) {
                    jc.setBackground(new java.awt.Color(30, 41, 59));
                    if (ch instanceof javax.swing.JLabel l) {
                        java.awt.Color fg = l.getForeground();
                        if (fg != null && fg.getBlue() > 120 && fg.getRed() < 130) {
                            l.setForeground(new java.awt.Color(56, 189, 248));
                        }
                    }
                }
            }
            if (ch instanceof java.awt.Container cc) estilizarCalendario(cc);
        }
    }

    // ---- lector de codigos de barras ----

    /**
     * Campo para el lector de codigos de barras (la pistola USB escribe como
     * un teclado y manda Enter). Se agrega en runtime para no tocar el diseno
     * del .form.
     */
    private void crearCampoCodigoBarras() {
        javax.swing.JLabel lbl = new javax.swing.JLabel("Código de barras (escaneá acá)");
        lbl.setFont(new java.awt.Font("Segoe UI", 0, 11));
        lbl.setForeground(new java.awt.Color(156, 163, 175));
        panelNorte.add(lbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(866, 52, 200, 18));

        txtCodigoBarras = new javax.swing.JTextField();
        txtCodigoBarras.setBackground(new java.awt.Color(15, 23, 42));
        txtCodigoBarras.setFont(new java.awt.Font("Segoe UI", 1, 13));
        txtCodigoBarras.setForeground(new java.awt.Color(56, 189, 248));
        txtCodigoBarras.setCaretColor(new java.awt.Color(243, 244, 246));
        txtCodigoBarras.setToolTipText("Escaneá el código de barras del producto y se agrega al detalle");
        panelNorte.add(txtCodigoBarras, new org.netbeans.lib.awtextra.AbsoluteConstraints(866, 75, 200, 35));

        txtCodigoBarras.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && txtCodigoBarras.isShowing()) {
                txtCodigoBarras.requestFocusInWindow();
            }
        });
    }

    /**
     * Agrega al detalle el producto del codigo escaneado (por codigo de barras
     * o por codigo interno). En compras el precio que se propone es el de costo.
     */
    private void agregarPorCodigo() {
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isEmpty()) return;

        txtCodigoBarras.setText("");
        if (idCompraActual != -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Estás viendo una compra ya registrada.\nTocá \"Nuevo\" para cargar otra compra.");
            return;
        }
        Producto p = new ProductoMetodos().buscarPorCodigo(codigo);
        if (p == null) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            javax.swing.JOptionPane.showMessageDialog(this, "No hay ningún producto con el código:\n" + codigo);
            txtCodigoBarras.requestFocusInWindow();
            return;
        }
        seleccionarProducto(p.getIdProducto());
        txtCantidad.setText("1");
        txtPrecioUnit.setText(String.valueOf((long) p.getPrecioCompra()));
        agregar();
        txtCodigoBarras.requestFocusInWindow();
    }

    /** Ubica el combo de productos en ese id. */
    private void seleccionarProducto(int idProducto) {
        for (int i = 0; i < listaProductos.size(); i++) {
            if (listaProductos.get(i).getIdProducto() == idProducto) {
                cboProducto.setSelectedIndex(i);
                return;
            }
        }
    }

    // ---- combos ----
    private void cargarProveedores() {
        listaProveedores = new ProveedorMetodos().listar();
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        for (Proveedor p : listaProveedores) m.addElement(p.getIdProveedor() + " - " + p.getNombre());
        cboProveedor.setModel(m);
    }

    private void cargarProductos() {
        listaProductos = new ProductoMetodos().listar();
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        for (Producto p : listaProductos) m.addElement(p.getIdProducto() + " - " + p.getNombre());
        cboProducto.setModel(m);
    }

    private void cargarEstados() {
        cboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"COMPLETADA", "PENDIENTE", "ANULADA"}));
    }

    private int idProveedorSeleccionado() {
        int i = cboProveedor.getSelectedIndex();
        if (i < 0 || i >= listaProveedores.size()) return -1;
        return listaProveedores.get(i).getIdProveedor();
    }

    private int idProductoSeleccionado() {
        int i = cboProducto.getSelectedIndex();
        if (i < 0 || i >= listaProductos.size()) return -1;
        return listaProductos.get(i).getIdProducto();
    }

    private String nombreProveedor(int id) {
        for (Proveedor p : listaProveedores) if (p.getIdProveedor() == id) return p.getNombre();
        return "-";
    }

    private String nombreProducto(int id) {
        for (Producto p : listaProductos) if (p.getIdProducto() == id) return p.getNombre();
        return "Producto #" + id;
    }

    // ---- tabla de compras ----
    private void cargarTabla(String filtro) {
        java.util.List<Compra> lista = new CompraMetodos().listar();
        String f = filtro == null ? "" : filtro.toLowerCase();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.util.List<Compra> fil = new java.util.ArrayList<>();
        for (Compra c : lista) {
            String prov = nombreProveedor(c.getIdProveedor());
            String fecha = c.getFechaCompra() == null ? "" : sdf.format(c.getFechaCompra());
            if (f.isEmpty() || String.valueOf(c.getIdCompra()).contains(f)
                    || prov.toLowerCase().contains(f)
                    || (c.getEstado() != null && c.getEstado().toLowerCase().contains(f))
                    || fecha.contains(f)) fil.add(c);
        }
        String[] cols = {"ID", "Proveedor", "Fecha", "Total", "Estado"};
        Object[][] data = new Object[fil.size()][5];
        for (int i = 0; i < fil.size(); i++) {
            Compra c = fil.get(i);
            data[i] = new Object[]{c.getIdCompra(), nombreProveedor(c.getIdProveedor()),
                c.getFechaCompra() == null ? "" : sdf.format(c.getFechaCompra()),
                UiTheme.gs(c.getTotal()), c.getEstado()};
        }
        tblCompras.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int cc) { return false; }
        });
        tblCompras.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblCompras.getColumnModel().getColumn(1).setPreferredWidth(260);
        tblCompras.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblCompras.getColumnModel().getColumn(3).setPreferredWidth(130);
        tblCompras.getColumnModel().getColumn(4).setPreferredWidth(120);
        UiTheme.styleTable(tblCompras);
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    // ---- detalle en memoria ----
    private void pintarDetalle() {
        String[] cols = {"Producto", "Cant.", "P. Unit.", "Subtotal"};
        Object[][] data = new Object[detalle.size()][4];
        for (int i = 0; i < detalle.size(); i++) {
            DetalleCompra d = detalle.get(i);
            data[i] = new Object[]{nombreProducto(d.getIdProducto()), d.getCantidad(),
                UiTheme.gs(d.getPrecioUnitario()), UiTheme.gs(d.getSubtotal())};
        }
        tblDetalle.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int cc) { return false; }
        });
        tblDetalle.getColumnModel().getColumn(0).setPreferredWidth(180);
        tblDetalle.getColumnModel().getColumn(1).setPreferredWidth(50);
        tblDetalle.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblDetalle.getColumnModel().getColumn(3).setPreferredWidth(110);
        UiTheme.styleTable(tblDetalle);
        actualizarTotal();
    }

    private void actualizarTotal() {
        double t = 0;
        for (DetalleCompra d : detalle) t += d.getSubtotal();
        lblTotal.setText("Total: " + UiTheme.gs(t));
    }

    private void agregar() {
        int idProd = idProductoSeleccionado();
        if (idProd < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return;
        }
        int cant;
        double pu;
        try {
            cant = UiTheme.parseGsInt(txtCantidad.getText().trim());
            if (cant < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "La cantidad debe ser un número ≥ 1");
            return;
        }
        try {
            pu = UiTheme.parseGs(txtPrecioUnit.getText().trim());
            if (pu < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "El precio unitario debe ser un número válido (≥ 0)");
            return;
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
        pintarDetalle();
        txtCantidad.setText("");
        txtPrecioUnit.setText("");
        cboProducto.requestFocus();
    }

    private void quitarFila() {
        int fila = tblDetalle.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione una fila del detalle");
            return;
        }
        detalle.remove(fila);
        pintarDetalle();
    }

    // ---- seleccionar compra de la tabla (vista) ----
    private void llenarDesdeFila() {
        int fila = tblCompras.getSelectedRow();
        if (fila < 0) return;
        int id = ((Number) tblCompras.getValueAt(fila, 0)).intValue();
        for (Compra c : new CompraMetodos().listar()) {
            if (c.getIdCompra() == id) {
                idCompraActual = id;
                if (c.getIdProveedor() > 0) {
                    for (int i = 0; i < listaProveedores.size(); i++) {
                        if (listaProveedores.get(i).getIdProveedor() == c.getIdProveedor()) {
                            cboProveedor.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                if (c.getFechaCompra() != null) dcFecha.setDate(c.getFechaCompra());
                if (c.getEstado() != null) cboEstado.setSelectedItem(c.getEstado());
                break;
            }
        }
        detalle.clear();
        detalle.addAll(new CompraMetodos().listarDetalles(id));
        pintarDetalle();
    }

    // ---- acciones ----
    private void guardar() {
        if (idCompraActual != -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "La compra #" + idCompraActual + " ya está registrada.\n"
                    + "Presione '+ Nueva' para registrar otra compra.");
            return;
        }
        if (dcFecha.getDate() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Elija la fecha de la compra");
            return;
        }
        int idProv = idProveedorSeleccionado();
        if (idProv < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un proveedor");
            return;
        }
        if (detalle.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Agregue al menos un producto al detalle");
            return;
        }
        Compra c = new Compra();
        c.setIdProveedor(idProv);
        Usuario u = Sesion.getUsuario();
        c.setIdUsuario(u != null ? u.getIdUsuario() : 1);
        c.setFechaCompra(dcFecha.getDate());
        double total = 0;
        for (DetalleCompra d : detalle) total += d.getSubtotal();
        c.setTotal(total);
        c.setEstado(String.valueOf(cboEstado.getSelectedItem()));
        boolean ok = new CompraMetodos().registrarCompra(c, detalle);
        if (!ok) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al registrar la compra");
            return;
        }
        String extra = "";
        if (!"ANULADA".equals(c.getEstado())) {
            CajaMetodos cm = new CajaMetodos();
            Caja caja = cm.cajaAbierta();
            if (caja != null) {
                cm.registrarMovimiento(caja.getIdCaja(), "EGRESO", total, "Compra #" + c.getIdCompra());
                extra = "\nEgreso asentado en la caja #" + caja.getIdCaja() + ".";
            }
        }
        javax.swing.JOptionPane.showMessageDialog(this,
                "Compra #" + c.getIdCompra() + " registrada (stock actualizado)." + extra);
        cargarTabla(txtBuscar.getText().trim());
        limpiar();
    }

    private void anular() {
        int fila = tblCompras.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione una compra de la tabla");
            return;
        }
        int id = ((Number) tblCompras.getValueAt(fila, 0)).intValue();
        String estado = String.valueOf(tblCompras.getValueAt(fila, 4));
        if ("ANULADA".equals(estado)) {
            javax.swing.JOptionPane.showMessageDialog(this, "La compra #" + id + " ya está anulada");
            return;
        }
        int conf = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Anular la compra #" + id + "?", "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (conf != javax.swing.JOptionPane.YES_OPTION) return;
        boolean ok = new CompraMetodos().anular(id);
        if (ok) {
            for (Compra c : new CompraMetodos().listar()) {
                if (c.getIdCompra() == id) {
                    CajaMetodos cm = new CajaMetodos();
                    Caja caja = cm.cajaAbierta();
                    if (caja != null) {
                        cm.registrarMovimiento(caja.getIdCaja(), "INGRESO", c.getTotal(),
                                "Anulación compra #" + id);
                    }
                    break;
                }
            }
        }
        javax.swing.JOptionPane.showMessageDialog(this,
                ok ? "Compra anulada (stock devuelto)" : "No se pudo anular");
        if (ok) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    private void limpiar() {
        idCompraActual = -1;
        if (cboProveedor.getItemCount() > 0) cboProveedor.setSelectedIndex(0);
        if (cboProducto.getItemCount() > 0) cboProducto.setSelectedIndex(0);
        cboEstado.setSelectedItem("COMPLETADA");
        dcFecha.setDate(new java.util.Date());
        txtCantidad.setText("");
        txtPrecioUnit.setText("");
        txtCodigoBarras.setText("");
        detalle.clear();
        pintarDetalle();
        tblCompras.clearSelection();
        txtCodigoBarras.requestFocusInWindow();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        panelNorte = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSub = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnAnular = new javax.swing.JButton();
        panelTabla = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCompras = new javax.swing.JTable();
        panelForm = new javax.swing.JPanel();
        lblProveedor = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        cboProveedor = new javax.swing.JComboBox<>();
        cboEstado = new javax.swing.JComboBox<>();
        txtFecha = new javax.swing.JTextField();
        lblProducto = new javax.swing.JLabel();
        cboProducto = new javax.swing.JComboBox<>();
        lblCantidad = new javax.swing.JLabel();
        lblPrecioUnit = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        txtPrecioUnit = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        scrollDetalle = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(18, 24, 36));
        jPanel1.setPreferredSize(new java.awt.Dimension(1084, 700));
        jPanel1.setLayout(new java.awt.BorderLayout());

        panelNorte.setBackground(new java.awt.Color(18, 24, 36));
        panelNorte.setPreferredSize(new java.awt.Dimension(0, 130));
        panelNorte.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("COMPRAS");
        panelNorte.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 30));

        lblSub.setForeground(new java.awt.Color(156, 163, 175));
        lblSub.setText("Registro de compras a proveedores");
        panelNorte.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 500, 22));

        txtBuscar.setBackground(new java.awt.Color(15, 23, 42));
        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(243, 244, 246));
        panelNorte.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 75, 250, 35));

        btnBuscar.setBackground(new java.awt.Color(51, 65, 85));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(243, 244, 246));
        btnBuscar.setText("Buscar");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        panelNorte.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 75, 110, 35));

        btnNuevo.setBackground(new java.awt.Color(56, 189, 248));
        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(15, 23, 42));
        btnNuevo.setText("+ Nueva");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        panelNorte.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(385, 75, 110, 35));

        btnAnular.setBackground(new java.awt.Color(239, 68, 68));
        btnAnular.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAnular.setForeground(new java.awt.Color(255, 255, 255));
        btnAnular.setText("Anular");
        btnAnular.setBorderPainted(false);
        btnAnular.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAnular.setFocusPainted(false);
        panelNorte.add(btnAnular, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 75, 110, 35));

        jPanel1.add(panelNorte, java.awt.BorderLayout.NORTH);

        panelTabla.setBackground(new java.awt.Color(18, 24, 36));
        panelTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));
        panelTabla.setLayout(new java.awt.BorderLayout());

        tblCompras.setBackground(new java.awt.Color(30, 41, 59));
        tblCompras.setForeground(new java.awt.Color(243, 244, 246));
        tblCompras.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5"
            }
        ));
        tblCompras.setFillsViewportHeight(true);
        tblCompras.setGridColor(new java.awt.Color(51, 65, 85));
        tblCompras.setRowHeight(32);
        tblCompras.setSelectionBackground(new java.awt.Color(2, 132, 199));
        jScrollPane1.setViewportView(tblCompras);

        panelTabla.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(panelTabla, java.awt.BorderLayout.CENTER);

        panelForm.setBackground(new java.awt.Color(30, 41, 59));
        panelForm.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        panelForm.setPreferredSize(new java.awt.Dimension(0, 275));
        panelForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblProveedor.setForeground(new java.awt.Color(156, 163, 175));
        lblProveedor.setText("Proveedor");
        panelForm.add(lblProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 15, 300, 25));

        lblFecha.setForeground(new java.awt.Color(156, 163, 175));
        lblFecha.setText("Fecha");
        panelForm.add(lblFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 15, 140, 25));

        lblEstado.setForeground(new java.awt.Color(156, 163, 175));
        lblEstado.setText("Estado");
        panelForm.add(lblEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 15, 130, 25));

        cboProveedor.setBackground(new java.awt.Color(15, 23, 42));
        cboProveedor.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboProveedor.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(cboProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 40, 140, 35));

        cboEstado.setBackground(new java.awt.Color(15, 23, 42));
        cboEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboEstado.setForeground(new java.awt.Color(243, 244, 246));
        cboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        panelForm.add(cboEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 40, 130, 35));

        txtFecha.setBackground(new java.awt.Color(15, 23, 42));
        txtFecha.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtFecha.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 40, 140, 35));

        lblProducto.setForeground(new java.awt.Color(156, 163, 175));
        lblProducto.setText("Producto");
        panelForm.add(lblProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 90, 300, 25));

        cboProducto.setBackground(new java.awt.Color(15, 23, 42));
        cboProducto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboProducto.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(cboProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 115, 300, 35));

        lblCantidad.setForeground(new java.awt.Color(156, 163, 175));
        lblCantidad.setText("Cantidad");
        panelForm.add(lblCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 90, 90, 25));

        lblPrecioUnit.setForeground(new java.awt.Color(156, 163, 175));
        lblPrecioUnit.setText("Precio Unit.");
        panelForm.add(lblPrecioUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(445, 90, 140, 25));

        txtCantidad.setBackground(new java.awt.Color(15, 23, 42));
        txtCantidad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCantidad.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 115, 90, 35));

        txtPrecioUnit.setBackground(new java.awt.Color(15, 23, 42));
        txtPrecioUnit.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtPrecioUnit.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtPrecioUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(445, 115, 110, 35));

        btnAgregar.setBackground(new java.awt.Color(22, 163, 74));
        btnAgregar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregar.setText("+ Agregar");
        btnAgregar.setBorderPainted(false);
        btnAgregar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregar.setFocusPainted(false);
        panelForm.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 115, 110, 35));

        btnQuitar.setBackground(new java.awt.Color(51, 65, 85));
        btnQuitar.setForeground(new java.awt.Color(243, 244, 246));
        btnQuitar.setText("Quitar fila");
        panelForm.add(btnQuitar, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 115, 110, 35));

        tblDetalle.setBackground(new java.awt.Color(30, 41, 59));
        tblDetalle.setForeground(new java.awt.Color(243, 244, 246));
        tblDetalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblDetalle.setFillsViewportHeight(true);
        tblDetalle.setGridColor(new java.awt.Color(51, 65, 85));
        tblDetalle.setRowHeight(28);
        tblDetalle.setSelectionBackground(new java.awt.Color(2, 132, 199));
        scrollDetalle.setViewportView(tblDetalle);

        panelForm.add(scrollDetalle, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 165, 600, 90));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(56, 189, 248));
        lblTotal.setText("Total: ₲ 0");
        panelForm.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(705, 170, 330, 35));

        btnGuardar.setBackground(new java.awt.Color(22, 163, 74));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        panelForm.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(705, 215, 150, 40));

        btnLimpiar.setBackground(new java.awt.Color(51, 65, 85));
        btnLimpiar.setForeground(new java.awt.Color(243, 244, 246));
        btnLimpiar.setText("Limpiar");
        panelForm.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(865, 215, 130, 40));

        jPanel1.add(panelForm, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1035, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnAnular;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JComboBox<String> cboEstado;
    private javax.swing.JComboBox<String> cboProducto;
    private javax.swing.JComboBox<String> cboProveedor;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblPrecioUnit;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelForm;
    private javax.swing.JPanel panelNorte;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scrollDetalle;
    private javax.swing.JTable tblCompras;
    private javax.swing.JTable tblDetalle;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtPrecioUnit;
    // End of variables declaration//GEN-END:variables
}
