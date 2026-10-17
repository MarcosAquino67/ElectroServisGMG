/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Empresa;
import com.electroservisgmg.metodos.CdcUtil;
import com.electroservisgmg.metodos.EmpresaMetodos;

/**
 *
 * @author Aurelio
 */
public class FormAjustes extends javax.swing.JPanel {

    // ---- datos del emisor para la Factura Electronica (se crean en runtime) ----
    private javax.swing.JTextField txtEmpRazonSocial;
    private javax.swing.JTextField txtEmpNombreComercial;
    private javax.swing.JTextField txtEmpRuc;
    private javax.swing.JTextField txtEmpDv;
    private javax.swing.JTextField txtEmpTimbrado;
    private javax.swing.JTextField txtEmpDireccion;
    private javax.swing.JTextField txtEmpCiudad;
    private javax.swing.JTextField txtEmpTelefono;
    private javax.swing.JTextField txtEmpEmail;
    private javax.swing.JTextField txtEmpEstablecimiento;
    private javax.swing.JTextField txtEmpPunto;
    private javax.swing.JTextField txtEmpNroInicio;
    private javax.swing.JTextField txtEmpNroFin;
    private javax.swing.JTextArea txtEmpActividades;
    private javax.swing.JComboBox<String> cboEmpContribuyente;
    private com.toedter.calendar.JDateChooser dcEmpVigencia;
    private javax.swing.JLabel lblEmpLogo;
    /** Archivo de logo elegido en Ajustes; null = usar el logo que viene con el sistema. */
    private String logoEmpresa;

    /**
     * Creates new form FormAjustes
     */
    public FormAjustes() {
        initComponents();
        String defecto = UiTheme.getDefecto();
        if (defecto.equals("oscuro")) rbOscuro.setSelected(true);
        else if (defecto.equals("claro")) rbClaro.setSelected(true);
        else rbUltimo.setSelected(true);
        actualizarLabelTema();
        btnOscuro.addActionListener(e -> { UiTheme.aplicarTema(true); actualizarLabelTema(); });
        btnClaro.addActionListener(e -> { UiTheme.aplicarTema(false); actualizarLabelTema(); });
        btnGuardarDefecto.addActionListener(e -> guardarDefecto());
        crearCardEmpresa();
        cargarEmpresa();
    }

    // ================= datos del emisor (Factura Electronica) =================

    /** Construye la tarjeta "Empresa y timbrado" debajo de las dos tarjetas de tema. */
    private void crearCardEmpresa() {
        javax.swing.JPanel card = new javax.swing.JPanel();
        card.setBackground(new java.awt.Color(30, 41, 59));
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        card.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        javax.swing.JLabel titulo = new javax.swing.JLabel("Empresa y timbrado (Factura Electrónica)");
        titulo.setFont(new java.awt.Font("Segoe UI", 1, 15));
        titulo.setForeground(new java.awt.Color(255, 255, 255));
        card.add(titulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 500, 25));

        txtEmpRazonSocial = campo(card, "Razón social", 20, 45, 350);
        txtEmpNombreComercial = campo(card, "Nombre comercial (ticket)", 380, 45, 350);
        txtEmpRuc = campo(card, "RUC", 740, 45, 110);
        txtEmpDv = campo(card, "DV", 860, 45, 50);
        txtEmpTimbrado = campo(card, "Timbrado N°", 920, 45, 105);

        etiqueta(card, "Vigencia desde", 20, 110, 150);
        dcEmpVigencia = new com.toedter.calendar.JDateChooser();
        dcEmpVigencia.setDateFormatString("dd/MM/yyyy");
        dcEmpVigencia.setFont(new java.awt.Font("Segoe UI", 0, 13));
        dcEmpVigencia.setBackground(new java.awt.Color(15, 23, 42));
        card.add(dcEmpVigencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 133, 150, 32));
        javax.swing.JTextField edVig = (javax.swing.JTextField) dcEmpVigencia.getDateEditor().getUiComponent();
        edVig.setBackground(new java.awt.Color(15, 23, 42));
        edVig.setForeground(new java.awt.Color(243, 244, 246));
        edVig.setCaretColor(new java.awt.Color(243, 244, 246));
        edVig.setFont(new java.awt.Font("Segoe UI", 0, 13));

        etiqueta(card, "Tipo de contribuyente", 180, 110, 180);
        cboEmpContribuyente = new javax.swing.JComboBox<>(
                new String[]{"Jurídica (2)", "Física (1)"});
        cboEmpContribuyente.setBackground(new java.awt.Color(15, 23, 42));
        cboEmpContribuyente.setForeground(new java.awt.Color(243, 244, 246));
        card.add(cboEmpContribuyente, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 133, 180, 32));

        txtEmpEstablecimiento = campo(card, "Establecimiento", 370, 110, 110);
        txtEmpPunto = campo(card, "Punto expedición", 490, 110, 110);
        txtEmpNroInicio = campo(card, "N° factura desde", 610, 110, 110);
        txtEmpNroFin = campo(card, "N° factura hasta", 730, 110, 110);

        txtEmpDireccion = campo(card, "Dirección", 20, 175, 460);
        txtEmpCiudad = campo(card, "Ciudad", 490, 175, 140);
        txtEmpTelefono = campo(card, "Teléfono", 640, 175, 140);
        txtEmpEmail = campo(card, "Correo electrónico", 790, 175, 235);

        // ---- actividades economicas (van impresas en la factura) ----
        etiqueta(card, "Actividades económicas (una por línea)", 20, 240, 400);
        txtEmpActividades = new javax.swing.JTextArea(3, 20);
        txtEmpActividades.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpActividades.setForeground(new java.awt.Color(243, 244, 246));
        txtEmpActividades.setCaretColor(new java.awt.Color(243, 244, 246));
        txtEmpActividades.setFont(new java.awt.Font("Segoe UI", 0, 12));
        txtEmpActividades.setLineWrap(true);
        txtEmpActividades.setWrapStyleWord(true);
        javax.swing.JScrollPane scAct = new javax.swing.JScrollPane(txtEmpActividades);
        scAct.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        card.add(scAct, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 263, 450, 62));

        // ---- logo del emisor ----
        etiqueta(card, "Logo de la factura", 490, 240, 150);
        lblEmpLogo = new javax.swing.JLabel();
        lblEmpLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEmpLogo.setOpaque(true);
        lblEmpLogo.setBackground(new java.awt.Color(248, 249, 250));
        lblEmpLogo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        card.add(lblEmpLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 263, 110, 62));

        javax.swing.JButton btnElegirLogo = new javax.swing.JButton("Elegir logo…");
        btnElegirLogo.setBackground(new java.awt.Color(51, 65, 85));
        btnElegirLogo.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnElegirLogo.setForeground(new java.awt.Color(243, 244, 246));
        btnElegirLogo.setBorderPainted(false);
        btnElegirLogo.setFocusPainted(false);
        btnElegirLogo.addActionListener(e -> elegirLogo());
        card.add(btnElegirLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 263, 130, 28));

        javax.swing.JButton btnQuitarLogo = new javax.swing.JButton("Logo por defecto");
        btnQuitarLogo.setBackground(new java.awt.Color(51, 65, 85));
        btnQuitarLogo.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnQuitarLogo.setForeground(new java.awt.Color(243, 244, 246));
        btnQuitarLogo.setBorderPainted(false);
        btnQuitarLogo.setFocusPainted(false);
        btnQuitarLogo.addActionListener(e -> { logoEmpresa = null; actualizarPreviewLogo(); });
        card.add(btnQuitarLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 297, 130, 28));

        etiqueta(card, "Si no elegís un logo se usa el que viene con el sistema", 490, 333, 370);

        javax.swing.JButton btnGuardarEmpresa = new javax.swing.JButton("Guardar datos");
        btnGuardarEmpresa.setBackground(new java.awt.Color(34, 197, 94));
        btnGuardarEmpresa.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnGuardarEmpresa.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarEmpresa.setBorderPainted(false);
        btnGuardarEmpresa.setFocusPainted(false);
        btnGuardarEmpresa.addActionListener(e -> guardarEmpresa());
        card.add(btnGuardarEmpresa, new org.netbeans.lib.awtextra.AbsoluteConstraints(875, 340, 150, 38));

        panelContenido.add(card, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 250, 1045, 395));
    }

    /** Abre un dialogo para elegir el archivo de logo de la factura. */
    private void elegirLogo() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Elegir el logo de la empresa");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (jpg, png, gif)", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            logoEmpresa = fc.getSelectedFile().getAbsolutePath();
            actualizarPreviewLogo();
        }
    }

    /** Muestra el logo que se va a imprimir en la factura. */
    private void actualizarPreviewLogo() {
        byte[] bytes = null;
        if (logoEmpresa != null && !logoEmpresa.trim().isEmpty()) {
            try {
                java.io.File f = new java.io.File(logoEmpresa.trim());
                if (f.isFile()) bytes = java.nio.file.Files.readAllBytes(f.toPath());
            } catch (Exception ex) {
                System.err.println("logo elegido: " + ex.getMessage());
            }
        }
        if (bytes == null) {
            Empresa soloLogo = new Empresa();
            soloLogo.setLogo(null);
            bytes = EmpresaMetodos.logoBytes(soloLogo);
        }
        if (bytes == null) {
            lblEmpLogo.setIcon(null);
            lblEmpLogo.setText("sin logo");
            return;
        }
        java.awt.Image img = new javax.swing.ImageIcon(bytes).getImage()
                .getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
        lblEmpLogo.setText("");
        lblEmpLogo.setIcon(new javax.swing.ImageIcon(img));
    }

    /** Etiqueta gris del formulario de empresa. */
    private void etiqueta(javax.swing.JPanel card, String texto, int x, int y, int w) {
        javax.swing.JLabel l = new javax.swing.JLabel(texto);
        l.setForeground(new java.awt.Color(156, 163, 175));
        card.add(l, new org.netbeans.lib.awtextra.AbsoluteConstraints(x, y, w, 20));
    }

    /** Etiqueta + campo de texto, alineados como el resto de la pantalla. */
    private javax.swing.JTextField campo(javax.swing.JPanel card, String etiqueta, int x, int y, int w) {
        etiqueta(card, etiqueta, x, y, w);
        javax.swing.JTextField f = new javax.swing.JTextField();
        f.setBackground(new java.awt.Color(15, 23, 42));
        f.setForeground(new java.awt.Color(243, 244, 246));
        f.setCaretColor(new java.awt.Color(243, 244, 246));
        f.setFont(new java.awt.Font("Segoe UI", 0, 13));
        card.add(f, new org.netbeans.lib.awtextra.AbsoluteConstraints(x, y + 23, w, 32));
        return f;
    }

    /** Muestra los datos guardados del emisor. */
    private void cargarEmpresa() {
        Empresa e = new EmpresaMetodos().obtener();
        txtEmpRazonSocial.setText(e.getRazonSocial());
        txtEmpNombreComercial.setText(e.getNombreComercial());
        txtEmpRuc.setText(e.getRuc());
        txtEmpDv.setText(e.getDvRuc());
        txtEmpTimbrado.setText(e.getTimbrado());
        dcEmpVigencia.setDate(e.getTimbradoVigenciaDesde());
        cboEmpContribuyente.setSelectedIndex("1".equals(e.getTipoContribuyente()) ? 1 : 0);
        txtEmpDireccion.setText(e.getDireccion());
        txtEmpCiudad.setText(e.getCiudad());
        txtEmpTelefono.setText(e.getTelefono());
        txtEmpEmail.setText(e.getEmail());
        txtEmpEstablecimiento.setText(e.getEstablecimiento());
        txtEmpPunto.setText(e.getPuntoExpedicion());
        txtEmpNroInicio.setText(String.valueOf(e.getNroFacturaInicio()));
        txtEmpNroFin.setText(String.valueOf(e.getNroFacturaFin()));
        txtEmpActividades.setText(e.getActividades());
        logoEmpresa = e.getLogo();
        actualizarPreviewLogo();
    }

    /** Guarda los datos del emisor (se usan al emitir la factura). */
    private void guardarEmpresa() {
        String razon = txtEmpRazonSocial.getText().trim();
        String ruc = txtEmpRuc.getText().trim();
        if (razon.isEmpty() || ruc.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "La razón social y el RUC son obligatorios para emitir facturas");
            return;
        }
        Empresa e = new Empresa();
        e.setRazonSocial(razon);
        e.setNombreComercial(txtEmpNombreComercial.getText().trim());
        e.setRuc(ruc);
        String dv = txtEmpDv.getText().trim();
        if (dv.isEmpty()) {
            dv = String.valueOf(CdcUtil.dvRuc(ruc));
            txtEmpDv.setText(dv);
        }
        e.setDvRuc(dv);
        e.setTimbrado(txtEmpTimbrado.getText().trim());
        e.setTimbradoVigenciaDesde(dcEmpVigencia.getDate());
        e.setTipoContribuyente(cboEmpContribuyente.getSelectedIndex() == 1 ? "1" : "2");
        e.setDireccion(txtEmpDireccion.getText().trim());
        e.setCiudad(txtEmpCiudad.getText().trim());
        e.setTelefono(txtEmpTelefono.getText().trim());
        e.setEmail(txtEmpEmail.getText().trim());
        e.setEstablecimiento(txtEmpEstablecimiento.getText().trim().isEmpty() ? "001" : txtEmpEstablecimiento.getText().trim());
        e.setPuntoExpedicion(txtEmpPunto.getText().trim().isEmpty() ? "001" : txtEmpPunto.getText().trim());
        try { e.setNroFacturaInicio(Integer.parseInt(txtEmpNroInicio.getText().trim())); } catch (NumberFormatException ex) { e.setNroFacturaInicio(1); }
        try { e.setNroFacturaFin(Integer.parseInt(txtEmpNroFin.getText().trim())); } catch (NumberFormatException ex) { e.setNroFacturaFin(9999999); }
        e.setActividades(txtEmpActividades.getText());
        e.setLogo(logoEmpresa);
        boolean ok = new EmpresaMetodos().guardar(e);
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Datos de la empresa guardados"
                : "No se pudieron guardar:\n" + EmpresaMetodos.ultimoError);
    }

    private void actualizarLabelTema() {
        lblTemaActual.setText("Tema actual: " + (UiTheme.isDark() ? "Oscuro" : "Claro"));
    }

    private void guardarDefecto() {
        String defecto = "ultimo";
        if (rbOscuro.isSelected()) defecto = "oscuro";
        else if (rbClaro.isSelected()) defecto = "claro";
        UiTheme.setDefecto(defecto);
        javax.swing.JOptionPane.showMessageDialog(this, "Preferencia guardada");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        panelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        subtítulo = new javax.swing.JLabel();
        panelContenido = new javax.swing.JPanel();
        panelCardTema = new javax.swing.JPanel();
        lblTema = new javax.swing.JLabel();
        btnOscuro = new javax.swing.JButton();
        btnClaro = new javax.swing.JButton();
        lblTemaActual = new javax.swing.JLabel();
        lblHintTema = new javax.swing.JLabel();
        panelCardDefecto = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rbOscuro = new javax.swing.JRadioButton();
        rbUltimo = new javax.swing.JRadioButton();
        rbClaro = new javax.swing.JRadioButton();
        btnGuardarDefecto = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(18, 24, 36));
        jPanel1.setLayout(new java.awt.BorderLayout());

        panelTitulo.setBackground(new java.awt.Color(18, 24, 36));
        panelTitulo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("AJUSTES");
        panelTitulo.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 30));

        subtítulo.setForeground(new java.awt.Color(156, 163, 175));
        subtítulo.setText("Configuración del sistema");
        panelTitulo.add(subtítulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 500, 20));

        jPanel1.add(panelTitulo, java.awt.BorderLayout.NORTH);

        panelContenido.setBackground(new java.awt.Color(18, 24, 36));
        panelContenido.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelContenido.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelCardTema.setBackground(new java.awt.Color(30, 41, 59));
        panelCardTema.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        panelCardTema.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTema.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblTema.setForeground(new java.awt.Color(255, 255, 255));
        lblTema.setText("Apariencia");
        panelCardTema.add(lblTema, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 400, 25));

        btnOscuro.setBackground(new java.awt.Color(51, 65, 85));
        btnOscuro.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnOscuro.setForeground(new java.awt.Color(243, 244, 246));
        btnOscuro.setText("Oscuro");
        btnOscuro.setBorderPainted(false);
        btnOscuro.setFocusPainted(false);
        panelCardTema.add(btnOscuro, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 55, 225, 45));

        btnClaro.setBackground(new java.awt.Color(248, 249, 250));
        btnClaro.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnClaro.setForeground(new java.awt.Color(33, 37, 41));
        btnClaro.setText("Claro");
        btnClaro.setBorderPainted(false);
        btnClaro.setFocusPainted(false);
        panelCardTema.add(btnClaro, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 55, 225, 45));

        lblTemaActual.setForeground(new java.awt.Color(156, 163, 175));
        lblTemaActual.setText("Tema actual: Oscuro");
        panelCardTema.add(lblTemaActual, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 115, 400, 25));

        lblHintTema.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblHintTema.setForeground(new java.awt.Color(100, 116, 139));
        lblHintTema.setText("El cambio se aplica al instante");
        panelCardTema.add(lblHintTema, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 145, 400, 20));

        panelContenido.add(panelCardTema, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 512, 230));

        panelCardDefecto.setBackground(new java.awt.Color(30, 41, 59));
        panelCardDefecto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        panelCardDefecto.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Al abrir el sistema");
        panelCardDefecto.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 400, 25));

        rbOscuro.setBackground(new java.awt.Color(30, 41, 59));
        buttonGroup1.add(rbOscuro);
        rbOscuro.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        rbOscuro.setForeground(new java.awt.Color(243, 244, 246));
        rbOscuro.setText("Siempre oscuro");
        panelCardDefecto.add(rbOscuro, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 55, 400, 30));

        rbUltimo.setBackground(new java.awt.Color(30, 41, 59));
        buttonGroup1.add(rbUltimo);
        rbUltimo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        rbUltimo.setForeground(new java.awt.Color(243, 244, 246));
        rbUltimo.setText("Recordar último tema");
        panelCardDefecto.add(rbUltimo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 125, 150, 38));

        rbClaro.setBackground(new java.awt.Color(30, 41, 59));
        buttonGroup1.add(rbClaro);
        rbClaro.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        rbClaro.setForeground(new java.awt.Color(243, 244, 246));
        rbClaro.setText("Siempre claro");
        panelCardDefecto.add(rbClaro, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 400, 30));

        btnGuardarDefecto.setBackground(new java.awt.Color(34, 197, 94));
        btnGuardarDefecto.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardarDefecto.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarDefecto.setText("Guardar");
        btnGuardarDefecto.setBorderPainted(false);
        btnGuardarDefecto.setFocusPainted(false);
        btnGuardarDefecto.addActionListener(this::btnGuardarDefectoActionPerformed);
        panelCardDefecto.add(btnGuardarDefecto, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 165, 150, 38));

        panelContenido.add(panelCardDefecto, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 0, 515, 230));

        jPanel1.add(panelContenido, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarDefectoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarDefectoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnGuardarDefectoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClaro;
    private javax.swing.JButton btnGuardarDefecto;
    private javax.swing.JButton btnOscuro;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblHintTema;
    private javax.swing.JLabel lblTema;
    private javax.swing.JLabel lblTemaActual;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelCardDefecto;
    private javax.swing.JPanel panelCardTema;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelTitulo;
    private javax.swing.JRadioButton rbClaro;
    private javax.swing.JRadioButton rbOscuro;
    private javax.swing.JRadioButton rbUltimo;
    private javax.swing.JLabel subtítulo;
    // End of variables declaration//GEN-END:variables
}
