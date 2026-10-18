/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.clases.DetalleVenta;
import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.clases.Venta;
import com.electroservisgmg.metodos.ClienteMetodos;
import com.electroservisgmg.metodos.ProductoMetodos;
import com.electroservisgmg.metodos.VentaMetodos;

import java.util.ArrayList;
import java.util.List;

/**
 * Registro de ventas a clientes. Descuenta stock y (si hay caja abierta)
 * deja el ingreso asentado en la caja.
 *
 * @author Aurelio
 */
public class FormVentas extends javax.swing.JPanel {

    private List<Cliente> listaClientes = new ArrayList<>();
    private List<Producto> listaProductos = new ArrayList<>();
    private final List<DetalleVenta> detalle = new ArrayList<>();
    private com.toedter.calendar.JDateChooser dcFecha;
    /** -1 = venta nueva; otro valor = venta ya registrada (solo vista). */
    private int idVentaActual = -1;

    /** Campo donde escribe el lector de codigos de barras (se crea en runtime). */
    private javax.swing.JTextField txtCodigoBarras;

    /**
     * Creates new form FormVentas
     */
    public FormVentas() {
        initComponents();
        crearDateChooser();
        crearCampoCodigoBarras();
        crearBotonVuelto();
        crearBotonAnular();
        UiTheme.styleTable(tblVentas);
        UiTheme.styleTable(tblDetalle);
        UiTheme.miles(txtCantidad);
        UiTheme.miles(txtPrecioUnit, true);
        cargarClientes();
        cargarProductos();
        cargarPagos();
        cargarCondiciones();
        cargarTabla("");
        pintarDetalle();
        mostrarFoto(null);
        // Los botones, el buscador y el combo de productos se conectan desde el
        // disenador (pestana Events -> actionPerformed). Aca quedan solo los
        // extras que el disenador no modela.
        txtCodigoBarras.addActionListener(e -> agregarPorCodigo());
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
        });
        tblVentas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) llenarDesdeFila();
        });
    }

    /** Cambia la foto y sugiere el precio al elegir otro producto. */
    private void productoCambiado() {
        Producto p = productoSeleccionado();
        mostrarFoto(p == null ? null : p.getFoto());
        if (p != null && idVentaActual == -1) {
            txtPrecioUnit.setText(String.valueOf((long) p.getPrecioVenta()));
        }
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
     * del .form. Es aparte de txtBuscar: ese filtra la tabla de ventas.
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

        // al abrir la pantalla el cursor ya queda listo para escanear
        txtCodigoBarras.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && txtCodigoBarras.isShowing()) {
                txtCodigoBarras.requestFocusInWindow();
            }
        });
    }

    /**
     * Agrega al detalle el producto del codigo escaneado (busca por codigo de
     * barras y, si no hay, por codigo interno). Reutiliza toda la validacion
     * de stock, IVA y precio del boton "+ Agregar".
     */
    private void agregarPorCodigo() {
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isEmpty()) return;

        txtCodigoBarras.setText("");
        if (idVentaActual != -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Estás viendo una venta ya registrada.\nTocá \"Nuevo\" para cargar otra venta.");
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
        txtPrecioUnit.setText(String.valueOf((long) p.getPrecioVenta()));
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

    // ---- vuelto ----

    /** Boton para abrir la ventana de vuelto a mano. */
    private void crearBotonVuelto() {
        javax.swing.JButton btn = new javax.swing.JButton("Vuelto");
        btn.setBackground(new java.awt.Color(51, 65, 85));
        btn.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btn.setForeground(new java.awt.Color(243, 244, 246));
        btn.setBorderPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn.setToolTipText("Calcula cuánto hay que devolverle al cliente");
        btn.addActionListener(e -> abrirVuelto());
        panelForm.add(btn, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 215, 72, 40));
    }

    /** Abre la ventana de vuelto con el total del detalle que se está cargando. */
    private void abrirVuelto() {
        if (detalle.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Cargá productos en el detalle (o elegí una venta de la lista)\n"
                    + "para calcular el vuelto.");
            return;
        }
        double t = 0;
        for (DetalleVenta d : detalle) t += d.getSubtotal();
        FormVuelto.mostrar(this, idVentaActual == -1 ? "Venta en curso" : "Venta #" + idVentaActual, t, "");
    }

    // ---- anular ----

    /** Boton para anular la venta elegida en la tabla. */
    private void crearBotonAnular() {
        javax.swing.JButton btn = new javax.swing.JButton("Anular");
        btn.setBackground(new java.awt.Color(153, 27, 27));
        btn.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btn.setForeground(new java.awt.Color(255, 255, 255));
        btn.setBorderPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn.setToolTipText("Anula la venta elegida en la lista y devuelve los productos al stock");
        btn.addActionListener(e -> anular());
        panelForm.add(btn, new org.netbeans.lib.awtextra.AbsoluteConstraints(998, 215, 82, 40));
    }

    /**
     * Anula la venta elegida en la lista: le devuelve el stock a los productos,
     * deja la venta marcada como ANULADA (no se borra, para no repetir el
     * numero de factura) y registra la salida del dinero en la caja.
     */
    private void anular() {
        int fila = tblVentas.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Seleccione en la lista la venta que quiere anular");
            return;
        }
        int id = ((Number) tblVentas.getValueAt(fila, 0)).intValue();
        int conf = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Anular la venta #" + id + "?\nLos productos vuelven al stock.",
                "Confirmar anulación",
                javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
        if (conf != javax.swing.JOptionPane.YES_OPTION) return;
        if (new VentaMetodos().anular(tblVentas)) {
            idVentaActual = -1;
            recargarProductos();
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    // ---- combos ----
    private void cargarClientes() {
        listaClientes = new ClienteMetodos().listar();
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        for (Cliente c : listaClientes) {
            m.addElement(c.getIdCliente() + " - " + c.getNombre() + " " + (c.getApellido() == null ? "" : c.getApellido()));
        }
        cboCliente.setModel(m);
    }

    private void cargarProductos() {
        listaProductos = new ProductoMetodos().listar();
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        for (Producto p : listaProductos) m.addElement(p.getNombre() + "  (stock: " + p.getStock() + ")");
        cboProducto.setModel(m);
    }

    private void cargarPagos() {
        cboPago.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"EFECTIVO", "TARJETA", "TRANSFERENCIA"}));
    }

    private void cargarCondiciones() {
        cboCondicion.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Contado", "Crédito"}));
    }

    private Producto productoSeleccionado() {
        int i = cboProducto.getSelectedIndex();
        if (i < 0 || i >= listaProductos.size()) return null;
        return listaProductos.get(i);
    }

    // ---- foto del producto ----
    private void mostrarFoto(String archivo) {
        lblPreview.setIcon(null);
        if (archivo == null || archivo.trim().isEmpty()) {
            lblPreview.setText("Sin foto");
            return;
        }
        java.io.File f = new java.io.File("imagenes_productos", archivo);
        if (!f.exists()) {
            lblPreview.setText("Foto no encontrada");
            return;
        }
        try {
            java.awt.Image img = new javax.swing.ImageIcon(f.getAbsolutePath()).getImage();
            int w = lblPreview.getWidth() > 4 ? lblPreview.getWidth() - 6 : 124;
            int h = lblPreview.getHeight() > 4 ? lblPreview.getHeight() - 6 : 139;
            int iw = img.getWidth(null), ih = img.getHeight(null);
            double esc = Math.min((double) w / iw, (double) h / ih);
            int nw = Math.max(1, (int) (iw * esc)), nh = Math.max(1, (int) (ih * esc));
            lblPreview.setText("");
            lblPreview.setIcon(new javax.swing.ImageIcon(
                    img.getScaledInstance(nw, nh, java.awt.Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            lblPreview.setIcon(null);
            lblPreview.setText("Error al cargar");
        }
    }

    // ---- tabla de ventas ----
    private void cargarTabla(String filtro) {
        new VentaMetodos().mostrar(tblVentas, filtro);
        UiTheme.styleTable(tblVentas);
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    // ---- detalle en memoria ----
    private void pintarDetalle() {
        new VentaMetodos().mostrarDetalle(tblDetalle, detalle, lblTotal);
        UiTheme.styleTable(tblDetalle);
    }

    private void agregar() {
        if (new VentaMetodos().agregar(cboProducto, txtCantidad, txtPrecioUnit, detalle, tblDetalle, lblTotal)) {
            txtCantidad.setText("");
            cboProducto.requestFocus();
        }
    }

    private void quitarFila() {
        new VentaMetodos().quitar(tblDetalle, detalle, lblTotal);
    }

    // ---- seleccionar venta de la tabla (vista) ----
    private void llenarDesdeFila() {
        int fila = tblVentas.getSelectedRow();
        if (fila < 0) return;
        int id = ((Number) tblVentas.getValueAt(fila, 0)).intValue();
        for (Venta v : new VentaMetodos().listar()) {
            if (v.getIdVenta() == id) {
                idVentaActual = id;
                if (v.getIdCliente() > 0) {
                    for (int i = 0; i < listaClientes.size(); i++) {
                        if (listaClientes.get(i).getIdCliente() == v.getIdCliente()) {
                            cboCliente.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                if (v.getFechaVenta() != null) dcFecha.setDate(v.getFechaVenta());
                if (v.getFormaPago() != null) cboPago.setSelectedItem(v.getFormaPago());
                if (v.getCondicionVenta() != null) cboCondicion.setSelectedItem(v.getCondicionVenta());
                break;
            }
        }
        detalle.clear();
        detalle.addAll(new VentaMetodos().listarDetalles(id));
        pintarDetalle();
        if (!detalle.isEmpty()) {
            Producto p = null;
            for (Producto x : listaProductos) if (x.getIdProducto() == detalle.get(0).getIdProducto()) { p = x; break; }
            mostrarFoto(p == null ? null : p.getFoto());
        }
    }

    // ---- acciones ----
    private void guardar() {
        VentaMetodos vm = new VentaMetodos();
        Venta v = vm.guardar(idVentaActual, dcFecha, cboCliente, cboPago, cboCondicion, detalle);
        if (v == null) return;
        recargarProductos();
        cargarTabla(txtBuscar.getText().trim());
        limpiar();
        // en efectivo se abre la ventana del vuelto (el aviso ya lo mostro la clase)
        if ("EFECTIVO".equalsIgnoreCase(v.getFormaPago())) {
            FormVuelto.mostrar(this, "Venta #" + v.getIdVenta() + " registrada", v.getTotal(), vm.ultimaNota);
        }
    }

    /** Refresca el combo de productos para ver el stock ya descontado. */
    private void recargarProductos() {
        int sel = cboProducto.getSelectedIndex();
        cargarProductos();
        if (sel >= 0 && sel < cboProducto.getItemCount()) cboProducto.setSelectedIndex(sel);
    }

    // ---- ticket ----
    private void imprimirTicket() {
        new VentaMetodos().imprimirTicket(tblVentas);
    }

    // ---- factura electronica ----
    private void imprimirFactura() {
        if (new VentaMetodos().imprimirFactura(tblVentas)) cargarTabla(txtBuscar.getText().trim());
    }

    private void limpiar() {
        idVentaActual = -1;
        if (cboCliente.getItemCount() > 0) cboCliente.setSelectedIndex(0);
        if (cboProducto.getItemCount() > 0) cboProducto.setSelectedIndex(0);
        cboPago.setSelectedItem("EFECTIVO");
        cboCondicion.setSelectedItem("Contado");
        dcFecha.setDate(new java.util.Date());
        txtCantidad.setText("");
        txtPrecioUnit.setText("");
        txtCodigoBarras.setText("");
        detalle.clear();
        pintarDetalle();
        tblVentas.clearSelection();
        mostrarFoto(productoSeleccionado() == null ? null : productoSeleccionado().getFoto());
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
        btnTicket = new javax.swing.JButton();
        btnFactura = new javax.swing.JButton();
        panelTabla = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVentas = new javax.swing.JTable();
        panelForm = new javax.swing.JPanel();
        lblCliente = new javax.swing.JLabel();
        cboCliente = new javax.swing.JComboBox<>();
        lblFecha = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();
        lblPago = new javax.swing.JLabel();
        cboPago = new javax.swing.JComboBox<>();
        lblCondicion = new javax.swing.JLabel();
        cboCondicion = new javax.swing.JComboBox<>();
        lblFoto = new javax.swing.JLabel();
        lblPreview = new javax.swing.JLabel();
        lblProducto = new javax.swing.JLabel();
        cboProducto = new javax.swing.JComboBox<>();
        lblCantidad = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        lblPrecioUnit = new javax.swing.JLabel();
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
        lblTitulo.setText("VENTAS");
        panelNorte.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 30));

        lblSub.setForeground(new java.awt.Color(156, 163, 175));
        lblSub.setText("Registro de ventas a clientes");
        panelNorte.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 500, 22));

        txtBuscar.setBackground(new java.awt.Color(15, 23, 42));
        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(243, 244, 246));
        txtBuscar.addActionListener(this::txtBuscarActionPerformed);
        panelNorte.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 75, 250, 35));

        btnBuscar.setBackground(new java.awt.Color(51, 65, 85));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(243, 244, 246));
        btnBuscar.setText("Buscar");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        panelNorte.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 75, 110, 35));

        btnNuevo.setBackground(new java.awt.Color(56, 189, 248));
        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(15, 23, 42));
        btnNuevo.setText("+ Nueva");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(this::btnNuevoActionPerformed);
        panelNorte.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 75, 110, 35));

        btnTicket.setBackground(new java.awt.Color(51, 65, 85));
        btnTicket.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnTicket.setForeground(new java.awt.Color(243, 244, 246));
        btnTicket.setText("Imprimir ticket");
        btnTicket.setBorderPainted(false);
        btnTicket.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTicket.setFocusPainted(false);
        btnTicket.addActionListener(this::btnTicketActionPerformed);
        panelNorte.add(btnTicket, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 75, 160, 35));

        btnFactura.setBackground(new java.awt.Color(34, 197, 94));
        btnFactura.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnFactura.setForeground(new java.awt.Color(255, 255, 255));
        btnFactura.setText("Factura electrónica");
        btnFactura.setBorderPainted(false);
        btnFactura.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFactura.setFocusPainted(false);
        btnFactura.addActionListener(this::btnFacturaActionPerformed);
        panelNorte.add(btnFactura, new org.netbeans.lib.awtextra.AbsoluteConstraints(675, 75, 180, 35));

        jPanel1.add(panelNorte, java.awt.BorderLayout.PAGE_START);

        panelTabla.setBackground(new java.awt.Color(18, 24, 36));
        panelTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));
        panelTabla.setLayout(new java.awt.BorderLayout());

        tblVentas.setBackground(new java.awt.Color(30, 41, 59));
        tblVentas.setForeground(new java.awt.Color(243, 244, 246));
        tblVentas.setModel(new javax.swing.table.DefaultTableModel(
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
        tblVentas.setFillsViewportHeight(true);
        tblVentas.setGridColor(new java.awt.Color(51, 65, 85));
        tblVentas.setRowHeight(32);
        tblVentas.setSelectionBackground(new java.awt.Color(2, 132, 199));
        tblVentas.setShowGrid(false);
        jScrollPane1.setViewportView(tblVentas);

        panelTabla.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(panelTabla, java.awt.BorderLayout.CENTER);

        panelForm.setBackground(new java.awt.Color(30, 41, 59));
        panelForm.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        panelForm.setPreferredSize(new java.awt.Dimension(0, 275));
        panelForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblCliente.setForeground(new java.awt.Color(156, 163, 175));
        lblCliente.setText("Cliente");
        panelForm.add(lblCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 15, 300, 25));

        cboCliente.setBackground(new java.awt.Color(15, 23, 42));
        cboCliente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboCliente.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(cboCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 40, 300, 35));

        lblFecha.setForeground(new java.awt.Color(156, 163, 175));
        lblFecha.setText("Fecha");
        panelForm.add(lblFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 15, 140, 25));

        txtFecha.setBackground(new java.awt.Color(15, 23, 42));
        txtFecha.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtFecha.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 40, 140, 35));

        lblPago.setForeground(new java.awt.Color(156, 163, 175));
        lblPago.setText("Forma de pago");
        panelForm.add(lblPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 15, 150, 25));

        cboPago.setBackground(new java.awt.Color(15, 23, 42));
        cboPago.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboPago.setForeground(new java.awt.Color(243, 244, 246));
        cboPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "EFECTIVO", "TARJETA", "TRANSFERENCIA" }));
        panelForm.add(cboPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 40, 150, 35));

        lblCondicion.setForeground(new java.awt.Color(156, 163, 175));
        lblCondicion.setText("Condición de venta");
        panelForm.add(lblCondicion, new org.netbeans.lib.awtextra.AbsoluteConstraints(665, 15, 190, 25));

        cboCondicion.setBackground(new java.awt.Color(15, 23, 42));
        cboCondicion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboCondicion.setForeground(new java.awt.Color(243, 244, 246));
        cboCondicion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Contado", "Crédito" }));
        panelForm.add(cboCondicion, new org.netbeans.lib.awtextra.AbsoluteConstraints(665, 40, 150, 35));

        lblFoto.setForeground(new java.awt.Color(156, 163, 175));
        lblFoto.setText("Foto del producto");
        panelForm.add(lblFoto, new org.netbeans.lib.awtextra.AbsoluteConstraints(865, 15, 150, 20));

        lblPreview.setBackground(new java.awt.Color(15, 23, 42));
        lblPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        lblPreview.setForeground(new java.awt.Color(156, 163, 175));
        lblPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPreview.setText("Sin foto");
        lblPreview.setOpaque(true);
        panelForm.add(lblPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(865, 40, 150, 110));

        lblProducto.setForeground(new java.awt.Color(156, 163, 175));
        lblProducto.setText("Producto");
        panelForm.add(lblProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 90, 300, 25));

        cboProducto.setBackground(new java.awt.Color(15, 23, 42));
        cboProducto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboProducto.setForeground(new java.awt.Color(243, 244, 246));
        cboProducto.addActionListener(this::cboProductoActionPerformed);
        panelForm.add(cboProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 115, 300, 35));

        lblCantidad.setForeground(new java.awt.Color(156, 163, 175));
        lblCantidad.setText("Cantidad");
        panelForm.add(lblCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 90, 90, 25));

        txtCantidad.setBackground(new java.awt.Color(15, 23, 42));
        txtCantidad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCantidad.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 115, 90, 35));

        lblPrecioUnit.setForeground(new java.awt.Color(156, 163, 175));
        lblPrecioUnit.setText("Precio Unit.");
        panelForm.add(lblPrecioUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(445, 90, 140, 25));

        txtPrecioUnit.setBackground(new java.awt.Color(15, 23, 42));
        txtPrecioUnit.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtPrecioUnit.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtPrecioUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(445, 115, 140, 35));

        btnAgregar.setBackground(new java.awt.Color(22, 163, 74));
        btnAgregar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregar.setText("+ Agregar");
        btnAgregar.setBorderPainted(false);
        btnAgregar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);
        panelForm.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 115, 110, 35));

        btnQuitar.setBackground(new java.awt.Color(51, 65, 85));
        btnQuitar.setForeground(new java.awt.Color(243, 244, 246));
        btnQuitar.setText("Quitar fila");
        btnQuitar.addActionListener(this::btnQuitarActionPerformed);
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
        tblDetalle.setShowGrid(false);
        scrollDetalle.setViewportView(tblDetalle);

        panelForm.add(scrollDetalle, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 165, 600, 90));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(56, 189, 248));
        lblTotal.setText("Total: Gs. 0");
        panelForm.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(705, 170, 330, 35));

        btnGuardar.setBackground(new java.awt.Color(22, 163, 74));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar venta");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        panelForm.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(705, 215, 150, 40));

        btnLimpiar.setBackground(new java.awt.Color(51, 65, 85));
        btnLimpiar.setForeground(new java.awt.Color(243, 244, 246));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        panelForm.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(865, 215, 130, 40));

        jPanel1.add(panelForm, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_txtBuscarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        limpiar();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTicketActionPerformed
        imprimirTicket();
    }//GEN-LAST:event_btnTicketActionPerformed

    private void btnFacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFacturaActionPerformed
        imprimirFactura();
    }//GEN-LAST:event_btnFacturaActionPerformed

    private void cboProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboProductoActionPerformed
        productoCambiado();
    }//GEN-LAST:event_cboProductoActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        agregar();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        quitarFila();
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiar();
    }//GEN-LAST:event_btnLimpiarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnFactura;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnTicket;
    private javax.swing.JComboBox<String> cboCliente;
    private javax.swing.JComboBox<String> cboCondicion;
    private javax.swing.JComboBox<String> cboPago;
    private javax.swing.JComboBox<String> cboProducto;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblCondicion;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFoto;
    private javax.swing.JLabel lblPago;
    private javax.swing.JLabel lblPrecioUnit;
    private javax.swing.JLabel lblPreview;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelForm;
    private javax.swing.JPanel panelNorte;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scrollDetalle;
    private javax.swing.JTable tblDetalle;
    private javax.swing.JTable tblVentas;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtPrecioUnit;
    // End of variables declaration//GEN-END:variables
}
