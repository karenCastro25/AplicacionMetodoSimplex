/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package principal;


import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author FORTY
 */
public abstract class PintarCelda extends JLabel implements TableCellRenderer {

    int Row, Columns;

    public void setRow(int Row) {
        this.Row = Row;
    }

    public void setColumns(int Columns) {
        this.Columns = Columns;
    }

    public PintarCelda() {
        setOpaque(true); // Permite que se vea el color en la celda del JLabel
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if ((row == Row) && (column == Columns)) {
            setBackground(Color.CYAN); // Una condicion arbitraria solo para pintar el JLabel que esta en la celda.
             //setForeground(Color.WHITE);
            setText(String.valueOf(value)); // Se agrega el valor que viene por defecto en la celda
        }
        if ((row != Row) && (column == Columns)) {
            setBackground(Color.WHITE); // Una condicion arbitraria solo para pintar el JLabel que esta en la celda.
            setText(String.valueOf(value)); // Se agrega el valor que viene por defecto en la celda
        }

        return this;
    }

}
