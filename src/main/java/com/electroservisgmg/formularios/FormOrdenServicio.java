/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.electroservisgmg.formularios;

import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.clases.Equipo;
import com.electroservisgmg.clases.OrdenServicio;
import com.electroservisgmg.clases.Sesion;
import com.electroservisgmg.metodos.ClienteMetodos;
import com.electroservisgmg.metodos.EquipoMetodos;
import com.electroservisgmg.metodos.OrdenServicioMetodos;

/**
 *
 * @author Aurelio
 */
public class FormOrdenServicio extends javax.swing.JPanel {

    private java.util.List<Cliente> listaClientes = new java.util.ArrayList<>();
    private java.util.List<Equipo> listaEquipos = new java.util.ArrayList<>();

    /**
     * Creates new form FormOrdenServicio
     */
    public FormOrdenServicio() {
        initComponents();
        txtBuscar.setText("");
        UiTheme.styleTable(tblOrdenes);
        UiTheme.miles(txtCosto, true);
        cargarClientes();
        cargarEstados();
        cargarEquipos();
        cargarTabla("");
        btnBuscar.addActionListener(e -> buscar());
        btnNuevo.addActionListener(e -> limpiar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRetirar.addActionListener(e -> retirar());
        crearBotonImprimir();
        crearBotonRepuestos();
        txtBuscar.addActionListener(e -> buscar());
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
        });
        cboCliente.addActionListener(e -> cargarEquipos());
        tblOrdenes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) llenarDesdeFila();
        });
    }

    // ---- boton Imprimir (fuera del GEN: no esta en el diseno de Matisse) ----
    private void crearBotonImprimir() {
        javax.swing.JButton btnImprimir = new javax.swing.JButton("Imprimir orden");
        btnImprimir.setBackground(new java.awt.Color(51, 65, 85));
        btnImprimir.setForeground(new java.awt.Color(243, 244, 246));
        btnImprimir.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnImprimir.setBorderPainted(false);
        btnImprimir.setFocusPainted(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.addActionListener(e -> imprimir());
        panelNorte.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(915, 75, 150, 35));
    }

    // ---- repuestos de la orden (fuera del GEN: no estan en el diseno de Matisse) ----

    private javax.swing.JLabel lblRepuestos;

    /**
     * Boton para cargar los repuestos usados en la reparacion. Va en el espacio
     * libre de abajo del formulario.
     */
    private void crearBotonRepuestos() {
        javax.swing.JButton btn = new javax.swing.JButton("Repuestos usados…");
        btn.setBackground(new java.awt.Color(51, 65, 85));
        btn.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btn.setForeground(new java.awt.Color(243, 244, 246));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setToolTipText("Carga los repuestos que se usaron: se descuentan del stock y se suman al costo de la reparación");
        btn.addActionListener(e -> abrirRepuestos());
        jPanel3.add(btn, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 160, 190, 32));

        lblRepuestos = new javax.swing.JLabel(" ");
        lblRepuestos.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblRepuestos.setForeground(new java.awt.Color(156, 163, 175));
        jPanel3.add(lblRepuestos, new org.netbeans.lib.awtextra.AbsoluteConstraints(225, 160, 700, 32));
    }

    /** Abre la ventana de repuestos de la orden elegida en la lista. */
    private void abrirRepuestos() {
        int fila = tblOrdenes.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Seleccione en la lista la orden que quiere cargarle repuestos");
            return;
        }
        int id = ((Number) tblOrdenes.getValueAt(fila, 0)).intValue();
        String detalle = tblOrdenes.getValueAt(fila, 1) + " · " + tblOrdenes.getValueAt(fila, 2);
        FormRepuestosOrden.mostrar(this, id, detalle, this::despuesDeRepuestos);
    }

    /** Se llama al cerrar cambios en los repuestos: el costo de la orden cambió. */
    private void despuesDeRepuestos() {
        llenarDesdeFila();
        mostrarResumenRepuestos();
    }

    /** Linea de abajo con el detalle de los repuestos de la orden elegida. */
    private void mostrarResumenRepuestos() {
        if (lblRepuestos == null) return;
        int fila = tblOrdenes.getSelectedRow();
        if (fila < 0) {
            lblRepuestos.setText(" ");
            return;
        }
        int id = ((Number) tblOrdenes.getValueAt(fila, 0)).intValue();
        java.util.List<com.electroservisgmg.clases.OrdenRepuesto> rep =
                new com.electroservisgmg.metodos.OrdenRepuestoMetodos().listar(id);
        if (rep.isEmpty()) {
            lblRepuestos.setText("Sin repuestos cargados en esta orden");
            return;
        }
        double total = 0;
        int unidades = 0;
        for (com.electroservisgmg.clases.OrdenRepuesto r : rep) {
            total += r.getSubtotal();
            unidades += r.getCantidad();
        }
        lblRepuestos.setText(rep.size() + (rep.size() == 1 ? " repuesto" : " repuestos")
                + " (" + unidades + (unidades == 1 ? " unidad" : " unidades") + ") · "
                + UiTheme.gs(total) + " ya incluidos en el costo de la reparación");
    }

    /** Comprobante por duplicado (taller y cliente) de la orden seleccionada. */
    private void imprimir() {
        int fila = tblOrdenes.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Seleccione una orden de la tabla para imprimir su comprobante");
            return;
        }
        int id = (int) tblOrdenes.getValueAt(fila, 0);
        try {
            java.io.File pdf = com.electroservisgmg.reportes.ReportesMetodos.comprobanteOrden(id);
            javax.swing.JOptionPane.showMessageDialog(this, "Comprobante generado:\n" + pdf.getAbsolutePath());
            com.electroservisgmg.reportes.ReportesMetodos.abrir(pdf);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se pudo generar el comprobante:\n" + e.getMessage());
        }
    }

    // ---- combos ----
    private void cargarClientes() {
        listaClientes = new ClienteMetodos().listar();
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        for (Cliente c : listaClientes) m.addElement(c.getIdCliente() + " - " + c.getNombre() + " " + c.getApellido());
        cboCliente.setModel(m);
    }

    private void cargarEquipos() {
        int idCli = idClienteSeleccionado();
        listaEquipos = new java.util.ArrayList<>();
        for (Equipo e : new EquipoMetodos().listar()) {
            if (idCli < 0 || e.getIdCliente() == idCli) listaEquipos.add(e);
        }
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        for (Equipo e : listaEquipos)
            m.addElement(e.getTipo() + " " + e.getMarca() + " " + e.getModelo() + " (" + e.getNroSerie() + ")");
        cboEquipo.setModel(m);
    }

    private void cargarEstados() {
        cboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"PENDIENTE", "EN_PROCESO", "COMPLETADO", "ENTREGADO"}));
    }

    private int idClienteSeleccionado() {
        int i = cboCliente.getSelectedIndex();
        if (i < 0 || i >= listaClientes.size()) return -1;
        return listaClientes.get(i).getIdCliente();
    }

    private void seleccionarCliente(int id) {
        for (int i = 0; i < listaClientes.size(); i++) {
            if (listaClientes.get(i).getIdCliente() == id) { cboCliente.setSelectedIndex(i); return; }
        }
    }

    private int idEquipoSeleccionado() {
        int i = cboEquipo.getSelectedIndex();
        if (i < 0 || i >= listaEquipos.size()) return -1;
        return listaEquipos.get(i).getIdEquipo();
    }

    private void seleccionarEquipo(int id) {
        for (int i = 0; i < listaEquipos.size(); i++) {
            if (listaEquipos.get(i).getIdEquipo() == id) { cboEquipo.setSelectedIndex(i); return; }
        }
    }

    // ---- mapa para tabla ----
    private java.util.Map<Integer, String> mapaClientes() {
        java.util.Map<Integer, String> m = new java.util.HashMap<>();
        for (Cliente c : new ClienteMetodos().listar()) m.put(c.getIdCliente(), c.getNombre() + " " + c.getApellido());
        return m;
    }

    private java.util.Map<Integer, String> mapaEquipos() {
        java.util.Map<Integer, String> m = new java.util.HashMap<>();
        for (Equipo e : new EquipoMetodos().listar())
            m.put(e.getIdEquipo(), e.getMarca() + " " + e.getModelo() + " (" + e.getNroSerie() + ")");
        return m;
    }

    // ---- tabla ----
    private void cargarTabla(String filtro) {
        java.util.List<OrdenServicio> lista = new OrdenServicioMetodos().listar();
        java.util.Map<Integer, String> cli = mapaClientes();
        java.util.Map<Integer, String> eq = mapaEquipos();
        String f = filtro == null ? "" : filtro.toLowerCase();
        java.util.List<OrdenServicio> fil = new java.util.ArrayList<>();
        for (OrdenServicio o : lista) {
            String nomCli = cli.getOrDefault(o.getIdCliente(), "");
            String nomEq = eq.getOrDefault(o.getIdEquipo(), "");
            if (f.isEmpty() || nomCli.toLowerCase().contains(f)
                    || nomEq.toLowerCase().contains(f)
                    || o.getEstado().toLowerCase().contains(f)) fil.add(o);
        }
        String[] cols = {"ID", "Cliente", "Equipo", "Ingreso", "Estado", "Costo"};
        Object[][] data = new Object[fil.size()][6];
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy");
        for (int i = 0; i < fil.size(); i++) {
            OrdenServicio o = fil.get(i);
            data[i] = new Object[]{o.getIdOrden(),
                cli.getOrDefault(o.getIdCliente(), "ID " + o.getIdCliente()),
                eq.getOrDefault(o.getIdEquipo(), "ID " + o.getIdEquipo()),
                o.getFechaIngreso() == null ? "" : df.format(o.getFechaIngreso()),
                o.getEstado(), UiTheme.gs(o.getCostoReparacion())};
        }
        tblOrdenes.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        UiTheme.styleTable(tblOrdenes);
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    // ---- formulario ----
    private void limpiar() {
        txtId.setText("");
        if (cboCliente.getItemCount() > 0) cboCliente.setSelectedIndex(0);
        cargarEquipos();
        if (cboEstado.getItemCount() > 0) cboEstado.setSelectedItem("PENDIENTE");
        txtCosto.setText("");
        txtDiagnostico.setText("");
        txtObservaciones.setText("");
        tblOrdenes.clearSelection();
        if (lblRepuestos != null) lblRepuestos.setText(" ");
        txtDiagnostico.requestFocus();
    }

    private void llenarDesdeFila() {
        int fila = tblOrdenes.getSelectedRow();
        if (fila < 0) return;
        int id = (int) tblOrdenes.getValueAt(fila, 0);
        for (OrdenServicio o : new OrdenServicioMetodos().listar()) {
            if (o.getIdOrden() == id) {
                txtId.setText(String.valueOf(o.getIdOrden()));
                seleccionarCliente(o.getIdCliente());
                cargarEquipos();
                seleccionarEquipo(o.getIdEquipo());
                cboEstado.setSelectedItem(o.getEstado());
                txtCosto.setText(String.valueOf(Math.round(o.getCostoReparacion())));
                txtDiagnostico.setText(o.getDiagnostico() == null ? "" : o.getDiagnostico());
                txtObservaciones.setText(o.getObservaciones() == null ? "" : o.getObservaciones());
                break;
            }
        }
        mostrarResumenRepuestos();
    }

    private void editar() {
        if (tblOrdenes.getSelectedRow() < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione una orden de la tabla");
            return;
        }
        llenarDesdeFila();
        txtDiagnostico.requestFocus();
    }

    private void guardar() {
        if (cboCliente.getItemCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Primero registre un cliente");
            return;
        }
        int idCli = idClienteSeleccionado();
        int idEq = idEquipoSeleccionado();
        if (idCli < 0 || idEq < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione cliente y equipo");
            return;
        }
        double costo = 0;
        if (!txtCosto.getText().trim().isEmpty()) {
            try {
                costo = UiTheme.parseGs(txtCosto.getText().trim());
                if (costo < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "El costo debe ser un número válido (≥ 0)");
                return;
            }
        }
        OrdenServicio o = new OrdenServicio();
        o.setIdCliente(idCli);
        o.setIdEquipo(idEq);
        o.setEstado(cboEstado.getSelectedItem().toString());
        o.setDiagnostico(txtDiagnostico.getText().trim());
        o.setCostoReparacion(costo);
        o.setObservaciones(txtObservaciones.getText().trim());
        OrdenServicioMetodos om = new OrdenServicioMetodos();
        boolean ok;
        String msg;
        if (txtId.getText().trim().isEmpty()) {
            o.setFechaIngreso(new java.util.Date());
            o.setIdUsuario(Sesion.getUsuario() == null ? 1 : Sesion.getUsuario().getIdUsuario());
            ok = om.insertar(o);
            msg = ok ? "Orden creada correctamente" : "Error al crear la orden";
        } else {
            o.setIdOrden(Integer.parseInt(txtId.getText().trim()));
            ok = om.modificar(o);
            msg = ok ? "Orden actualizada" : "Error al guardar";
        }
        javax.swing.JOptionPane.showMessageDialog(this, msg);
        if (ok) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    private void eliminar() {
        int fila = tblOrdenes.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione una orden de la tabla");
            return;
        }
        int id = (int) tblOrdenes.getValueAt(fila, 0);
        int conf = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Eliminar orden #" + id + "?", "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (conf != javax.swing.JOptionPane.YES_OPTION) return;
        boolean ok = new OrdenServicioMetodos().eliminar(id);
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Eliminada" : "Error al eliminar");
        if (ok) {
            cargarTabla(txtBuscar.getText().trim());
            limpiar();
        }
    }

    /** Pone la orden seleccionada como ENTREGADA con fecha de hoy. */
    private void retirar() {
        int fila = tblOrdenes.getSelectedRow();
        if (fila < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione una orden de la tabla");
            return;
        }
        String estado = String.valueOf(tblOrdenes.getValueAt(fila, 4));
        if ("ENTREGADO".equals(estado)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Esta orden ya fue retirada");
            return;
        }
        int id = (int) tblOrdenes.getValueAt(fila, 0);
        int conf = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Marcar la orden #" + id + " como retirada (ENTREGADO)?",
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (conf != javax.swing.JOptionPane.YES_OPTION) return;
        boolean ok = new OrdenServicioMetodos().marcarRetirado(id);
        javax.swing.JOptionPane.showMessageDialog(this, ok ? "Equipo retirado. Orden entregada." : "Error al actualizar");
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
        btnRetirar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblOrdenes = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        lblCliente = new javax.swing.JLabel();
        lblEquipo = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        lblCosto = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtCosto = new javax.swing.JTextField();
        cboCliente = new javax.swing.JComboBox<>();
        cboEquipo = new javax.swing.JComboBox<>();
        cboEstado = new javax.swing.JComboBox<>();
        lblDiagnostico = new javax.swing.JLabel();
        lblObservaciones = new javax.swing.JLabel();
        txtDiagnostico = new javax.swing.JTextField();
        txtObservaciones = new javax.swing.JTextField();
        btnGuardar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(18, 24, 36));
        jPanel1.setLayout(new java.awt.BorderLayout());

        panelNorte.setBackground(new java.awt.Color(18, 24, 36));
        panelNorte.setPreferredSize(new java.awt.Dimension(0, 130));
        panelNorte.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(243, 244, 246));
        lblTitulo.setText("ÓRDENES DE SERVICIO");
        panelNorte.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 12, 400, 30));

        lblSub.setForeground(new java.awt.Color(156, 163, 175));
        lblSub.setText("Órdenes de reparación del taller");
        panelNorte.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 44, 500, 22));

        txtBuscar.setBackground(new java.awt.Color(15, 23, 42));
        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(243, 244, 246));
        panelNorte.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 75, 250, 35));

        btnBuscar.setBackground(new java.awt.Color(51, 65, 85));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(243, 244, 246));
        btnBuscar.setText("Buscar");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        panelNorte.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 75, 110, 35));

        btnNuevo.setBackground(new java.awt.Color(56, 189, 248));
        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(15, 23, 42));
        btnNuevo.setText("+ Nueva");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        panelNorte.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(385, 75, 110, 35));

        btnEditar.setBackground(new java.awt.Color(51, 65, 85));
        btnEditar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setText("Editar");
        btnEditar.setBorderPainted(false);
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.setFocusPainted(false);
        panelNorte.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 75, 100, 35));

        btnEliminar.setBackground(new java.awt.Color(239, 68, 68));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar");
        btnEliminar.setBorderPainted(false);
        btnEliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminar.setFocusPainted(false);
        panelNorte.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(615, 75, 110, 35));

        btnRetirar.setBackground(new java.awt.Color(34, 197, 94));
        btnRetirar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRetirar.setForeground(new java.awt.Color(255, 255, 255));
        btnRetirar.setText("Marcar retirado");
        btnRetirar.setBorderPainted(false);
        btnRetirar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRetirar.setFocusPainted(false);
        panelNorte.add(btnRetirar, new org.netbeans.lib.awtextra.AbsoluteConstraints(735, 75, 170, 35));

        jPanel1.add(panelNorte, java.awt.BorderLayout.PAGE_START);

        jPanel2.setBackground(new java.awt.Color(18, 24, 36));
        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 10, 15));
        jPanel2.setLayout(new java.awt.BorderLayout());

        tblOrdenes.setBackground(new java.awt.Color(30, 41, 59));
        tblOrdenes.setForeground(new java.awt.Color(243, 244, 246));
        tblOrdenes.setModel(new javax.swing.table.DefaultTableModel(
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
        tblOrdenes.setFillsViewportHeight(true);
        tblOrdenes.setGridColor(new java.awt.Color(51, 65, 85));
        tblOrdenes.setRowHeight(32);
        tblOrdenes.setSelectionBackground(new java.awt.Color(2, 132, 199));
        tblOrdenes.setShowGrid(false);
        jScrollPane1.setViewportView(tblOrdenes);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setBackground(new java.awt.Color(30, 41, 59));
        jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85))));
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 200));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblId.setForeground(new java.awt.Color(156, 163, 175));
        lblId.setText("ID");
        jPanel3.add(lblId, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 15, 60, 25));

        lblCliente.setForeground(new java.awt.Color(156, 163, 175));
        lblCliente.setText("Cliente");
        jPanel3.add(lblCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 15, 220, 25));

        lblEquipo.setForeground(new java.awt.Color(156, 163, 175));
        lblEquipo.setText("Equipo");
        jPanel3.add(lblEquipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 15, 240, 25));

        lblEstado.setForeground(new java.awt.Color(156, 163, 175));
        lblEstado.setText("Estado");
        jPanel3.add(lblEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 15, 150, 25));

        lblCosto.setForeground(new java.awt.Color(156, 163, 175));
        lblCosto.setText("Costo Rep.");
        jPanel3.add(lblCosto, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 15, 130, 25));

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(15, 23, 42));
        txtId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtId.setForeground(new java.awt.Color(243, 244, 246));
        jPanel3.add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 40, 70, 35));

        txtCosto.setBackground(new java.awt.Color(15, 23, 42));
        txtCosto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCosto.setForeground(new java.awt.Color(243, 244, 246));
        jPanel3.add(txtCosto, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 40, 130, 35));

        cboCliente.setBackground(new java.awt.Color(15, 23, 42));
        cboCliente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboCliente.setForeground(new java.awt.Color(243, 244, 246));
        jPanel3.add(cboCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, 220, 35));

        cboEquipo.setBackground(new java.awt.Color(15, 23, 42));
        cboEquipo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboEquipo.setForeground(new java.awt.Color(243, 244, 246));
        jPanel3.add(cboEquipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 40, 240, 35));

        cboEstado.setBackground(new java.awt.Color(15, 23, 42));
        cboEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboEstado.setForeground(new java.awt.Color(243, 244, 246));
        jPanel3.add(cboEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 40, 150, 35));

        lblDiagnostico.setForeground(new java.awt.Color(156, 163, 175));
        lblDiagnostico.setText("Diagnóstico");
        jPanel3.add(lblDiagnostico, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 90, 350, 25));

        lblObservaciones.setForeground(new java.awt.Color(156, 163, 175));
        lblObservaciones.setText("Observaciones");
        jPanel3.add(lblObservaciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 90, 300, 25));

        txtDiagnostico.setBackground(new java.awt.Color(15, 23, 42));
        txtDiagnostico.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtDiagnostico.setForeground(new java.awt.Color(243, 244, 246));
        jPanel3.add(txtDiagnostico, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 115, 350, 35));

        txtObservaciones.setBackground(new java.awt.Color(15, 23, 42));
        txtObservaciones.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtObservaciones.setForeground(new java.awt.Color(243, 244, 246));
        jPanel3.add(txtObservaciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 115, 300, 35));

        btnGuardar.setBackground(new java.awt.Color(34, 197, 94));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        jPanel3.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 115, 120, 38));

        btnLimpiar.setBackground(new java.awt.Color(51, 65, 85));
        btnLimpiar.setForeground(new java.awt.Color(243, 244, 246));
        btnLimpiar.setText("Limpiar");
        jPanel3.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 115, 110, 38));

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 982, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnRetirar;
    private javax.swing.JComboBox<String> cboCliente;
    private javax.swing.JComboBox<String> cboEquipo;
    private javax.swing.JComboBox<String> cboEstado;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblCosto;
    private javax.swing.JLabel lblDiagnostico;
    private javax.swing.JLabel lblEquipo;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblObservaciones;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelNorte;
    private javax.swing.JTable tblOrdenes;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCosto;
    private javax.swing.JTextField txtDiagnostico;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtObservaciones;
    // End of variables declaration//GEN-END:variables
}
