/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.clases.Proveedor;
import com.electroservisgmg.metodos.CodigoBarras;
import com.electroservisgmg.metodos.ProductoMetodos;
import com.electroservisgmg.metodos.ProveedorMetodos;
import com.electroservisgmg.reportes.ReportesMetodos;

/**
 *
 * @author Aurelio
 */
public class FormProductos extends javax.swing.JPanel {

    private java.util.List<Proveedor> listaProveedores = new java.util.ArrayList<>();
    private String fotoSeleccionada = "";

    /** Codigo de barras del producto y su vista previa (se crean en runtime). */
    private javax.swing.JTextField txtCodigoBarras;
    private javax.swing.JLabel lblBarras;

    /** Cantidad a partir de la cual se avisa que hay que reponer (se crea en runtime). */
    private javax.swing.JTextField txtStockMinimo;

    /**
     * Creates new form FormProductos
     */
    public FormProductos() {
        initComponents();
        crearCampoCodigoBarras();
        crearCampoStockMinimo();
        crearBotonEtiquetas();
        UiTheme.styleTable(tblProductos);
        UiTheme.miles(txtStock);
        UiTheme.miles(txtStockMinimo);
        UiTheme.miles(txtPrecioCompra, true);
        UiTheme.miles(txtPrecioVenta, true);
        cargarProveedores();
        cargarTabla("");
        // Los botones y el campo de busqueda se conectan desde el disenador
        // (pestana Events -> actionPerformed). Aca quedan solo los extras que
        // el disenador no modela.
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
        });
        tblProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) llenarDesdeFila();
        });
    }

    // ---- stock minimo ----

    /**
     * Campo "Stock mínimo" (se crea en runtime para no tocar el diseno del .form).
     * Queda a la derecha de la vista previa del codigo de barras.
     */
    private void crearCampoStockMinimo() {
        javax.swing.JLabel lbl = new javax.swing.JLabel("Stock mín.");
        lbl.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lbl.setForeground(new java.awt.Color(156, 163, 175));
        lbl.setToolTipText("Cantidad a la que se avisa que hay que reponer. 0 = no avisar");
        panelForm.add(lbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 168, 120, 22));

        txtStockMinimo = new javax.swing.JTextField();
        txtStockMinimo.setBackground(new java.awt.Color(15, 23, 42));
        txtStockMinimo.setFont(new java.awt.Font("Segoe UI", 1, 13));
        txtStockMinimo.setForeground(new java.awt.Color(243, 244, 246));
        txtStockMinimo.setCaretColor(new java.awt.Color(243, 244, 246));
        txtStockMinimo.setToolTipText("Cantidad a la que se avisa que hay que reponer. 0 = no avisar");
        panelForm.add(txtStockMinimo, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 191, 80, 33));
    }

    // ---- codigo de barras ----

    /**
     * Campo de codigo de barras + boton para generar uno interno + vista previa
     * del simbolo. Se agrega en runtime para no tocar el diseno del .form.
     */
    private void crearCampoCodigoBarras() {
        javax.swing.JLabel lbl = new javax.swing.JLabel("Código de barras");
        lbl.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lbl.setForeground(new java.awt.Color(156, 163, 175));
        panelForm.add(lbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 165, 230, 25));

        txtCodigoBarras = new javax.swing.JTextField();
        txtCodigoBarras.setBackground(new java.awt.Color(15, 23, 42));
        txtCodigoBarras.setFont(new java.awt.Font("Segoe UI", 1, 13));
        txtCodigoBarras.setForeground(new java.awt.Color(243, 244, 246));
        txtCodigoBarras.setCaretColor(new java.awt.Color(243, 244, 246));
        txtCodigoBarras.setToolTipText("Escaneá el código del producto, o generá uno interno con el botón de al lado");
        panelForm.add(txtCodigoBarras, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 190, 200, 35));
        txtCodigoBarras.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarPreviewBarras(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarPreviewBarras(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarPreviewBarras(); }
        });

        javax.swing.JButton btnGen = new javax.swing.JButton("Generar");
        btnGen.setBackground(new java.awt.Color(51, 65, 85));
        btnGen.setFont(new java.awt.Font("Segoe UI", 0, 11));
        btnGen.setForeground(new java.awt.Color(243, 244, 246));
        btnGen.setBorderPainted(false);
        btnGen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGen.setFocusPainted(false);
        btnGen.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnGen.setToolTipText("Genera un EAN-13 interno para este producto");
        btnGen.addActionListener(e -> generarCodigoBarras());
        panelForm.add(btnGen, new org.netbeans.lib.awtextra.AbsoluteConstraints(584, 190, 68, 35));

        lblBarras = new javax.swing.JLabel();
        lblBarras.setOpaque(true);
        lblBarras.setBackground(java.awt.Color.WHITE);
        lblBarras.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBarras.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        panelForm.add(lblBarras, new org.netbeans.lib.awtextra.AbsoluteConstraints(655, 163, 235, 62));
    }

    /** Dibuja la vista previa del EAN-13 que hay escrito en el campo. */
    private void actualizarPreviewBarras() {
        if (lblBarras == null || txtCodigoBarras == null) return;
        String codigo = txtCodigoBarras.getText().trim();
        lblBarras.setIcon(null);
        if (codigo.isEmpty()) {
            lblBarras.setText("");
            return;
        }
        java.awt.image.BufferedImage img = CodigoBarras.dibujar(codigo, 2, 44, true);
        if (img == null) {
            lblBarras.setFont(new java.awt.Font("Segoe UI", 0, 11));
            lblBarras.setForeground(new java.awt.Color(120, 113, 108));
            lblBarras.setText("(no es EAN-13)");
        } else {
            lblBarras.setText("");
            lblBarras.setIcon(new javax.swing.ImageIcon(img));
        }
    }

    /** Le pone al producto un EAN-13 interno que no este usado por otro. */
    private void generarCodigoBarras() {
        int id = 0;
        try { id = Integer.parseInt(txtId.getText().trim()); } catch (NumberFormatException ignored) { }
        if (id <= 0) id = (int) (System.currentTimeMillis() % 1000000000L);
        txtCodigoBarras.setText(new ProductoMetodos().generarCodigoBarrasLibre(id));
    }

    // ---- etiquetas ----

    /** Boton para imprimir las etiquetas del producto seleccionado. */
    private void crearBotonEtiquetas() {
        javax.swing.JButton btn = new javax.swing.JButton("Etiquetas");
        btn.setBackground(new java.awt.Color(51, 65, 85));
        btn.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btn.setForeground(new java.awt.Color(255, 255, 255));
        btn.setBorderPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setToolTipText("Imprime etiquetas con el código de barras del producto seleccionado");
        btn.addActionListener(e -> imprimirEtiquetas());
        panelNorte.add(btn, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 75, 150, 35));
    }

    /** Pregunta cuantas etiquetas y arma la hoja A4 (24 por hoja). */
    private void imprimirEtiquetas() {
        int fila = tblProductos.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Seleccione un producto de la tabla para imprimir sus etiquetas");
            return;
        }
        int id = ((Number) tblProductos.getValueAt(fila, 0)).intValue();
        Producto p = null;
        for (Producto x : new ProductoMetodos().listar()) if (x.getIdProducto() == id) { p = x; break; }
        if (p == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se encontró el producto seleccionado");
            return;
        }
        if (p.getCodigoBarras() == null || p.getCodigoBarras().trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Ese producto todavía no tiene código de barras.\n"
                    + "Cargá uno (o usá el botón \"Generar\") y volvé a intentar.");
            return;
        }
        String resp = javax.swing.JOptionPane.showInputDialog(this,
                "¿Cuántas etiquetas de \"" + p.getNombre() + "\" querés?  (entran 24 por hoja)", "1");
        if (resp == null) return;
        int cuantas;
        try { cuantas = UiTheme.parseGsInt(resp); } catch (NumberFormatException e) { cuantas = 0; }
        if (cuantas < 1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Poné una cantidad mayor a 0");
            return;
        }
        if (cuantas > 240) cuantas = 240;
        java.util.List<Producto> items = new java.util.ArrayList<>();
        for (int i = 0; i < cuantas; i++) items.add(p);
        try {
            java.io.File pdf = ReportesMetodos.etiquetas(items);
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Etiquetas generadas (" + cuantas + "):\n" + pdf.getAbsolutePath());
            ReportesMetodos.abrir(pdf);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No se pudieron generar las etiquetas:\n" + e.getMessage());
        }
    }

    // ---- proveedores (combo) ----
    private void cargarProveedores() {
        listaProveedores = new ProveedorMetodos().listar();
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        m.addElement("(sin proveedor)");
        for (Proveedor p : listaProveedores) m.addElement(p.getIdProveedor() + " - " + p.getNombre());
        cboProveedor.setModel(m);
    }

    private void seleccionarProveedor(int id) {
        if (id <= 0) { cboProveedor.setSelectedIndex(0); return; }
        for (int i = 0; i < listaProveedores.size(); i++) {
            if (listaProveedores.get(i).getIdProveedor() == id) { cboProveedor.setSelectedIndex(i + 1); return; }
        }
        cboProveedor.setSelectedIndex(0);
    }

    // ---- tabla ----
    private void cargarTabla(String filtro) {
        new ProductoMetodos().mostrar(tblProductos, filtro);
        UiTheme.styleTable(tblProductos);
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    // ---- formulario ----
    private void limpiar() {
        txtId.setText("");
        txtCodigo.setText("");
        txtCodigoBarras.setText("");
        txtNombre.setText("");
        txtStock.setText("");
        txtStockMinimo.setText("");
        txtPrecioCompra.setText("");
        txtPrecioVenta.setText("");
        txtDescripcion.setText("");
        cboProveedor.setSelectedIndex(0);
        cboIva.setSelectedIndex(0);
        fotoSeleccionada = "";
        lblFoto.setText("");
        mostrarPreview("");
        tblProductos.clearSelection();
        txtNombre.requestFocus();
    }

    /** Ubica el combo en la tasa guardada del producto. */
    private void seleccionarIva(double iva) {
        if (iva >= 9.99) cboIva.setSelectedIndex(0);
        else if (iva >= 4.99) cboIva.setSelectedIndex(1);
        else cboIva.setSelectedIndex(2);
    }

    private void llenarDesdeFila() {
        int fila = tblProductos.getSelectedRow();
        if (fila < 0) return;
        int id = (int) tblProductos.getValueAt(fila, 0);
        for (Producto p : new ProductoMetodos().listar()) {
            if (p.getIdProducto() == id) {
                txtId.setText(String.valueOf(p.getIdProducto()));
                txtCodigo.setText(p.getCodigo() == null ? "" : p.getCodigo());
                txtCodigoBarras.setText(p.getCodigoBarras() == null ? "" : p.getCodigoBarras());
                txtNombre.setText(p.getNombre());
                txtStock.setText(String.valueOf(p.getStock()));
                txtStockMinimo.setText(p.getStockMinimo() > 0 ? String.valueOf(p.getStockMinimo()) : "");
                txtPrecioCompra.setText(String.valueOf(Math.round(p.getPrecioCompra())));
                txtPrecioVenta.setText(String.valueOf(Math.round(p.getPrecioVenta())));
                txtDescripcion.setText(p.getDescripcion() == null ? "" : p.getDescripcion());
                seleccionarProveedor(p.getIdProveedor());
                seleccionarIva(p.getIva());
                fotoSeleccionada = p.getFoto() == null ? "" : p.getFoto();
                lblFoto.setText(fotoSeleccionada);
                mostrarPreview(fotoSeleccionada);
                break;
            }
        }
    }

    private void editar() {
        if (tblProductos.getSelectedRow() < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla");
            return;
        }
        llenarDesdeFila();
        txtNombre.requestFocus();
    }

    private void guardar() {
        if (new ProductoMetodos().guardar(txtId, txtCodigo, txtCodigoBarras, txtNombre, txtStock,
                txtStockMinimo, txtPrecioCompra, txtPrecioVenta, txtDescripcion, cboIva, cboProveedor,
                fotoSeleccionada)) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    private void eliminar() {
        int fila = tblProductos.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla");
            return;
        }
        int conf = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Eliminar producto '" + tblProductos.getValueAt(fila, 1) + "'?",
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (conf != javax.swing.JOptionPane.YES_OPTION) return;
        if (new ProductoMetodos().eliminar(txtId)) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    // ---- fotos ----
    private void seleccionarFoto() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (jpg, png)", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        java.io.File orig = fc.getSelectedFile();
        try {
            java.io.File dir = new java.io.File("imagenes_productos");
            if (!dir.exists()) dir.mkdirs();
            String destino = System.currentTimeMillis() + "_" + orig.getName().replaceAll("[^\\w.-]", "_");
            java.nio.file.Files.copy(orig.toPath(),
                    new java.io.File(dir, destino).toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            fotoSeleccionada = destino;
            lblFoto.setText(destino);
            mostrarPreview(destino);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen: " + ex.getMessage());
        }
    }

    /** Muestra la foto escalada (sin deformar) dentro de lblPreview. */
    private void mostrarPreview(String archivo) {
        if (archivo == null || archivo.isEmpty()) {
            lblPreview.setIcon(null);
            lblPreview.setText("Sin vista previa");
            return;
        }
        java.io.File f = new java.io.File("imagenes_productos", archivo);
        if (!f.exists()) {
            lblPreview.setIcon(null);
            lblPreview.setText("Foto no encontrada");
            return;
        }
        try {
            java.awt.Image img = new javax.swing.ImageIcon(f.getAbsolutePath()).getImage();
            int w = lblPreview.getWidth() > 4 ? lblPreview.getWidth() - 6 : 124;
            int h = lblPreview.getHeight() > 4 ? lblPreview.getHeight() - 6 : 139;
            double esc = Math.min((double) w / img.getWidth(null), (double) h / img.getHeight(null));
            int nw = Math.max(1, (int) (img.getWidth(null) * esc));
            int nh = Math.max(1, (int) (img.getHeight(null) * esc));
            lblPreview.setText("");
            lblPreview.setIcon(new javax.swing.ImageIcon(
                    img.getScaledInstance(nw, nh, java.awt.Image.SCALE_SMOOTH)));
        } catch (Exception ex) {
            lblPreview.setIcon(null);
            lblPreview.setText("Error al cargar");
        }
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
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        panelTabla = new javax.swing.JPanel();
        scroll = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        panelForm = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        lblNombre = new javax.swing.JLabel();
        lblStock = new javax.swing.JLabel();
        lblPCompra = new javax.swing.JLabel();
        lblPVenta = new javax.swing.JLabel();
        lblProveedor = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        lblFoto = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtStock = new javax.swing.JTextField();
        txtPrecioCompra = new javax.swing.JTextField();
        txtPrecioVenta = new javax.swing.JTextField();
        txtDescripcion = new javax.swing.JTextField();
        cboProveedor = new javax.swing.JComboBox<>();
        btnGuardar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        lblPreview = new javax.swing.JLabel();
        btnFoto = new javax.swing.JButton();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblIva = new javax.swing.JLabel();
        cboIva = new javax.swing.JComboBox<>();

        jPanel1.setBackground(new java.awt.Color(18, 24, 36));
        jPanel1.setLayout(new java.awt.BorderLayout());

        panelNorte.setBackground(new java.awt.Color(18, 24, 36));
        panelNorte.setPreferredSize(new java.awt.Dimension(0, 130));
        panelNorte.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("PRODUCTOS");
        panelNorte.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 30));

        lblSub.setForeground(new java.awt.Color(156, 163, 175));
        lblSub.setText("Inventario de productos y repuestos");
        panelNorte.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 500, 22));

        txtBuscar.setBackground(new java.awt.Color(15, 23, 42));
        txtBuscar.setForeground(new java.awt.Color(243, 244, 246));
        txtBuscar.addActionListener(this::txtBuscarActionPerformed);
        panelNorte.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 75, 250, 35));

        btnBuscar.setBackground(new java.awt.Color(51, 65, 85));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(243, 244, 246));
        btnBuscar.setText("Buscar");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        panelNorte.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 75, 100, 35));

        btnNuevo.setBackground(new java.awt.Color(56, 189, 248));
        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(15, 23, 42));
        btnNuevo.setText("+ Nuevo");
        btnNuevo.addActionListener(this::btnNuevoActionPerformed);
        panelNorte.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(385, 75, 110, 35));

        btnEditar.setBackground(new java.awt.Color(51, 65, 85));
        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setText("Editar");
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        panelNorte.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 75, 100, 35));

        btnEliminar.setBackground(new java.awt.Color(239, 68, 68));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        panelNorte.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 75, 110, 35));

        jPanel1.add(panelNorte, java.awt.BorderLayout.NORTH);

        panelTabla.setBackground(new java.awt.Color(18, 24, 36));
        panelTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));
        panelTabla.setLayout(new java.awt.BorderLayout());

        tblProductos.setBackground(new java.awt.Color(30, 41, 59));
        tblProductos.setForeground(new java.awt.Color(243, 244, 246));
        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
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
        tblProductos.setFillsViewportHeight(true);
        tblProductos.setGridColor(new java.awt.Color(51, 65, 85));
        tblProductos.setRowHeight(32);
        tblProductos.setSelectionBackground(new java.awt.Color(2, 132, 199));
        scroll.setViewportView(tblProductos);

        panelTabla.add(scroll, java.awt.BorderLayout.CENTER);

        jPanel1.add(panelTabla, java.awt.BorderLayout.CENTER);

        panelForm.setBackground(new java.awt.Color(30, 41, 59));
        panelForm.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        panelForm.setPreferredSize(new java.awt.Dimension(0, 252));
        panelForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblId.setForeground(new java.awt.Color(156, 163, 175));
        lblId.setText("ID");
        panelForm.add(lblId, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 15, -1, -1));

        lblNombre.setForeground(new java.awt.Color(156, 163, 175));
        lblNombre.setText("Nombre");
        panelForm.add(lblNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 15, 220, 25));

        lblStock.setForeground(new java.awt.Color(156, 163, 175));
        lblStock.setText("Stock");
        panelForm.add(lblStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 15, 80, 25));

        lblPCompra.setForeground(new java.awt.Color(156, 163, 175));
        lblPCompra.setText("Precio Compra");
        panelForm.add(lblPCompra, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 15, 110, 25));

        lblPVenta.setForeground(new java.awt.Color(156, 163, 175));
        lblPVenta.setText("Precio Venta");
        panelForm.add(lblPVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 15, 110, 25));

        lblProveedor.setForeground(new java.awt.Color(156, 163, 175));
        lblProveedor.setText("Proveedor");
        panelForm.add(lblProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 15, 110, 25));

        lblDescripcion.setForeground(new java.awt.Color(156, 163, 175));
        lblDescripcion.setText("Descripción");
        panelForm.add(lblDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 300, 25));

        lblFoto.setForeground(new java.awt.Color(156, 163, 175));
        panelForm.add(lblFoto, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 115, 120, 35));

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(15, 23, 42));
        txtId.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtId.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 40, 70, 35));

        txtNombre.setBackground(new java.awt.Color(15, 23, 42));
        txtNombre.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtNombre.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, 220, 35));

        txtStock.setBackground(new java.awt.Color(15, 23, 42));
        txtStock.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtStock.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 40, 80, 35));

        txtPrecioCompra.setBackground(new java.awt.Color(15, 23, 42));
        txtPrecioCompra.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtPrecioCompra.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtPrecioCompra, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 40, 110, 35));

        txtPrecioVenta.setBackground(new java.awt.Color(15, 23, 42));
        txtPrecioVenta.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtPrecioVenta.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtPrecioVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 40, 110, 35));

        txtDescripcion.setBackground(new java.awt.Color(15, 23, 42));
        txtDescripcion.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtDescripcion.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 115, 320, 35));

        cboProveedor.setBackground(new java.awt.Color(15, 23, 42));
        cboProveedor.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(cboProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 40, 220, 35));

        btnGuardar.setBackground(new java.awt.Color(34, 197, 94));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        panelForm.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 115, 100, 38));

        btnLimpiar.setBackground(new java.awt.Color(51, 65, 85));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        panelForm.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 115, 110, 38));

        lblPreview.setBackground(new java.awt.Color(15, 23, 42));
        lblPreview.setForeground(new java.awt.Color(156, 163, 175));
        lblPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        panelForm.add(lblPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(895, 25, 130, 145));

        btnFoto.setBackground(new java.awt.Color(51, 65, 85));
        btnFoto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnFoto.setText("Seleccionar foto");
        btnFoto.addActionListener(this::btnFotoActionPerformed);
        panelForm.add(btnFoto, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 115, 170, 35));

        lblCodigo.setForeground(new java.awt.Color(156, 163, 175));
        lblCodigo.setText("Código");
        panelForm.add(lblCodigo, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 165, 130, 25));

        txtCodigo.setBackground(new java.awt.Color(15, 23, 42));
        txtCodigo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtCodigo.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtCodigo, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 190, 130, 35));

        lblIva.setForeground(new java.awt.Color(156, 163, 175));
        lblIva.setText("IVA");
        panelForm.add(lblIva, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 165, 200, 25));

        cboIva.setBackground(new java.awt.Color(15, 23, 42));
        cboIva.setForeground(new java.awt.Color(243, 244, 246));
        cboIva.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "10% (IVA general)", "5% (IVA reducido)", "Exenta" }));
        cboIva.addActionListener(this::cboIvaActionPerformed);
        panelForm.add(cboIva, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 190, 180, 35));

        jPanel1.add(panelForm, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1069, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cboIvaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboIvaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboIvaActionPerformed

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_txtBuscarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        limpiar();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        editar();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnFotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFotoActionPerformed
        seleccionarFoto();
    }//GEN-LAST:event_btnFotoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnFoto;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<String> cboIva;
    private javax.swing.JComboBox<String> cboProveedor;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblFoto;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblIva;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPCompra;
    private javax.swing.JLabel lblPVenta;
    private javax.swing.JLabel lblPreview;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblStock;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelForm;
    private javax.swing.JPanel panelNorte;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecioCompra;
    private javax.swing.JTextField txtPrecioVenta;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}
