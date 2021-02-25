package principal;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

/**
 *
 * @author FORTY
 */
public class FrameSimplex extends javax.swing.JFrame {

    int numRestricciones, numVariables, columSR;
    int numIteraciones;
    JComboBox< Object> cmbSigno = new JComboBox<>();
    int numFotos;
    File directorio;

    public FrameSimplex() {

        initComponents();
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Metodo Simplex");
        panelRestrFunc.setVisible(false);
        panelTablaInicial.setVisible(false);
        lblMaxz.setVisible(false);
        lblFuncionObjetivo.setVisible(false);
        DefaultComboBoxModel modelo = new DefaultComboBoxModel();
        modelo.addElement("<=");
        cmbSigno.setModel(modelo);
    }

    //funcion que crea tabla para introducir la funcion objetivo
    private void llenarTablaVar() {
        DefaultTableModel modelo = new DefaultTableModel();
        tblFuncion.setModel(modelo);
        for (int i = 1; i <= numVariables; i++) {//ciclo que añade columnas
            modelo.addColumn("X" + i);
        }
        Object obj[] = null;
        modelo.insertRow(0, obj);//tabla con una unica fila
    }

    //funcion que crea tabla para introducir las restricciones
    private void llenarTablaRestr() {
        DefaultTableModel modeloTablaRestricciones = new DefaultTableModel();
        tblSR.setModel(modeloTablaRestricciones);
        for (int i = 1; i <= numVariables; i++) { //ciclo que añade columnas
            modeloTablaRestricciones.addColumn("X" + i);
        }
        modeloTablaRestricciones.addColumn("<="); //columna <=
        modeloTablaRestricciones.addColumn("Bi"); //columna Bi

        //Ahora vamos a recoger una columna que será donde insertemos el combobox
        TableColumn columna = tblSR.getColumnModel().getColumn(numVariables);
        //Creamos un nuevo editor de celda. Tambien puede insertarse checkboxs y textfields
        TableCellEditor editor = new DefaultCellEditor(cmbSigno);
        //Le asignamos a la columna el editor creado
        columna.setCellEditor(editor);

        Object[] obj = new Object[numVariables + 2];
        for (int i = 0; i < numRestricciones; i++) { //ciclo que añade filas al mismo tiempo que ingresa <=
            for (int j = 0; j < obj.length; j++) {
                obj[j] = "";
            }
            modeloTablaRestricciones.insertRow(i, obj);
        }

    }

    //funcion para guardar en matriz las inecuaciones
    private Object[][] sistemaRestricciones() {
        columSR = numVariables + numRestricciones + 1; //numero de columnas que ocupan las inecuaciones
        Object restricciones[][] = new Object[numRestricciones][columSR];
        String stringDecimal1 = "";
        String stringDecimal2 = "";
        double decimal1 = 0;
        double decimal2 = 0;
        Fraccion frac = new Fraccion();
        for (int iResFila = 0; iResFila < numRestricciones; iResFila++) { //ciclo filas
            for (int iResCol = 0; iResCol < columSR; iResCol++) { //ciclo columnas
                if (iResCol < numVariables) {//si se cumple tomamos los valores de la tabla de restricciones
                    stringDecimal1 = String.valueOf(tblSR.getValueAt(iResFila, iResCol));
                    if (frac.posicionBarra(stringDecimal1) == -1) { //si no es fraccion
                        decimal1 = Double.parseDouble(stringDecimal1);
                    } else { //si es fraccion
                        decimal1 = 1;
                    }
                    if (decimal1 % (int) decimal1 == 0) {
                        restricciones[iResFila][iResCol] = stringDecimal1;//tblSR.getValueAt(iResFila, iResCol);
                    } else {
                        restricciones[iResFila][iResCol] = frac.toFraccion(decimal1).toString();
                    }
                } else {
                    if (iResCol - numVariables == iResFila) {//calculo para ingresar 1
                        restricciones[iResFila][iResCol] = 1;
                    } else if (iResCol == columSR - 1) { //calculo para ingresar Bi
                        stringDecimal2 = String.valueOf(tblSR.getValueAt(iResFila, (tblSR.getColumnCount() - 1)));
                        if (frac.posicionBarra(stringDecimal2) == -1) { //si no es fraccion
                            decimal2 = Double.parseDouble(stringDecimal2);
                        } else { //si es fraccion
                            decimal2 = 1;
                        }
                        if (decimal2 % (int) decimal2 == 0) {
                            restricciones[iResFila][iResCol] = stringDecimal2;//tblSR.getValueAt(iResFila, (tblSR.getColumnCount() - 1));
                        } else {
                            restricciones[iResFila][iResCol] = frac.toFraccion(decimal2).toString();
                        }
                    } else {
                        restricciones[iResFila][iResCol] = 0; //calculo para ingresar 0
                    }
                }
            }
        }
        return restricciones;
    }

    //funcion para llenar la tabla con la matriz inicial
    private void matrizInicial() {
        int numColumnas = numVariables + numRestricciones + 3;
        int numFilas = numRestricciones + 4;

        Object matriz[][] = new Object[numFilas][numColumnas];//matriz inicial
        //espacios en blanco y datos que no cambian
        matriz[0][0] = " ";
        matriz[0][1] = "CJ";
        matriz[0][numColumnas - 1] = " ";
        matriz[1][0] = "CB";
        matriz[1][1] = "XB";
        matriz[1][numColumnas - 1] = "BI";
        matriz[numFilas - 2][0] = " ";
        matriz[numFilas - 2][1] = "ZJ";
        matriz[numFilas - 1][0] = " ";
        matriz[numFilas - 1][1] = "ZJ-CJ";
        matriz[numFilas - 1][numColumnas - 1] = " ";

        int iVar = 0;
        int x = 2;
        double decimal = 0;
        String stringDecimal = "";
        String funcion = "";
        Fraccion frac = new Fraccion();
        for (int iCol = 2; iCol < numColumnas - 1; iCol++) {//ciclo para guardar las 2 primeras filas y los coeficientes CB
            while (iVar < numVariables) {
                stringDecimal = String.valueOf(tblFuncion.getValueAt(0, iVar));

                if (frac.posicionBarra(stringDecimal) == -1) { //si no es fraccion
                    decimal = Double.parseDouble(stringDecimal);
                } else { //si es fraccion
                    decimal = 1;
                }
                if (decimal % (int) decimal == 0) {
                    matriz[0][iCol] = stringDecimal;//tblFuncion.getValueAt(0, iVar);

                } else {
                    matriz[0][iCol] = frac.toFraccion(decimal).toString();
                }
                //llenar funcion objetivo
                funcion += " " + matriz[0][iCol];
                // matriz[0][iCol] = tblFuncion.getValueAt(0, iVar);//se toma datos de la tabla función 4,5
                matriz[1][iCol] = "X" + (iVar + 1);// se agrega los X1,X2
                //llenar funcion objetivo
                funcion += String.valueOf(matriz[1][iCol] + " +");
                iCol++;
                iVar++;
            }
            iVar += 1;
            matriz[0][iCol] = 0; //se termina de agregar 0,0,0
            matriz[1][iCol] = "X" + (iVar);//se termina de agregar X3,X4,X5 
            matriz[x][1] = "X" + (iVar);//se agrega los Xb ejemplo X3,X4,X5
            matriz[x][0] = 0; //se agrega los Cb 0,0,0
            x++;
        }
        //Mostrar función objetivo
        lblFuncionObjetivo.setVisible(true);

        lblFuncionObjetivo.setText(funcion.substring(0, funcion.length() - 1));

        //guardamos las inecuaciones
        Object restricciones[][] = sistemaRestricciones();

        //agregamos inecuaciones a la tabla
        for (int q = 0; q < numRestricciones; q++) {//filas
            for (int w = 0; w < columSR; w++) { //columnas
                matriz[q + 2][w + 2] = restricciones[q][w];
            }
        }

        int columnasZj = numRestricciones + numVariables + 1;
        //obtener fila zj
        for (int i = 0; i < columnasZj; i++) {
            matriz[numFilas - 2][i + 2] = "0";
        }
        //obtener fila cj-zj
        for (int i = 0; i < (columnasZj - 1); i++) {
            matriz[numFilas - 1][i + 2] = matriz[0][i + 2];
        }
        //mostrar matriz en la tabla
        DefaultTableModel modeloInicial = new DefaultTableModel();
        tbl_Inicial.setModel(modeloInicial);
        for (int i = 0; i < numColumnas; i++) { //Agregar columnas
            modeloInicial.addColumn("");
        }
        Object obj[] = new Object[numColumnas];
        for (int i = 0; i < numFilas; i++) {
            for (int k = 0; k < numColumnas; k++) {
                obj[k] = matriz[i][k];
            }
            modeloInicial.addRow(obj);
        }

        //pintamos el pivote
        pintarPivote();

        //mostrar iteracion 0
        numIteraciones = 0;
        lblNumIteracion.setText("" + numIteraciones + "");

    }

    //funcion pinta la celda del numero pivote
    private void pintarPivote() {
        int fila = variableSalida();//ubicacion de la variable de salida
        int col = variableEntrada();//ubicacion de la variable de entrada
        TableColumn columna = tbl_Inicial.getColumnModel().getColumn(col);// selecciono la columna que me interesa de la tabla
        PintarCelda TableCellRenderer = new PintarCelda() {
        };
        TableCellRenderer.setColumns(col); //se le da por parametro la columna que se quiere modificar
        TableCellRenderer.setRow(fila);//se le da por parametro la fila que se quiere modificar
        columna.setCellRenderer(TableCellRenderer); // le aplico la edicion
    }

    //funcion que devuelve la ubicacion de la variable de salida
    private int variableSalida() {
        Fraccion fraccionAij = new Fraccion();
        Fraccion fraccionBi = new Fraccion();
        double vSal = 0d;
        double siguiente = 0d;
        int numColumnas = numVariables + numRestricciones + 3;
        int numFilas = numRestricciones + 4;
        int menor = 2; //primera ubicacion
        int ve = variableEntrada(); //ubicacion de la variable de entrada
        boolean entra = true; //booleana para ejecutarse una sola vez
        double aij = 0;
        double bi = 0;
        String stringAij = "";
        String stringBi = "";
        for (int i = 2; i < numFilas - 2; i++) { //ciclo recorre las filas de las inecuaciones

            stringAij = String.valueOf(tbl_Inicial.getValueAt(i, ve));
            if (fraccionAij.posicionBarra(stringAij) == -1) {
                aij = Double.parseDouble(stringAij);
            } else {
                fraccionAij = fraccionAij.deTablaFraccion(stringAij);
                aij = fraccionAij.toDecimal();
            }

            stringBi = String.valueOf(tbl_Inicial.getValueAt(i, numColumnas - 1));
            if (fraccionBi.posicionBarra(stringBi) == -1) {
                bi = Double.parseDouble(stringBi);
            } else {
                fraccionBi = fraccionBi.deTablaFraccion(stringBi);
                bi = fraccionBi.toDecimal();
            }

            if (aij > 0 && bi > 0) { //
                siguiente = (double) bi / aij;
                if (entra) {//solo se ejecuta una vez al inicio del ciclo
                    vSal = siguiente;// vSal toma el primer valor de siguiente
                    entra = false;
                    menor = i;
                }

                if (siguiente < vSal) {// condicional para que determinar el menor valor
                    vSal = siguiente; //guarda el valor menor para ser usado en la condicional a la sgt vuelta del ciclo
                    menor = i; //guarda la ubicacion de la fila 
                }
            }
        }
        return menor;
    }

    //funcion que devuelve la ubicacion de la variable de entrada
    private int variableEntrada() {
        int numFilas = numRestricciones + 4;
        int numColumnas = numVariables + numRestricciones + 3;
        int contador = 3;
        int mayor = 2;// primera posicion
        Fraccion fraccionVent = new Fraccion();
        Fraccion fraccionSig = new Fraccion();
        double vEnt = 0;
        double siguiente = 0;
        String stringVent = "";
        String stringSig = "";
        //el primer valor puede ser el mayor si el resto de valores son iguales o menores
        stringVent = String.valueOf(tbl_Inicial.getValueAt(numFilas - 1, 2));
        if (fraccionVent.posicionBarra(stringVent) == -1) {
            vEnt = Double.parseDouble(stringVent);
        } else {
            fraccionVent = fraccionVent.deTablaFraccion(stringVent);
            vEnt = fraccionVent.toDecimal();
        }

        while (contador < numColumnas - 1) {//ciclo para hallar el valor mayor

            stringSig = String.valueOf(tbl_Inicial.getValueAt(numFilas - 1, contador));
            if (fraccionSig.posicionBarra(stringSig) == -1) {
                siguiente = Double.parseDouble(stringSig);
            } else {
                fraccionSig = fraccionSig.deTablaFraccion(stringSig);
                siguiente = fraccionSig.toDecimal();
            }

            if (siguiente > vEnt) {//si el siguiente valor es mayor
                vEnt = siguiente;//vEnt toma ese valor para ser comparado en la siguiente vuelta del ciclo
                mayor = contador;//guarda la ubicacion de la columna 
            }
            contador++;
        }
        return mayor;
    }

    //funcion que realiza todas las iteraciones
    private void iteracciones() {
        int numColumnas = numVariables + numRestricciones + 3;
        int numFilas = numRestricciones + 4;
        Object anteriorIteracion[][] = new Object[numFilas][numColumnas]; //matriz para tomar los datos actuales de la tabla
        Object nuevaIteracion[][] = new Object[numFilas][numColumnas]; //matriz para insertar nuevos datos a la tabla

        //llenar matriz anterior
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                anteriorIteracion[i][j] = tbl_Inicial.getValueAt(i, j);
            }
        }

        //llenar 2 primeras filas de la nueva matriz
        for (int k = 0; k < 2; k++) {
            for (int m = 0; m < numColumnas; m++) {
                nuevaIteracion[k][m] = anteriorIteracion[k][m];
            }
        }

        int fila = variableSalida(); //ubicacion de la fila del pivote
        int col = variableEntrada(); //ubicacion de la columna del pivote

        //obtener fila 1
        Fraccion f1Fila1 = new Fraccion();
        Fraccion f2Fila1 = new Fraccion();
        // double fila1[] = new double[numColumnas - 2]; //arreglo para la fila 1
        Fraccion fila1[] = new Fraccion[numColumnas - 2];
        String string1Fila1 = "";
        String string2Fila1 = "";
        // System.out.println("Fila 1");
        for (int i = 0; i < fila1.length; i++) {
            string1Fila1 = String.valueOf(anteriorIteracion[fila][2 + i]);
            string2Fila1 = String.valueOf(anteriorIteracion[fila][col]);
            f1Fila1 = f1Fila1.deTablaFraccion(string1Fila1);
            fila1[i] = f1Fila1.dividir(f2Fila1.deTablaFraccion(string2Fila1));
        }

        //obtener las otras filas 0 y llenar la tabla de ecuaciones
        // int numColEcuaciones = numColumnas - 1;
        // int numFilaEcuaciones = (numRestricciones - 1) * 6;
        // Object matrizEcuaciones[][] = new Object[numFilaEcuaciones][numColEcuaciones]; //matriz 
        String stringNega = "";
        String stringFila0 = "";
        Fraccion fracNega = new Fraccion();
        Fraccion fracFila0 = new Fraccion();
        Fraccion fracMulti0 = new Fraccion();
        int saltoFila = -6;
        String multiplicador = "";
        for (int iterador = 2; iterador < numFilas - 2; iterador++) {

            if (iterador != fila) {//si se cumpla se calcula el valor de las nuevas filas 0
                saltoFila += 6;
                System.out.println("\nFila 0\n");
                for (int j = 2; j < numColumnas; j++) {
                    stringNega = String.valueOf(anteriorIteracion[iterador][col]);
                    fracNega = fracNega.deTablaFraccion(stringNega);
                    fracNega = fracNega.multiplicar(new Fraccion(-1, 1));
                    stringFila0 = String.valueOf(anteriorIteracion[iterador][j]);
                    fracFila0 = fracFila0.deTablaFraccion(stringFila0);
                    fracMulti0 = fila1[j - 2].multiplicar(fracNega);
                    //       matrizEcuaciones[saltoFila + 1][j - 1] = String.valueOf(fila1[j - 2]);
                    //       matrizEcuaciones[saltoFila + 2][j - 1] = String.valueOf(fracMulti0.toString());
                    fracMulti0 = fracMulti0.sumar(fracFila0);
                    //      matrizEcuaciones[saltoFila + 3][j - 1] = String.valueOf(fracFila0.toString());
                    //     matrizEcuaciones[saltoFila + 4][j - 1] = String.valueOf(fracMulti0.toString());
                    nuevaIteracion[iterador][j] = fracMulti0.toString();

                    multiplicador = String.valueOf(anteriorIteracion[iterador][col]);
                    if (multiplicador.charAt(0) == '-') {
                        multiplicador.subSequence(1, multiplicador.length());
                    } else {
                        multiplicador = "-" + multiplicador;
                    }
                    /*
                    matrizEcuaciones[saltoFila][0] = anteriorIteracion[iterador][1] + "' = " + anteriorIteracion[1][col]
                            + "(" + multiplicador + ") + " + anteriorIteracion[iterador][1];
                    matrizEcuaciones[1 + saltoFila][0] = anteriorIteracion[1][col] + " = ";
                    matrizEcuaciones[2 + saltoFila][0] = multiplicador + "(" + anteriorIteracion[1][col] + ") = ";
                    matrizEcuaciones[3 + saltoFila][0] = anteriorIteracion[iterador][1] + " = ";
                    matrizEcuaciones[4 + saltoFila][0] = anteriorIteracion[iterador][1] + "' = ";
                     */
                }

            } else { //si no se cumple se guarda la fila 1 donde corresponde
                for (int j = 2; j < numColumnas; j++) {
                    nuevaIteracion[iterador][j] = fila1[j - 2].toString();
                    System.out.print(" - " + nuevaIteracion[iterador][j].toString() + " - ");
                }
            }
        }

        //llenar CB y XB
        for (int icb = 2; icb < numFilas - 2; icb++) {
            for (int ixb = 0; ixb < 2; ixb++) {
                if (icb == fila) {
                    nuevaIteracion[fila][ixb] = anteriorIteracion[ixb][col];
                } else {
                    nuevaIteracion[icb][ixb] = anteriorIteracion[icb][ixb];
                }
            }
        }
        //llenar Zj
        nuevaIteracion[numFilas - 2][0] = anteriorIteracion[numFilas - 2][0]; //" "
        nuevaIteracion[numFilas - 2][1] = anteriorIteracion[numFilas - 2][1]; //"Zj"
        String stringCB = "";
        String stringFracZj = "";
        Fraccion fracCB = new Fraccion();
        Fraccion fracAcumulador = new Fraccion(0, 1);
        Fraccion fracMultiZj = new Fraccion();
        Fraccion fracZj = new Fraccion();
        for (int i = 2; i < numColumnas; i++) {
            fracAcumulador = new Fraccion(0, 1);
            for (int j = 2; j < numFilas - 2; j++) {
                stringCB = String.valueOf(nuevaIteracion[j][0]);
                fracCB = fracCB.deTablaFraccion(stringCB);
                stringFracZj = String.valueOf(nuevaIteracion[j][i]);
                fracZj = fracZj.deTablaFraccion(stringFracZj);
                fracMultiZj = fracCB.multiplicar(fracZj);
                fracAcumulador = fracAcumulador.sumar(fracMultiZj);
            }
            nuevaIteracion[numFilas - 2][i] = fracAcumulador.toString();
        }

        //llenar Cj-Zj
        nuevaIteracion[numFilas - 1][0] = anteriorIteracion[numFilas - 1][0]; //" "
        nuevaIteracion[numFilas - 1][1] = anteriorIteracion[numFilas - 1][1]; //"Zj-Cj"
        nuevaIteracion[numFilas - 1][numColumnas - 1] = anteriorIteracion[numFilas - 1][numColumnas - 1]; //" "
        double resta = 0;
        Fraccion fracA = new Fraccion();
        Fraccion fracB = new Fraccion();
        Fraccion fracResta = new Fraccion();
        String stringA = "";
        String stringB = "";
        for (int i = 2; i < numColumnas - 1; i++) {
            stringA = String.valueOf(nuevaIteracion[0][i]);
            fracA = fracA.deTablaFraccion(stringA);
            stringB = String.valueOf(nuevaIteracion[numFilas - 2][i]);
            fracB = fracB.deTablaFraccion(stringB);
            fracResta = fracA.restar(fracB);
            nuevaIteracion[numFilas - 1][i] = fracResta.toString();
        }

        //mostrar matriz en la tabla
        DefaultTableModel modeloIteracion = new DefaultTableModel();
        Object obj[] = new Object[numColumnas];
        tbl_Inicial.setModel(modeloIteracion);
        for (int i = 0; i < numColumnas; i++) { //Agregar columnas
            modeloIteracion.addColumn(" ");
        }
        for (int i = 0; i < numFilas; i++) { //llenar filas
            for (int k = 0; k < numColumnas; k++) {
                obj[k] = nuevaIteracion[i][k];
            }
            modeloIteracion.addRow(obj);
        }

        //pintar pivote
        pintarPivote();

        numIteraciones++;
        lblNumIteracion.setText("" + numIteraciones + "");

    }

    public boolean seguirIteracionesSimplex() {
        boolean seSigue = false;
        int numColumnas = numVariables + numRestricciones + 3;
        int numFilas = numRestricciones + 4;
        String stringSig = "";
        double siguiente = 0;

        Fraccion fraccionSig = new Fraccion();
        for (int i = 2; i < numColumnas - 2; i++) {
            stringSig = String.valueOf(tbl_Inicial.getValueAt(numFilas - 1, i));
            if (fraccionSig.posicionBarra(stringSig) == -1) {
                siguiente = Double.parseDouble(stringSig);
            } else {
                fraccionSig = fraccionSig.deTablaFraccion(stringSig);
                siguiente = fraccionSig.toDecimal();
            }
            if (siguiente > 0) {
                seSigue = true;
            }
        }
        return seSigue;
    }

    //funciones no utilizadas
    private void resetearTablas(JTable tabla) {

        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();

        if (modelo.getColumnCount() > 0) {
            System.out.println("Resetea");
            System.out.println("Columnas: " + modelo.getColumnCount());
            System.out.println("Filas: " + tabla.getRowCount());
            for (int i = tabla.getRowCount() - 1; i >= 0; i--) {
                modelo.removeRow(i);
            }
            for (int i = tabla.getColumnCount() - 1; i >= 0; i--) {
                tabla.getColumnModel().removeColumn(tabla.getColumnModel().getColumn(i));
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblFondo = new javax.swing.JLabel();
        panelGeneral = new javax.swing.JPanel();
        lblVariables = new javax.swing.JLabel();
        lblRestricciones = new javax.swing.JLabel();
        txtVariables = new javax.swing.JTextField();
        txtRestricciones = new javax.swing.JTextField();
        btnGenerar = new javax.swing.JButton();
        panelTablaInicial = new javax.swing.JPanel();
        scroll_TInicial = new javax.swing.JScrollPane();
        tbl_Inicial = new javax.swing.JTable();
        btnIteraccion = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lblNumIteracion = new javax.swing.JLabel();
        btnCrearPdf = new javax.swing.JButton();
        panelRestrFunc = new javax.swing.JPanel();
        scrollSR = new javax.swing.JScrollPane();
        tblSR = new javax.swing.JTable();
        scrollTFuncion = new javax.swing.JScrollPane();
        tblFuncion = new javax.swing.JTable();
        btnTablaU = new javax.swing.JButton();
        lblMaxz = new javax.swing.JLabel();
        lblFuncionObjetivo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 520));

        panelGeneral.setMaximumSize(new java.awt.Dimension(900, 520));
        panelGeneral.setMinimumSize(new java.awt.Dimension(900, 520));

        lblVariables.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        lblVariables.setForeground(new java.awt.Color(0, 0, 255));
        lblVariables.setText("Ingrese el número de variables:");

        lblRestricciones.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        lblRestricciones.setForeground(new java.awt.Color(0, 0, 255));
        lblRestricciones.setText("Ingrese el número de restricciones:");

        txtVariables.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N

        txtRestricciones.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N

        btnGenerar.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        btnGenerar.setForeground(new java.awt.Color(0, 0, 255));
        btnGenerar.setText("Crear Tablas");
        btnGenerar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarActionPerformed(evt);
            }
        });

        scroll_TInicial.setViewportView(tbl_Inicial);

        btnIteraccion.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        btnIteraccion.setForeground(new java.awt.Color(0, 0, 255));
        btnIteraccion.setText("Iteración");
        btnIteraccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIteraccionActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("Iteración #");

        lblNumIteracion.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        lblNumIteracion.setForeground(new java.awt.Color(0, 0, 0));

        btnCrearPdf.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        btnCrearPdf.setForeground(new java.awt.Color(0, 0, 255));
        btnCrearPdf.setText("Exportar a PDF");
        btnCrearPdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearPdfActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelTablaInicialLayout = new javax.swing.GroupLayout(panelTablaInicial);
        panelTablaInicial.setLayout(panelTablaInicialLayout);
        panelTablaInicialLayout.setHorizontalGroup(
            panelTablaInicialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaInicialLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTablaInicialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnCrearPdf, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelTablaInicialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(scroll_TInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(panelTablaInicialLayout.createSequentialGroup()
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblNumIteracion, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnIteraccion, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 17, Short.MAX_VALUE))
        );
        panelTablaInicialLayout.setVerticalGroup(
            panelTablaInicialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaInicialLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(panelTablaInicialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnIteraccion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNumIteracion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scroll_TInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCrearPdf)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        panelRestrFunc.setOpaque(false);

        scrollSR.setViewportView(tblSR);

        scrollTFuncion.setViewportView(tblFuncion);

        btnTablaU.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        btnTablaU.setForeground(new java.awt.Color(0, 0, 255));
        btnTablaU.setText("Crear Tabla U");
        btnTablaU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTablaUActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelRestrFuncLayout = new javax.swing.GroupLayout(panelRestrFunc);
        panelRestrFunc.setLayout(panelRestrFuncLayout);
        panelRestrFuncLayout.setHorizontalGroup(
            panelRestrFuncLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRestrFuncLayout.createSequentialGroup()
                .addGroup(panelRestrFuncLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelRestrFuncLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(panelRestrFuncLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(scrollTFuncion, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                            .addComponent(scrollSR, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(panelRestrFuncLayout.createSequentialGroup()
                        .addGap(142, 142, 142)
                        .addComponent(btnTablaU)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        panelRestrFuncLayout.setVerticalGroup(
            panelRestrFuncLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRestrFuncLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(scrollTFuncion, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(scrollSR, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTablaU)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        lblMaxz.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        lblMaxz.setForeground(new java.awt.Color(0, 0, 255));
        lblMaxz.setText("Max Z =");

        lblFuncionObjetivo.setFont(new java.awt.Font("Microsoft JhengHei", 1, 15)); // NOI18N
        lblFuncionObjetivo.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
        panelGeneral.setLayout(panelGeneralLayout);
        panelGeneralLayout.setHorizontalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addComponent(lblRestricciones, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(txtRestricciones, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addGap(130, 130, 130)
                        .addComponent(btnGenerar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelRestrFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addComponent(lblVariables, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(txtVariables, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(40, 40, 40)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addComponent(lblMaxz, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFuncionObjetivo, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelTablaInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelGeneralLayout.setVerticalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblVariables, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtVariables, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblMaxz, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFuncionObjetivo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRestricciones, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtRestricciones, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addComponent(btnGenerar)
                        .addGap(17, 17, 17)
                        .addComponent(panelRestrFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelTablaInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, 481, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarActionPerformed
      
        lblMaxz.setVisible(false);
        lblFuncionObjetivo.setVisible(false);
        panelRestrFunc.setVisible(true);
        panelTablaInicial.setVisible(false);
        numVariables = Integer.parseInt(txtVariables.getText());
        numRestricciones = Integer.parseInt(txtRestricciones.getText());
        llenarTablaVar();
        llenarTablaRestr();
        resetearTablas(tbl_Inicial);
        crearCarpeta();
    }//GEN-LAST:event_btnGenerarActionPerformed

    private void btnTablaUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTablaUActionPerformed
   
        if (tblFuncion.isEditing()) {//si se esta edtando la tabla
            tblFuncion.getCellEditor().stopCellEditing();//detenga la edicion
        }

        if (tblSR.isEditing()) {//si se esta edtando la tabla
            tblSR.getCellEditor().stopCellEditing();//detenga la edicion
        }
      
        lblMaxz.setVisible(true);
        btnCrearPdf.setVisible(false);
        btnCrearPdf.setEnabled(true);
        lblFuncionObjetivo.setVisible(true);
        panelTablaInicial.setVisible(true);
        matrizInicial();
        //sistemaRestricciones(); // why?
        final SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
               numFotos=-1;
               guardarCaptura(panelRestrFunc);
                numFotos = 0;
                guardarCaptura(panelTablaInicial);
                return null;
            }
        };
        worker.execute();
    }//GEN-LAST:event_btnTablaUActionPerformed
    public void crearCarpeta() {
        String rutaCarpeta = "capturasSimplexBigM";
        File home = FileSystemView.getFileSystemView().getHomeDirectory();
        String absPath = home.getAbsolutePath();
        directorio = new File(absPath + "/" + rutaCarpeta);
        directorio.mkdir();
    }
    private void btnIteraccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIteraccionActionPerformed
        if (seguirIteracionesSimplex()) {
            iteracciones();
            final SwingWorker worker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    numFotos++;
                    guardarCaptura(panelTablaInicial);
                    return null;
                }
            };
            worker.execute();
        } else {
            JOptionPane.showMessageDialog(this, "Se han terminado las iteraciones");
            btnCrearPdf.setVisible(true);

        }


    }//GEN-LAST:event_btnIteraccionActionPerformed

    private void btnCrearPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearPdfActionPerformed

        String funcion = "";
        String ruta = escogerRuta();

        funcion = "Max Z = " + lblFuncionObjetivo.getText();
        if (!ruta.isEmpty()) {
            GenerarPDF generarPDF = new GenerarPDF(directorio, ruta, funcion, guardarRestricciones(), numIteraciones);
            generarPDF.start();
        }
        btnCrearPdf.setEnabled(false);

    }//GEN-LAST:event_btnCrearPdfActionPerformed

    private void guardarCaptura(Component component) {
        BufferedImage imagen = new BufferedImage(
                component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        // call the Component's paint method, using 
        // the Graphics object of the image. 
        component.paint(imagen.getGraphics()); // alternately use .printAll(..) 
        //guardar captura 
        try {
            // write the image as a PNG 

            ImageIO.write(imagen, "png", new File(directorio.getPath() + "/" + numFotos + ".png"));
//            numFotos++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] guardarRestricciones() {
        String[] restriccionesString = new String[numRestricciones + 1];
        int columnas = numVariables + 2;
        String valor = "";
        //restricciones
        for (int i = 0; i < restriccionesString.length - 1; i++) {
            for (int j = 0; j < columnas; j++) {
                valor = String.valueOf(tblSR.getValueAt(i, j));
                if (j < numVariables) {
                    if (j == 0) {
                        restriccionesString[i] = valor + "X" + (j + 1);

                    } else {
                        if (valor.charAt(0) == '-') {
                            restriccionesString[i] += valor + "X" + (j + 1);
                        } else {
                            restriccionesString[i] += "+" + valor + "X" + (j + 1);
                        }
                    }
                } else {
                    restriccionesString[i] += valor;

                }
            }
        }
        //no negativadad
        restriccionesString[restriccionesString.length - 1] = "";
        for (int fila = 0; fila < numRestricciones; fila++) {

            restriccionesString[restriccionesString.length - 1] += String.valueOf("X" + (fila + 1)) + "<=0; ";
        }
        return restriccionesString;
    }

    private String escogerRuta() {
        JFileChooser jF1 = new JFileChooser();
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("*.pdf", "pdf");
        jF1.setFileFilter(filtro);
        String ruta = "";
        String newRuta = "";
        try {
            if (jF1.showSaveDialog(this) == jF1.APPROVE_OPTION) {
                ruta = jF1.getSelectedFile().getAbsolutePath();
                ruta = devolverExtension(ruta);
                if (new File(ruta).exists()) {
                    int decision = JOptionPane.showConfirmDialog(this,
                            "¿El fichero ya existe,deseas reemplazarlo?", "Guardar PDF", JOptionPane.YES_NO_OPTION);
                    if (JOptionPane.OK_OPTION == decision) {
                        newRuta = ruta;
                    }
                } else {
                    newRuta = ruta;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return newRuta;
    }

    private String devolverExtension(String pdf) {
        String extension = pdf.substring(pdf.length() - 4, pdf.length());
        if (".pdf".equals(extension)) {
            return pdf;
        } else {
            return pdf + ".pdf";
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrameSimplex.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameSimplex.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameSimplex.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameSimplex.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrameSimplex().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCrearPdf;
    private javax.swing.JButton btnGenerar;
    private javax.swing.JButton btnIteraccion;
    private javax.swing.JButton btnTablaU;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblFondo;
    private javax.swing.JLabel lblFuncionObjetivo;
    private javax.swing.JLabel lblMaxz;
    private javax.swing.JLabel lblNumIteracion;
    private javax.swing.JLabel lblRestricciones;
    private javax.swing.JLabel lblVariables;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelRestrFunc;
    private javax.swing.JPanel panelTablaInicial;
    private javax.swing.JScrollPane scrollSR;
    private javax.swing.JScrollPane scrollTFuncion;
    private javax.swing.JScrollPane scroll_TInicial;
    private javax.swing.JTable tblFuncion;
    private javax.swing.JTable tblSR;
    private javax.swing.JTable tbl_Inicial;
    private javax.swing.JTextField txtRestricciones;
    private javax.swing.JTextField txtVariables;
    // End of variables declaration//GEN-END:variables
}
