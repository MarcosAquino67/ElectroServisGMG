/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.clases.OrdenServicio;
import com.electroservisgmg.clases.Venta;
import com.electroservisgmg.metodos.ClienteMetodos;
import com.electroservisgmg.metodos.OrdenServicioMetodos;
import com.electroservisgmg.metodos.VentaMetodos;
import com.electroservisgmg.reportes.ReportesMetodos;

import java.util.List;

/**
 * Pantalla de reportes: genera los PDF (listados y comprobantes) y los abre.
 *
 * @author Aurelio
 */
public class FormReportes extends javax.swing.JPanel {

    /**
     * Creates new form FormReportes
     */
    public FormReportes() {
        initComponents();
        btnInventario.addActionListener(e -> generar(() -> ReportesMetodos.inventario(), "Reporte de inventario"));
        btnClientes.addActionListener(e -> generar(() -> ReportesMetodos.clientes(), "Reporte de clientes"));
        btnVentasTodas.addActionListener(e -> generar(() -> ReportesMetodos.ventas(null, null), "Reporte de ventas"));
        btnVentasRango.addActionListener(e -> ventasPorRango());
        btnTicket.addActionListener(e -> elegirVenta());
        btnOrden.addActionListener(e -> elegirOrden());
        btnCarpeta.addActionListener(e -> abrirCarpeta());
        btnFacturaReporte.addActionListener(e -> elegirVentaFactura());
    }

    /** Ejecuta la generacion y avisa el resultado; abre el PDF si salio bien. */
    private void generar(java.util.concurrent.Callable<java.io.File> tarea, String titulo) {
        try {
            java.io.File pdf = tarea.call();
            javax.swing.JOptionPane.showMessageDialog(this,
                    titulo + " generado:\n" + pdf.getAbsolutePath());
            ReportesMetodos.abrir(pdf);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No se pudo generar " + titulo.toLowerCase() + ":\n" + e.getMessage());
        }
    }

    private void ventasPorRango() {
        com.toedter.calendar.JDateChooser d1 = new com.toedter.calendar.JDateChooser();
        com.toedter.calendar.JDateChooser d2 = new com.toedter.calendar.JDateChooser();
        d1.setDateFormatString("dd/MM/yyyy");
        d2.setDateFormatString("dd/MM/yyyy");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        d2.setDate(cal.getTime());
        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        d1.setDate(cal.getTime());
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 6, 6));
        p.add(new javax.swing.JLabel("Desde:"));
        p.add(d1);
        p.add(new javax.swing.JLabel("Hasta:"));
        p.add(d2);
        int r = javax.swing.JOptionPane.showConfirmDialog(this, p, "Rango de fechas de ventas",
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (r != javax.swing.JOptionPane.OK_OPTION) return;
        if (d1.getDate() == null || d2.getDate() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Elija las dos fechas");
            return;
        }
        if (d1.getDate().after(d2.getDate())) {
            javax.swing.JOptionPane.showMessageDialog(this, "La fecha \"Desde\" no puede ser mayor que \"Hasta\"");
            return;
        }
        java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd");
        generar(() -> ReportesMetodos.ventas(iso.format(d1.getDate()), iso.format(d2.getDate())),
                "Reporte de ventas");
    }

    private void elegirVenta() {
        List<Venta> ventas = new VentaMetodos().listar();
        if (ventas.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Todavía no hay ventas registradas");
            return;
        }
        List<Cliente> clientes = new ClienteMetodos().listar();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String[] op = new String[ventas.size()];
        for (int i = 0; i < ventas.size(); i++) {
            Venta v = ventas.get(i);
            op[i] = "#" + v.getIdVenta() + "  ·  " + (v.getFechaVenta() == null ? "" : sdf.format(v.getFechaVenta()))
                    + "  ·  " + nombreCliente(clientes, v.getIdCliente())
                    + "  ·  " + UiTheme.gs(v.getTotal());
        }
        String sel = (String) javax.swing.JOptionPane.showInputDialog(this, "Elija la venta:", "Ticket de venta",
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, op, op[0]);
        if (sel == null) return;
        final int idx = java.util.Arrays.asList(op).indexOf(sel);
        if (idx < 0) return;
        generar(() -> ReportesMetodos.ticketVenta(ventas.get(idx).getIdVenta()), "Ticket de venta");
    }

    private void elegirOrden() {
        List<OrdenServicio> ordenes = new OrdenServicioMetodos().listar();
        if (ordenes.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Todavía no hay órdenes de servicio registradas");
            return;
        }
        List<Cliente> clientes = new ClienteMetodos().listar();
        String[] op = new String[ordenes.size()];
        for (int i = 0; i < ordenes.size(); i++) {
            OrdenServicio o = ordenes.get(i);
            op[i] = "#" + o.getIdOrden() + "  ·  " + nombreCliente(clientes, o.getIdCliente())
                    + "  ·  " + (o.getEstado() == null ? "" : o.getEstado())
                    + "  ·  " + UiTheme.gs(o.getCostoReparacion());
        }
        String sel = (String) javax.swing.JOptionPane.showInputDialog(this, "Elija la orden de servicio:",
                "Comprobante de orden", javax.swing.JOptionPane.QUESTION_MESSAGE, null, op, op[0]);
        if (sel == null) return;
        final int idx = java.util.Arrays.asList(op).indexOf(sel);
        if (idx < 0) return;
        generar(() -> ReportesMetodos.comprobanteOrden(ordenes.get(idx).getIdOrden()), "Comprobante de orden");
    }

    private void abrirCarpeta() {
        java.io.File d = new java.io.File("reportes_generados");
        if (!d.exists()) d.mkdirs();
        ReportesMetodos.abrir(d);
    }

    /** Elige una venta y emite (o reimprime) su Factura Electronica. */
    private void elegirVentaFactura() {
        List<Venta> ventas = new VentaMetodos().listar();
        if (ventas.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Todavía no hay ventas registradas");
            return;
        }
        List<Cliente> clientes = new ClienteMetodos().listar();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String[] op = new String[ventas.size()];
        for (int i = 0; i < ventas.size(); i++) {
            Venta v = ventas.get(i);
            String doc = (v.getCdc() == null || v.getCdc().isEmpty())
                    ? "sin factura" : ("factura N° " + v.getNroFactura());
            op[i] = "#" + v.getIdVenta() + "  ·  " + (v.getFechaVenta() == null ? "" : sdf.format(v.getFechaVenta()))
                    + "  ·  " + nombreCliente(clientes, v.getIdCliente())
                    + "  ·  " + UiTheme.gs(v.getTotal()) + "  ·  " + doc;
        }
        String sel = (String) javax.swing.JOptionPane.showInputDialog(this, "Elija la venta:", "Factura electrónica",
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, op, op[0]);
        if (sel == null) return;
        final int idx = java.util.Arrays.asList(op).indexOf(sel);
        if (idx < 0) return;
        int id = ventas.get(idx).getIdVenta();
        try {
            java.io.File pdf = ReportesMetodos.facturaElectronica(id);
            Venta vf = new VentaMetodos().buscar(id);
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Factura electrónica emitida.\n"
                    + "N°: " + (vf == null ? "" : vf.getNroFactura()) + "\n"
                    + "CDC: " + (vf == null ? "" : vf.getCdc()) + "\n\n" + pdf.getAbsolutePath());
            ReportesMetodos.abrir(pdf);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No se pudo emitir la factura:\n" + e.getMessage());
        }
    }

    private String nombreCliente(List<Cliente> clientes, int id) {
        for (Cliente c : clientes) {
            if (c.getIdCliente() == id) {
                return (c.getNombre() == null ? "" : c.getNombre()) + " "
                        + (c.getApellido() == null ? "" : c.getApellido());
            }
        }
        return "Cliente #" + id;
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
        panelContenido = new javax.swing.JPanel();
        cardInventario = new javax.swing.JPanel();
        lblT1 = new javax.swing.JLabel();
        lblD1 = new javax.swing.JLabel();
        btnInventario = new javax.swing.JButton();
        cardClientes = new javax.swing.JPanel();
        lblT2 = new javax.swing.JLabel();
        lblD2 = new javax.swing.JLabel();
        btnClientes = new javax.swing.JButton();
        cardVentas = new javax.swing.JPanel();
        lblT3 = new javax.swing.JLabel();
        lblD3 = new javax.swing.JLabel();
        btnVentasTodas = new javax.swing.JButton();
        btnVentasRango = new javax.swing.JButton();
        cardTicket = new javax.swing.JPanel();
        lblT4 = new javax.swing.JLabel();
        lblD4 = new javax.swing.JLabel();
        btnTicket = new javax.swing.JButton();
        cardOrden = new javax.swing.JPanel();
        lblT5 = new javax.swing.JLabel();
        lblD5 = new javax.swing.JLabel();
        btnOrden = new javax.swing.JButton();
        cardCarpeta = new javax.swing.JPanel();
        lblT6 = new javax.swing.JLabel();
        lblD6 = new javax.swing.JLabel();
        btnCarpeta = new javax.swing.JButton();
        cardFactura = new javax.swing.JPanel();
        lblT7 = new javax.swing.JLabel();
        lblD7 = new javax.swing.JLabel();
        btnFacturaReporte = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(18, 24, 36));
        jPanel1.setPreferredSize(new java.awt.Dimension(1084, 700));
        jPanel1.setLayout(new java.awt.BorderLayout());

        panelNorte.setBackground(new java.awt.Color(18, 24, 36));
        panelNorte.setPreferredSize(new java.awt.Dimension(0, 95));
        panelNorte.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("REPORTES");
        panelNorte.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 30));

        lblSub.setForeground(new java.awt.Color(156, 163, 175));
        lblSub.setText("Genera los listados y comprobantes del sistema en PDF");
        panelNorte.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 700, 22));

        jPanel1.add(panelNorte, java.awt.BorderLayout.NORTH);

        panelContenido.setBackground(new java.awt.Color(18, 24, 36));
        panelContenido.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelContenido.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cardInventario.setBackground(new java.awt.Color(30, 41, 59));
        cardInventario.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        cardInventario.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblT1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblT1.setForeground(new java.awt.Color(243, 244, 246));
        lblT1.setText("Inventario de productos");
        cardInventario.add(lblT1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 430, 26));

        lblD1.setForeground(new java.awt.Color(156, 163, 175));
        lblD1.setText("Listado de productos con stock y precios");
        cardInventario.add(lblD1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 43, 430, 20));

        btnInventario.setBackground(new java.awt.Color(56, 189, 248));
        btnInventario.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnInventario.setForeground(new java.awt.Color(15, 23, 42));
        btnInventario.setText("Generar PDF");
        btnInventario.setBorderPainted(false);
        btnInventario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnInventario.setFocusPainted(false);
        cardInventario.add(btnInventario, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 72, 200, 40));

        panelContenido.add(cardInventario, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 25, 470, 135));

        cardClientes.setBackground(new java.awt.Color(30, 41, 59));
        cardClientes.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        cardClientes.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblT2.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblT2.setForeground(new java.awt.Color(243, 244, 246));
        lblT2.setText("Clientes");
        cardClientes.add(lblT2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 430, 26));

        lblD2.setForeground(new java.awt.Color(156, 163, 175));
        lblD2.setText("Listado de clientes con teléfono y correo");
        cardClientes.add(lblD2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 43, 430, 20));

        btnClientes.setBackground(new java.awt.Color(56, 189, 248));
        btnClientes.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnClientes.setForeground(new java.awt.Color(15, 23, 42));
        btnClientes.setText("Generar PDF");
        btnClientes.setBorderPainted(false);
        btnClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClientes.setFocusPainted(false);
        cardClientes.add(btnClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 72, 200, 40));

        panelContenido.add(cardClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 175, 470, 135));

        cardVentas.setBackground(new java.awt.Color(30, 41, 59));
        cardVentas.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        cardVentas.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblT3.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblT3.setForeground(new java.awt.Color(243, 244, 246));
        lblT3.setText("Ventas");
        cardVentas.add(lblT3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 430, 26));

        lblD3.setForeground(new java.awt.Color(156, 163, 175));
        lblD3.setText("Todas las ventas o las de un rango de fechas");
        cardVentas.add(lblD3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 43, 430, 20));

        btnVentasTodas.setBackground(new java.awt.Color(56, 189, 248));
        btnVentasTodas.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnVentasTodas.setForeground(new java.awt.Color(15, 23, 42));
        btnVentasTodas.setText("Todas las ventas");
        btnVentasTodas.setBorderPainted(false);
        btnVentasTodas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVentasTodas.setFocusPainted(false);
        cardVentas.add(btnVentasTodas, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 72, 200, 40));

        btnVentasRango.setBackground(new java.awt.Color(51, 65, 85));
        btnVentasRango.setForeground(new java.awt.Color(243, 244, 246));
        btnVentasRango.setText("Elegir rango de fechas...");
        cardVentas.add(btnVentasRango, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 72, 200, 40));

        panelContenido.add(cardVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 325, 470, 135));

        cardTicket.setBackground(new java.awt.Color(30, 41, 59));
        cardTicket.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        cardTicket.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblT4.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblT4.setForeground(new java.awt.Color(243, 244, 246));
        lblT4.setText("Ticket de venta");
        cardTicket.add(lblT4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 430, 26));

        lblD4.setForeground(new java.awt.Color(156, 163, 175));
        lblD4.setText("Comprobante de una venta ya registrada");
        cardTicket.add(lblD4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 43, 430, 20));

        btnTicket.setBackground(new java.awt.Color(56, 189, 248));
        btnTicket.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTicket.setForeground(new java.awt.Color(15, 23, 42));
        btnTicket.setText("Elegir venta...");
        btnTicket.setBorderPainted(false);
        btnTicket.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTicket.setFocusPainted(false);
        cardTicket.add(btnTicket, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 72, 200, 40));

        panelContenido.add(cardTicket, new org.netbeans.lib.awtextra.AbsoluteConstraints(555, 25, 470, 135));

        cardOrden.setBackground(new java.awt.Color(30, 41, 59));
        cardOrden.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        cardOrden.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblT5.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblT5.setForeground(new java.awt.Color(243, 244, 246));
        lblT5.setText("Comprobante de orden de servicio");
        cardOrden.add(lblT5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 430, 26));

        lblD5.setForeground(new java.awt.Color(156, 163, 175));
        lblD5.setText("Comprobante por duplicado (taller y cliente)");
        cardOrden.add(lblD5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 43, 430, 20));

        btnOrden.setBackground(new java.awt.Color(56, 189, 248));
        btnOrden.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnOrden.setForeground(new java.awt.Color(15, 23, 42));
        btnOrden.setText("Elegir orden...");
        btnOrden.setBorderPainted(false);
        btnOrden.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnOrden.setFocusPainted(false);
        cardOrden.add(btnOrden, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 72, 200, 40));

        panelContenido.add(cardOrden, new org.netbeans.lib.awtextra.AbsoluteConstraints(555, 175, 470, 135));

        cardCarpeta.setBackground(new java.awt.Color(30, 41, 59));
        cardCarpeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        cardCarpeta.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblT6.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblT6.setForeground(new java.awt.Color(243, 244, 246));
        lblT6.setText("Carpeta de reportes");
        cardCarpeta.add(lblT6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 430, 26));

        lblD6.setForeground(new java.awt.Color(156, 163, 175));
        lblD6.setText("Todos los PDF se guardan en la carpeta \"reportes_generados\"");
        cardCarpeta.add(lblD6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 43, 430, 20));

        btnCarpeta.setBackground(new java.awt.Color(51, 65, 85));
        btnCarpeta.setForeground(new java.awt.Color(243, 244, 246));
        btnCarpeta.setText("Abrir carpeta");
        cardCarpeta.add(btnCarpeta, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 72, 200, 40));

        panelContenido.add(cardCarpeta, new org.netbeans.lib.awtextra.AbsoluteConstraints(555, 325, 470, 135));

        cardFactura.setBackground(new java.awt.Color(30, 41, 59));
        cardFactura.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        cardFactura.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblT7.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblT7.setForeground(new java.awt.Color(243, 244, 246));
        lblT7.setText("Factura electrónica");
        cardFactura.add(lblT7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 700, 26));

        lblD7.setForeground(new java.awt.Color(156, 163, 175));
        lblD7.setText("Emite la factura de una venta: le asigna N° correlativo y CDC");
        cardFactura.add(lblD7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 43, 700, 20));

        btnFacturaReporte.setBackground(new java.awt.Color(34, 197, 94));
        btnFacturaReporte.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnFacturaReporte.setForeground(new java.awt.Color(255, 255, 255));
        btnFacturaReporte.setText("Elegir venta...");
        btnFacturaReporte.setBorderPainted(false);
        btnFacturaReporte.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFacturaReporte.setFocusPainted(false);
        cardFactura.add(btnFacturaReporte, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 72, 220, 40));

        panelContenido.add(cardFactura, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 475, 1000, 135));

        jPanel1.add(panelContenido, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1035, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCarpeta;
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnFacturaReporte;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnOrden;
    private javax.swing.JButton btnTicket;
    private javax.swing.JButton btnVentasRango;
    private javax.swing.JButton btnVentasTodas;
    private javax.swing.JPanel cardCarpeta;
    private javax.swing.JPanel cardClientes;
    private javax.swing.JPanel cardFactura;
    private javax.swing.JPanel cardInventario;
    private javax.swing.JPanel cardOrden;
    private javax.swing.JPanel cardTicket;
    private javax.swing.JPanel cardVentas;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblD1;
    private javax.swing.JLabel lblD2;
    private javax.swing.JLabel lblD3;
    private javax.swing.JLabel lblD4;
    private javax.swing.JLabel lblD5;
    private javax.swing.JLabel lblD6;
    private javax.swing.JLabel lblD7;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblT1;
    private javax.swing.JLabel lblT2;
    private javax.swing.JLabel lblT3;
    private javax.swing.JLabel lblT4;
    private javax.swing.JLabel lblT5;
    private javax.swing.JLabel lblT6;
    private javax.swing.JLabel lblT7;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelNorte;
    // End of variables declaration//GEN-END:variables
}
