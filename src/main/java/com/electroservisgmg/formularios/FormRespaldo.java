package com.electroservisgmg.formularios;

import com.electroservisgmg.conexion.Conexion;
import com.electroservisgmg.metodos.RespaldoMetodos;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Ventana de respaldo: guarda toda la base en un archivo .sql y tambien
 * permite volver a cargar un respaldo anterior.
 *
 * Se abre desde Ajustes. No depende de mysqldump ni de ninguna herramienta
 * externa: el respaldo lo arma el mismo sistema.
 *
 * @author Aurelio
 */
public class FormRespaldo extends javax.swing.JDialog {

    // ---- colores que se usan al mostrar el estado del proceso (fuera del GEN) ----
    private static final Color TEXTO = new Color(243, 244, 246);
    private static final Color GRIS = new Color(156, 163, 175);
    private static final Color VERDE = new Color(22, 163, 74);
    private static final Color ROJO = new Color(153, 27, 27);

    /**
     * Creates new form FormRespaldo
     */
    public FormRespaldo(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        // ---- detalles que no hacen falta en el disenador ----
        btnRespaldar.setToolTipText("Guarda todos los datos en un archivo .sql");
        btnRestaurar.setToolTipText("Reemplaza los datos actuales por los de un respaldo anterior");

        // ---- eventos ----
        // Los botones se conectan desde el disenador (pestana Events ->
        // actionPerformed); aca queda solo lo que el diseno no modela.
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        refrescarUltimo();
    }

    /**
     * Ventana de respaldo de la base de datos.
     *
     * @param dueno ventana de la que depende (puede ser null)
     */
    public FormRespaldo(Window dueno) {
        this(dueno instanceof java.awt.Frame ? (java.awt.Frame) dueno : null, true);
    }

    /** Abre la ventana y espera a que la cierren. */
    public static void mostrar(Component padre) {
        FormRespaldo d = new FormRespaldo(
                padre == null ? null : SwingUtilities.getWindowAncestor(padre));
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }

    // ==================== acciones ====================

    private void refrescarUltimo() {
        File ultimo = RespaldoMetodos.ultimoRespaldo();
        if (ultimo == null) {
            lblUltimo.setText("Todavía no hiciste ningún respaldo");
            lblUltimo.setForeground(GRIS);
            lblUltimo.setToolTipText(null);
        } else {
            java.text.DecimalFormat miles = new java.text.DecimalFormat("#,##0");
            lblUltimo.setText(ultimo.getName() + "   (" + miles.format(ultimo.length() / 1024.0) + " KB, "
                    + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(ultimo.lastModified())) + ")");
            lblUltimo.setForeground(TEXTO);
            lblUltimo.setToolTipText(ultimo.getAbsolutePath());
        }
    }

    private void respaldar() {
        JFileChooser fc = new JFileChooser(RespaldoMetodos.carpetaRespaldos());
        fc.setDialogTitle("¿Dónde guardo el respaldo?");
        fc.setSelectedFile(new File(RespaldoMetodos.carpetaRespaldos(), RespaldoMetodos.nombreSugerido()));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File destino = fc.getSelectedFile();
        if (destino == null) return;
        if (!destino.getName().toLowerCase().endsWith(".sql")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".sql");
        }
        if (destino.exists()) {
            int conf = JOptionPane.showConfirmDialog(this,
                    "Ya existe " + destino.getName() + ".\n¿Lo reemplazo?",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf != JOptionPane.YES_OPTION) return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            RespaldoMetodos rm = new RespaldoMetodos();
            rm.respaldar(destino);
            estado("Respaldo listo ✔   " + rm.ultimasTablas + " tablas, "
                    + new java.text.DecimalFormat("#,##0").format(rm.ultimasFilas) + " filas.", VERDE);
            refrescarUltimo();
            JOptionPane.showMessageDialog(this, "Respaldo guardado en:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            estado("No se pudo respaldar: " + mensaje(e), ROJO);
            JOptionPane.showMessageDialog(this, "No se pudo respaldar:\n" + mensaje(e));
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void restaurar() {
        JFileChooser fc = new JFileChooser(RespaldoMetodos.carpetaRespaldos());
        fc.setDialogTitle("Elegí el respaldo que querés restaurar");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = fc.getSelectedFile();
        if (archivo == null) return;

        int conf = JOptionPane.showConfirmDialog(this,
                "Vas a reemplazar TODOS los datos actuales por los del respaldo:\n\n"
                + archivo.getName() + "\n\n"
                + "Lo que hay cargado ahora en el sistema se pierde.\n¿Seguro que querés restaurar?",
                "Confirmar restauración", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            int n = new RespaldoMetodos().restaurar(archivo);
            Conexion.cerrarConexion();     // que la proxima consulta vuelva a conectar
            estado("Restauración lista ✔   " + n + " sentencias ejecutadas.", VERDE);
            JOptionPane.showMessageDialog(this,
                    "Respaldo restaurado (" + n + " sentencias).\n\n"
                    + "Volvé a abrir el Panel Principal para ver los datos al día.");
        } catch (Exception e) {
            estado("No se pudo restaurar: " + mensaje(e), ROJO);
            JOptionPane.showMessageDialog(this, "No se pudo restaurar:\n" + mensaje(e));
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void abrirCarpeta() {
        try {
            File dir = RespaldoMetodos.carpetaRespaldos();
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(dir);
            else estado("Carpeta: " + dir.getAbsolutePath(), GRIS);
        } catch (Exception e) {
            estado("No se pudo abrir la carpeta: " + mensaje(e), ROJO);
        }
    }

    private void estado(String texto, Color color) {
        lblEstado.setText("<html>" + texto.replace("\n", "<br>") + "</html>");
        lblEstado.setForeground(color);
    }

    private static String mensaje(Exception e) {
        String m = e.getMessage();
        return m == null || m.trim().isEmpty() ? e.getClass().getSimpleName() : m;
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
        lblAyuda = new javax.swing.JLabel();
        card = new javax.swing.JPanel();
        lblCardTitulo = new javax.swing.JLabel();
        lblUltimo = new javax.swing.JLabel();
        lblCarpeta = new javax.swing.JLabel();
        btnRespaldar = new javax.swing.JButton();
        btnRestaurar = new javax.swing.JButton();
        btnCarpeta = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        lblEstado = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setTitle("Respaldo de la base de datos");

        panelContenido.setBackground(new java.awt.Color(18, 24, 36));
        panelContenido.setPreferredSize(new java.awt.Dimension(600, 400));
        panelContenido.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("Respaldo de la base de datos");
        panelContenido.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 18, 560, 26));

        lblAyuda.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblAyuda.setForeground(new java.awt.Color(156, 163, 175));
        lblAyuda.setText("<html>Guardá una copia de todos los datos del sistema en un archivo <b>.sql</b>.<br>También podés volver a cargar un respaldo anterior.</html>");
        panelContenido.add(lblAyuda, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 46, 560, 40));

        card.setBackground(new java.awt.Color(30, 41, 59));
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        card.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblCardTitulo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCardTitulo.setForeground(new java.awt.Color(156, 163, 175));
        lblCardTitulo.setText("Último respaldo");
        card.add(lblCardTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 8, 200, 18));

        lblUltimo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblUltimo.setForeground(new java.awt.Color(243, 244, 246));
        lblUltimo.setText("-");
        card.add(lblUltimo, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 28, 470, 20));

        lblCarpeta.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblCarpeta.setForeground(new java.awt.Color(156, 163, 175));
        lblCarpeta.setText("Los respaldos se guardan en la carpeta \"respaldos\" del sistema.");
        card.add(lblCarpeta, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 50, 470, 18));

        panelContenido.add(card, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 92, 560, 76));

        btnRespaldar.setBackground(new java.awt.Color(22, 163, 74));
        btnRespaldar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRespaldar.setForeground(new java.awt.Color(255, 255, 255));
        btnRespaldar.setText("Respaldar ahora…");
        btnRespaldar.setBorderPainted(false);
        btnRespaldar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRespaldar.setFocusPainted(false);
        btnRespaldar.addActionListener(this::btnRespaldarActionPerformed);
        panelContenido.add(btnRespaldar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 186, 270, 46));

        btnRestaurar.setBackground(new java.awt.Color(153, 27, 27));
        btnRestaurar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRestaurar.setForeground(new java.awt.Color(255, 255, 255));
        btnRestaurar.setText("Restaurar desde archivo…");
        btnRestaurar.setBorderPainted(false);
        btnRestaurar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRestaurar.setFocusPainted(false);
        btnRestaurar.addActionListener(this::btnRestaurarActionPerformed);
        panelContenido.add(btnRestaurar, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 186, 270, 46));

        btnCarpeta.setBackground(new java.awt.Color(51, 65, 85));
        btnCarpeta.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCarpeta.setForeground(new java.awt.Color(243, 244, 246));
        btnCarpeta.setText("Abrir la carpeta de respaldos");
        btnCarpeta.setBorderPainted(false);
        btnCarpeta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCarpeta.setFocusPainted(false);
        btnCarpeta.addActionListener(this::btnCarpetaActionPerformed);
        panelContenido.add(btnCarpeta, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 244, 270, 38));

        btnCerrar.setBackground(new java.awt.Color(51, 65, 85));
        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCerrar.setForeground(new java.awt.Color(243, 244, 246));
        btnCerrar.setText("Cerrar");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        panelContenido.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 244, 270, 38));

        lblEstado.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(156, 163, 175));
        lblEstado.setText(" ");
        panelContenido.add(lblEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 296, 560, 60));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRespaldarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRespaldarActionPerformed
        respaldar();
    }//GEN-LAST:event_btnRespaldarActionPerformed

    private void btnRestaurarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestaurarActionPerformed
        restaurar();
    }//GEN-LAST:event_btnRestaurarActionPerformed

    private void btnCarpetaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCarpetaActionPerformed
        abrirCarpeta();
    }//GEN-LAST:event_btnCarpetaActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        dispose();
    }//GEN-LAST:event_btnCerrarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCarpeta;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnRespaldar;
    private javax.swing.JButton btnRestaurar;
    private javax.swing.JPanel card;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblCardTitulo;
    private javax.swing.JLabel lblCarpeta;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUltimo;
    private javax.swing.JPanel panelContenido;
    // End of variables declaration//GEN-END:variables
}
