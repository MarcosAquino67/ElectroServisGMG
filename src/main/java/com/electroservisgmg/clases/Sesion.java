package com.electroservisgmg.clases;

/**
 * Sesión global: usuario logueado + listener para notificar cambios
 * (por ejemplo, cuando se modifica el nombre del usuario en sesión).
 */
public class Sesion {

    private static Usuario usuario;
    private static Runnable listener;

    public static void setUsuario(Usuario u) { usuario = u; }

    public static Usuario getUsuario() { return usuario; }

    public static void setListener(Runnable r) { listener = r; }

    public static void notificarCambio() {
        if (listener != null) listener.run();
    }
}
