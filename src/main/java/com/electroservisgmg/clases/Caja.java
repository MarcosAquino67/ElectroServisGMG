package com.electroservisgmg.clases;

import java.util.Date;

public class Caja {
    private int idCaja;
    private int idUsuario;
    private Date fechaApertura;
    private Date fechaCierre;
    private double montoInicial;
    private double montoFinal;
    private String estado;

    public Caja() {}

    public int getIdCaja() { return idCaja; }
    public void setIdCaja(int idCaja) { this.idCaja = idCaja; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public Date getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Date fechaApertura) { this.fechaApertura = fechaApertura; }
    public Date getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(Date fechaCierre) { this.fechaCierre = fechaCierre; }
    public double getMontoInicial() { return montoInicial; }
    public void setMontoInicial(double montoInicial) { this.montoInicial = montoInicial; }
    public double getMontoFinal() { return montoFinal; }
    public void setMontoFinal(double montoFinal) { this.montoFinal = montoFinal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
