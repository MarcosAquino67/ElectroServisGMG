/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Empresa;
import com.electroservisgmg.metodos.EmpresaMetodos;

/**
 *
 * @author Aurelio
 */
public class FormAjustes extends javax.swing.JPanel {

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
        // Los botones se conectan desde el disenador (pestana Events ->
        // actionPerformed); aca queda solo lo que el diseno no modela.
        btnRespaldo.setToolTipText("Guarda una copia de toda la base en un archivo .sql, o vuelve a cargar un respaldo anterior");
        // El cursor de texto y el editor del calendario no son propiedades del diseno.
        for (javax.swing.JTextField f : new javax.swing.JTextField[]{
            txtEmpRazonSocial, txtEmpNombreComercial, txtEmpRuc, txtEmpDv, txtEmpTimbrado,
            txtEmpEstablecimiento, txtEmpPunto, txtEmpNroInicio, txtEmpNroFin,
            txtEmpDireccion, txtEmpCiudad, txtEmpTelefono, txtEmpEmail}) {
            f.setCaretColor(new java.awt.Color(243, 244, 246));
        }
        txtEmpActividades.setCaretColor(new java.awt.Color(243, 244, 246));
        lblEmpLogoPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cargarEmpresa();
        // El editor del calendario se pinta al final: JDateChooser.setDate() le reinicia el color del texto.
        javax.swing.JTextField edVig = (javax.swing.JTextField) dcEmpVigencia.getDateEditor().getUiComponent();
        edVig.setBackground(new java.awt.Color(15, 23, 42));
        edVig.setForeground(new java.awt.Color(243, 244, 246));
        edVig.setCaretColor(new java.awt.Color(243, 244, 246));
        edVig.setFont(new java.awt.Font("Segoe UI", 0, 13));
    }

    // ================= datos del emisor (Factura Electronica) =================

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
            lblEmpLogoPreview.setIcon(null);
            lblEmpLogoPreview.setText("sin logo");
            return;
        }
        java.awt.Image img = new javax.swing.ImageIcon(bytes).getImage()
                .getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
        lblEmpLogoPreview.setText("");
        lblEmpLogoPreview.setIcon(new javax.swing.ImageIcon(img));
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
        new EmpresaMetodos().guardarDesdeFormulario(txtEmpRazonSocial, txtEmpNombreComercial, txtEmpRuc, txtEmpDv,
                txtEmpTimbrado, dcEmpVigencia, cboEmpContribuyente, txtEmpDireccion, txtEmpCiudad, txtEmpTelefono,
                txtEmpEmail, txtEmpEstablecimiento, txtEmpPunto, txtEmpNroInicio, txtEmpNroFin, txtEmpActividades,
                logoEmpresa);
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
        btnRespaldo = new javax.swing.JButton();
        panelCardEmpresa = new javax.swing.JPanel();
        lblEmpTitulo = new javax.swing.JLabel();
        lblEmpRazonSocial = new javax.swing.JLabel();
        txtEmpRazonSocial = new javax.swing.JTextField();
        lblEmpNombreComercial = new javax.swing.JLabel();
        txtEmpNombreComercial = new javax.swing.JTextField();
        lblEmpRuc = new javax.swing.JLabel();
        txtEmpRuc = new javax.swing.JTextField();
        lblEmpDv = new javax.swing.JLabel();
        txtEmpDv = new javax.swing.JTextField();
        lblEmpTimbrado = new javax.swing.JLabel();
        txtEmpTimbrado = new javax.swing.JTextField();
        lblEmpVigencia = new javax.swing.JLabel();
        dcEmpVigencia = new com.toedter.calendar.JDateChooser();
        lblEmpContribuyente = new javax.swing.JLabel();
        cboEmpContribuyente = new javax.swing.JComboBox<>();
        lblEmpEstablecimiento = new javax.swing.JLabel();
        txtEmpEstablecimiento = new javax.swing.JTextField();
        lblEmpPunto = new javax.swing.JLabel();
        txtEmpPunto = new javax.swing.JTextField();
        lblEmpNroInicio = new javax.swing.JLabel();
        txtEmpNroInicio = new javax.swing.JTextField();
        lblEmpNroFin = new javax.swing.JLabel();
        txtEmpNroFin = new javax.swing.JTextField();
        lblEmpDireccion = new javax.swing.JLabel();
        txtEmpDireccion = new javax.swing.JTextField();
        lblEmpCiudad = new javax.swing.JLabel();
        txtEmpCiudad = new javax.swing.JTextField();
        lblEmpTelefono = new javax.swing.JLabel();
        txtEmpTelefono = new javax.swing.JTextField();
        lblEmpEmail = new javax.swing.JLabel();
        txtEmpEmail = new javax.swing.JTextField();
        lblEmpActividades = new javax.swing.JLabel();
        scEmpActividades = new javax.swing.JScrollPane();
        txtEmpActividades = new javax.swing.JTextArea();
        lblEmpLogo = new javax.swing.JLabel();
        lblEmpLogoPreview = new javax.swing.JLabel();
        btnElegirLogo = new javax.swing.JButton();
        btnQuitarLogo = new javax.swing.JButton();
        lblEmpLogoHint = new javax.swing.JLabel();
        btnGuardarEmpresa = new javax.swing.JButton();

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
        btnOscuro.addActionListener(this::btnOscuroActionPerformed);
        panelCardTema.add(btnOscuro, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 55, 225, 45));

        btnClaro.setBackground(new java.awt.Color(248, 249, 250));
        btnClaro.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnClaro.setForeground(new java.awt.Color(33, 37, 41));
        btnClaro.setText("Claro");
        btnClaro.setBorderPainted(false);
        btnClaro.setFocusPainted(false);
        btnClaro.addActionListener(this::btnClaroActionPerformed);
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

        btnRespaldo.setBackground(new java.awt.Color(51, 65, 85));
        btnRespaldo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRespaldo.setForeground(new java.awt.Color(243, 244, 246));
        btnRespaldo.setText("Respaldo de la base…");
        btnRespaldo.setBorderPainted(false);
        btnRespaldo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRespaldo.setFocusPainted(false);
        btnRespaldo.addActionListener(this::btnRespaldoActionPerformed);
        panelCardDefecto.add(btnRespaldo, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 165, 300, 38));

        panelContenido.add(panelCardDefecto, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 0, 515, 230));

        panelCardEmpresa.setBackground(new java.awt.Color(30, 41, 59));
        panelCardEmpresa.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        panelCardEmpresa.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblEmpTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblEmpTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblEmpTitulo.setText("Empresa y timbrado (Factura Electrónica)");
        panelCardEmpresa.add(lblEmpTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 500, 25));

        lblEmpRazonSocial.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpRazonSocial.setText("Razón social");
        panelCardEmpresa.add(lblEmpRazonSocial, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 45, 350, 20));

        txtEmpRazonSocial.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpRazonSocial.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpRazonSocial.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpRazonSocial, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 68, 350, 32));

        lblEmpNombreComercial.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpNombreComercial.setText("Nombre comercial (ticket)");
        panelCardEmpresa.add(lblEmpNombreComercial, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 45, 350, 20));

        txtEmpNombreComercial.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpNombreComercial.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpNombreComercial.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpNombreComercial, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 68, 350, 32));

        lblEmpRuc.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpRuc.setText("RUC");
        panelCardEmpresa.add(lblEmpRuc, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 45, 110, 20));

        txtEmpRuc.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpRuc.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpRuc.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpRuc, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 68, 110, 32));

        lblEmpDv.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpDv.setText("DV");
        panelCardEmpresa.add(lblEmpDv, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 45, 50, 20));

        txtEmpDv.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpDv.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpDv.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpDv, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 68, 50, 32));

        lblEmpTimbrado.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpTimbrado.setText("Timbrado N°");
        panelCardEmpresa.add(lblEmpTimbrado, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 45, 105, 20));

        txtEmpTimbrado.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpTimbrado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpTimbrado.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpTimbrado, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 68, 105, 32));

        lblEmpVigencia.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpVigencia.setText("Vigencia desde");
        panelCardEmpresa.add(lblEmpVigencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 150, 20));

        dcEmpVigencia.setBackground(new java.awt.Color(15, 23, 42));
        dcEmpVigencia.setDateFormatString("dd/MM/yyyy");
        dcEmpVigencia.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        panelCardEmpresa.add(dcEmpVigencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 133, 150, 32));

        lblEmpContribuyente.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpContribuyente.setText("Tipo de contribuyente");
        panelCardEmpresa.add(lblEmpContribuyente, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 110, 180, 20));

        cboEmpContribuyente.setBackground(new java.awt.Color(15, 23, 42));
        cboEmpContribuyente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboEmpContribuyente.setForeground(new java.awt.Color(243, 244, 246));
        cboEmpContribuyente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Jurídica (2)", "Física (1)" }));
        panelCardEmpresa.add(cboEmpContribuyente, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 133, 180, 32));

        lblEmpEstablecimiento.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpEstablecimiento.setText("Establecimiento");
        panelCardEmpresa.add(lblEmpEstablecimiento, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 110, 110, 20));

        txtEmpEstablecimiento.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpEstablecimiento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpEstablecimiento.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpEstablecimiento, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 133, 110, 32));

        lblEmpPunto.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpPunto.setText("Punto expedición");
        panelCardEmpresa.add(lblEmpPunto, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 110, 110, 20));

        txtEmpPunto.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpPunto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpPunto.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpPunto, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 133, 110, 32));

        lblEmpNroInicio.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpNroInicio.setText("N° factura desde");
        panelCardEmpresa.add(lblEmpNroInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 110, 110, 20));

        txtEmpNroInicio.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpNroInicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpNroInicio.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpNroInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 133, 110, 32));

        lblEmpNroFin.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpNroFin.setText("N° factura hasta");
        panelCardEmpresa.add(lblEmpNroFin, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 110, 110, 20));

        txtEmpNroFin.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpNroFin.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpNroFin.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpNroFin, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 133, 110, 32));

        lblEmpDireccion.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpDireccion.setText("Dirección");
        panelCardEmpresa.add(lblEmpDireccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 175, 460, 20));

        txtEmpDireccion.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpDireccion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpDireccion.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpDireccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 198, 460, 32));

        lblEmpCiudad.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpCiudad.setText("Ciudad");
        panelCardEmpresa.add(lblEmpCiudad, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 175, 140, 20));

        txtEmpCiudad.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpCiudad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpCiudad.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpCiudad, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 198, 140, 32));

        lblEmpTelefono.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpTelefono.setText("Teléfono");
        panelCardEmpresa.add(lblEmpTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 175, 140, 20));

        txtEmpTelefono.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpTelefono.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpTelefono.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 198, 140, 32));

        lblEmpEmail.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpEmail.setText("Correo electrónico");
        panelCardEmpresa.add(lblEmpEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 175, 235, 20));

        txtEmpEmail.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpEmail.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmpEmail.setForeground(new java.awt.Color(243, 244, 246));
        panelCardEmpresa.add(txtEmpEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 198, 235, 32));

        lblEmpActividades.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpActividades.setText("Actividades económicas (una por línea)");
        panelCardEmpresa.add(lblEmpActividades, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 400, 20));

        scEmpActividades.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));

        txtEmpActividades.setBackground(new java.awt.Color(15, 23, 42));
        txtEmpActividades.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        txtEmpActividades.setForeground(new java.awt.Color(243, 244, 246));
        txtEmpActividades.setLineWrap(true);
        txtEmpActividades.setWrapStyleWord(true);
        scEmpActividades.setViewportView(txtEmpActividades);

        panelCardEmpresa.add(scEmpActividades, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 263, 450, 62));

        lblEmpLogo.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpLogo.setText("Logo de la factura");
        panelCardEmpresa.add(lblEmpLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 240, 150, 20));

        lblEmpLogoPreview.setBackground(new java.awt.Color(248, 249, 250));
        lblEmpLogoPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        lblEmpLogoPreview.setOpaque(true);
        panelCardEmpresa.add(lblEmpLogoPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 263, 110, 62));

        btnElegirLogo.setBackground(new java.awt.Color(51, 65, 85));
        btnElegirLogo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnElegirLogo.setForeground(new java.awt.Color(243, 244, 246));
        btnElegirLogo.setText("Elegir logo…");
        btnElegirLogo.setBorderPainted(false);
        btnElegirLogo.setFocusPainted(false);
        btnElegirLogo.addActionListener(this::btnElegirLogoActionPerformed);
        panelCardEmpresa.add(btnElegirLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 263, 130, 28));

        btnQuitarLogo.setBackground(new java.awt.Color(51, 65, 85));
        btnQuitarLogo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnQuitarLogo.setForeground(new java.awt.Color(243, 244, 246));
        btnQuitarLogo.setText("Logo por defecto");
        btnQuitarLogo.setBorderPainted(false);
        btnQuitarLogo.setFocusPainted(false);
        btnQuitarLogo.addActionListener(this::btnQuitarLogoActionPerformed);
        panelCardEmpresa.add(btnQuitarLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 297, 130, 28));

        lblEmpLogoHint.setForeground(new java.awt.Color(156, 163, 175));
        lblEmpLogoHint.setText("Si no elegís un logo se usa el que viene con el sistema");
        panelCardEmpresa.add(lblEmpLogoHint, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 333, 370, 20));

        btnGuardarEmpresa.setBackground(new java.awt.Color(34, 197, 94));
        btnGuardarEmpresa.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardarEmpresa.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarEmpresa.setText("Guardar datos");
        btnGuardarEmpresa.setBorderPainted(false);
        btnGuardarEmpresa.setFocusPainted(false);
        btnGuardarEmpresa.addActionListener(this::btnGuardarEmpresaActionPerformed);
        panelCardEmpresa.add(btnGuardarEmpresa, new org.netbeans.lib.awtextra.AbsoluteConstraints(875, 340, 150, 38));

        panelContenido.add(panelCardEmpresa, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 250, 1045, 395));

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
        guardarDefecto();
    }//GEN-LAST:event_btnGuardarDefectoActionPerformed

    private void btnOscuroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOscuroActionPerformed
        UiTheme.aplicarTema(true);
        actualizarLabelTema();
    }//GEN-LAST:event_btnOscuroActionPerformed

    private void btnClaroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClaroActionPerformed
        UiTheme.aplicarTema(false);
        actualizarLabelTema();
    }//GEN-LAST:event_btnClaroActionPerformed

    private void btnRespaldoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRespaldoActionPerformed
        FormRespaldo.mostrar(this);
    }//GEN-LAST:event_btnRespaldoActionPerformed

    private void btnElegirLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElegirLogoActionPerformed
        elegirLogo();
    }//GEN-LAST:event_btnElegirLogoActionPerformed

    private void btnQuitarLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarLogoActionPerformed
        logoEmpresa = null;
        actualizarPreviewLogo();
    }//GEN-LAST:event_btnQuitarLogoActionPerformed

    private void btnGuardarEmpresaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarEmpresaActionPerformed
        guardarEmpresa();
    }//GEN-LAST:event_btnGuardarEmpresaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClaro;
    private javax.swing.JButton btnElegirLogo;
    private javax.swing.JButton btnGuardarDefecto;
    private javax.swing.JButton btnGuardarEmpresa;
    private javax.swing.JButton btnOscuro;
    private javax.swing.JButton btnQuitarLogo;
    private javax.swing.JButton btnRespaldo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboEmpContribuyente;
    private com.toedter.calendar.JDateChooser dcEmpVigencia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblEmpActividades;
    private javax.swing.JLabel lblEmpCiudad;
    private javax.swing.JLabel lblEmpContribuyente;
    private javax.swing.JLabel lblEmpDireccion;
    private javax.swing.JLabel lblEmpDv;
    private javax.swing.JLabel lblEmpEmail;
    private javax.swing.JLabel lblEmpEstablecimiento;
    private javax.swing.JLabel lblEmpLogo;
    private javax.swing.JLabel lblEmpLogoHint;
    private javax.swing.JLabel lblEmpLogoPreview;
    private javax.swing.JLabel lblEmpNombreComercial;
    private javax.swing.JLabel lblEmpNroFin;
    private javax.swing.JLabel lblEmpNroInicio;
    private javax.swing.JLabel lblEmpPunto;
    private javax.swing.JLabel lblEmpRazonSocial;
    private javax.swing.JLabel lblEmpRuc;
    private javax.swing.JLabel lblEmpTelefono;
    private javax.swing.JLabel lblEmpTimbrado;
    private javax.swing.JLabel lblEmpTitulo;
    private javax.swing.JLabel lblEmpVigencia;
    private javax.swing.JLabel lblHintTema;
    private javax.swing.JLabel lblTema;
    private javax.swing.JLabel lblTemaActual;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelCardDefecto;
    private javax.swing.JPanel panelCardEmpresa;
    private javax.swing.JPanel panelCardTema;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelTitulo;
    private javax.swing.JRadioButton rbClaro;
    private javax.swing.JRadioButton rbOscuro;
    private javax.swing.JRadioButton rbUltimo;
    private javax.swing.JScrollPane scEmpActividades;
    private javax.swing.JLabel subtítulo;
    private javax.swing.JTextArea txtEmpActividades;
    private javax.swing.JTextField txtEmpCiudad;
    private javax.swing.JTextField txtEmpDireccion;
    private javax.swing.JTextField txtEmpDv;
    private javax.swing.JTextField txtEmpEmail;
    private javax.swing.JTextField txtEmpEstablecimiento;
    private javax.swing.JTextField txtEmpNombreComercial;
    private javax.swing.JTextField txtEmpNroFin;
    private javax.swing.JTextField txtEmpNroInicio;
    private javax.swing.JTextField txtEmpPunto;
    private javax.swing.JTextField txtEmpRazonSocial;
    private javax.swing.JTextField txtEmpRuc;
    private javax.swing.JTextField txtEmpTelefono;
    private javax.swing.JTextField txtEmpTimbrado;
    // End of variables declaration//GEN-END:variables
}
