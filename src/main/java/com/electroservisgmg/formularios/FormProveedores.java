/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Proveedor;
import com.electroservisgmg.metodos.ProveedorMetodos;

/**
 *
 * @author Aurelio
 */
public class FormProveedores extends javax.swing.JPanel {

    /**
     * Creates new form FormProveedores
     */
    public FormProveedores() {
        initComponents();
        txtBuscar.setText("");
        UiTheme.styleTable(tblProveedores);
        cargarTabla("");
        btnBuscar.addActionListener(e -> buscar());
        btnNuevo.addActionListener(e -> limpiar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
        txtBuscar.addActionListener(e -> buscar());
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
        });
        tblProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) llenarDesdeFila();
        });
    }

    private void cargarTabla(String filtro) {
        ProveedorMetodos pm = new ProveedorMetodos();
        java.util.List<Proveedor> lista = pm.listar();
        String f = filtro == null ? "" : filtro.toLowerCase();
        java.util.List<Proveedor> fil = new java.util.ArrayList<>();
        for (Proveedor p : lista) {
            if (f.isEmpty() || p.getNombre().toLowerCase().contains(f)
                    || p.getRuc().toLowerCase().contains(f)) fil.add(p);
        }
        String[] cols = {"ID", "Nombre", "RUC", "Teléfono", "Email"};
        Object[][] data = new Object[fil.size()][5];
        for (int i = 0; i < fil.size(); i++) {
            Proveedor p = fil.get(i);
            data[i] = new Object[]{p.getIdProveedor(), p.getNombre(), p.getRuc(),
                p.getTelefono(), p.getEmail()};
        }
        tblProveedores.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        UiTheme.styleTable(tblProveedores);
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtRuc.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtDireccion.setText("");
        tblProveedores.clearSelection();
        txtNombre.requestFocus();
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    private void llenarDesdeFila() {
        int fila = tblProveedores.getSelectedRow();
        if (fila < 0) return;
        txtId.setText(String.valueOf(tblProveedores.getValueAt(fila, 0)));
        txtNombre.setText(String.valueOf(tblProveedores.getValueAt(fila, 1)));
        txtRuc.setText(String.valueOf(tblProveedores.getValueAt(fila, 2)));
        txtTelefono.setText(String.valueOf(tblProveedores.getValueAt(fila, 3)));
        txtEmail.setText(String.valueOf(tblProveedores.getValueAt(fila, 4)));
        ProveedorMetodos pm = new ProveedorMetodos();
        for (Proveedor p : pm.listar()) {
            if (String.valueOf(p.getIdProveedor()).equals(txtId.getText())) {
                txtDireccion.setText(p.getDireccion() == null ? "" : p.getDireccion());
                break;
            }
        }
    }

    private void editar() {
        if (tblProveedores.getSelectedRow() < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un proveedor de la tabla");
            return;
        }
        llenarDesdeFila();
        txtNombre.requestFocus();
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
            return;
        }
        Proveedor p = new Proveedor();
        p.setNombre(nombre);
        p.setRuc(txtRuc.getText().trim());
        p.setTelefono(txtTelefono.getText().trim());
        p.setEmail(txtEmail.getText().trim());
        p.setDireccion(txtDireccion.getText().trim());
        ProveedorMetodos pm = new ProveedorMetodos();
        boolean ok;
        if (txtId.getText().trim().isEmpty()) {
            ok = pm.insertar(p);
        } else {
            p.setIdProveedor(Integer.parseInt(txtId.getText().trim()));
            ok = pm.modificar(p);
        }
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Guardado correctamente" : "Error al guardar");
        if (ok) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    private void eliminar() {
        int fila = tblProveedores.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un proveedor de la tabla");
            return;
        }
        int id = (int) tblProveedores.getValueAt(fila, 0);
        int conf = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Eliminar proveedor '" + tblProveedores.getValueAt(fila, 1) + "'?",
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (conf != javax.swing.JOptionPane.YES_OPTION) return;
        boolean ok = new ProveedorMetodos().eliminar(id);
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Eliminado"
                : "No se puede eliminar: tiene compras o productos asociados");
        if (ok) {
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
        tblProveedores = new javax.swing.JTable();
        panelForm = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        lblNombre = new javax.swing.JLabel();
        lblRuc = new javax.swing.JLabel();
        lblTelefono = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        lblDireccion = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtRuc = new javax.swing.JTextField();
        txtDireccion = new javax.swing.JTextField();
        btnGuardar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(18, 24, 36));
        jPanel1.setLayout(new java.awt.BorderLayout());

        panelNorte.setBackground(new java.awt.Color(18, 24, 36));
        panelNorte.setPreferredSize(new java.awt.Dimension(0, 130));
        panelNorte.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("PROVEEDORES");
        panelNorte.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 20));

        lblSub.setForeground(new java.awt.Color(156, 163, 175));
        lblSub.setText("Gestión de proveedores del taller");
        panelNorte.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 500, 20));

        txtBuscar.setBackground(new java.awt.Color(15, 23, 42));
        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(243, 244, 246));
        txtBuscar.setText("jTextField1");
        panelNorte.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 75, 250, 35));

        btnBuscar.setBackground(new java.awt.Color(51, 65, 85));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(243, 244, 246));
        btnBuscar.setText("Buscar");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setFocusPainted(false);
        panelNorte.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 75, 100, 35));

        btnNuevo.setBackground(new java.awt.Color(56, 189, 248));
        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(15, 23, 42));
        btnNuevo.setText("+ Nuevo");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setFocusPainted(false);
        panelNorte.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(385, 75, 110, 35));

        btnEditar.setBackground(new java.awt.Color(51, 65, 85));
        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEditar.setForeground(new java.awt.Color(243, 244, 246));
        btnEditar.setText("Editar");
        btnEditar.setBorderPainted(false);
        btnEditar.setFocusPainted(false);
        panelNorte.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 75, 100, 35));

        btnEliminar.setBackground(new java.awt.Color(239, 68, 68));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar");
        btnEliminar.setBorderPainted(false);
        btnEliminar.setFocusPainted(false);
        panelNorte.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 75, 110, 35));

        jPanel1.add(panelNorte, java.awt.BorderLayout.NORTH);

        panelTabla.setBackground(new java.awt.Color(18, 24, 36));
        panelTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));
        panelTabla.setLayout(new java.awt.BorderLayout());

        tblProveedores.setBackground(new java.awt.Color(30, 41, 59));
        tblProveedores.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblProveedores.setForeground(new java.awt.Color(243, 244, 246));
        tblProveedores.setModel(new javax.swing.table.DefaultTableModel(
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
        tblProveedores.setFillsViewportHeight(true);
        tblProveedores.setGridColor(new java.awt.Color(51, 65, 85));
        tblProveedores.setRowHeight(32);
        tblProveedores.setSelectionBackground(new java.awt.Color(2, 132, 199));
        tblProveedores.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblProveedores.setShowGrid(false);
        scroll.setViewportView(tblProveedores);

        panelTabla.add(scroll, java.awt.BorderLayout.CENTER);

        jPanel1.add(panelTabla, java.awt.BorderLayout.CENTER);

        panelForm.setBackground(new java.awt.Color(30, 41, 59));
        panelForm.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        panelForm.setPreferredSize(new java.awt.Dimension(0, 190));
        panelForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblId.setForeground(new java.awt.Color(156, 163, 175));
        lblId.setText("ID");
        panelForm.add(lblId, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 60, 25));

        lblNombre.setForeground(new java.awt.Color(156, 163, 175));
        lblNombre.setText("Nombre");
        panelForm.add(lblNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 15, 250, 25));

        lblRuc.setForeground(new java.awt.Color(156, 163, 175));
        lblRuc.setText("RUC");
        panelForm.add(lblRuc, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 15, 180, 25));

        lblTelefono.setForeground(new java.awt.Color(156, 163, 175));
        lblTelefono.setText("Teléfono");
        panelForm.add(lblTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 15, 230, 25));

        lblEmail.setForeground(new java.awt.Color(156, 163, 175));
        lblEmail.setText("Email");
        panelForm.add(lblEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 15, 230, 25));

        lblDireccion.setForeground(new java.awt.Color(156, 163, 175));
        lblDireccion.setText("Dirección");
        panelForm.add(lblDireccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 85, 400, 25));

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(15, 23, 42));
        txtId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtId.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 80, 35));

        txtTelefono.setBackground(new java.awt.Color(15, 23, 42));
        txtTelefono.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtTelefono.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, 180, 35));

        txtNombre.setBackground(new java.awt.Color(15, 23, 42));
        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtNombre.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 250, 35));

        txtEmail.setBackground(new java.awt.Color(15, 23, 42));
        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmail.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 40, 230, 35));

        txtRuc.setBackground(new java.awt.Color(15, 23, 42));
        txtRuc.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtRuc.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtRuc, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, 180, 35));

        txtDireccion.setBackground(new java.awt.Color(15, 23, 42));
        txtDireccion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtDireccion.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtDireccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 690, 35));

        btnGuardar.setBackground(new java.awt.Color(34, 197, 94));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setFocusPainted(false);
        panelForm.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 110, 150, 38));

        btnLimpiar.setBackground(new java.awt.Color(51, 65, 85));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(243, 244, 246));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setFocusPainted(false);
        panelForm.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 110, 180, 35));

        jPanel1.add(panelForm, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1110, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblRuc;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelForm;
    private javax.swing.JPanel panelNorte;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JTable tblProveedores;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtRuc;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
