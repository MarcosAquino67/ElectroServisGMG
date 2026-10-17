package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Empresa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Genera y descompone el Codigo de Control (CDC) de una Factura Electronica.
 *
 * El CDC tiene 44 digitos y se compone asi:
 * <pre>
 *   2   tipo de documento      (01 = Factura electronica)
 *   8   RUC del emisor         (sin DV, rellenado con ceros a la izquierda)
 *   1   DV del RUC
 *   3   establecimiento
 *   3   punto de expedicion
 *   7   numero del documento
 *   1   tipo de contribuyente
 *   8   fecha de emision       (AAAAMMDD)
 *   1   tipo de emision        (1 = normal)
 *   9   codigo de seguridad    (aleatorio)
 *   1   DV del CDC
 * </pre>
 *
 * El DV del CDC se calcula con modulo 11 y pesos 2..7 aplicados de derecha a
 * izquierda; el del RUC con pesos 2..9. En ambos, si el resultado es 10 u 11 el
 * digito verificador es 0.
 */
public class CdcUtil {

    /** Tipo de documento "01": Factura electronica. */
    public static final String TIPO_FACTURA_ELECTRONICA = "01";

    private static final int[] PESOS_CDC = {2, 3, 4, 5, 6, 7};
    private static final int[] PESOS_RUC = {2, 3, 4, 5, 6, 7, 8, 9};

    private CdcUtil() { }

    /** Arma el CDC completo (44 digitos) con un codigo de seguridad aleatorio. */
    public static String generar(Empresa e, int nroFactura, Date fecha) {
        return armar(e, nroFactura, fecha, codigoSeguridad());
    }

    /** Arma el CDC indicando el codigo de seguridad (permite reproducir uno ya emitido). */
    public static String armar(Empresa e, int nroFactura, Date fecha, String codSeguridad) {
        StringBuilder sb = new StringBuilder(43);
        sb.append(TIPO_FACTURA_ELECTRONICA);
        sb.append(e.ruc8());
        sb.append(digito(e.getDvRuc()));
        sb.append(ceros(e.getEstablecimiento(), 3));
        sb.append(ceros(e.getPuntoExpedicion(), 3));
        sb.append(String.format("%07d", nroFactura));
        sb.append(digito(e.getTipoContribuyente()));
        sb.append(new SimpleDateFormat("yyyyMMdd").format(fecha == null ? new Date() : fecha));
        sb.append("1"); // tipo de emision: 1 = normal
        sb.append(ceros(codSeguridad, 9));
        String base = sb.toString();
        return base + digitoVerificador(base, PESOS_CDC);
    }

    /** Digito verificador modulo 11; los pesos se aplican de derecha a izquierda. */
    public static int digitoVerificador(String base, int[] pesos) {
        int suma = 0;
        for (int i = 0; i < base.length(); i++) {
            char ch = base.charAt(base.length() - 1 - i);
            int d = (ch >= '0' && ch <= '9') ? ch - '0' : 0;
            suma += d * pesos[i % pesos.length];
        }
        int dv = 11 - (suma % 11);
        return dv >= 10 ? 0 : dv;
    }

    /** DV de un RUC (pesos 2..9). */
    public static int dvRuc(String ruc) {
        return digitoVerificador((ruc == null ? "" : ruc).replaceAll("[^0-9]", ""), PESOS_RUC);
    }

    /** Codigo de seguridad de 9 digitos (puede empezar con 0). */
    public static String codigoSeguridad() {
        return String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
    }

    /** Desglose legible de un CDC (se usa para verificar en pantalla). */
    public static String descomponer(String cdc) {
        if (cdc == null || cdc.length() != 44) return "CDC invalido";
        return "Tipo documento: " + cdc.substring(0, 2)
                + "\nRUC: " + cdc.substring(2, 10) + "-" + cdc.substring(10, 11)
                + "\nEstablecimiento: " + cdc.substring(11, 14)
                + "\nPunto de expedicion: " + cdc.substring(14, 17)
                + "\nNumero: " + cdc.substring(17, 24)
                + "\nTipo de contribuyente: " + cdc.substring(24, 25)
                + "\nFecha de emision: " + cdc.substring(25, 33)
                + "\nTipo de emision: " + cdc.substring(33, 34)
                + "\nCodigo de seguridad: " + cdc.substring(34, 43)
                + "\nDV: " + cdc.substring(43, 44)
                + "\nDV " + (verificar(cdc) ? "correcto" : "INCORRECTO");
    }

    /** Comprueba el DV de un CDC ya emitido. */
    public static boolean verificar(String cdc) {
        if (cdc == null || cdc.length() != 44) return false;
        int dv = digitoVerificador(cdc.substring(0, 43), PESOS_CDC);
        return cdc.charAt(43) == (char) ('0' + dv);
    }

    private static String ceros(String valor, int largo) {
        String v = (valor == null ? "" : valor).replaceAll("[^0-9]", "");
        if (v.length() > largo) v = v.substring(v.length() - largo);
        return String.format("%" + largo + "s", v).replace(' ', '0');
    }

    private static String digito(String valor) {
        char c = (valor == null || valor.trim().isEmpty()) ? '0' : valor.trim().charAt(valor.trim().length() - 1);
        return (c >= '0' && c <= '9') ? String.valueOf(c) : "0";
    }
}
