package com.electroservisgmg;

import com.electroservisgmg.formularios.FrmLogin;
import com.electroservisgmg.formularios.UiTheme;
import javax.swing.SwingUtilities;

public class ElectroServisGMG {
    public static void main(String[] args) {
        try { UiTheme.init(); } catch (Exception e) { System.err.println("FlatLaf no cargado: " + e.getMessage()); }
        SwingUtilities.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}
