package com.electroservisgmg.formularios;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
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
public class FormVuelto extends JDialog {

    private static final Color FONDO = new Color(18, 24, 36);
    private static final Color CAMPO = new Color(15, 23, 42);
    private static final Color TEXTO = new Color(243, 244, 246);
    private static final Color GRIS = new Color(156, 163, 175);
    private static final Color CELESTE = new Color(56, 189, 248);
    private static final Color VERDE = new Color(34, 197, 94);
    private static final Color ROJO = new Color(239, 68, 68);
    private static final Color BOTON = new Color(51, 65, 85);

    private static final int ANCHO = 470;

    /** Total a cobrar (no cambia nunca). */
    private final double total;

    private JTextField txtPago;
    private JLabel lblCaption;
    private JLabel lblVuelto;

    public FormVuelto(Window dueno, String titulo, double total, String nota) {
        super(dueno, "Vuelto", ModalityType.APPLICATION_MODAL);
        this.total = total;
        construir(titulo == null || titulo.trim().isEmpty() ? "Vuelto" : titulo,
                nota == null ? "" : nota);
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
        FormVuelto d = new FormVuelto(SwingUtilities.getWindowAncestor(padre), titulo, total, nota);
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }

    // ==================== armado de la ventana ====================

    private void construir(String titulo, String nota) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(FONDO);
        p.setPreferredSize(new Dimension(ANCHO, 372));

        JLabel lblTitulo = texto(titulo, 1, 15, TEXTO);
        lblTitulo.setBounds(20, 16, ANCHO - 40, 24);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblTitulo);

        JLabel lblNota = texto(nota, 0, 11, GRIS);
        lblNota.setBounds(20, 40, ANCHO - 40, 16);
        lblNota.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblNota);

        JLabel lblTotal = texto("Total: " + UiTheme.gs(total), 1, 22, CELESTE);
        lblTotal.setBounds(20, 64, ANCHO - 40, 30);
        lblTotal.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblTotal);

        JLabel lblPago = texto("¿Con cuánto pagó el cliente?", 0, 12, GRIS);
        lblPago.setBounds(20, 108, ANCHO - 40, 18);
        p.add(lblPago);

        txtPago = new JTextField();
        txtPago.setBackground(CAMPO);
        txtPago.setFont(new Font("Segoe UI", 1, 22));
        txtPago.setForeground(TEXTO);
        txtPago.setCaretColor(TEXTO);
        txtPago.setHorizontalAlignment(SwingConstants.RIGHT);
        txtPago.setBorder(javax.swing.BorderFactory.createLineBorder(BOTON, 1));
        txtPago.setToolTipText("Escribí con cuánto pagó el cliente (Enter para cerrar)");
        UiTheme.miles(txtPago, true);
        txtPago.setBounds(20, 128, ANCHO - 40, 48);
        txtPago.addActionListener(e -> dispose());
        txtPago.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calcular(); }
            @Override public void removeUpdate(DocumentEvent e) { calcular(); }
            @Override public void changedUpdate(DocumentEvent e) { calcular(); }
        });
        p.add(txtPago);

        // ---- botones rapidos ----
        JButton btnJusto = boton("Justo", BOTON, TEXTO);
        btnJusto.setToolTipText("Paga con el importe exacto");
        btnJusto.setBounds(20, 186, 136, 34);
        btnJusto.addActionListener(e -> setPago((long) total));
        p.add(btnJusto);

        JButton b10 = boton("+ 10.000", BOTON, TEXTO);
        b10.setBounds(167, 186, 136, 34);
        b10.addActionListener(e -> sumar(10000));
        p.add(b10);

        JButton b20 = boton("+ 20.000", BOTON, TEXTO);
        b20.setBounds(314, 186, 136, 34);
        b20.addActionListener(e -> sumar(20000));
        p.add(b20);

        JButton b50 = boton("+ 50.000", BOTON, TEXTO);
        b50.setBounds(20, 224, 136, 34);
        b50.addActionListener(e -> sumar(50000));
        p.add(b50);

        JButton b100 = boton("+ 100.000", BOTON, TEXTO);
        b100.setBounds(167, 224, 136, 34);
        b100.addActionListener(e -> sumar(100000));
        p.add(b100);

        JButton btnLimpiar = boton("Limpiar", BOTON, TEXTO);
        btnLimpiar.setBounds(314, 224, 136, 34);
        btnLimpiar.addActionListener(e -> { txtPago.setText(""); txtPago.requestFocusInWindow(); });
        p.add(btnLimpiar);

        // ---- resultado ----
        lblCaption = texto("VUELTO A ENTREGAR", 1, 12, GRIS);
        lblCaption.setBounds(20, 268, ANCHO - 40, 18);
        lblCaption.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblCaption);

        lblVuelto = texto(UiTheme.gs(0), 1, 30, GRIS);
        lblVuelto.setBounds(20, 288, ANCHO - 40, 40);
        lblVuelto.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblVuelto);

        JLabel lblAyuda = texto("Enter o Esc para cerrar", 0, 10, GRIS);
        lblAyuda.setBounds(20, 344, 250, 16);
        p.add(lblAyuda);

        JButton btnListo = boton("Listo", new Color(22, 163, 74), Color.WHITE);
        btnListo.setBounds(300, 336, 150, 30);
        btnListo.addActionListener(e -> dispose());
        p.add(btnListo);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                txtPago.requestFocusInWindow();
            }
        });

        setContentPane(p);
        pack();
        calcular();
    }

    private static JLabel texto(String t, int negrita, int tamano, Color color) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", negrita, tamano));
        l.setForeground(color);
        return l;
    }

    private static JButton boton(String t, Color fondo, Color colorTexto) {
        JButton b = new JButton(t);
        b.setBackground(fondo);
        b.setForeground(colorTexto);
        b.setFont(new Font("Segoe UI", 1, 12));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return b;
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
}
