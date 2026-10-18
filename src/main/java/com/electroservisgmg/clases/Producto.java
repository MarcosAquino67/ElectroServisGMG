package com.electroservisgmg.clases;

public class Producto {
    private int idProducto;
    private String codigo;
    /** Codigo de barras (EAN-13) del producto; puede ser null. */
    private String codigoBarras;
    private String nombre;
    private String descripcion;
    private int stock;
    /** Cantidad a partir de la cual se avisa que hay que reponer (0 = sin aviso). */
    private int stockMinimo;
    private double precioCompra;
    private double precioVenta;
    /** Tasa de IVA del producto en % (10, 5 o 0 = exenta). */
    private double iva = 10;
    private int idProveedor;
    private String foto;

    public Producto() {}

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    /** True si el stock llego al minimo (o mas abajo) y hay que reponer. */
    public boolean estaBajoMinimo() { return stockMinimo > 0 && stock <= stockMinimo; }
    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }
    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
}
