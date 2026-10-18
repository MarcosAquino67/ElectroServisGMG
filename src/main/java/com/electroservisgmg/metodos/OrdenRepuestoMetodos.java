package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.OrdenRepuesto;
import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.conexion.Conexion;
import com.electroservisgmg.formularios.UiTheme;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repuestos usados en las ordenes de servicio.
 *
 * Cuando se agrega un repuesto se descuenta del stock y su precio se suma al
 * costo de la orden; cuando se quita, vuelve al stock y se descuenta del costo.
 * Todo se hace en una sola transaccion para que nunca quede a medias.
 *
 * @author Aurelio
 */
public class OrdenRepuestoMetodos {

    /** Ultimo problema que hubo (para mostrar en pantalla). */
    public static String ultimoError = "";

    /** Repuestos de una orden, con el nombre del producto. */
    public List<OrdenRepuesto> listar(int idOrden) {
        List<OrdenRepuesto> lista = new ArrayList<>();
        String sql = "SELECT r.*, p.nombre FROM orden_repuesto r "
                + "JOIN producto p ON p.idProducto = r.idProducto "
                + "WHERE r.idOrdenServicio=? ORDER BY r.idRepuesto";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(armar(rs, true));
            }
        } catch (SQLException e) { System.out.println("listar repuestos: " + e.getMessage()); }
        return lista;
    }

    /** Suma de los repuestos de una orden. */
    public double totalRepuestos(int idOrden) {
        String sql = "SELECT COALESCE(SUM(cantidad * precioUnitario), 0) FROM orden_repuesto WHERE idOrdenServicio=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { System.out.println("total repuestos: " + e.getMessage()); }
        return 0;
    }

    /**
     * Agrega un repuesto a la orden: controla que haya stock, lo descuenta,
     * guarda la linea y le suma el importe al costo de la reparacion.
     *
     * @return true si se pudo; si no, {@link #ultimoError} explica por que
     */
    public boolean agregar(int idOrden, int idProducto, int cantidad) {
        ultimoError = "";
        if (cantidad <= 0) { ultimoError = "La cantidad tiene que ser mayor a cero"; return false; }
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);

            double precio;
            int stock;
            String nombre;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT nombre, precioVenta, stock FROM producto WHERE idProducto=?")) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); ultimoError = "El producto no existe"; return false; }
                    nombre = rs.getString("nombre");
                    precio = rs.getDouble("precioVenta");
                    stock = rs.getInt("stock");
                }
            }
            if (stock < cantidad) {
                con.rollback();
                ultimoError = "De \"" + nombre + "\" solo quedan " + stock
                        + (stock == 1 ? " unidad" : " unidades");
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE producto SET stock = stock - ? WHERE idProducto=? AND stock >= ?")) {
                ps.setInt(1, cantidad);
                ps.setInt(2, idProducto);
                ps.setInt(3, cantidad);
                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    ultimoError = "No se pudo descontar el stock de \"" + nombre + "\"";
                    return false;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO orden_repuesto(idOrdenServicio, idProducto, cantidad, precioUnitario, fecha) "
                    + "VALUES(?,?,?,?,NOW())")) {
                ps.setInt(1, idOrden);
                ps.setInt(2, idProducto);
                ps.setInt(3, cantidad);
                ps.setDouble(4, precio);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ordenservicio SET costoReparacion = costoReparacion + ? WHERE idOrden=?")) {
                ps.setDouble(1, Math.round(precio * cantidad));
                ps.setInt(2, idOrden);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            ultimoError = "No se pudo agregar el repuesto: " + e.getMessage();
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException ignored) {}
        }
    }

    /**
     * Quita un repuesto: lo devuelve al stock, borra la linea y le resta el
     * importe al costo de la orden.
     */
    public boolean quitar(int idRepuesto) {
        ultimoError = "";
        Connection con = null;
        try {
            con = Conexion.nuevaConexion();
            con.setAutoCommit(false);

            int idOrden, idProducto, cantidad;
            double precio;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT idOrdenServicio, idProducto, cantidad, precioUnitario FROM orden_repuesto WHERE idRepuesto=?")) {
                ps.setInt(1, idRepuesto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); ultimoError = "Ese repuesto ya no está en la orden"; return false; }
                    idOrden = rs.getInt("idOrdenServicio");
                    idProducto = rs.getInt("idProducto");
                    cantidad = rs.getInt("cantidad");
                    precio = rs.getDouble("precioUnitario");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE producto SET stock = stock + ? WHERE idProducto=?")) {
                ps.setInt(1, cantidad);
                ps.setInt(2, idProducto);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM orden_repuesto WHERE idRepuesto=?")) {
                ps.setInt(1, idRepuesto);
                if (ps.executeUpdate() == 0) { con.rollback(); ultimoError = "No se pudo quitar el repuesto"; return false; }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ordenservicio SET costoReparacion = GREATEST(0, costoReparacion - ?) WHERE idOrden=?")) {
                ps.setDouble(1, Math.round(precio * cantidad));
                ps.setInt(2, idOrden);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            ultimoError = "No se pudo quitar el repuesto: " + e.getMessage();
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException ignored) {}
        }
    }

    /**
     * Llena la tabla con los repuestos de la orden y actualiza la etiqueta del
     * total.
     */
    public void mostrar(javax.swing.JTable tabla, javax.swing.JLabel lblTotal, int idOrden) {
        List<OrdenRepuesto> lista = listar(idOrden);
        String[] cols = {"Producto", "Cantidad", "Precio", "Subtotal"};
        Object[][] data = new Object[lista.size()][4];
        double total = 0;
        for (int i = 0; i < lista.size(); i++) {
            OrdenRepuesto r = lista.get(i);
            total += r.getSubtotal();
            data[i] = new Object[]{r.getNombreProducto(), r.getCantidad(),
                UiTheme.gs(r.getPrecioUnitario()), UiTheme.gs(r.getSubtotal())};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        lblTotal.setText(lista.isEmpty() ? "Sin repuestos cargados"
                : "Total de repuestos:  " + UiTheme.gs(total));
    }

    /** Llena el combo con los productos que se pueden usar como repuesto. */
    public void cargarProductos(javax.swing.JComboBox<String> cboProducto) {
        cboProducto.removeAllItems();
        for (Producto p : new ProductoMetodos().listar()) {
            cboProducto.addItem(p.getNombre() + "   (stock " + p.getStock() + ")");
        }
        if (cboProducto.getItemCount() == 0) cboProducto.addItem("(no hay productos cargados)");
    }

    /**
     * Lee el producto y la cantidad elegidos en el formulario, lo agrega a la
     * orden (descuenta el stock y suma al costo), deja pintada la tabla y avisa
     * por la etiqueta de estado. Devuelve true si agrego.
     */
    public boolean agregar(javax.swing.JComboBox<String> cboProducto, javax.swing.JTextField txtCantidad,
                           javax.swing.JTable tabla, javax.swing.JLabel lblTotal,
                           javax.swing.JLabel lblEstado, int idOrden) {
        List<Producto> productos = new ProductoMetodos().listar();
        int i = cboProducto.getSelectedIndex();
        if (productos.isEmpty() || i < 0 || i >= productos.size()) {
            estado(lblEstado, "Primero cargá productos en el inventario", UiTheme.DANGER);
            return false;
        }
        int cantidad;
        try {
            cantidad = UiTheme.parseGsInt(txtCantidad.getText().trim());
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            estado(lblEstado, "La cantidad tiene que ser un número mayor a cero", UiTheme.DANGER);
            return false;
        }
        Producto p = productos.get(i);
        if (!agregar(idOrden, p.getIdProducto(), cantidad)) {
            estado(lblEstado, ultimoError, UiTheme.DANGER);
            javax.swing.JOptionPane.showMessageDialog(null, ultimoError);
            return false;
        }
        estado(lblEstado, "Se agregó " + cantidad + " x " + p.getNombre() + " y se descontó del stock.",
                UiTheme.SUCCESS);
        txtCantidad.setText("1");
        mostrar(tabla, lblTotal, idOrden);
        cargarProductos(cboProducto);
        return true;
    }

    /**
     * Quita el repuesto elegido en la tabla (vuelve al stock y se descuenta del
     * costo de la orden) y deja todo pintado. Devuelve true si quito.
     */
    public boolean quitar(javax.swing.JComboBox<String> cboProducto, javax.swing.JTable tabla,
                          javax.swing.JLabel lblTotal, javax.swing.JLabel lblEstado, int idOrden) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            estado(lblEstado, "Elegí en la lista el repuesto que querés quitar", UiTheme.DANGER);
            return false;
        }
        List<OrdenRepuesto> lista = listar(idOrden);
        if (fila >= lista.size()) return false;
        if (!quitar(lista.get(fila).getIdRepuesto())) {
            estado(lblEstado, ultimoError, UiTheme.DANGER);
            return false;
        }
        estado(lblEstado, "Repuesto quitado: volvió al stock y se descontó del costo de la orden.",
                UiTheme.SUCCESS);
        mostrar(tabla, lblTotal, idOrden);
        cargarProductos(cboProducto);
        return true;
    }

    /**
     * Pide los datos de un producto nuevo, lo carga en el inventario y lo deja
     * elegido en el combo. Devuelve true si lo cargo.
     */
    public boolean nuevoProducto(javax.swing.JComboBox<String> cboProducto, javax.swing.JLabel lblEstado) {
        javax.swing.JTextField nombre = new javax.swing.JTextField();
        javax.swing.JTextField precio = new javax.swing.JTextField();
        javax.swing.JTextField cantidad = new javax.swing.JTextField("1");
        Object[] mensaje = {
            "Nombre del repuesto:", nombre,
            "Precio de venta (Gs.):", precio,
            "Cantidad que entra al stock:", cantidad,
            "", "Se carga como producto nuevo en el inventario."
        };
        int ok = javax.swing.JOptionPane.showConfirmDialog(null, mensaje, "Nuevo repuesto",
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (ok != javax.swing.JOptionPane.OK_OPTION) return false;
        if (nombre.getText().trim().isEmpty()) {
            estado(lblEstado, "El nombre del repuesto no puede quedar vacío", UiTheme.DANGER);
            return false;
        }
        double pv;
        int cantStock;
        try {
            pv = UiTheme.parseGs(precio.getText().trim());
            cantStock = UiTheme.parseGsInt(cantidad.getText().trim());
            if (pv < 0 || cantStock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            estado(lblEstado, "Precio y cantidad tienen que ser números válidos", UiTheme.DANGER);
            return false;
        }
        Producto nuevo = new Producto();
        nuevo.setNombre(nombre.getText().trim());
        nuevo.setPrecioVenta(pv);
        nuevo.setPrecioCompra(Math.round(pv * 0.7));
        nuevo.setStock(cantStock);
        nuevo.setIva(10);
        if (!new ProductoMetodos().insertar(nuevo)) {
            estado(lblEstado, "No se pudo cargar el producto: " + ProductoMetodos.ultimoError, UiTheme.DANGER);
            return false;
        }
        cargarProductos(cboProducto);
        cboProducto.setSelectedIndex(Math.max(0, cboProducto.getItemCount() - 1));
        estado(lblEstado, "Producto cargado. Ahora poné la cantidad y apretá Agregar.", UiTheme.SUCCESS);
        return true;
    }

    /** Pinta el aviso de estado de la ventana. */
    private static void estado(javax.swing.JLabel lblEstado, String texto, java.awt.Color color) {
        lblEstado.setText("<html>" + texto.replace("\n", "<br>") + "</html>");
        lblEstado.setForeground(color);
    }

    private static OrdenRepuesto armar(ResultSet rs, boolean conNombre) throws SQLException {
        OrdenRepuesto r = new OrdenRepuesto();
        r.setIdRepuesto(rs.getInt("idRepuesto"));
        r.setIdOrdenServicio(rs.getInt("idOrdenServicio"));
        r.setIdProducto(rs.getInt("idProducto"));
        r.setCantidad(rs.getInt("cantidad"));
        r.setPrecioUnitario(rs.getDouble("precioUnitario"));
        if (conNombre) r.setNombreProducto(rs.getString("nombre"));
        return r;
    }
}
