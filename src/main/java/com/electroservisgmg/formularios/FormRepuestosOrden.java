package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.OrdenRepuesto;
import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.metodos.OrdenRepuestoMetodos;
import com.electroservisgmg.metodos.ProductoMetodos;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Ventana de repuestos de una orden de servicio: muestra que productos se
 * usaron en la reparacion y permite agregar o quitar.
 *
 * Al agregar un repuesto se descuenta del stock y su precio se suma al costo
 * de la orden; al quitarlo vuelve al stock y se descuenta del costo.
 *
 * @author Aurelio
 */
public class FormRepuestosOrden extends javax.swing.JDialog {

    // ---- colores que se usan al mostrar el estado del proceso (fuera del GEN) ----
    private static final Color GRIS = new Color(156, 163, 175);
    private static final Color VERDE = new Color(34, 197, 94);
    private static final Color ROJO = new Color(239, 68, 68);

    private final OrdenRepuestoMetodos metodos = new OrdenRepuestoMetodos();

    /** Orden que se esta mirando. Lo carga el constructor propio. */
    private int idOrden;

    /** Se ejecuta cada vez que cambian los repuestos (puede ser null). */
    private Runnable alCambiar;

    /** Productos que se pueden usar como repuesto. */
    private List<Producto> productos;

    /**
     * Creates new form FormRepuestosOrden
     */
    public FormRepuestosOrden(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        // ---- detalles que no hacen falta en el disenador ----
        btnNuevo.setToolTipText("Carga un producto nuevo en el sistema y lo agrega como repuesto");
        tabla.setShowVerticalLines(false);
        txtCantidad.setCaretColor(new Color(243, 244, 246));

        // ---- eventos ----
        btnAgregar.addActionListener(e -> agregar());
        txtCantidad.addActionListener(e -> agregar());
        btnQuitar.addActionListener(e -> quitar());
        btnNuevo.addActionListener(e -> nuevoProducto());
        btnCerrar.addActionListener(e -> dispose());

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        cargarProductos();
        refrescar();
    }

    /**
     * Ventana de repuestos de una orden.
     *
     * @param dueno        ventana de la que depende (puede ser null)
     * @param idOrden      orden de servicio
     * @param detalleOrden texto que se muestra debajo del titulo (puede ir vacio)
     * @param alCambiar    se ejecuta cada vez que cambian los repuestos (puede ser null)
     */
    public FormRepuestosOrden(Window dueno, int idOrden, String detalleOrden, Runnable alCambiar) {
        this(dueno instanceof java.awt.Frame ? (java.awt.Frame) dueno : null, true);
        this.idOrden = idOrden;
        this.alCambiar = alCambiar;
        setTitle("Repuestos de la orden #" + idOrden);
        lblDetalle.setText("Orden #" + idOrden
                + (detalleOrden == null || detalleOrden.isEmpty() ? "" : "  ·  " + detalleOrden));
        refrescar();
    }

    /** Abre la ventana; alCambiar se ejecuta cada vez que cambian los repuestos. */
    public static void mostrar(Component padre, int idOrden, String detalleOrden, Runnable alCambiar) {
        FormRepuestosOrden d = new FormRepuestosOrden(
                padre == null ? null : SwingUtilities.getWindowAncestor(padre), idOrden, detalleOrden, alCambiar);
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }

    // ==================== datos ====================

    /** Carga los productos que se pueden usar como repuesto. */
    private void cargarProductos() {
        productos = new ProductoMetodos().listar();
        cboProducto.removeAllItems();
        for (Producto p : productos) {
            cboProducto.addItem(p.getNombre() + "   (stock " + p.getStock() + ")");
        }
        if (productos.isEmpty()) cboProducto.addItem("(no hay productos cargados)");
    }

    /** Vuelve a dibujar la tabla y el total. */
    private void refrescar() {
        List<OrdenRepuesto> lista = metodos.listar(idOrden);
        java.text.DecimalFormat miles = new java.text.DecimalFormat("#,##0");
        String[] cols = {"Producto", "Cantidad", "Precio", "Subtotal"};
        Object[][] data = new Object[lista.size()][4];
        double total = 0;
        for (int i = 0; i < lista.size(); i++) {
            OrdenRepuesto r = lista.get(i);
            total += r.getSubtotal();
            data[i] = new Object[]{r.getNombreProducto(), r.getCantidad(),
                "Gs. " + miles.format(r.getPrecioUnitario()), "Gs. " + miles.format(r.getSubtotal())};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        lblTotal.setText(lista.isEmpty() ? "Sin repuestos cargados"
                : "Total de repuestos:  Gs. " + miles.format(Math.round(total)));
    }

    // ==================== acciones ====================

    private void agregar() {
        int i = cboProducto.getSelectedIndex();
        if (productos == null || productos.isEmpty() || i < 0) {
            estado("Primero cargá productos en el inventario", ROJO);
            return;
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            estado("La cantidad tiene que ser un número mayor a cero", ROJO);
            return;
        }
        Producto p = productos.get(i);
        if (!metodos.agregar(idOrden, p.getIdProducto(), cantidad)) {
            estado(OrdenRepuestoMetodos.ultimoError, ROJO);
            JOptionPane.showMessageDialog(this, OrdenRepuestoMetodos.ultimoError);
            return;
        }
        estado("Se agregó " + cantidad + " x " + p.getNombre() + " y se descontó del stock.", VERDE);
        txtCantidad.setText("1");
        if (alCambiar != null) alCambiar.run();
        refrescar();
        cargarProductos();
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            estado("Elegí en la lista el repuesto que querés quitar", ROJO);
            return;
        }
        List<OrdenRepuesto> lista = metodos.listar(idOrden);
        if (fila >= lista.size()) return;
        int id = lista.get(fila).getIdRepuesto();
        if (!metodos.quitar(id)) {
            estado(OrdenRepuestoMetodos.ultimoError, ROJO);
            return;
        }
        estado("Repuesto quitado: volvió al stock y se descontó del costo de la orden.", VERDE);
        if (alCambiar != null) alCambiar.run();
        refrescar();
        cargarProductos();
    }

    /** Carga un producto nuevo en el inventario y lo usa como repuesto acá. */
    private void nuevoProducto() {
        JTextField nombre = new JTextField();
        JTextField precio = new JTextField();
        JTextField cantidad = new JTextField("1");
        Object[] mensaje = {
            "Nombre del repuesto:", nombre,
            "Precio de venta (Gs.):", precio,
            "Cantidad que entra al stock:", cantidad,
            "", "Se carga como producto nuevo en el inventario."
        };
        int ok = JOptionPane.showConfirmDialog(this, mensaje, "Nuevo repuesto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        if (nombre.getText().trim().isEmpty()) {
            estado("El nombre del repuesto no puede quedar vacío", ROJO);
            return;
        }
        double pv;
        int cantStock;
        try {
            pv = UiTheme.parseGs(precio.getText().trim());
            cantStock = Integer.parseInt(cantidad.getText().trim());
            if (pv < 0 || cantStock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            estado("Precio y cantidad tienen que ser números válidos", ROJO);
            return;
        }
        Producto nuevo = new Producto();
        nuevo.setNombre(nombre.getText().trim());
        nuevo.setPrecioVenta(pv);
        nuevo.setPrecioCompra(Math.round(pv * 0.7));
        nuevo.setStock(cantStock);
        nuevo.setIva(10);
        if (!new ProductoMetodos().insertar(nuevo)) {
            estado("No se pudo cargar el producto: " + ProductoMetodos.ultimoError, ROJO);
            return;
        }
        cargarProductos();
        cboProducto.setSelectedIndex(Math.max(0, productos.size() - 1));
        estado("Producto cargado. Ahora poné la cantidad y apretá Agregar.", VERDE);
    }

    private void estado(String texto, Color color) {
        lblEstado.setText("<html>" + texto.replace("\n", "<br>") + "</html>");
        lblEstado.setForeground(color);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelContenido = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblDetalle = new javax.swing.JLabel();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        lblProd = new javax.swing.JLabel();
        cboProducto = new javax.swing.JComboBox<>();
        lblCant = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        lblEstado = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setTitle("Repuestos de la orden");

        panelContenido.setBackground(new java.awt.Color(18, 24, 36));
        panelContenido.setPreferredSize(new java.awt.Dimension(700, 520));
        panelContenido.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("Repuestos usados en la reparación");
        panelContenido.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 16, 660, 24));

        lblDetalle.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblDetalle.setForeground(new java.awt.Color(156, 163, 175));
        lblDetalle.setText("Orden");
        panelContenido.add(lblDetalle, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 660, 18));

        scrollTabla.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));

        tabla.setBackground(new java.awt.Color(30, 41, 59));
        tabla.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tabla.setForeground(new java.awt.Color(243, 244, 246));
        tabla.setModel(new javax.swing.table.DefaultTableModel(
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
        tabla.setFillsViewportHeight(true);
        tabla.setGridColor(new java.awt.Color(51, 65, 85));
        tabla.setRowHeight(30);
        tabla.setSelectionBackground(new java.awt.Color(2, 132, 199));
        tabla.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrollTabla.setViewportView(tabla);

        panelContenido.add(scrollTabla, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 68, 660, 250));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(56, 189, 248));
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotal.setText("");
        panelContenido.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 324, 660, 22));

        lblProd.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblProd.setForeground(new java.awt.Color(156, 163, 175));
        lblProd.setText("Producto (del stock)");
        panelContenido.add(lblProd, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 356, 300, 18));

        cboProducto.setBackground(new java.awt.Color(15, 23, 42));
        cboProducto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboProducto.setForeground(new java.awt.Color(243, 244, 246));
        panelContenido.add(cboProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 376, 430, 34));

        lblCant.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblCant.setForeground(new java.awt.Color(156, 163, 175));
        lblCant.setText("Cantidad");
        panelContenido.add(lblCant, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 356, 100, 18));

        txtCantidad.setBackground(new java.awt.Color(15, 23, 42));
        txtCantidad.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        txtCantidad.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtCantidad.setForeground(new java.awt.Color(243, 244, 246));
        txtCantidad.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCantidad.setText("1");
        panelContenido.add(txtCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 376, 100, 34));

        btnAgregar.setBackground(new java.awt.Color(34, 197, 94));
        btnAgregar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregar.setText("Agregar");
        btnAgregar.setBorderPainted(false);
        btnAgregar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregar.setFocusPainted(false);
        panelContenido.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 376, 100, 34));

        btnQuitar.setBackground(new java.awt.Color(239, 68, 68));
        btnQuitar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnQuitar.setForeground(new java.awt.Color(255, 255, 255));
        btnQuitar.setText("Quitar el seleccionado");
        btnQuitar.setBorderPainted(false);
        btnQuitar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnQuitar.setFocusPainted(false);
        panelContenido.add(btnQuitar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 426, 220, 38));

        btnNuevo.setBackground(new java.awt.Color(51, 65, 85));
        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(243, 244, 246));
        btnNuevo.setText("Nuevo repuesto del catálogo…");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        panelContenido.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 426, 250, 38));

        btnCerrar.setBackground(new java.awt.Color(51, 65, 85));
        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setForeground(new java.awt.Color(243, 244, 246));
        btnCerrar.setText("Cerrar");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        panelContenido.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 426, 100, 38));

        lblEstado.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(156, 163, 175));
        lblEstado.setText(" ");
        panelContenido.add(lblEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 470, 660, 34));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JComboBox<String> cboProducto;
    private javax.swing.JLabel lblCant;
    private javax.swing.JLabel lblDetalle;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblProd;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tabla;
    private javax.swing.JTextField txtCantidad;
    // End of variables declaration//GEN-END:variables
}
