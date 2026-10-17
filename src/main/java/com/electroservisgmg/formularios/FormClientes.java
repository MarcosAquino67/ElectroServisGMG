/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.metodos.ClienteMetodos;

/**
 *
 * @author Aurelio
 */
public class FormClientes extends javax.swing.JPanel {

    /**
     * Creates new form FormClientes
     */
    public FormClientes() {
        initComponents();
        UiTheme.styleTable(tblClientes);
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
        tblClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) llenarDesdeFila();
        });
    }

    private void cargarTabla(String filtro) {
        ClienteMetodos cm = new ClienteMetodos();
        java.util.List<Cliente> lista = cm.listar();
        String f = filtro == null ? "" : filtro.toLowerCase();
        java.util.List<Cliente> fil = new java.util.ArrayList<>();
        for (Cliente c : lista) {
            if (f.isEmpty() || c.getNombre().toLowerCase().contains(f)
                    || c.getApellido().toLowerCase().contains(f)
                    || c.getCedula().toLowerCase().contains(f)) fil.add(c);
        }
        String[] cols = {"ID", "Nombre", "Apellido", "Cédula", "Teléfono", "Email"};
        Object[][] data = new Object[fil.size()][6];
        for (int i = 0; i < fil.size(); i++) {
            Cliente c = fil.get(i);
            data[i] = new Object[]{c.getIdCliente(), c.getNombre(), c.getApellido(),
                c.getCedula(), c.getTelefono(), c.getEmail()};
        }
        tblClientes.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        UiTheme.styleTable(tblClientes);
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtCedula.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtDireccion.setText("");
        tblClientes.clearSelection();
        txtNombre.requestFocus();
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    private void llenarDesdeFila() {
        int fila = tblClientes.getSelectedRow();
        if (fila < 0) return;
        txtId.setText(String.valueOf(tblClientes.getValueAt(fila, 0)));
        txtNombre.setText(String.valueOf(tblClientes.getValueAt(fila, 1)));
        txtApellido.setText(String.valueOf(tblClientes.getValueAt(fila, 2)));
        txtCedula.setText(String.valueOf(tblClientes.getValueAt(fila, 3)));
        txtTelefono.setText(String.valueOf(tblClientes.getValueAt(fila, 4)));
        txtEmail.setText(String.valueOf(tblClientes.getValueAt(fila, 5)));
        ClienteMetodos cm = new ClienteMetodos();
        for (Cliente c : cm.listar()) {
            if (String.valueOf(c.getIdCliente()).equals(txtId.getText())) {
                txtDireccion.setText(c.getDireccion() == null ? "" : c.getDireccion());
                break;
            }
        }
    }

    private void editar() {
        if (tblClientes.getSelectedRow() < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla");
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
        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setApellido(txtApellido.getText().trim());
        c.setCedula(txtCedula.getText().trim());
        c.setTelefono(txtTelefono.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setDireccion(txtDireccion.getText().trim());
        ClienteMetodos cm = new ClienteMetodos();
        boolean ok;
        if (txtId.getText().trim().isEmpty()) {
            ok = cm.insertar(c);
        } else {
            c.setIdCliente(Integer.parseInt(txtId.getText().trim()));
            ok = cm.modificar(c);
        }
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Guardado correctamente" : "Error al guardar");
        if (ok) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    private void eliminar() {
        int fila = tblClientes.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla");
            return;
        }
        int id = (int) tblClientes.getValueAt(fila, 0);
        int conf = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Eliminar cliente '" + tblClientes.getValueAt(fila, 1) + " " + tblClientes.getValueAt(fila, 2) + "'?",
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (conf != javax.swing.JOptionPane.YES_OPTION) return;
        boolean ok = new ClienteMetodos().eliminar(id);
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Eliminado"
                : "No se puede eliminar: tiene ventas u órdenes asociadas");
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
        btnNuevo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        panelTabla = new javax.swing.JPanel();
        scroll = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        panelForm = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        lblNombre = new javax.swing.JLabel();
        lblCedula = new javax.swing.JLabel();
        lblTelefono = new javax.swing.JLabel();
        lblApellido = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtCedula = new javax.swing.JTextField();
        txtApellido = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtId = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        lblDireccion = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
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
        lblTitulo.setText("CLIENTES");
        panelNorte.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 30));

        lblSub.setForeground(new java.awt.Color(156, 163, 175));
        lblSub.setText("Gestión de clientes del taller");
        panelNorte.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 500, 20));

        txtBuscar.setBackground(new java.awt.Color(15, 23, 42));
        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(243, 244, 246));
        panelNorte.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 75, 250, 35));

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

        btnBuscar.setBackground(new java.awt.Color(51, 65, 85));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(243, 244, 246));
        btnBuscar.setText("Buscar");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setFocusPainted(false);
        panelNorte.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 75, 100, 35));

        btnEliminar.setBackground(new java.awt.Color(239, 68, 68));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar");
        btnEliminar.setBorderPainted(false);
        btnEliminar.setFocusPainted(false);
        panelNorte.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 75, 110, 35));

        jPanel1.add(panelNorte, java.awt.BorderLayout.PAGE_START);

        panelTabla.setBackground(new java.awt.Color(18, 24, 36));
        panelTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));
        panelTabla.setLayout(new java.awt.BorderLayout());

        tblClientes.setBackground(new java.awt.Color(30, 41, 59));
        tblClientes.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblClientes.setForeground(new java.awt.Color(243, 244, 246));
        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
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
        tblClientes.setFillsViewportHeight(true);
        tblClientes.setGridColor(new java.awt.Color(51, 65, 85));
        tblClientes.setRowHeight(32);
        tblClientes.setSelectionBackground(new java.awt.Color(2, 132, 199));
        tblClientes.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblClientes.setShowHorizontalLines(true);
        scroll.setViewportView(tblClientes);

        panelTabla.add(scroll, java.awt.BorderLayout.CENTER);

        jPanel1.add(panelTabla, java.awt.BorderLayout.CENTER);

        panelForm.setBackground(new java.awt.Color(30, 41, 59));
        panelForm.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        panelForm.setPreferredSize(new java.awt.Dimension(0, 190));
        panelForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblId.setForeground(new java.awt.Color(156, 163, 175));
        lblId.setText("ID");
        panelForm.add(lblId, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 60, 25));

        lblNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblNombre.setForeground(new java.awt.Color(156, 163, 175));
        lblNombre.setText("Nombre");
        panelForm.add(lblNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 15, 200, 25));

        lblCedula.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCedula.setForeground(new java.awt.Color(156, 163, 175));
        lblCedula.setText("Cédula");
        panelForm.add(lblCedula, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 15, 180, 25));

        lblTelefono.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTelefono.setForeground(new java.awt.Color(156, 163, 175));
        lblTelefono.setText("Teléfono");
        panelForm.add(lblTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 15, 180, 25));

        lblApellido.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblApellido.setForeground(new java.awt.Color(156, 163, 175));
        lblApellido.setText("Apellido");
        panelForm.add(lblApellido, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 15, 200, 25));

        txtNombre.setBackground(new java.awt.Color(15, 23, 42));
        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtNombre.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 200, 35));

        txtCedula.setBackground(new java.awt.Color(15, 23, 42));
        txtCedula.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCedula.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtCedula, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 40, 150, 35));

        txtApellido.setBackground(new java.awt.Color(15, 23, 42));
        txtApellido.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtApellido.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtApellido, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 40, 200, 35));

        txtTelefono.setBackground(new java.awt.Color(15, 23, 42));
        txtTelefono.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtTelefono.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 40, 180, 35));

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(15, 23, 42));
        txtId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtId.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 80, 35));

        lblEmail.setForeground(new java.awt.Color(156, 163, 175));
        lblEmail.setText("Email");
        panelForm.add(lblEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 85, 300, 25));

        lblDireccion.setForeground(new java.awt.Color(156, 163, 175));
        lblDireccion.setText("Dirección");
        panelForm.add(lblDireccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 85, 370, 25));

        txtEmail.setBackground(new java.awt.Color(15, 23, 42));
        txtEmail.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 300, 35));

        txtDireccion.setBackground(new java.awt.Color(15, 23, 42));
        txtDireccion.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtDireccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 110, 370, 35));

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
        panelForm.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 110, 150, 38));

        jPanel1.add(panelForm, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1074, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
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
    private javax.swing.JLabel lblApellido;
    private javax.swing.JLabel lblCedula;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelForm;
    private javax.swing.JPanel panelNorte;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtApellido;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCedula;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
