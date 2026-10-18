package com.electroservisgmg.clases;

import java.util.Date;

public class Venta {
    public static final String TICKET = "TICKET";
    public static final String FACTURA = "FACTURA";

    private int idVenta;
    private int idCliente;
    private int idUsuario;
    private Date fechaVenta;
    private double total;
    private String formaPago;
    private String condicionVenta;
    private String tipoTransaccion;
    private String tipoDocumento = TICKET;
    private int nroFactura;
    /** Codigo de Control (44 digitos) de la Factura Electronica; null si solo se emitio ticket. */
    private String cdc;
    /** Estado de la venta: REGISTRADA o ANULADA. Una venta anulada no se borra (para no repetir el nro de factura). */
    private String estado = REGISTRADA;

    public Venta() {}

    public static final String REGISTRADA = "REGISTRADA";
    public static final String ANULADA = "ANULADA";

    public String getEstado() { return estado == null || estado.isEmpty() ? REGISTRADA : estado; }
    public void setEstado(String estado) { this.estado = estado; }
    /** True si la venta fue anulada (se le devolvio el stock al producto). */
    public boolean estaAnulada() { return ANULADA.equalsIgnoreCase(getEstado()); }

    public String getCondicionVenta() { return condicionVenta; }
    public void setCondicionVenta(String condicionVenta) { this.condicionVenta = condicionVenta; }
    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public int getNroFactura() { return nroFactura; }
    public void setNroFactura(int nroFactura) { this.nroFactura = nroFactura; }
    public String getCdc() { return cdc; }
    public void setCdc(String cdc) { this.cdc = cdc; }
    public boolean esFactura() { return cdc != null && !cdc.isEmpty(); }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public Date getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(Date fechaVenta) { this.fechaVenta = fechaVenta; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
}
