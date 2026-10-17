package com.electroservisgmg.formularios;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Tema visual ElectroServis G.M.G — Paletas 60/30/10 + helpers + switch claro/oscuro
 */
public final class UiTheme {
    private UiTheme() {}

    // ==================== PALETA MODO OSCURO (60/30/10) ====================
    public static final Color BG        = new Color(0x121824); // #121824 - 60% fondo
    public static final Color PANEL     = new Color(0x1E293B); // #1E293B - 30% paneles/tarjetas
    public static final Color INPUT     = new Color(0x334155); // #334155 - inputs
    public static final Color BORDER    = new Color(0x475569); // #475569 - bordes
    public static final Color TEXT      = new Color(0xF3F4F6); // #F3F4F6 - texto principal
    public static final Color MUTED     = new Color(0x9CA3AF); // #9CA3AF - texto secundario
    public static final Color ACCENT    = new Color(0x38BDF8); // #38BDF8 - 10% acento
    public static final Color ACCENT_DARK = new Color(0x0284C7);
    public static final Color SUCCESS   = new Color(0x22C55E);
    public static final Color DANGER    = new Color(0xEF4444);
    public static final Color WARNING   = new Color(0xFBBF24);

    // ==================== PALETA MODO CLARO (60/30/10) ====================
    public static final Color L_BG      = new Color(0xF8F9FA); // #F8F9FA - 60% fondo
    public static final Color L_PANEL   = new Color(0xFFFFFF); // #FFFFFF - 30% paneles
    public static final Color L_INPUT   = new Color(0xF8F9FA); // #F8F9FA - inputs
    public static final Color L_BORDER  = new Color(0xE9ECEF); // #E9ECEF - bordes
    public static final Color L_TEXT    = new Color(0x212529); // #212529 - texto principal
    public static final Color L_MUTED   = new Color(0x6C757D); // #6C757D - texto secundario
    public static final Color L_ACCENT  = new Color(0x0066FF); // #0066FF - 10% acento

    // Alias colores viejos (para recolorear forms existentes)
    private static final Color OLD_BG   = new Color(0x0F172A);
    private static final Color OLD_TEXT = new Color(0xF1F5F9);

    // Fuentes
    public static final Font TITLE      = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font SUBTITLE   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font NORMAL     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font MONO       = new Font("JetBrains Mono", Font.PLAIN, 12);

    // Estado
    private static boolean dark = true;
    public static boolean isDark() { return dark; }

    // ==================== PERSISTENCIA ====================
    // tema.cfg guarda 2 claves: defecto=(oscuro|claro|ultimo) y actual=(oscuro|claro)
    private static java.io.File temaFile() {
        return new java.io.File(System.getProperty("user.dir"), "tema.cfg");
    }

    private static String[] leerCfg() {
        String defecto = "ultimo", actual = "oscuro";
        try {
            java.io.File f = temaFile();
            if (f.exists()) {
                String t = new String(java.nio.file.Files.readAllBytes(f.toPath())).trim();
                if (t.contains("=")) {
                    for (String lin : t.split("\\R")) {
                        String[] kv = lin.split("=", 2);
                        if (kv.length == 2) {
                            if (kv[0].trim().equalsIgnoreCase("defecto")) defecto = kv[1].trim().toLowerCase();
                            if (kv[0].trim().equalsIgnoreCase("actual")) actual = kv[1].trim().toLowerCase();
                        }
                    }
                } else if ("claro".equalsIgnoreCase(t)) {
                    actual = "claro"; // formato viejo: solo una palabra
                }
            }
        } catch (Exception ignored) {}
        if (!defecto.equals("oscuro") && !defecto.equals("claro")) defecto = "ultimo";
        if (!actual.equals("claro")) actual = "oscuro";
        return new String[]{defecto, actual};
    }

    public static boolean temaGuardado() {
        String[] cfg = leerCfg();
        String defecto = cfg[0], actual = cfg[1];
        if (defecto.equals("oscuro")) return true;
        if (defecto.equals("claro")) return false;
        return actual.equals("oscuro");
    }

    private static void guardarTema() {
        try {
            String defecto = getDefecto();
            String texto = "defecto=" + defecto + System.lineSeparator()
                + "actual=" + (dark ? "oscuro" : "claro") + System.lineSeparator();
            java.nio.file.Files.write(temaFile().toPath(),
                texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) { System.err.println("guardarTema: " + e.getMessage()); }
    }

    /** Tema por defecto al abrir: "oscuro", "claro" o "ultimo". */
    public static String getDefecto() {
        return leerCfg()[0];
    }

    public static void setDefecto(String defecto) {
        if (!"oscuro".equals(defecto) && !"claro".equals(defecto)) defecto = "ultimo";
        try {
            String texto = "defecto=" + defecto + System.lineSeparator()
                + "actual=" + (dark ? "oscuro" : "claro") + System.lineSeparator();
            java.nio.file.Files.write(temaFile().toPath(),
                texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) { System.err.println("setDefecto: " + e.getMessage()); }
    }

    // ==================== SETUP / TOGGLE ====================
    public static void init() { setup(temaGuardado()); }

    /** Cambia el tema en vivo, guarda como actual y recolorea ventanas abiertas. */
    public static void aplicarTema(boolean oscuro) {
        setup(oscuro);
        guardarTema();
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            recolor(w);
        }
        avisarTema();
    }

    public static void setup(boolean oscuro) {
        dark = oscuro;
        if (oscuro) FlatDarkLaf.setup(); else FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.arc", 12);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
    }

    public static boolean toggle() {
        setup(!dark);
        guardarTema();
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            recolor(w);
        }
        avisarTema();
        return dark;
    }

    public static void applyTo(java.awt.Window w) {
        SwingUtilities.updateComponentTreeUI(w);
        recolor(w);
    }

    // ==================== ESTILOS BOTONES ====================
    public static void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(bg == WARNING ? new Color(0x1F2937) : Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setMargin(new Insets(8, 16, 8, 16));
    }
    public static void primary(JButton b) { styleButton(b, dark ? ACCENT_DARK : L_ACCENT); }
    public static void secondary(JButton b) { styleButton(b, dark ? INPUT : L_BORDER); b.setForeground(dark ? TEXT : L_TEXT); }
    public static void ghost(JButton b) { b.setBackground(new Color(0,0,0,0)); b.setForeground(dark ? ACCENT : L_ACCENT); b.setBorder(BorderFactory.createLineBorder(dark ? ACCENT : L_ACCENT, 1)); }
    public static void danger(JButton b) { styleButton(b, DANGER); }
    public static void success(JButton b) { styleButton(b, SUCCESS); }

    // ==================== INPUTS ====================
    public static void styleInput(JComponent c) {
        c.setBackground(dark ? INPUT : L_INPUT);
        c.setForeground(dark ? TEXT : L_TEXT);
        c.setFont(NORMAL);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dark ? BORDER : L_BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    // ==================== TABLAS ====================
    public static void styleTable(JTable t) {
        t.setBackground(dark ? PANEL : L_PANEL);
        t.setForeground(dark ? TEXT : L_TEXT);
        t.setGridColor(dark ? BORDER : L_BORDER);
        t.setSelectionBackground(dark ? ACCENT_DARK : L_ACCENT);
        t.setSelectionForeground(Color.WHITE);
        t.setRowHeight(32);
        t.setFont(NORMAL);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(dark ? INPUT : L_INPUT);
        h.setForeground(dark ? TEXT : L_TEXT);
        h.setFont(SUBTITLE);
        h.setPreferredSize(new Dimension(0, 36));
    }

    // ==================== PANELES ====================
    public static JPanel paddedPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(dark ? BG : L_BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return p;
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(dark ? PANEL : L_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dark ? BORDER : L_BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static JPanel sidebarPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        return p;
    }

    // ==================== LABELS ====================
    public static JLabel title(String s) {
        JLabel l = new JLabel(s);
        l.setFont(TITLE);
        l.setForeground(dark ? TEXT : L_TEXT);
        return l;
    }
    public static JLabel subtitle(String s) {
        JLabel l = new JLabel(s);
        l.setFont(SUBTITLE);
        l.setForeground(dark ? MUTED : L_MUTED);
        return l;
    }
    public static JLabel muted(String s) {
        JLabel l = new JLabel(s);
        l.setFont(NORMAL);
        l.setForeground(dark ? MUTED : L_MUTED);
        return l;
    }
    public static JLabel body(String s) {
        JLabel l = new JLabel(s);
        l.setFont(NORMAL);
        l.setForeground(dark ? TEXT : L_TEXT);
        return l;
    }

    // ==================== BORDES REDONDEADOS ====================
    public static Border roundBorder() {
        return new RoundedBorder(dark ? BORDER : L_BORDER, 12);
    }
    public static Border accentBorder() {
        return new RoundedBorder(dark ? ACCENT : L_ACCENT, 12);
    }

    public static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color; private final int radius;
        public RoundedBorder(Color c, int r) { color = c; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 10, 4, 10); }
        @Override public Insets getBorderInsets(Component c, Insets insets) { insets.set(4, 10, 4, 10); return insets; }
    }

    // ==================== SOMBRAS ====================
    public static class ShadowBorder extends javax.swing.border.AbstractBorder {
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 4; i++) {
                g2.setColor(new Color(0, 0, 0, 15 - i * 3));
                g2.drawRoundRect(x + i, y + i, w - i * 2 - 1, h - i * 2 - 1, 12, 12);
            }
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 4, 6, 4); }
    }

    // ==================== COLOR HELPERS ====================
    public static Color lighten(Color c, double amt) {
        return new Color(
            (int) Math.min(255, c.getRed() + (255 - c.getRed()) * amt),
            (int) Math.min(255, c.getGreen() + (255 - c.getGreen()) * amt),
            (int) Math.min(255, c.getBlue() + (255 - c.getBlue()) * amt));
    }
    public static Color darken(Color c, double amt) {
        return new Color(
            (int) (c.getRed() * (1 - amt)),
            (int) (c.getGreen() * (1 - amt)),
            (int) (c.getBlue() * (1 - amt)));
    }

    // ==================== SIDEBAR GRADIENTE ====================
    public static class SideBarGradient extends JPanel {
        public SideBarGradient() { setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(0x0F172A), 0, h, new Color(0x1E293B)));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    /** Degradado oscuro para un JPanel comun (el sidebar queda siempre oscuro). */
    public static void fondoDegradadoSidebar(JPanel panel) {
        panel.setUI(new javax.swing.plaf.basic.BasicPanelUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = c.getWidth(), h = c.getHeight();
                g2.setPaint(new GradientPaint(0, 0, new Color(0x0F172A), 0, h, new Color(0x1E293B)));
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        });
    }

    // ==================== LISTENERS DE TEMA ====================
    private static final java.util.List<Runnable> temaListeners = new java.util.ArrayList<>();

    /** Se avisa al cambiar claro/oscuro (p.ej. para repintar el sidebar bloqueado). */
    public static void addTemaListener(Runnable r) {
        if (r != null) temaListeners.add(r);
    }

    private static void avisarTema() {
        for (Runnable r : temaListeners) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }

    // ==================== MONEDA PARAGUAY ====================
    public static final double IVA_PY = 0.10;
    private static final java.text.DecimalFormat FMT_GS;
    static {
        java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols(new java.util.Locale("es", "PY"));
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        FMT_GS = new java.text.DecimalFormat("#,##0", sym);
    }
    public static String gs(double v) { return "Gs. " + FMT_GS.format(Math.round(v)); }
    public static String gsShort(double v) {
        if (v >= 1_000_000) return "Gs. " + String.format(java.util.Locale.US, "%.1f", v / 1_000_000).replace(".0", "").replace('.', ',') + "M";
        if (v >= 1000) return "Gs. " + String.format(java.util.Locale.US, "%.1f", v / 1000).replace(".0", "").replace('.', ',') + "k";
        return "Gs. " + FMT_GS.format(Math.round(v));
    }
    public static double ivaIncluido(double total) { return total - total / (1 + IVA_PY); }
    public static double baseGravada(double total) { return total / (1 + IVA_PY); }
    public static double parseGs(String s) throws NumberFormatException {
        if (s == null) throw new NumberFormatException("vacio");
        String t = s.replace("Gs.", "").replace("Gs", "").replace("$", "").replace("₲", "").replace(" ", "").replace(",", "").trim();
        if (t.isEmpty()) throw new NumberFormatException("vacio");
        if (t.matches(".*\\.\\d{3}(\\.\\d{3})*$") || t.matches("\\d{1,3}(\\.\\d{3})+")) t = t.replace(".", "");
        return Double.parseDouble(t);
    }
    public static int parseGsInt(String s) throws NumberFormatException { return (int) parseGs(s); }

    /** Solo números + puntos de miles (ej: 1000 -> 1.000). */
    public static void miles(javax.swing.JTextField txt) { miles(txt, false); }

    /** Solo números + puntos de miles, con símbolo ₲ adelante (ej: 1000 -> ₲ 1.000). */
    public static void miles(javax.swing.JTextField txt, boolean conSimbolo) {
        if (!(txt.getDocument() instanceof AbstractDocument doc)) return;
        doc.setDocumentFilter(new DocumentFilter() {
            private String formatear(String s) {
                String dig = s.replaceAll("[^0-9]", "").replaceFirst("^0+(?!$)", "");
                if (dig.isEmpty()) return "";
                String n;
                try { n = FMT_GS.format(Long.parseLong(dig)); } catch (NumberFormatException e) { n = dig; }
                return (conSimbolo ? "₲ " : "") + n;
            }
            private void aplicar(DocumentFilter.FilterBypass fb, String nuevo, AttributeSet a)
                    throws BadLocationException {
                super.replace(fb, 0, fb.getDocument().getLength(), formatear(nuevo), a);
                SwingUtilities.invokeLater(() -> { try { txt.setCaretPosition(txt.getText().length()); } catch (Exception ignored) {} });
            }
            @Override public void insertString(DocumentFilter.FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                int end = Math.min(off, cur.length());
                aplicar(fb, cur.substring(0, end) + (str == null ? "" : str) + cur.substring(end), a);
            }
            @Override public void replace(DocumentFilter.FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                int start = Math.min(off, cur.length());
                int end = Math.min(off + len, cur.length());
                aplicar(fb, cur.substring(0, start) + (str == null ? "" : str) + cur.substring(end), a);
            }
            @Override public void remove(DocumentFilter.FilterBypass fb, int off, int len)
                    throws BadLocationException { replace(fb, off, len, "", null); }
        });
    }

    // ==================== RECOLOREO DINÁMICO ====================
    private static Color swapBg(Color c) {
        if (c == null) return null;
        if (eq(c, BG) || eq(c, OLD_BG)) return dark ? BG : L_BG;
        if (eq(c, L_BG) || eq(c, OLD_TEXT)) return dark ? BG : L_BG;
        if (eq(c, PANEL)) return dark ? PANEL : L_PANEL;
        if (eq(c, L_PANEL)) return dark ? PANEL : L_PANEL;
        if (eq(c, INPUT)) return dark ? INPUT : L_INPUT;
        if (eq(c, L_INPUT)) return dark ? INPUT : L_INPUT;
        if (eq(c, BORDER)) return dark ? BORDER : L_BORDER;
        if (eq(c, L_BORDER)) return dark ? BORDER : L_BORDER;
        return null;
    }

    private static boolean eq(Color a, Color b) {
        return a.getRed() == b.getRed() && a.getGreen() == b.getGreen() && a.getBlue() == b.getBlue();
    }

    private static boolean isWhiteFg(Color c) {
        return eq(c, Color.WHITE) || eq(c, TEXT) || eq(c, OLD_TEXT);
    }

    public static void recolor(Component comp) {
        if (comp == null) return;
        if (comp instanceof JComponent jc && jc.getClientProperty("lockDark") != null) return;

        if (comp instanceof JTable t) {
            Color nb = swapBg(t.getBackground());
            if (nb != null) t.setBackground(nb);
            Color fg = t.getForeground();
            if (!dark && isWhiteFg(fg)) t.setForeground(L_TEXT);
            else if (dark && eq(fg, L_TEXT)) t.setForeground(TEXT);
            JTableHeader h = t.getTableHeader();
            if (h != null) {
                Color hb = swapBg(h.getBackground());
                if (hb != null) h.setBackground(hb);
                Color hfg = h.getForeground();
                if (!dark && isWhiteFg(hfg)) h.setForeground(L_TEXT);
                else if (dark && eq(hfg, L_TEXT)) h.setForeground(TEXT);
            }
        } else if (comp instanceof JButton b) {
            Color nb = swapBg(b.getBackground());
            boolean acento = eq(b.getBackground(), ACCENT_DARK) || eq(b.getBackground(), L_ACCENT)
                || eq(b.getBackground(), ACCENT) || eq(b.getBackground(), SUCCESS)
                || eq(b.getBackground(), DANGER) || eq(b.getBackground(), WARNING);
            if (nb != null && !acento) b.setBackground(nb);
            if (!acento) {
                Color fg = b.getForeground();
                if (!dark && isWhiteFg(fg)) b.setForeground(L_TEXT);
                else if (dark && eq(fg, L_TEXT)) b.setForeground(Color.WHITE);
            }
        } else if (comp instanceof javax.swing.AbstractButton ab) {
            Color nb = swapBg(ab.getBackground());
            if (nb != null) ab.setBackground(nb);
            Color fg = ab.getForeground();
            if (fg != null && (eq(fg, MUTED) || eq(fg, L_MUTED) || eq(fg, new Color(0x94A3B8)))) {
                ab.setForeground(dark ? MUTED : L_MUTED);
            }
            if (!dark && isWhiteFg(ab.getForeground())) ab.setForeground(L_TEXT);
            else if (dark && eq(ab.getForeground(), L_TEXT)) ab.setForeground(Color.WHITE);
        } else if (comp instanceof JLabel l) {
            Color fg = l.getForeground();
            if (fg != null && (eq(fg, MUTED) || eq(fg, L_MUTED) || eq(fg, new Color(0x94A3B8)))) {
                l.setForeground(dark ? MUTED : L_MUTED);
            }
            if (!dark && isWhiteFg(fg)) l.setForeground(L_TEXT);
            else if (dark && eq(fg, L_TEXT)) l.setForeground(Color.WHITE);
        } else if (comp instanceof JTextField || comp instanceof JPasswordField || comp instanceof JTextArea || comp instanceof JComboBox || comp instanceof JSpinner) {
            Color nb = swapBg(comp.getBackground());
            if (nb != null) comp.setBackground(nb);
            Color fg = comp.getForeground();
            if (!dark && isWhiteFg(fg)) comp.setForeground(L_TEXT);
            else if (dark && eq(fg, L_TEXT)) comp.setForeground(Color.WHITE);
        } else if (comp instanceof JComponent jc) {
            Color nb = swapBg(jc.getBackground());
            if (nb != null) jc.setBackground(nb);
            if (jc instanceof JScrollPane sp && sp.getViewport() != null) {
                Color vb = swapBg(sp.getViewport().getBackground());
                if (vb != null) sp.getViewport().setBackground(vb);
            }
        }
        if (comp instanceof Container ct) {
            for (Component ch : ct.getComponents()) recolor(ch);
        }
        comp.repaint();
    }
}