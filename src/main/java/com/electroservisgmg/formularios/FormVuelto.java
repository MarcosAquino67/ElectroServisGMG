package com.electroservisgmg.formularios;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Ventana de vuelto: muestra el total de la venta y va calculando cuanto hay
 * que devolverle al cliente mientras se escribe con cuanto pago.
 *
 * Se abre sola despues de cobrar en efectivo, y tambien esta el boton
 * "Vuelto" en Ventas para abrirla cuando se quiera.
 *
 * No guarda nada en la base: es solo un calculador.
 *
 * @author Aurelio
 */
public class FormVuelto extends javax.swing.JDialog {

    // ---- colores que se usan al recalcular el vuelto (fuera del GEN) ----
    private static final Color GRIS = new Color(156, 163, 175);
    private static final Color VERDE = new Color(34, 197, 94);
    private static final Color ROJO = new Color(239, 68, 68);

    /** Total a cobrar (no cambia nunca). Lo carga el constructor propio. */
    private double total;

    /**
     * Creates new form FormVuelto
     */
    public FormVuelto(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        // ---- detalles que no hacen falta en el disenador ----
        txtPago.setCaretColor(new Color(243, 244, 246));
        txtPago.setToolTipText("Escribí con cuánto pagó el cliente (Enter para cerrar)");
        btnJusto.setToolTipText("Paga con el importe exacto");
        UiTheme.miles(txtPago, true);

        // ---- eventos ----
        btnJusto.addActionListener(e -> setPago(Math.round(total)));
        btn10.addActionListener(e -> sumar(10000));
        btn20.addActionListener(e -> sumar(20000));
        btn50.addActionListener(e -> sumar(50000));
        btn100.addActionListener(e -> sumar(100000));
        btnLimpiar.addActionListener(e -> {
            txtPago.setText("");
            txtPago.requestFocusInWindow();
        });
        btnListo.addActionListener(e -> dispose());
        txtPago.addActionListener(e -> dispose());
        txtPago.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calcular(); }
            @Override public void removeUpdate(DocumentEvent e) { calcular(); }
            @Override public void changedUpdate(DocumentEvent e) { calcular(); }
        });

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                txtPago.requestFocusInWindow();
            }
        });
    }

    /**
     * Ventana de vuelto con los datos de la venta.
     *
     * @param dueno ventana de la que depende (puede ser null)
     * @param titulo texto de arriba (ej: "Venta #21 registrada")
     * @param total  total de la venta
     * @param nota   aclaracion chica debajo del titulo (puede ir vacia)
     */
    public FormVuelto(Window dueno, String titulo, double total, String nota) {
        this(dueno instanceof java.awt.Frame ? (java.awt.Frame) dueno : null, true);
        this.total = total;
        lblTitulo.setText(titulo == null || titulo.trim().isEmpty() ? "Vuelto" : titulo);
        lblNota.setText(nota == null ? "" : nota);
        lblTotal.setText("Total: " + UiTheme.gs(total));
        calcular();
        pack();
    }

    /**
     * Abre la ventana y espera a que la cierren (Enter, Esc o el boton Listo).
     *
     * @param padre  componente sobre el que se centra la ventana (puede ser null)
     * @param titulo texto de arriba (ej: "Venta #21 registrada")
     * @param total  total de la venta
     * @param nota   aclaracion chica debajo del titulo (puede ir vacia)
     */
    public static void mostrar(Component padre, String titulo, double total, String nota) {
        FormVuelto d = new FormVuelto(
                padre == null ? null : SwingUtilities.getWindowAncestor(padre), titulo, total, nota);
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }

    // ==================== cuentas ====================

    /** Cuanto pago el cliente (0 si el campo esta vacio). */
    public double getPago() {
        try { return UiTheme.parseGs(txtPago.getText()); }
        catch (NumberFormatException e) { return 0; }
    }

    /** Lo que hay que devolverle (negativo = todavia falta que pague). */
    public double getVuelto() {
        return getPago() - total;
    }

    private void setPago(long importe) {
        txtPago.setText(String.valueOf(importe));
        txtPago.requestFocusInWindow();
    }

    private void sumar(long cuanto) {
        setPago(Math.round(getPago()) + cuanto);
    }

    /** Repinta el vuelto cada vez que cambia el importe escrito. */
    private void calcular() {
        if (lblVuelto == null) return;
        double pago = getPago();
        double dif = pago - total;
        if (pago <= 0) {
            lblCaption.setText("VUELTO A ENTREGAR");
            lblCaption.setForeground(GRIS);
            lblVuelto.setForeground(GRIS);
            lblVuelto.setText(UiTheme.gs(0));
        } else if (dif < 0) {
            lblCaption.setText("FALTA COBRAR");
            lblCaption.setForeground(ROJO);
            lblVuelto.setForeground(ROJO);
            lblVuelto.setText(UiTheme.gs(-dif));
        } else {
            lblCaption.setText("VUELTO A ENTREGAR");
            lblCaption.setForeground(VERDE);
            lblVuelto.setForeground(VERDE);
            lblVuelto.setText(UiTheme.gs(dif));
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

        panelContenido = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblNota = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        lblPago = new javax.swing.JLabel();
        txtPago = new javax.swing.JTextField();
        btnJusto = new javax.swing.JButton();
        btn10 = new javax.swing.JButton();
        btn20 = new javax.swing.JButton();
        btn50 = new javax.swing.JButton();
        btn100 = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        lblCaption = new javax.swing.JLabel();
        lblVuelto = new javax.swing.JLabel();
        lblAyuda = new javax.swing.JLabel();
        btnListo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setTitle("Vuelto");

        panelContenido.setBackground(new java.awt.Color(18, 24, 36));
        panelContenido.setPreferredSize(new java.awt.Dimension(470, 372));
        panelContenido.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Vuelto");
        panelContenido.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 16, 430, 24));

        lblNota.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblNota.setForeground(new java.awt.Color(156, 163, 175));
        lblNota.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNota.setText("");
        panelContenido.add(lblNota, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 430, 16));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(56, 189, 248));
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTotal.setText("Total: Gs. 0");
        panelContenido.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 64, 430, 30));

        lblPago.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblPago.setForeground(new java.awt.Color(156, 163, 175));
        lblPago.setText("¿Con cuánto pagó el cliente?");
        panelContenido.add(lblPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 108, 430, 18));

        txtPago.setBackground(new java.awt.Color(15, 23, 42));
        txtPago.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        txtPago.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        txtPago.setForeground(new java.awt.Color(243, 244, 246));
        txtPago.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        panelContenido.add(txtPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 128, 430, 48));

        btnJusto.setBackground(new java.awt.Color(51, 65, 85));
        btnJusto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnJusto.setForeground(new java.awt.Color(243, 244, 246));
        btnJusto.setText("Justo");
        btnJusto.setBorderPainted(false);
        btnJusto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnJusto.setFocusPainted(false);
        panelContenido.add(btnJusto, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 186, 136, 34));

        btn10.setBackground(new java.awt.Color(51, 65, 85));
        btn10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn10.setForeground(new java.awt.Color(243, 244, 246));
        btn10.setText("+ 10.000");
        btn10.setBorderPainted(false);
        btn10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn10.setFocusPainted(false);
        panelContenido.add(btn10, new org.netbeans.lib.awtextra.AbsoluteConstraints(167, 186, 136, 34));

        btn20.setBackground(new java.awt.Color(51, 65, 85));
        btn20.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn20.setForeground(new java.awt.Color(243, 244, 246));
        btn20.setText("+ 20.000");
        btn20.setBorderPainted(false);
        btn20.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn20.setFocusPainted(false);
        panelContenido.add(btn20, new org.netbeans.lib.awtextra.AbsoluteConstraints(314, 186, 136, 34));

        btn50.setBackground(new java.awt.Color(51, 65, 85));
        btn50.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn50.setForeground(new java.awt.Color(243, 244, 246));
        btn50.setText("+ 50.000");
        btn50.setBorderPainted(false);
        btn50.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn50.setFocusPainted(false);
        panelContenido.add(btn50, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 224, 136, 34));

        btn100.setBackground(new java.awt.Color(51, 65, 85));
        btn100.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn100.setForeground(new java.awt.Color(243, 244, 246));
        btn100.setText("+ 100.000");
        btn100.setBorderPainted(false);
        btn100.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn100.setFocusPainted(false);
        panelContenido.add(btn100, new org.netbeans.lib.awtextra.AbsoluteConstraints(167, 224, 136, 34));

        btnLimpiar.setBackground(new java.awt.Color(51, 65, 85));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(243, 244, 246));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimpiar.setFocusPainted(false);
        panelContenido.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(314, 224, 136, 34));

        lblCaption.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCaption.setForeground(new java.awt.Color(156, 163, 175));
        lblCaption.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCaption.setText("VUELTO A ENTREGAR");
        panelContenido.add(lblCaption, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 268, 430, 18));

        lblVuelto.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblVuelto.setForeground(new java.awt.Color(156, 163, 175));
        lblVuelto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblVuelto.setText("Gs. 0");
        panelContenido.add(lblVuelto, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 288, 430, 40));

        lblAyuda.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblAyuda.setForeground(new java.awt.Color(156, 163, 175));
        lblAyuda.setText("Enter o Esc para cerrar");
        panelContenido.add(lblAyuda, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 344, 250, 16));

        btnListo.setBackground(new java.awt.Color(22, 163, 74));
        btnListo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnListo.setForeground(new java.awt.Color(255, 255, 255));
        btnListo.setText("Listo");
        btnListo.setBorderPainted(false);
        btnListo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnListo.setFocusPainted(false);
        panelContenido.add(btnListo, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 336, 150, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn10;
    private javax.swing.JButton btn100;
    private javax.swing.JButton btn20;
    private javax.swing.JButton btn50;
    private javax.swing.JButton btnJusto;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnListo;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblCaption;
    private javax.swing.JLabel lblNota;
    private javax.swing.JLabel lblPago;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblVuelto;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JTextField txtPago;
    // End of variables declaration//GEN-END:variables
}
