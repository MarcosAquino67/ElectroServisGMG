package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.conexion.Conexion;
import com.electroservisgmg.formularios.UiTheme;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoMetodos {
    /** Ultimo error de BD (para mostrarlo en pantalla en vez de "Error" generico). */
    public static String ultimoError = "";

    /**
     * Lee los campos del formulario, valida, decide si es alta o modificacion,
     * guarda y muestra el mensaje. Devuelve true si guardo, asi el formulario
     * sabe si refrescar la tabla.
     */
    public boolean guardar(javax.swing.JTextField txtId, javax.swing.JTextField txtCodigo,
                           javax.swing.JTextField txtCodigoBarras, javax.swing.JTextField txtNombre,
                           javax.swing.JTextField txtStock, javax.swing.JTextField txtStockMinimo,
                           javax.swing.JTextField txtPrecioCompra, javax.swing.JTextField txtPrecioVenta,
                           javax.swing.JTextField txtDescripcion, javax.swing.JComboBox<String> cboIva,
                           javax.swing.JComboBox<String> cboProveedor, String foto) {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "El nombre es obligatorio");
            return false;
        }
        int stock, stockMinimo;
        double pc, pv;
        try {
            stock = UiTheme.parseGsInt(txtStock.getText().trim());
            stockMinimo = UiTheme.parseGsInt(txtStockMinimo.getText().trim());
            pc = UiTheme.parseGs(txtPrecioCompra.getText().trim());
            pv = UiTheme.parseGs(txtPrecioVenta.getText().trim());
            if (stock < 0 || stockMinimo < 0 || pc < 0 || pv < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Stock, stock mínimo y precios deben ser números válidos (≥ 0)");
            return false;
        }
        String codigoBarras = txtCodigoBarras.getText().trim();
        int idActual = 0;
        try { idActual = Integer.parseInt(txtId.getText().trim()); } catch (NumberFormatException ignored) {}
        if (!codigoBarras.isEmpty()) {
            Producto otro = buscarPorCodigoBarras(codigoBarras);
            if (otro != null && otro.getIdProducto() != idActual) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Ese código de barras ya lo tiene el producto \"" + otro.getNombre() + "\".");
                return false;
            }
        }
        Producto p = new Producto();
        String codigo = txtCodigo.getText().trim();
        p.setCodigo(codigo.isEmpty() ? null : codigo);
        p.setCodigoBarras(codigoBarras.isEmpty() ? null : codigoBarras);
        p.setNombre(nombre);
        p.setDescripcion(txtDescripcion.getText().trim());
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setPrecioCompra(pc);
        p.setPrecioVenta(pv);
        p.setIva(ivaDelCombo(cboIva));
        p.setIdProveedor(idProveedorDelCombo(cboProveedor));
        p.setFoto(foto == null || foto.trim().isEmpty() ? null : foto.trim());
        boolean ok;
        if (idActual > 0) {
            p.setIdProducto(idActual);
            ok = modificar(p);
        } else {
            ok = insertar(p);
        }
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Guardado correctamente"
                : "Error al guardar" + (ultimoError.isEmpty() ? "" : ":\n" + ultimoError));
        return ok;
    }

    /** Tasa de IVA elegida en el combo (10, 5 o 0). */
    private static double ivaDelCombo(javax.swing.JComboBox<String> cboIva) {
        int i = cboIva.getSelectedIndex();
        if (i == 1) return 5;
        if (i == 2) return 0;
        return 10;
    }

    /** Id del proveedor elegido en el combo ("(sin proveedor)" = 0). */
    private static int idProveedorDelCombo(javax.swing.JComboBox<String> cboProveedor) {
        if (cboProveedor.getSelectedIndex() <= 0) return 0;
        Object sel = cboProveedor.getSelectedItem();
        if (sel == null) return 0;
        String s = sel.toString();
        int guion = s.indexOf(" -");
        if (guion <= 0) return 0;
        try { return Integer.parseInt(s.substring(0, guion).trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    public boolean insertar(Producto p) {
        ultimoError = "";
        String sql = "INSERT INTO producto(codigo, codigoBarras, nombre, descripcion, stock, precioCompra, precioVenta, iva, idProveedor, foto, stockMinimo)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            textoONull(ps, 2, p.getCodigoBarras());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getDescripcion());
            ps.setInt(5, p.getStock());
            ps.setDouble(6, p.getPrecioCompra());
            ps.setDouble(7, p.getPrecioVenta());
            ps.setDouble(8, p.getIva());
            if (p.getIdProveedor() > 0) ps.setInt(9, p.getIdProveedor());
            else ps.setNull(9, Types.INTEGER);
            textoONull(ps, 10, p.getFoto());
            ps.setInt(11, p.getStockMinimo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { ultimoError = e.getMessage(); System.out.println("insertar producto: " + e.getMessage()); return false; }
    }

    public boolean modificar(Producto p) {
        ultimoError = "";
        String sql = "UPDATE producto SET codigo=?, codigoBarras=?, nombre=?, descripcion=?, stock=?, precioCompra=?, precioVenta=?, iva=?, idProveedor=?, foto=?, stockMinimo=? WHERE idProducto=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            textoONull(ps, 2, p.getCodigoBarras());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getDescripcion());
            ps.setInt(5, p.getStock());
            ps.setDouble(6, p.getPrecioCompra());
            ps.setDouble(7, p.getPrecioVenta());
            ps.setDouble(8, p.getIva());
            if (p.getIdProveedor() > 0) ps.setInt(9, p.getIdProveedor());
            else ps.setNull(9, Types.INTEGER);
            textoONull(ps, 10, p.getFoto());
            ps.setInt(11, p.getStockMinimo());
            ps.setInt(12, p.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { ultimoError = e.getMessage(); System.out.println("modificar producto: " + e.getMessage()); return false; }
    }

    /**
     * Elimina el producto seleccionado (el id viene del campo txtId) y muestra
     * el mensaje. Devuelve true si elimino.
     */
    public boolean eliminar(javax.swing.JTextField txtId) {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un producto de la tabla");
            return false;
        }
        ultimoError = "";
        boolean ok = eliminarFila(id);
        javax.swing.JOptionPane.showMessageDialog(null, ok ? "Eliminado"
                : "No se puede eliminar: tiene ventas o compras asociadas");
        return ok;
    }

    private boolean eliminarFila(int id) {
        String sql = "DELETE FROM producto WHERE idProducto=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { ultimoError = e.getMessage(); System.out.println(e.getMessage()); return false; }
    }

    /**
     * Llena la tabla con los productos, opcionalmente filtrados por nombre,
     * codigo, codigo de barras, descripcion o proveedor. Los que llegaron al
     * stock minimo se muestran en rojo.
     */
    public void mostrar(javax.swing.JTable tabla, String filtro) {
        java.util.Map<Integer, String> prov = new java.util.HashMap<>();
        for (com.electroservisgmg.clases.Proveedor x : new ProveedorMetodos().listar()) {
            prov.put(x.getIdProveedor(), x.getNombre());
        }
        String f = filtro == null ? "" : filtro.toLowerCase();
        List<Producto> fil = new ArrayList<>();
        for (Producto p : listar()) {
            String nomProv = prov.getOrDefault(p.getIdProveedor(), "");
            if (f.isEmpty() || p.getNombre().toLowerCase().contains(f)
                    || (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(f))
                    || (p.getCodigoBarras() != null && p.getCodigoBarras().toLowerCase().contains(f))
                    || (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(f))
                    || nomProv.toLowerCase().contains(f)) fil.add(p);
        }
        String[] cols = {"ID", "Código", "Cód. barras", "Nombre", "Stock", "Mín.", "Precio Compra", "Precio Venta", "IVA", "Proveedor", "Descripción"};
        Object[][] data = new Object[fil.size()][11];
        for (int i = 0; i < fil.size(); i++) {
            Producto p = fil.get(i);
            data[i] = new Object[]{p.getIdProducto(), p.getCodigo() == null ? "" : p.getCodigo(),
                p.getCodigoBarras() == null ? "" : p.getCodigoBarras(), p.getNombre(), p.getStock(),
                p.getStockMinimo() > 0 ? p.getStockMinimo() : "",
                UiTheme.gs(p.getPrecioCompra()), UiTheme.gs(p.getPrecioVenta()), textoIva(p.getIva()),
                p.getIdProveedor() > 0 ? prov.getOrDefault(p.getIdProveedor(), "") : "-",
                p.getDescripcion() == null ? "" : p.getDescripcion()};
        }
        tabla.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        // los productos que llegaron al minimo se ven en rojo
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    boolean bajo = false;
                    try {
                        Object st = t.getValueAt(row, 4), mn = t.getValueAt(row, 5);
                        bajo = mn instanceof Number && ((Number) mn).intValue() > 0
                                && st instanceof Number && ((Number) st).intValue() <= ((Number) mn).intValue();
                    } catch (RuntimeException ignored) {}
                    c.setForeground(bajo ? UiTheme.DANGER : t.getForeground());
                }
                return c;
            }
        });
    }

    /** Texto de la tasa de IVA para la tabla. */
    private static String textoIva(double iva) {
        if (iva >= 9.99) return "10%";
        if (iva >= 4.99) return "5%";
        return "Exenta";
    }

    public boolean descontarStock(int idProducto, int cantidad) {
        String sql = "UPDATE producto SET stock = stock - ? WHERE idProducto=? AND stock >= ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); return false; }
    }

    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(armar(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return lista;
    }

    /**
     * Productos cuyo stock ya llego al minimo cargado (o mas abajo).
     * Los que tienen el minimo en 0 no entran: quiere decir "no avisar".
     */
    public List<Producto> listarBajoMinimo() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE stockMinimo > 0 AND stock <= stockMinimo "
                + "ORDER BY stock - stockMinimo ASC";
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(armar(rs));
        } catch (SQLException e) { System.out.println("listarBajoMinimo: " + e.getMessage()); }
        return lista;
    }

    // ---- busqueda por codigo ----

    /**
     * Busca un producto por codigo de barras y, si no aparece, por el codigo
     * interno. Es lo que usa el lector al escanear.
     */
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;
        Producto p = buscarUno("codigoBarras", codigo.trim());
        if (p == null) p = buscarUno("codigo", codigo.trim());
        return p;
    }

    /** Busca por codigo de barras exacto. */
    public Producto buscarPorCodigoBarras(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;
        return buscarUno("codigoBarras", codigo.trim());
    }

    private Producto buscarUno(String columna, String valor) {
        String sql = "SELECT * FROM producto WHERE " + columna + " = ? LIMIT 1";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return armar(rs);
            }
        } catch (SQLException e) { System.out.println("buscar producto: " + e.getMessage()); }
        return null;
    }

    /**
     * Devuelve un EAN-13 libre para ese producto: parte del id y, si ese ya
     * esta usado, prueba con el siguiente. Evita chocar con el indice unico.
     */
    public String generarCodigoBarrasLibre(int idProducto) {
        for (int i = 0; i < 50; i++) {
            String candidato = CodigoBarras.generarEan13((long) idProducto + i * 1000003L);
            if (buscarPorCodigoBarras(candidato) == null) return candidato;
        }
        return CodigoBarras.generarEan13(System.currentTimeMillis());
    }

    // ---- auxiliares ----

    private static Producto armar(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("idProducto"));
        try { p.setCodigo(rs.getString("codigo")); } catch (SQLException ignored) {}
        try { p.setCodigoBarras(rs.getString("codigoBarras")); } catch (SQLException ignored) {}
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setStock(rs.getInt("stock"));
        try { p.setStockMinimo(rs.getInt("stockMinimo")); } catch (SQLException ignored) {}
        p.setPrecioCompra(rs.getDouble("precioCompra"));
        p.setPrecioVenta(rs.getDouble("precioVenta"));
        try { p.setIva(rs.getDouble("iva")); } catch (SQLException ignored) {}
        p.setIdProveedor(rs.getInt("idProveedor"));
        try { p.setFoto(rs.getString("foto")); } catch (SQLException ignored) {}
        return p;
    }

    /** Los campos de texto vacios se guardan como NULL (el codigo de barras es unico). */
    private static void textoONull(PreparedStatement ps, int pos, String valor) throws SQLException {
        if (valor == null || valor.trim().isEmpty()) ps.setNull(pos, Types.VARCHAR);
        else ps.setString(pos, valor.trim());
    }
}
