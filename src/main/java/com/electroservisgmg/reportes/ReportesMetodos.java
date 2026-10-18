package com.electroservisgmg.reportes;

import com.electroservisgmg.clases.Cliente;
import com.electroservisgmg.clases.DetalleVenta;
import com.electroservisgmg.clases.Empresa;
import com.electroservisgmg.clases.Equipo;
import com.electroservisgmg.clases.OrdenServicio;
import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.clases.Venta;
import com.electroservisgmg.conexion.Conexion;
import com.electroservisgmg.formularios.UiTheme;
import com.electroservisgmg.metodos.ClienteMetodos;
import com.electroservisgmg.metodos.EmpresaMetodos;
import com.electroservisgmg.metodos.EquipoMetodos;
import com.electroservisgmg.metodos.OrdenServicioMetodos;
import com.electroservisgmg.metodos.ProductoMetodos;
import com.electroservisgmg.metodos.VentaMetodos;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera los reportes (.jrxml de src/main/resources/reportes) como PDF.
 * Los reportes de listado traen su propio SQL (queryString) y se llenan con la
 * conexion JDBC; los comprobantes se llenan con parametros + una lista de filas.
 */
public class ReportesMetodos {

    private static final String CARPETA = "reportes_generados";

    private ReportesMetodos() { }

    private static File carpeta() {
        File d = new File(CARPETA);
        if (!d.exists()) d.mkdirs();
        return d;
    }

    private static JasperReport compilar(String jrxml) throws Exception {
        return compilar(jrxml, null);
    }

    /**
     * Compila la plantilla de /reportes. Si altoPagina no es null, le cambia el
     * alto de la hoja antes de compilar (se usa para el ticket, que se ajusta a
     * la cantidad de items de la venta).
     */
    private static JasperReport compilar(String jrxml, Integer altoPagina) throws Exception {
        try (InputStream is = ReportesMetodos.class.getResourceAsStream("/reportes/" + jrxml)) {
            if (is == null) throw new Exception("No se encontro la plantilla: " + jrxml);
            if (altoPagina == null) return JasperCompileManager.compileReport(is);
            net.sf.jasperreports.engine.design.JasperDesign diseno =
                    net.sf.jasperreports.engine.xml.JRXmlLoader.load(is);
            diseno.setPageHeight(altoPagina);
            return JasperCompileManager.compileReport(diseno);
        }
    }

    private static String sello() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
    }

    private static File exportar(JasperPrint print, String nombre) throws Exception {
        File out = new File(carpeta(), nombre + "_" + sello() + ".pdf");
        JasperExportManager.exportReportToPdfFile(print, out.getAbsolutePath());
        return out;
    }

    /** Reporte cuyo SQL vive en el propio .jrxml (inventario, clientes, ventas). */
    public static File pdfSql(String jrxml, Map<String, Object> params, String nombre) throws Exception {
        try (Connection con = Conexion.nuevaConexion()) {
            return exportar(JasperFillManager.fillReport(compilar(jrxml), params, con), nombre);
        }
    }

    /** Comprobante: parametros + filas (mapas con los nombres de los fields). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static File pdfComprobante(String jrxml, Map<String, Object> params,
                                      List<Map<String, Object>> filas, String nombre) throws Exception {
        return pdfComprobante(jrxml, params, filas, nombre, null);
    }

    /** Igual que el anterior, pero permite fijar el alto de la hoja. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static File pdfComprobante(String jrxml, Map<String, Object> params,
                                      List<Map<String, Object>> filas, String nombre,
                                      Integer altoPagina) throws Exception {
        // Separador de miles con punto, igual que en el resto del sistema.
        if (params != null && !params.containsKey(JRParameter.REPORT_LOCALE)) {
            params.put(JRParameter.REPORT_LOCALE, new java.util.Locale("es", "PY"));
        }
        JRDataSource ds = new JRMapCollectionDataSource((java.util.Collection) filas);
        return exportar(JasperFillManager.fillReport(compilar(jrxml, altoPagina), params, ds), nombre);
    }

    /** Abre el PDF con el visor del sistema. */
    public static void abrir(File pdf) {
        try {
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                java.awt.Desktop.getDesktop().open(pdf);
            }
        } catch (Exception e) {
            System.err.println("No se pudo abrir el PDF: " + e.getMessage());
        }
    }

    // ================== reportes de listado ==================

    public static File inventario() throws Exception {
        return pdfSql("reporte_inventario.jrxml", new HashMap<>(), "reporte_inventario");
    }

    public static File clientes() throws Exception {
        return pdfSql("reporte_clientes.jrxml", new HashMap<>(), "reporte_clientes");
    }

    /** Ventas entre dos fechas (formato yyyy-MM-dd). null = sin limite. */
    public static File ventas(String desde, String hasta) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("desde", desde == null || desde.trim().isEmpty() ? "1900-01-01" : desde.trim());
        p.put("hasta", hasta == null || hasta.trim().isEmpty() ? "2999-12-31" : hasta.trim());
        return pdfSql("reporte_ventas.jrxml", p, "reporte_ventas");
    }

    // ================== etiquetas ==================

    /**
     * Etiquetas de productos: hoja A4 con 24 etiquetas de 66 x 34 mm
     * (3 columnas x 8 filas). La lista se manda ya armada; si se quieren
     * varias copias del mismo producto, se repite el producto en la lista.
     */
    public static File etiquetas(List<Producto> items) throws Exception {
        java.text.DecimalFormat miles = new java.text.DecimalFormat("#,##0",
                new java.text.DecimalFormatSymbols(new java.util.Locale("es", "PY")));
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Producto p : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("nombre", safe(p.getNombre()));
            m.put("precio", "Gs. " + miles.format(p.getPrecioVenta()));
            m.put("codigoBarras", safe(p.getCodigoBarras()));
            filas.add(m);
        }
        Empresa emp = new EmpresaMetodos().obtener();
        String nombreEmpresa = emp == null ? "" : safe(emp.getNombreParaTicket());
        if (nombreEmpresa.trim().isEmpty()) nombreEmpresa = "ELECTROSERVIS G.M.G.";
        Map<String, Object> params = new HashMap<>();
        params.put("empresa", nombreEmpresa);
        return pdfComprobante("etiquetas_productos.jrxml", params, filas, "etiquetas");
    }

    // ================== comprobantes ==================

    private static String nombreCliente(List<Cliente> clientes, int id) {
        for (Cliente c : clientes) {
            if (c.getIdCliente() == id) {
                return (c.getNombre() == null ? "" : c.getNombre()) + " " + (c.getApellido() == null ? "" : c.getApellido());
            }
        }
        return "Cliente #" + id;
    }

    private static String nombreProducto(List<Producto> productos, int id) {
        for (Producto p : productos) if (p.getIdProducto() == id) return p.getNombre();
        return "Producto #" + id;
    }

    /** Ticket / comprobante de una venta ya registrada (hoja de 80 mm). */
    public static File ticketVenta(int idVenta) throws Exception {
        VentaMetodos vm = new VentaMetodos();
        Venta venta = null;
        for (Venta v : vm.listar()) if (v.getIdVenta() == idVenta) { venta = v; break; }
        if (venta == null) throw new Exception("No existe la venta #" + idVenta);
        List<Cliente> clientes = new ClienteMetodos().listar();
        List<Producto> productos = new ProductoMetodos().listar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Empresa emp = new EmpresaMetodos().obtener();
        Cliente cli = null;
        for (Cliente c : clientes) if (c.getIdCliente() == venta.getIdCliente()) { cli = c; break; }

        Map<String, Object> params = new HashMap<>();
        params.put("empresa", safe(emp.getNombreParaTicket()));
        params.put("direccionEmpresa", safe(emp.getDireccion()));
        params.put("ubicacion", ubicacionEmpresa(emp));
        params.put("nroVenta", String.format("%06d", idVenta));
        params.put("cliente", venta.getIdCliente() > 0 ? nombreCliente(clientes, venta.getIdCliente()) : "Consumidor final");
        params.put("rucCi", cli == null || safe(cli.getCedula()).isEmpty() ? "-" : safe(cli.getCedula()));
        params.put("vendedor", nombreVendedor(venta.getIdUsuario()));
        params.put("formaPago", safe(venta.getFormaPago()).isEmpty() ? "-" : venta.getFormaPago());
        params.put("fecha", venta.getFechaVenta() == null ? "" : sdf.format(venta.getFechaVenta()));
        params.put("total", UiTheme.gs(venta.getTotal()));

        // IVA discriminado por tasa, a partir del IVA incluido en cada renglon.
        List<DetalleVenta> detalles = vm.listarDetalles(idVenta);
        double iva10 = 0, iva5 = 0;
        for (DetalleVenta d : detalles) {
            double sub = d.getSubtotal();
            if (d.getIva() >= 9.99) iva10 += sub - sub / 1.10;
            else if (d.getIva() >= 4.99) iva5 += sub - sub / 1.05;
        }
        iva10 = Math.floor(iva10);
        iva5 = Math.floor(iva5);
        StringBuilder ivaTxt = new StringBuilder();
        if (iva10 > 0) ivaTxt.append(UiTheme.gs(iva10)).append(" (10%)");
        if (iva5 > 0) {
            if (ivaTxt.length() > 0) ivaTxt.append("  +  ");
            ivaTxt.append(UiTheme.gs(iva5)).append(" (5%)");
        }
        if (ivaTxt.length() == 0) ivaTxt.append("0");
        params.put("iva", ivaTxt.toString());

        List<Map<String, Object>> filas = new ArrayList<>();
        for (DetalleVenta d : detalles) {
            Map<String, Object> r = new HashMap<>();
            r.put("producto", nombreProducto(productos, d.getIdProducto()));
            r.put("cantidad", d.getCantidad());
            r.put("subtotal", d.getSubtotal());
            filas.add(r);
        }
        // Hoja a medida: margenes (8+8) + encabezado (150) + una linea por item (12) + pie (90).
        int altoHoja = 256 + filas.size() * 12;
        return pdfComprobante("comprobante_venta.jrxml", params, filas, "ticket_venta_" + idVenta, altoHoja);
    }

    /**
     * Marca la condicion de venta como en la factura (ej: Contado [X]    Crédito [  ]).
     * Se usan corchetes porque las fuentes estandar del PDF no traen el simbolo ☑.
     */
    private static String marcasCondicion(String condicion) {
        boolean credito = condicion != null && condicion.trim().toLowerCase().startsWith("cr");
        return "Contado " + (credito ? "[  ]" : "[X]") + "     Crédito " + (credito ? "[X]" : "[  ]");
    }

    /** Ciudad y telefono del emisor para el ticket (ej: CAAGUAZU - Tel: (0971)452427). */
    private static String ubicacionEmpresa(Empresa emp) {
        String ciudad = safe(emp.getCiudad()).trim();
        String tel = safe(emp.getTelefono()).trim();
        String base = ciudad.isEmpty() ? "" : ciudad + " - ";
        return base + (tel.isEmpty() ? "" : "Tel: " + tel);
    }

    /** Nombre del usuario que registro la venta. */
    private static String nombreVendedor(int idUsuario) {
        try {
            for (com.electroservisgmg.clases.Usuario u : new com.electroservisgmg.metodos.UsuarioMetodos().listar()) {
                if (u.getIdUsuario() == idUsuario) return safe(u.getNombre());
            }
        } catch (Exception e) {
            System.err.println("vendedor: " + e.getMessage());
        }
        if (com.electroservisgmg.clases.Sesion.getUsuario() != null) {
            return safe(com.electroservisgmg.clases.Sesion.getUsuario().getNombre());
        }
        return "-";
    }

    /** Comprobante (por duplicado) de una orden de servicio. */
    public static File comprobanteOrden(int idOrden) throws Exception {
        OrdenServicio orden = null;
        for (OrdenServicio o : new OrdenServicioMetodos().listar()) if (o.getIdOrden() == idOrden) { orden = o; break; }
        if (orden == null) throw new Exception("No existe la orden #" + idOrden);
        List<Cliente> clientes = new ClienteMetodos().listar();
        Equipo equipo = null;
        for (Equipo e : new EquipoMetodos().listar()) if (e.getIdEquipo() == orden.getIdEquipo()) { equipo = e; break; }

        String telefono = "";
        for (Cliente c : clientes) if (c.getIdCliente() == orden.getIdCliente()) {
            telefono = c.getTelefono() == null ? "" : c.getTelefono();
            break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Map<String, Object> params = new HashMap<>();
        params.put("nroOrden", String.format("%06d", idOrden));
        params.put("fecha", orden.getFechaIngreso() == null ? "" : sdf.format(orden.getFechaIngreso()));
        params.put("estado", orden.getEstado() == null ? "" : orden.getEstado());
        params.put("cliente", nombreCliente(clientes, orden.getIdCliente()));
        params.put("telefono", telefono);
        params.put("equipo", equipo == null ? "" : safe(equipo.getTipo()));
        params.put("marca", equipo == null ? "" : safe(equipo.getMarca()));
        params.put("modelo", equipo == null ? "" : safe(equipo.getModelo()));
        params.put("serie", equipo == null ? "" : safe(equipo.getNroSerie()));
        params.put("falla", equipo == null || equipo.getFallaReportada() == null || equipo.getFallaReportada().isEmpty()
                ? safe(orden.getDiagnostico()) : equipo.getFallaReportada());
        params.put("costo", UiTheme.gs(orden.getCostoReparacion()));
        params.put("observaciones", safe(orden.getObservaciones()));
        // El detalle del comprobante solo usa parametros, pero necesita al menos
        // una fila para que la banda de detalle se imprima:
        List<Map<String, Object>> unaFila = new ArrayList<>();
        unaFila.add(new HashMap<>());
        return pdfComprobante("comprobante_orden_duplicado.jrxml", params,
                unaFila, "comprobante_orden_" + idOrden);
    }

    private static String safe(String s) { return s == null ? "" : s; }

    // ================== factura electronica ==================

    /**
     * Factura Electronica de una venta. Si la venta todavia no tiene factura le
     * asigna el numero correlativo y le genera el CDC (que queda guardado en la
     * base); si ya la tenia, la reimprime con el mismo numero y CDC.
     */
    public static File facturaElectronica(int idVenta) throws Exception {
        VentaMetodos vm = new VentaMetodos();
        Venta venta = vm.emitirFactura(idVenta);

        Empresa emp = new EmpresaMetodos().obtener();
        List<Cliente> clientes = new ClienteMetodos().listar();
        List<Producto> productos = new ProductoMetodos().listar();

        Cliente cli = null;
        for (Cliente c : clientes) if (c.getIdCliente() == venta.getIdCliente()) { cli = c; break; }

        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy");

        double totalDescuento = 0, totalExenta = 0, total5 = 0, total10 = 0;
        List<Map<String, Object>> filas = new ArrayList<>();
        for (DetalleVenta d : vm.listarDetalles(idVenta)) {
            Producto p = null;
            for (Producto x : productos) if (x.getIdProducto() == d.getIdProducto()) { p = x; break; }

            double valor = d.getSubtotal();
            double vExenta = 0, v5 = 0, v10 = 0;
            if (d.getIva() >= 9.99) v10 = valor;
            else if (d.getIva() >= 4.99) v5 = valor;
            else vExenta = valor;

            totalDescuento += d.getDescuento();
            totalExenta += vExenta;
            total5 += v5;
            total10 += v10;

            Map<String, Object> r = new HashMap<>();
            r.put("codigo", p == null || p.getCodigo() == null ? "" : p.getCodigo());
            r.put("cantidad", d.getCantidad());
            r.put("descripcion", p == null ? ("Producto #" + d.getIdProducto()) : p.getNombre());
            r.put("precioUnitario", d.getPrecioUnitario());
            r.put("descuento", d.getDescuento());
            r.put("valorExenta", vExenta);
            r.put("valor5", v5);
            r.put("valor10", v10);
            filas.add(r);
        }

        double subtotal = totalExenta + total5 + total10;
        double iva5 = Math.floor(total5 - total5 / 1.05);
        double iva10 = Math.floor(total10 - total10 / 1.10);

        // Logo del emisor (el elegido en Ajustes; si no, el que viene con el sistema).
        byte[] logo = EmpresaMetodos.logoBytes(emp);

        Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, new java.util.Locale("es", "PY"));
        params.put("razonSocial", safe(emp.getRazonSocial()));
        params.put("logo", logo == null ? null : new java.io.ByteArrayInputStream(logo));
        params.put("direccionEmpresa", safe(emp.getDireccion()));
        params.put("ciudadTelefono", emp.getCiudadTelefono());
        params.put("actividades", safe(emp.getActividades()));
        params.put("timbrado", safe(emp.getTimbrado()));
        params.put("vigencia", emp.getTimbradoVigenciaDesde() == null ? "" : df.format(emp.getTimbradoVigenciaDesde()));
        // La factura imprime el RUC con el digito verificador separado (2403405 - 3).
        params.put("ruc", safe(emp.getRucCompleto()).replace("-", " - "));
        params.put("nroFactura", emp.nroFacturaFormateado(venta.getNroFactura()));
        params.put("fechaEmision", venta.getFechaVenta() == null ? "" : df.format(venta.getFechaVenta()));
        params.put("tipoTransaccion", venta.getTipoTransaccion() == null ? "Venta de mercadería" : venta.getTipoTransaccion());
        params.put("condicionMarcas", marcasCondicion(venta.getCondicionVenta()));
        params.put("rucCliente", cli == null ? "" : safe(cli.getCedula()));
        params.put("cliente", cli == null ? "Consumidor final" : (safe(cli.getNombre()) + " " + safe(cli.getApellido())).trim());
        params.put("direccionCliente", cli == null ? "" : safe(cli.getDireccion()));
        params.put("telefonoCliente", cli == null ? "" : safe(cli.getTelefono()));
        params.put("emailCliente", cli == null ? "" : safe(cli.getEmail()));
        params.put("totalDescuento", totalDescuento);
        params.put("totalExenta", totalExenta);
        params.put("total5", total5);
        params.put("total10", total10);
        params.put("totalOperacion", subtotal);
        params.put("iva5", iva5);
        params.put("iva10", iva10);
        params.put("totalIva", iva5 + iva10);
        params.put("cdc", safe(venta.getCdc()));

        return pdfComprobante("factura_electronica.jrxml", params, filas, "factura_venta_" + idVenta);
    }

    // ================== pantalla de reportes (acciones de la UI) ==================

    /**
     * Ejecuta la generacion del PDF, avisa el resultado y lo abre si salio bien.
     */
    private static void generar(java.util.concurrent.Callable<File> tarea, String titulo) {
        try {
            File pdf = tarea.call();
            javax.swing.JOptionPane.showMessageDialog(null,
                    titulo + " generado:\n" + pdf.getAbsolutePath());
            abrir(pdf);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No se pudo generar " + titulo.toLowerCase() + ":\n" + e.getMessage());
        }
    }

    /** Boton "Generar PDF" de la tarjeta Inventario. */
    public static void generarInventario() {
        generar(() -> inventario(), "Reporte de inventario");
    }

    /** Boton "Generar PDF" de la tarjeta Clientes. */
    public static void generarClientes() {
        generar(() -> clientes(), "Reporte de clientes");
    }

    /** Boton "Todas las ventas". */
    public static void generarVentasTodas() {
        generar(() -> ventas(null, null), "Reporte de ventas");
    }

    /**
     * Boton "Elegir rango de fechas...": pide las dos fechas y genera el reporte
     * de ventas de ese rango.
     */
    public static void generarVentasPorRango() {
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
        int r = javax.swing.JOptionPane.showConfirmDialog(null, p, "Rango de fechas de ventas",
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (r != javax.swing.JOptionPane.OK_OPTION) return;
        if (d1.getDate() == null || d2.getDate() == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Elija las dos fechas");
            return;
        }
        if (d1.getDate().after(d2.getDate())) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "La fecha \"Desde\" no puede ser mayor que \"Hasta\"");
            return;
        }
        java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd");
        generar(() -> ventas(iso.format(d1.getDate()), iso.format(d2.getDate())), "Reporte de ventas");
    }

    /**
     * Arma el texto de cada venta para el desplegable del elegidor. Si
     * conDocumento es true agrega el estado de la factura electronica.
     */
    private static String[] opcionesDeVentas(List<Venta> ventas, boolean conDocumento) {
        List<Cliente> clientes = new ClienteMetodos().listar();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String[] op = new String[ventas.size()];
        for (int i = 0; i < ventas.size(); i++) {
            Venta v = ventas.get(i);
            String doc = "";
            if (conDocumento) {
                doc = "  ·  " + ((v.getCdc() == null || v.getCdc().isEmpty())
                        ? "sin factura" : ("factura N° " + v.getNroFactura()));
            }
            op[i] = "#" + v.getIdVenta() + "  ·  " + (v.getFechaVenta() == null ? "" : sdf.format(v.getFechaVenta()))
                    + "  ·  " + nombreCliente(clientes, v.getIdCliente())
                    + "  ·  " + UiTheme.gs(v.getTotal()) + doc;
        }
        return op;
    }

    /** Boton "Elegir venta..." de la tarjeta Ticket: genera el ticket de esa venta. */
    public static void elegirTicketVenta() {
        List<Venta> ventas = new VentaMetodos().listar();
        if (ventas.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Todavía no hay ventas registradas");
            return;
        }
        String[] op = opcionesDeVentas(ventas, false);
        String sel = (String) javax.swing.JOptionPane.showInputDialog(null, "Elija la venta:", "Ticket de venta",
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, op, op[0]);
        if (sel == null) return;
        final int idx = java.util.Arrays.asList(op).indexOf(sel);
        if (idx < 0) return;
        generar(() -> ticketVenta(ventas.get(idx).getIdVenta()), "Ticket de venta");
    }

    /** Boton "Elegir orden...": genera el comprobante de la orden elegida. */
    public static void elegirComprobanteOrden() {
        List<OrdenServicio> ordenes = new OrdenServicioMetodos().listar();
        if (ordenes.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Todavía no hay órdenes de servicio registradas");
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
        String sel = (String) javax.swing.JOptionPane.showInputDialog(null, "Elija la orden de servicio:",
                "Comprobante de orden", javax.swing.JOptionPane.QUESTION_MESSAGE, null, op, op[0]);
        if (sel == null) return;
        final int idx = java.util.Arrays.asList(op).indexOf(sel);
        if (idx < 0) return;
        generar(() -> comprobanteOrden(ordenes.get(idx).getIdOrden()), "Comprobante de orden");
    }

    /** Boton "Abrir carpeta": abre la carpeta donde se guardan los PDF. */
    public static void abrirCarpetaReportes() {
        abrir(carpeta());
    }

    /**
     * Boton "Elegir venta..." de la tarjeta Factura: emite (o reimprime) la
     * factura electronica de la venta elegida.
     */
    public static void emitirFacturaElectronica() {
        List<Venta> ventas = new VentaMetodos().listar();
        if (ventas.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Todavía no hay ventas registradas");
            return;
        }
        String[] op = opcionesDeVentas(ventas, true);
        String sel = (String) javax.swing.JOptionPane.showInputDialog(null, "Elija la venta:", "Factura electrónica",
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, op, op[0]);
        if (sel == null) return;
        final int idx = java.util.Arrays.asList(op).indexOf(sel);
        if (idx < 0) return;
        int id = ventas.get(idx).getIdVenta();
        try {
            File pdf = facturaElectronica(id);
            Venta vf = new VentaMetodos().buscar(id);
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Factura electrónica emitida.\n"
                    + "N°: " + (vf == null ? "" : vf.getNroFactura()) + "\n"
                    + "CDC: " + (vf == null ? "" : vf.getCdc()) + "\n\n" + pdf.getAbsolutePath());
            abrir(pdf);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No se pudo emitir la factura:\n" + e.getMessage());
        }
    }
}
