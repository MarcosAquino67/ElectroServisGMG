/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.clases.Equipo;
import com.electroservisgmg.clases.TipoEquipo;
import com.electroservisgmg.metodos.ClienteMetodos;
import com.electroservisgmg.metodos.EquipoMetodos;
import com.electroservisgmg.metodos.TipoEquipoMetodos;

/**
 *
 * @author Aurelio
 */
public class FormEquipos extends javax.swing.JPanel {

    private java.util.List<Cliente> listaClientes = new java.util.ArrayList<>();
    private javax.swing.JButton btnAddTipo;

    /**
     * Creates new form FormEquipos
     */
    public FormEquipos() {
        initComponents();
        crearBotonAddTipo();
        cargarClientes();
        cargarTipos();
        UiTheme.styleTable(tblEquipos);
        cargarTabla("");
        // Los botones y el campo de busqueda se conectan desde el disenador
        // (pestana Events -> actionPerformed). Aca quedan solo los extras que
        // el disenador no modela.
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
        });
        tblEquipos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) llenarDesdeFila();
        });
    }

    /** Boton "+" para agregar tipos desde la BD (fuera del GEN). */
    private void crearBotonAddTipo() {
        panelForm.remove(cboTipo);
        panelForm.add(cboTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, 105, 35));
        btnAddTipo = new javax.swing.JButton("+");
        btnAddTipo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        btnAddTipo.setBackground(new java.awt.Color(51, 65, 85));
        btnAddTipo.setForeground(new java.awt.Color(56, 189, 248));
        btnAddTipo.setBorderPainted(false);
        btnAddTipo.setFocusPainted(false);
        btnAddTipo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddTipo.setToolTipText("Agregar tipo");
        btnAddTipo.addActionListener(e -> agregarTipo());
        panelForm.add(btnAddTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 40, 45, 35));
    }

    private void cargarClientes() {
        listaClientes = new ClienteMetodos().listar();
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        for (Cliente c : listaClientes) m.addElement(c.getIdCliente() + " - " + c.getNombre() + " " + c.getApellido());
        cboCliente.setModel(m);
    }

    private void cargarTipos() {
        Object sel = cboTipo.getSelectedItem();
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        for (TipoEquipo t : new TipoEquipoMetodos().listar()) m.addElement(t.getNombre());
        cboTipo.setModel(m);
        if (sel != null) cboTipo.setSelectedItem(sel.toString());
    }

    private void agregarTipo() {
        String n = javax.swing.JOptionPane.showInputDialog(this, "Nuevo tipo de equipo:", "Agregar tipo", javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (n == null) return;
        n = n.trim().toUpperCase();
        if (n.isEmpty()) return;
        if (new TipoEquipoMetodos().insertar(n)) {
            cargarTipos();
            cboTipo.setSelectedItem(n);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "No se pudo agregar (¿ya existe?)");
        }
    }

    private void seleccionarCliente(int id) {
        for (int i = 0; i < listaClientes.size(); i++) {
            if (listaClientes.get(i).getIdCliente() == id) { cboCliente.setSelectedIndex(i); return; }
        }
    }

    private void cargarTabla(String filtro) {
        new EquipoMetodos().mostrar(tblEquipos, filtro);
        UiTheme.styleTable(tblEquipos);
    }

    private void limpiar() {
        txtId.setText("");
        if (cboCliente.getItemCount() > 0) cboCliente.setSelectedIndex(0);
        if (cboTipo.getItemCount() > 0) cboTipo.setSelectedIndex(0);
        txtMarca.setText("");
        txtModelo.setText("");
        txtSerie.setText("");
        txtFalla.setText("");
        tblEquipos.clearSelection();
        txtMarca.requestFocus();
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    private void llenarDesdeFila() {
        int fila = tblEquipos.getSelectedRow();
        if (fila < 0) return;
        int id = (int) tblEquipos.getValueAt(fila, 0);
        txtId.setText(String.valueOf(id));
        EquipoMetodos em = new EquipoMetodos();
        for (Equipo e : em.listar()) {
            if (e.getIdEquipo() == id) {
                seleccionarCliente(e.getIdCliente());
                cboTipo.setSelectedItem(e.getTipo());
                txtMarca.setText(e.getMarca() == null ? "" : e.getMarca());
                txtModelo.setText(e.getModelo() == null ? "" : e.getModelo());
                txtSerie.setText(e.getNroSerie() == null ? "" : e.getNroSerie());
                txtFalla.setText(e.getFallaReportada() == null ? "" : e.getFallaReportada());
                break;
            }
        }
    }

    private void editar() {
        if (tblEquipos.getSelectedRow() < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un equipo de la tabla");
            return;
        }
        llenarDesdeFila();
        txtMarca.requestFocus();
    }

    private void guardar() {
        if (new EquipoMetodos().guardar(txtId, cboCliente, cboTipo, txtMarca, txtModelo, txtSerie, txtFalla)) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    private void eliminar() {
        int fila = tblEquipos.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un equipo de la tabla");
            return;
        }
        int conf = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Eliminar equipo '" + tblEquipos.getValueAt(fila, 3) + " " + tblEquipos.getValueAt(fila, 4) + "'?",
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (conf != javax.swing.JOptionPane.YES_OPTION) return;
        if (new EquipoMetodos().eliminar(txtId)) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
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
        tblEquipos = new javax.swing.JTable();
        panelForm = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        lblCliente = new javax.swing.JLabel();
        lblTipo = new javax.swing.JLabel();
        lblMarca = new javax.swing.JLabel();
        lblModelo = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtMarca = new javax.swing.JTextField();
        txtModelo = new javax.swing.JTextField();
        cboCliente = new javax.swing.JComboBox<>();
        cboTipo = new javax.swing.JComboBox<>();
        lblSerie = new javax.swing.JLabel();
        lblFalla = new javax.swing.JLabel();
        txtSerie = new javax.swing.JTextField();
        txtFalla = new javax.swing.JTextField();
        btnGuardar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(18, 24, 36));
        jPanel1.setLayout(new java.awt.BorderLayout());

        panelNorte.setBackground(new java.awt.Color(18, 24, 36));
        panelNorte.setPreferredSize(new java.awt.Dimension(0, 130));
        panelNorte.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("EQUIPOS");
        panelNorte.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 30));

        lblSub.setForeground(new java.awt.Color(156, 163, 175));
        lblSub.setText("Gestión de equipos del taller");
        panelNorte.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 500, 20));

        txtBuscar.setBackground(new java.awt.Color(15, 23, 42));
        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(243, 244, 246));
        txtBuscar.addActionListener(this::txtBuscarActionPerformed);
        panelNorte.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 75, 250, 35));

        btnBuscar.setBackground(new java.awt.Color(51, 65, 85));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(243, 244, 246));
        btnBuscar.setText("Buscar");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        panelNorte.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 75, 100, 35));

        btnNuevo.setBackground(new java.awt.Color(56, 189, 248));
        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(51, 51, 51));
        btnNuevo.setText("+ Nuevo");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(this::btnNuevoActionPerformed);
        panelNorte.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(385, 75, 110, 35));

        btnEditar.setBackground(new java.awt.Color(51, 65, 85));
        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEditar.setForeground(new java.awt.Color(243, 244, 246));
        btnEditar.setText("Editar");
        btnEditar.setBorderPainted(false);
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        panelNorte.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 75, 100, 35));

        btnEliminar.setBackground(new java.awt.Color(239, 68, 68));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar");
        btnEliminar.setBorderPainted(false);
        btnEliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        panelNorte.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 75, 110, 35));

        jPanel1.add(panelNorte, java.awt.BorderLayout.PAGE_START);

        panelTabla.setBackground(new java.awt.Color(18, 24, 36));
        panelTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));
        panelTabla.setLayout(new java.awt.BorderLayout());

        tblEquipos.setBackground(new java.awt.Color(30, 41, 59));
        tblEquipos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblEquipos.setForeground(new java.awt.Color(243, 244, 246));
        tblEquipos.setModel(new javax.swing.table.DefaultTableModel(
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
        tblEquipos.setFillsViewportHeight(true);
        tblEquipos.setGridColor(new java.awt.Color(51, 65, 85));
        tblEquipos.setRowHeight(32);
        tblEquipos.setSelectionBackground(new java.awt.Color(2, 132, 199));
        tblEquipos.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblEquipos.setShowHorizontalLines(true);
        scroll.setViewportView(tblEquipos);

        panelTabla.add(scroll, java.awt.BorderLayout.CENTER);

        jPanel1.add(panelTabla, java.awt.BorderLayout.CENTER);

        panelForm.setBackground(new java.awt.Color(30, 41, 59));
        panelForm.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        panelForm.setPreferredSize(new java.awt.Dimension(0, 190));
        panelForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblId.setForeground(new java.awt.Color(156, 163, 175));
        lblId.setText("ID");
        panelForm.add(lblId, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 15, 60, 25));

        lblCliente.setForeground(new java.awt.Color(156, 163, 175));
        lblCliente.setText("Cliente");
        panelForm.add(lblCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 15, 250, 25));

        lblTipo.setForeground(new java.awt.Color(156, 163, 175));
        lblTipo.setText("Tipo");
        panelForm.add(lblTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 15, 150, 25));

        lblMarca.setForeground(new java.awt.Color(156, 163, 175));
        lblMarca.setText("Marca");
        panelForm.add(lblMarca, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 15, 150, 25));

        lblModelo.setForeground(new java.awt.Color(156, 163, 175));
        lblModelo.setText("Modelo");
        panelForm.add(lblModelo, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 15, 180, 25));

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(15, 23, 42));
        txtId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtId.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 40, 80, 35));

        txtMarca.setBackground(new java.awt.Color(15, 23, 42));
        txtMarca.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtMarca.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtMarca, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 40, 150, 35));

        txtModelo.setBackground(new java.awt.Color(15, 23, 42));
        txtModelo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtModelo.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtModelo, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 40, 180, 35));

        cboCliente.setBackground(new java.awt.Color(15, 23, 42));
        cboCliente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboCliente.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(cboCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 250, 35));

        cboTipo.setBackground(new java.awt.Color(15, 23, 42));
        cboTipo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboTipo.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(cboTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, 150, 35));
        cboTipo.getAccessibleContext().setAccessibleName("");

        lblSerie.setForeground(new java.awt.Color(156, 163, 175));
        lblSerie.setText("N° Serie");
        panelForm.add(lblSerie, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 85, 200, 25));

        lblFalla.setForeground(new java.awt.Color(156, 163, 175));
        lblFalla.setText("Falla reportada");
        panelForm.add(lblFalla, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 85, 450, 25));

        txtSerie.setBackground(new java.awt.Color(15, 23, 42));
        txtSerie.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtSerie.setForeground(new java.awt.Color(243, 244, 246));
        txtSerie.addActionListener(this::txtSerieActionPerformed);
        panelForm.add(txtSerie, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 110, 200, 35));

        txtFalla.setBackground(new java.awt.Color(15, 23, 42));
        txtFalla.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtFalla.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtFalla, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 110, 150, 35));

        btnGuardar.setBackground(new java.awt.Color(34, 197, 94));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        panelForm.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 110, 150, 38));

        btnLimpiar.setBackground(new java.awt.Color(51, 65, 85));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(243, 244, 246));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        panelForm.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 110, 150, 38));

        jPanel1.add(panelForm, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1049, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtSerieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSerieActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSerieActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        limpiar();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_txtBuscarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<String> cboCliente;
    private javax.swing.JComboBox<String> cboTipo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblFalla;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblModelo;
    private javax.swing.JLabel lblSerie;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelForm;
    private javax.swing.JPanel panelNorte;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JTable tblEquipos;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtFalla;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtMarca;
    private javax.swing.JTextField txtModelo;
    private javax.swing.JTextField txtSerie;
    // End of variables declaration//GEN-END:variables
}
