/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Usuario;
import com.electroservisgmg.metodos.UsuarioMetodos;

/**
 *
 * @author Aurelio
 */
public class FormUsuarios extends javax.swing.JPanel {

    /**
     * Creates new form FormUsuarios
     */
    public FormUsuarios() {
        initComponents();
        echoOriginal = txtClave.getEchoChar();
        crearBotonOjo();
        crearComboRol();
        cboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"ACTIVO", "INACTIVO"}));
        UiTheme.styleTable(tblUsuarios);
        cargarTabla("");
        btnBuscar.addActionListener(e -> buscar());
        btnNuevo.addActionListener(e -> limpiar());
        btnEditar.addActionListener(e -> editar());   // Editar
        btnEliminar.addActionListener(e -> eliminar()); // Eliminar
        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
        txtBuscar.addActionListener(e -> buscar());
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
        });
        tblUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) llenarDesdeFila();
        });
        limpiar();   // arranca con el formulario vacio y el rol USUARIO puesto
    }

    // ---- ojito ver / ocultar clave (fuera del GEN) ----
    private char echoOriginal = '•';
    private javax.swing.JButton btnOjo;
    private boolean viendoClave = false;

    private void crearBotonOjo() {
        panelForm.remove(txtClave);
        panelForm.add(txtClave, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 40, 158, 35));
        btnOjo = new javax.swing.JButton();
        btnOjo.setContentAreaFilled(false);
        btnOjo.setBorderPainted(false);
        btnOjo.setFocusPainted(false);
        btnOjo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnOjo.setToolTipText("Mostrar / ocultar contraseña");
        btnOjo.addActionListener(e -> alternarOjo());
        actualizarIconoOjo();
        panelForm.add(btnOjo, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 40, 36, 35));
    }

    // ---- rol del usuario (fuera del GEN) ----
    private javax.swing.JComboBox<String> cboRol;

    /**
     * Combo de rol, agregado en tiempo de ejecucion. Queda abajo a la derecha,
     * alineado con el combo de estado.
     */
    private void crearComboRol() {
        javax.swing.JLabel lblRol = new javax.swing.JLabel("Rol:");
        lblRol.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblRol.setForeground(new java.awt.Color(156, 163, 175));
        lblRol.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        panelForm.add(lblRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 90, 120, 38));

        cboRol = new javax.swing.JComboBox<>(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"ADMINISTRADOR", "USUARIO"}));
        cboRol.setBackground(new java.awt.Color(15, 23, 42));
        cboRol.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboRol.setForeground(new java.awt.Color(243, 244, 246));
        cboRol.setToolTipText("El ADMINISTRADOR entra a todo; el USUARIO no ve Usuarios ni Ajustes");
        panelForm.add(cboRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 90, 140, 38));
    }

    /** Rol elegido en el combo. */
    private String rolSeleccionado() {
        Object r = cboRol == null ? null : cboRol.getSelectedItem();
        return r == null ? "USUARIO" : r.toString();
    }

    /** Marca en el combo el rol que trae el usuario. */
    private void seleccionarRol(String rol) {
        if (cboRol == null) return;
        cboRol.setSelectedItem("ADMINISTRADOR".equalsIgnoreCase(String.valueOf(rol)) ? "ADMINISTRADOR" : "USUARIO");
    }

    private void alternarOjo() {
        viendoClave = !viendoClave;
        txtClave.setEchoChar(viendoClave ? (char) 0 : echoOriginal);
        actualizarIconoOjo();
    }

    private void actualizarIconoOjo() {
        javax.swing.ImageIcon icon = iconoOjo(viendoClave ? "/iconos/ojo_abierto.png" : "/iconos/ojo_cerrado.png");
        if (icon != null) {
            btnOjo.setText("");
            btnOjo.setIcon(icon);
        } else {
            btnOjo.setText(viendoClave ? "Ocultar" : "Ver");
        }
    }

    private static javax.swing.ImageIcon iconoOjo(String ruta) {
        try {
            java.net.URL url = FormUsuarios.class.getResource(ruta);
            if (url == null) return null;
            java.awt.Image img = new javax.swing.ImageIcon(url).getImage()
                    .getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH);
            return new javax.swing.ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    private void cargarTabla(String filtro) {
        UsuarioMetodos um = new UsuarioMetodos();
        java.util.List<Usuario> lista = um.listar();
        String f = filtro == null ? "" : filtro.toLowerCase();
        java.util.List<Usuario> fil = new java.util.ArrayList<>();
        for (Usuario u : lista) {
            if (f.isEmpty() || u.getNombre().toLowerCase().contains(f)
                    || u.getUsuario().toLowerCase().contains(f)) fil.add(u);
        }
        String[] cols = {"ID", "Nombre", "Usuario", "Rol", "Estado"};
        Object[][] data = new Object[fil.size()][5];
        for (int i = 0; i < fil.size(); i++) {
            Usuario u = fil.get(i);
            data[i] = new Object[]{u.getIdUsuario(), u.getNombre(), u.getUsuario(), u.getRol(), u.getEstado()};
        }
        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        UiTheme.styleTable(tblUsuarios);
        tblUsuarios.getColumnModel().getColumn(3).setPreferredWidth(140);   // "ADMINISTRADOR" completo
    }

    private void limpiar() {
        txtId1.setText("");
        txtNombre1.setText("");
        txtUsuario.setText("");
        txtClave.setText("");
        cboEstado.setSelectedItem("ACTIVO");
        seleccionarRol("USUARIO");
        tblUsuarios.clearSelection();
        txtNombre1.requestFocus();
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    private void llenarDesdeFila() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila < 0) return;
        txtId1.setText(String.valueOf(tblUsuarios.getValueAt(fila, 0)));
        txtNombre1.setText(String.valueOf(tblUsuarios.getValueAt(fila, 1)));
        txtUsuario.setText(String.valueOf(tblUsuarios.getValueAt(fila, 2)));
        txtClave.setText("");
        cboEstado.setSelectedItem(String.valueOf(tblUsuarios.getValueAt(fila, 4)));
        seleccionarRol(String.valueOf(tblUsuarios.getValueAt(fila, 3)));
    }

    private void editar() {
        if (tblUsuarios.getSelectedRow() < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla");
            return;
        }
        llenarDesdeFila();
        txtNombre1.requestFocus();
    }

    private void guardar() {
        String nombre = txtNombre1.getText().trim();
        String user = txtUsuario.getText().trim();
        String pass = new String(txtClave.getPassword());
        if (nombre.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Complete nombre, usuario y clave");
            return;
        }
        if (pass.length() < 4) {
            javax.swing.JOptionPane.showMessageDialog(this, "La clave debe tener al menos 4 caracteres");
            return;
        }
        String rol = rolSeleccionado();
        // si te estas editando a vos mismo no te podes sacar el rol de administrador:
        // despues no podrias volver a entrar a Usuarios ni a Ajustes
        Usuario sesion = com.electroservisgmg.clases.Sesion.getUsuario();
        if (!txtId1.getText().trim().isEmpty() && sesion != null && !"ADMINISTRADOR".equalsIgnoreCase(rol)) {
            try {
                if (Integer.parseInt(txtId1.getText().trim()) == sesion.getIdUsuario()) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "No te podés quitar a vos mismo el rol de ADMINISTRADOR:\n"
                            + "después no podrías volver a entrar a Usuarios ni a Ajustes.");
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setUsuario(user);
        u.setClave(pass);
        u.setRol(rol);
        u.setEstado(cboEstado.getSelectedItem().toString());
        UsuarioMetodos um = new UsuarioMetodos();
        boolean ok;
        if (txtId1.getText().trim().isEmpty()) {
            ok = um.insertar(u);
        } else {
            u.setIdUsuario(Integer.parseInt(txtId1.getText().trim()));
            ok = um.modificar(u);
        }
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Guardado correctamente" : "Error al guardar");
        if (ok) {
            cargarTabla(txtBuscar.getText().trim());
            String idTxt = txtId1.getText().trim();
            if (!idTxt.isEmpty()) notificarSesion(Integer.parseInt(idTxt));
            limpiar();
        }
    }

    /** Si se modificó el usuario en sesión, recarga sus datos y avisa al menú. */
    private void notificarSesion(int idGuardado) {
        com.electroservisgmg.clases.Usuario sesion = com.electroservisgmg.clases.Sesion.getUsuario();
        if (sesion == null || sesion.getIdUsuario() != idGuardado) return;
        for (Usuario u : new UsuarioMetodos().listar()) {
            if (u.getIdUsuario() == idGuardado) {
                com.electroservisgmg.clases.Sesion.setUsuario(u);
                com.electroservisgmg.clases.Sesion.notificarCambio();
                return;
            }
        }
    }

    private void eliminar() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla");
            return;
        }
        int id = (int) tblUsuarios.getValueAt(fila, 0);
        if (id == 1) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se puede eliminar el usuario principal");
            return;
        }
        // no podes borrarte a vos mismo: seguirias en sesion como un usuario que ya no existe
        com.electroservisgmg.clases.Usuario sesion = com.electroservisgmg.clases.Sesion.getUsuario();
        if (sesion != null && id == sesion.getIdUsuario()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No te podés eliminar a vos mismo estando en sesión");
            return;
        }
        int c = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Eliminar usuario '" + tblUsuarios.getValueAt(fila, 1) + "'?",
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (c != javax.swing.JOptionPane.YES_OPTION) return;
        boolean ok = new UsuarioMetodos().eliminar(id);
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Eliminado" : "Error al eliminar");
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

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        txttitulo2 = new javax.swing.JLabel();
        panelToolbar = new javax.swing.JPanel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        panelTabla = new javax.swing.JPanel();
        scroll = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        panelForm = new javax.swing.JPanel();
        lblEstado = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        txtId1 = new javax.swing.JTextField();
        lblNombre1 = new javax.swing.JLabel();
        txtNombre1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtClave = new javax.swing.JPasswordField();
        lblClave1 = new javax.swing.JLabel();
        cboEstado = new javax.swing.JComboBox<>();
        btnGuardar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setBackground(new java.awt.Color(18, 24, 36));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(18, 24, 36));
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 10, 15));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("USUARIOS");
        jPanel1.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 400, 30));

        txttitulo2.setForeground(new java.awt.Color(156, 163, 175));
        txttitulo2.setText("Gestión de usuarios del sistema");
        jPanel1.add(txttitulo2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 32, 400, 20));

        add(jPanel1, java.awt.BorderLayout.NORTH);

        panelToolbar.setBackground(new java.awt.Color(18, 24, 36));
        panelToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));

        txtBuscar.setBackground(new java.awt.Color(15, 23, 42));
        txtBuscar.setForeground(new java.awt.Color(243, 244, 246));
        txtBuscar.setPreferredSize(new java.awt.Dimension(250, 35));

        btnBuscar.setBackground(new java.awt.Color(51, 65, 85));
        btnBuscar.setForeground(new java.awt.Color(243, 244, 246));
        btnBuscar.setText("Buscar");

        btnNuevo.setBackground(new java.awt.Color(56, 189, 248));
        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(15, 23, 42));
        btnNuevo.setText("+ Nuevo");

        btnEditar.setBackground(new java.awt.Color(51, 65, 85));
        btnEditar.setForeground(new java.awt.Color(243, 244, 246));
        btnEditar.setText("Editar");

        btnEliminar.setBackground(new java.awt.Color(239, 68, 68));
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar");

        panelTabla.setBackground(new java.awt.Color(18, 24, 36));
        panelTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));
        panelTabla.setLayout(new java.awt.BorderLayout());

        tblUsuarios.setBackground(new java.awt.Color(30, 41, 59));
        tblUsuarios.setForeground(new java.awt.Color(243, 244, 246));
        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
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
        tblUsuarios.setGridColor(new java.awt.Color(51, 65, 85));
        tblUsuarios.setRowHeight(32);
        tblUsuarios.setSelectionBackground(new java.awt.Color(2, 132, 199));
        tblUsuarios.setShowGrid(false);
        scroll.setViewportView(tblUsuarios);

        panelTabla.add(scroll, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout panelToolbarLayout = new javax.swing.GroupLayout(panelToolbar);
        panelToolbar.setLayout(panelToolbarLayout);
        panelToolbarLayout.setHorizontalGroup(
            panelToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelToolbarLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnBuscar)
                .addGap(5, 5, 5)
                .addComponent(btnNuevo)
                .addGap(5, 5, 5)
                .addComponent(btnEditar)
                .addGap(5, 5, 5)
                .addComponent(btnEliminar))
            .addGroup(panelToolbarLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(panelTabla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelToolbarLayout.setVerticalGroup(
            panelToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelToolbarLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(panelToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelToolbarLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(panelToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnBuscar)
                            .addComponent(btnNuevo)
                            .addComponent(btnEditar)
                            .addComponent(btnEliminar))))
                .addGap(5, 5, 5)
                .addComponent(panelTabla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(panelToolbar, java.awt.BorderLayout.CENTER);

        panelForm.setBackground(new java.awt.Color(30, 41, 59));
        panelForm.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        panelForm.setPreferredSize(new java.awt.Dimension(0, 150));
        panelForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblEstado.setBackground(new java.awt.Color(156, 163, 175));
        lblEstado.setText("Estado");
        panelForm.add(lblEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 15, 140, 25));

        lblUsuario.setText("Usuario");
        panelForm.add(lblUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 15, 200, 25));

        txtUsuario.setBackground(new java.awt.Color(15, 23, 42));
        txtUsuario.setForeground(new java.awt.Color(243, 244, 246));
        txtUsuario.setPreferredSize(new java.awt.Dimension(64, 25));
        panelForm.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, 200, 35));

        txtId1.setEditable(false);
        txtId1.setBackground(new java.awt.Color(15, 23, 42));
        txtId1.setForeground(new java.awt.Color(243, 244, 246));
        txtId1.setPreferredSize(new java.awt.Dimension(64, 25));
        panelForm.add(txtId1, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 40, 80, 35));

        lblNombre1.setText("Nombre");
        panelForm.add(lblNombre1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 15, 200, 25));

        txtNombre1.setBackground(new java.awt.Color(15, 23, 42));
        txtNombre1.setForeground(new java.awt.Color(243, 244, 246));
        txtNombre1.setPreferredSize(new java.awt.Dimension(64, 25));
        panelForm.add(txtNombre1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 250, 35));

        jLabel2.setBackground(new java.awt.Color(156, 163, 175));
        jLabel2.setText("ID");
        panelForm.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 15, 60, 25));

        txtClave.setBackground(new java.awt.Color(15, 23, 42));
        txtClave.setForeground(new java.awt.Color(243, 244, 246));
        panelForm.add(txtClave, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 40, 200, 35));

        lblClave1.setBackground(new java.awt.Color(156, 163, 175));
        lblClave1.setText("Clave");
        panelForm.add(lblClave1, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 15, 200, 25));

        cboEstado.setBackground(new java.awt.Color(15, 23, 42));
        cboEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboEstado.setForeground(new java.awt.Color(243, 244, 246));
        cboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ACTIVO", "INACTIVO" }));
        panelForm.add(cboEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 40, 140, 35));

        btnGuardar.setBackground(new java.awt.Color(34, 197, 94));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setFocusPainted(false);
        panelForm.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 150, 38));

        btnLimpiar.setBackground(new java.awt.Color(51, 65, 85));
        btnLimpiar.setForeground(new java.awt.Color(243, 244, 246));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setFocusPainted(false);
        panelForm.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, 150, 38));

        add(panelForm, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<String> cboEstado;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblClave1;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblNombre1;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel panelForm;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JPanel panelToolbar;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JPasswordField txtClave;
    private javax.swing.JTextField txtId1;
    private javax.swing.JTextField txtNombre1;
    private javax.swing.JTextField txtUsuario;
    private javax.swing.JLabel txttitulo2;
    // End of variables declaration//GEN-END:variables
}
