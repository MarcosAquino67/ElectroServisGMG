package com.electroservisgmg.clases;

import java.util.Date;

/**
 * Datos del emisor (una sola fila de la tabla empresa) que se imprimen en la
 * Factura Electronica: razon social, RUC, timbrado y numeracion.
 */
public class Empresa {

    private int idEmpresa = 1;
    private String razonSocial = "";
    private String nombreComercial = "";
    private String ruc = "";
    private String dvRuc = "";
    private String direccion = "";
    private String ciudad = "";
    private String telefono = "";
    private String email = "";
    private String actividades = "";
    private String timbrado = "";
    private Date timbradoVigenciaDesde;
    private String establecimiento = "001";
    private String puntoExpedicion = "001";
    private int nroFacturaInicio = 1;
    private int nroFacturaFin = 9999999;
    private String tipoContribuyente = "2";
    private String logo;

    public Empresa() { }

    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getDvRuc() { return dvRuc; }
    public void setDvRuc(String dvRuc) { this.dvRuc = dvRuc; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getActividades() { return actividades; }
    public void setActividades(String actividades) { this.actividades = actividades; }
    public String getTimbrado() { return timbrado; }
    public void setTimbrado(String timbrado) { this.timbrado = timbrado; }
    public Date getTimbradoVigenciaDesde() { return timbradoVigenciaDesde; }
    public void setTimbradoVigenciaDesde(Date timbradoVigenciaDesde) { this.timbradoVigenciaDesde = timbradoVigenciaDesde; }
    public String getEstablecimiento() { return establecimiento; }
    public void setEstablecimiento(String establecimiento) { this.establecimiento = establecimiento; }
    public String getPuntoExpedicion() { return puntoExpedicion; }
    public void setPuntoExpedicion(String puntoExpedicion) { this.puntoExpedicion = puntoExpedicion; }
    public int getNroFacturaInicio() { return nroFacturaInicio; }
    public void setNroFacturaInicio(int nroFacturaInicio) { this.nroFacturaInicio = nroFacturaInicio; }
    public int getNroFacturaFin() { return nroFacturaFin; }
    public void setNroFacturaFin(int nroFacturaFin) { this.nroFacturaFin = nroFacturaFin; }
    public String getTipoContribuyente() { return tipoContribuyente; }
    public void setTipoContribuyente(String tipoContribuyente) { this.tipoContribuyente = tipoContribuyente; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    /** RUC con digito verificador, tal como se imprime (ej: 2403405-3). */
    public String getRucCompleto() {
        String r = ruc == null ? "" : ruc.trim();
        String d = dvRuc == null ? "" : dvRuc.trim();
        return d.isEmpty() ? r : r + "-" + d;
    }

    /** Numero de factura con el formato establecimiento-punto-expedicion-numero. */
    public String nroFacturaFormateado(int nro) {
        return establecimiento + "-" + puntoExpedicion + "-" + String.format("%07d", nro);
    }

    /** RUC de 8 digitos, tal como lo usa el CDC. */    public String ruc8() {
        String soloDigitos = (ruc == null ? "" : ruc).replaceAll("[^0-9]", "");
        try {
            return String.format("%08d", Long.parseLong(soloDigitos));
        } catch (NumberFormatException e) {
            return "00000000";
        }
    }

    /** Las actividades economicas, una por linea. */
    public String[] actividadesLineas() {
        if (actividades == null || actividades.trim().isEmpty()) return new String[0];
        return actividades.split("\\r?\\n");
    }

    /**
     * Ciudad y telefono en una sola linea, tal como se imprime en la factura
     * (ej: CAAGUAZU - TELÉF. (0971)452427).
     */
    public String getCiudadTelefono() {
        String c = ciudad == null ? "" : ciudad.trim();
        String t = telefono == null ? "" : telefono.trim();
        if (t.isEmpty()) return c;
        return (c.isEmpty() ? "" : c + " - ") + "TELÉF. " + t;
    }

    /** Nombre que se usa arriba del ticket (si no se cargo, la razon social). */
    public String getNombreParaTicket() {
        String n = nombreComercial == null ? "" : nombreComercial.trim();
        return n.isEmpty() ? (razonSocial == null ? "" : razonSocial) : n;
    }
}
