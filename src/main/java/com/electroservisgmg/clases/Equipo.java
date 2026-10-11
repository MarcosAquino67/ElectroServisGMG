package com.electroservisgmg.clases;

public class Equipo {
    private int idEquipo;
    private int idCliente;
    private String tipo;
    private String marca;
    private String modelo;
    private String nroSerie;
    private String fallaReportada;

    public Equipo() {}

    public Equipo(int idEquipo, int idCliente, String tipo, String marca, String modelo, String nroSerie, String fallaReportada) {
        this.idEquipo = idEquipo;
        this.idCliente = idCliente;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.nroSerie = nroSerie;
        this.fallaReportada = fallaReportada;
    }

    public int getIdEquipo() { return idEquipo; }
    public void setIdEquipo(int idEquipo) { this.idEquipo = idEquipo; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getNroSerie() { return nroSerie; }
    public void setNroSerie(String nroSerie) { this.nroSerie = nroSerie; }
    public String getFallaReportada() { return fallaReportada; }
    public void setFallaReportada(String fallaReportada) { this.fallaReportada = fallaReportada; }
}
