package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.OrdenRepuesto;
import com.electroservisgmg.conexion.Conexion;
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
