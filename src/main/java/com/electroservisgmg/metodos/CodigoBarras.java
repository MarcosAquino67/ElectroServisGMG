package com.electroservisgmg.metodos;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Utilidades de codigo de barras EAN-13: digito verificador, validacion,
 * generacion de codigos internos y dibujo del simbolo (para la vista previa
 * en pantalla y para las etiquetas impresas).
 *
 * Se dibuja a mano, sin librerias externas, asi funciona aunque la maquina
 * no tenga internet.
 *
 * @author Aurelio
 */
public class CodigoBarras {

    /** Patrones del juego A (paridad impar) para los digitos 0-9. */
    private static final String[] A = {
        "0001101", "0011001", "0010011", "0111101", "0100011",
        "0110001", "0101111", "0111011", "0110111", "0001011"};
    /** Patrones del juego B (paridad par). */
    private static final String[] B = {
        "0100111", "0110011", "0011011", "0100001", "0011101",
        "0111001", "0000101", "0010001", "0001001", "0010111"};
    /** Patrones del juego C (lado derecho del simbolo). */
    private static final String[] C = {
        "1110010", "1100110", "1101100", "1000010", "1011100",
        "1001110", "1010000", "1000100", "1001000", "1110100"};
    /**
     * Que juego usa cada uno de los 6 digitos de la izquierda segun el primer
     * digito del codigo: 'A' = juego A, 'B' = juego B.
     */
    private static final String[] PARIDAD = {
        "AAAAAA", "AABABB", "AABBAB", "AABBBA", "ABAABB",
        "ABBAAB", "ABBBAA", "ABABAB", "ABABBA", "ABBABA"};

    private CodigoBarras() { }

    /**
     * Digito verificador de un EAN-13 a partir de los 12 primeros digitos.
     * Devuelve -1 si el texto no son 12 digitos.
     */
    public static int dvEan13(String base12) {
        if (base12 == null || base12.length() != 12) return -1;
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            char c = base12.charAt(i);
            if (c < '0' || c > '9') return -1;
            suma += (c - '0') * (i % 2 == 0 ? 1 : 3);
        }
        return (10 - (suma % 10)) % 10;
    }

    /** true si el texto es un EAN-13 valido (13 digitos y digito verificador correcto). */
    public static boolean esEan13(String codigo) {
        if (codigo == null || codigo.length() != 13) return false;
        int dv = dvEan13(codigo.substring(0, 12));
        return dv >= 0 && dv == (codigo.charAt(12) - '0');
    }

    /**
     * Genera un EAN-13 valido con el prefijo de Paraguay (784) a partir de una
     * semilla. Sirve para darle codigo interno a los productos que no traen
     * uno del fabricante.
     */
    public static String generarEan13(long semilla) {
        long s = Math.abs(semilla) % 1000000000L;
        String base = "784" + String.format("%09d", s);
        return base + dvEan13(base);
    }

    /** Los 95 modulos del simbolo (1 = barra negra). null si el codigo no es EAN-13. */
    public static String patron(String codigo) {
        if (!esEan13(codigo)) return null;
        StringBuilder sb = new StringBuilder(95);
        sb.append("101");                                    // guarda de inicio
        String paridad = PARIDAD[codigo.charAt(0) - '0'];
        for (int i = 1; i <= 6; i++) {
            int d = codigo.charAt(i) - '0';
            sb.append(paridad.charAt(i - 1) == 'A' ? A[d] : B[d]);
        }
        sb.append("01010");                                  // guarda central
        for (int i = 7; i <= 12; i++) sb.append(C[codigo.charAt(i) - '0']);
        sb.append("101");                                    // guarda de fin
        return sb.toString();
    }

    /**
     * Dibuja el simbolo EAN-13.
     *
     * @param codigo      codigo de 13 digitos
     * @param anchoModulo ancho en pixeles de la barra mas fina (1 = chico, 2 = normal)
     * @param alto        alto en pixeles de las barras
     * @param conTexto    si abajo se escribe el numero
     * @return la imagen lista para mostrar o imprimir; null si el codigo no es EAN-13
     */
    public static BufferedImage dibujar(String codigo, int anchoModulo, int alto, boolean conTexto) {
        String patron = patron(codigo);
        if (patron == null || anchoModulo < 1 || alto < 8) return null;

        int quieta = 11 * anchoModulo;                       // zona blanca obligatoria
        int ancho = patron.length() * anchoModulo + 2 * quieta;
        int porTexto = conTexto ? 14 : 0;
        int sobraGuardas = conTexto ? 6 : 0;

        BufferedImage img = new BufferedImage(ancho, alto + porTexto + 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.setColor(Color.BLACK);

        int arriba = 1, abajo = 1 + alto;
        for (int i = 0; i < patron.length(); i++) {
            if (patron.charAt(i) != '1') continue;
            boolean guarda = i <= 2 || (i >= 45 && i <= 49) || i >= 92;
            g.fillRect(quieta + i * anchoModulo, arriba, anchoModulo, (abajo - arriba) + (guarda ? sobraGuardas : 0));
        }

        if (conTexto) {
            g.setFont(new Font("Monospaced", Font.PLAIN, Math.max(9, anchoModulo * 5 + 5)));
            FontMetrics fm = g.getFontMetrics();
            int linea = abajo + fm.getAscent() - 1;
            // primer digito: a la izquierda de la guarda de inicio
            g.drawString(codigo.substring(0, 1), quieta - 3 - fm.stringWidth(codigo.substring(0, 1)), linea);
            // digitos 2-7: centrados bajo el bloque izquierdo
            bloque(g, codigo.substring(1, 7), quieta + 24 * anchoModulo, linea, fm);
            // digitos 8-13: centrados bajo el bloque derecho
            bloque(g, codigo.substring(7, 13), quieta + 71 * anchoModulo, linea, fm);
        }
        g.dispose();
        return img;
    }

    private static void bloque(Graphics2D g, String texto, int centro, int linea, FontMetrics fm) {
        g.drawString(texto, centro - fm.stringWidth(texto) / 2, linea);
    }
}
