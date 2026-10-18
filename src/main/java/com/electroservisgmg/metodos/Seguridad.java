package com.electroservisgmg.metodos;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Manejo de las contrasenas de los usuarios.
 *
 * Las claves no se guardan como las escribe el usuario: se guardan como el
 * hash SHA-256 (64 caracteres hexadecimales). Asi, si alguien mira la tabla
 * usuario, no ve las claves.
 *
 * Las claves viejas que quedaron en texto plano se siguen aceptando, y en el
 * primer inicio de sesion se convierten solas a hash.
 *
 * @author Aurelio
 */
public class Seguridad {

    /** Sal fija del sistema (hace que el mismo texto no de el hash obvio). */
    private static final String SAL = "electroservisgmg:";

    private Seguridad() {}

    /** Hash SHA-256 de la clave, en hexadecimal minuscula. */
    public static String hash(String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest((SAL + (clave == null ? "" : clave)).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : d) {
                int v = b & 0xff;
                if (v < 16) sb.append('0');
                sb.append(Integer.toHexString(v));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Este Java no tiene SHA-256", e);
        }
    }

    /** True si el texto guardado ya es un hash del sistema. */
    public static boolean esHash(String valor) {
        return valor != null && valor.matches("[0-9a-f]{64}");
    }

    /**
     * Compara la clave que escribio el usuario con la que esta guardada.
     * Acepta tambien las claves viejas en texto plano, para no dejar a nadie afuera.
     */
    public static boolean verificar(String escrita, String guardada) {
        if (escrita == null || guardada == null) return false;
        if (esHash(guardada)) return guardada.equalsIgnoreCase(hash(escrita));
        return guardada.equals(escrita);
    }
}
