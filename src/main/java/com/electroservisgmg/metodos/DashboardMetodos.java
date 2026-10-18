package com.electroservisgmg.metodos;

import com.electroservisgmg.clases.Producto;
import com.electroservisgmg.formularios.UiTheme;

import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Arma el panel de inicio: cuenta las ordenes de servicio por estado, suma el
 * stock de todos los productos y llena la tabla de stock, avisando los que
 * llegaron al minimo.
 */
public class DashboardMetodos {

    /**
     * Lee los datos del sistema y los muestra en el panel.
     *
     * @param lblIngresados  ordenes que siguen en el taller (pendientes, en
     *                       proceso u otras, o sea las que no estan listas ni
     *                       entregadas)
     * @param lblARetirar    ordenes completadas (listas para retirar)
     * @param lblTerminados  ordenes entregadas
     * @param lblStock       stock total de todos los productos
     * @param lblTituloStock titulo del card de stock (avisa cuantos hay que reponer)
     * @param tabla          tabla con el stock de cada producto
     */
    public void cargar(JLabel lblIngresados, JLabel lblARetirar, JLabel lblTerminados,
            JLabel lblStock, JLabel lblTituloStock, JTable tabla) {
        OrdenServicioMetodos om = new OrdenServicioMetodos();
        ProductoMetodos pm = new ProductoMetodos();

        lblIngresados.setText(String.valueOf(om.contarActivas()));
        lblARetirar.setText(String.valueOf(om.contarPorEstado("COMPLETADO")));
        lblTerminados.setText(String.valueOf(om.contarPorEstado("ENTREGADO")));

        List<Producto> prods = pm.listar();
        int stockTotal = 0;
        String[] cols = {"ID", "Nombre", "Stock", "Mín.", "Precio", "Descripción"};
        Object[][] data = new Object[prods.size()][6];
        for (int i = 0; i < prods.size(); i++) {
            Producto p = prods.get(i);
            stockTotal += p.getStock();
            data[i] = new Object[]{p.getIdProducto(), p.getNombre(), p.getStock(),
                p.getStockMinimo() > 0 ? p.getStockMinimo() : "",
                UiTheme.gs(p.getPrecioVenta()), p.getDescripcion()};
        }
        lblStock.setText(String.valueOf(stockTotal));
        tabla.setModel(new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        });
        UiTheme.styleTable(tabla);

        // aviso de reposicion: cuantos productos llegaron al minimo
        List<Producto> bajos = pm.listarBajoMinimo();
        if (bajos.isEmpty()) {
            lblTituloStock.setText("Stock Total");
            lblTituloStock.setForeground(UiTheme.MUTED);
            lblTituloStock.setToolTipText(null);
        } else {
            lblTituloStock.setText("Stock Total · " + bajos.size() + "!");
            lblTituloStock.setForeground(UiTheme.DANGER);
            StringBuilder sb = new StringBuilder("<html><b>Hay que reponer:</b>");
            for (Producto p : bajos) {
                sb.append("<br>· ").append(p.getNombre())
                        .append("  (stock ").append(p.getStock())
                        .append(", mín. ").append(p.getStockMinimo()).append(")");
            }
            lblTituloStock.setToolTipText(sb.append("</html>").toString());
        }
        // en la lista, los que estan bajo el minimo se ven en rojo
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    int id = ((Number) t.getValueAt(row, 0)).intValue();
                    boolean bajo = false;
                    for (Producto p : bajos) {
                        if (p.getIdProducto() == id) { bajo = true; break; }
                    }
                    c.setForeground(bajo ? UiTheme.DANGER : t.getForeground());
                }
                return c;
            }
        });
    }
}
