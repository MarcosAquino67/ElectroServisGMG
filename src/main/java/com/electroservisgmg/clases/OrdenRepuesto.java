package com.electroservisgmg.clases;

/**
 * Un repuesto (que en realidad es un producto del stock) usado en una
 * reparacion. El precio queda guardado como estaba el dia que se uso.
 *
 * @author Aurelio
 */
public class OrdenRepuesto {

    private int idRepuesto;
    private int idOrdenServicio;
    private int idProducto;
    private int cantidad;
    private double precioUnitario;
    /** Nombre del producto; solo para mostrar en pantalla. */
    private String nombreProducto;

    public OrdenRepuesto() {}

    public int getIdRepuesto() { return idRepuesto; }
    public void setIdRepuesto(int idRepuesto) { this.idRepuesto = idRepuesto; }

    public int getIdOrdenServicio() { return idOrdenServicio; }
    public void setIdOrdenServicio(int idOrdenServicio) { this.idOrdenServicio = idOrdenServicio; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    /** Importe de esta linea. */
    public double getSubtotal() { return cantidad * precioUnitario; }
}
