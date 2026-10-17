package com.electroservisgmg.clases;

public class TipoEquipo {
    private int idTipo;
    private String nombre;

    public TipoEquipo() {}

    public int getIdTipo() { return idTipo; }
    public void setIdTipo(int idTipo) { this.idTipo = idTipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override public String toString() { return nombre; }
}
