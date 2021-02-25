package principal;



public class Fraccion {

    private int numerador;
    private int denominador;

    public Fraccion() {
        this.numerador = 0;
        this.denominador = 0;
    }

    public Fraccion(int numerador, int denominador) {
        if (denominador != 0) {
            this.numerador = numerador;
            this.denominador = denominador;
        }
    }

    /*Metodos get y set */
    public int getNumerador() {
        return numerador;
    }

    public void setNumerador(int numerador) {
        this.numerador = numerador;
    }

    public int getDenominador() {
        return denominador;
    }

    public void setDenominador(int denominador) {
        this.denominador = denominador;
    }

    /*Operaciones Basicas: SUMA, RESTA, MULTIPLICACION Y DIVISION*/

    public Fraccion sumar(Fraccion f) {
        return new Fraccion(f.getNumerador() * getDenominador() + f.getDenominador() * getNumerador(), f.getDenominador() * getDenominador());
    }

    public Fraccion restar(Fraccion f) {
        return new Fraccion((getNumerador() * f.getDenominador()) - (getDenominador() * f.getNumerador()), f.getDenominador() * getDenominador());
    }

    public Fraccion multiplicar(Fraccion f) {
        return new Fraccion(f.getNumerador() * getNumerador(), f.getDenominador() * getDenominador());
    }

    public Fraccion dividir(Fraccion f) {
        return new Fraccion(getNumerador() * f.getDenominador(), f.getNumerador() * getDenominador());
    }

    /*Metodos para simplificar la Fraccion resultante */
    private int mcd(Fraccion f) {

        int aux_num, aux_den, mcd;
        int num = f.getNumerador();
        int den = f.getDenominador();

        if (num != 0) {

            if (num < 0) {
                num = -1 * num;
            }
            if (den < 0) {
                den = -1 * den;
            }

            if (num > den) {
                aux_num = num;
                aux_den = den;
            } else {
                aux_num = den;
                aux_den = num;
            }

            mcd = aux_den;
            while (aux_den != 0) {
                mcd = aux_den;
                aux_den = aux_num % aux_den;
                aux_num = mcd;
            }

        } else {
            mcd = 1;
        }
        return mcd;

    }

    public Fraccion simplificar(Fraccion f) {

        int num = mcd(f);
        f.setNumerador(f.getNumerador() / num);
        f.setDenominador(f.getDenominador() / num);
        if (f.getNumerador() < 0 && f.getDenominador() < 0) {
            f.setNumerador(-1 * f.getNumerador());
            f.setDenominador(-1 * f.getDenominador());
        } else if (f.getNumerador() > -1 && f.getDenominador() < 0) {
            f.setNumerador(-1 * f.getNumerador());
            f.setDenominador(-1 * f.getDenominador());
        }
        return f;
    }

    public double toDecimal() {
        return (double) this.getNumerador() / (double) this.getDenominador();
    }

    public int posicionBarra(String cadena) {
        return cadena.indexOf("/");
    }

    public Fraccion deTablaFraccion(String fraccionString) {
        Fraccion newFraccion;
        int posicionBarra = posicionBarra(fraccionString);
        String numerador = "", denominador = "";
        if (posicionBarra == -1) {
            newFraccion = new Fraccion(Integer.parseInt(fraccionString), 1);// ?/1
            return newFraccion;

        } else {
            for (int i = 0; i < posicionBarra; i++) {
                numerador += fraccionString.charAt(i);
            }
            for (int j = posicionBarra + 1; j < fraccionString.length(); j++) {
                denominador += fraccionString.charAt(j);
            }
            newFraccion = new Fraccion(Integer.parseInt(numerador), Integer.parseInt(denominador));
            return newFraccion;
        }

    }

    public Fraccion toFraccion(double decimal) {
        Fraccion newFraccion;
        String partInt = getPartInt(String.valueOf(decimal));
        String partDecimal = getPartDecimal(String.valueOf(decimal));

        int n = Integer.parseInt(partInt);
        int d = Integer.parseInt(partDecimal);
        int factorMultiplicador = factorMultiplicador(partDecimal.length());
        //System.out.println("factorMultiplicador: " + factorMultiplicador);

        if (n == 0) {
            newFraccion = new Fraccion(d, factorMultiplicador);
        } else {
            Fraccion f1 = new Fraccion(n, 1);
            Fraccion f2 = new Fraccion(d, factorMultiplicador);
            newFraccion = f1.sumar(f2);
            newFraccion = simplificar(newFraccion);
        }

        return newFraccion;

    }

    public String getPartInt(String val) {
        int i = getPuntoDecimal(val);
        return val.substring(0, i);
    }

    public String getPartDecimal(String val) {
        int i = getPuntoDecimal(val);
        return val.substring(i + 1, val.length());
    }

    private int getPuntoDecimal(String val) {
        int i = 0;
        while (val.charAt(i) != '.') {
            i++;
            if (i == val.length()) {
                break;
            }
        }
        return i;
    }

    private int factorMultiplicador(int longitud) {

        String factorMultiplicador = "1";

        for (int i = 0; i < longitud; i++) {
            factorMultiplicador = factorMultiplicador + "0";
        }

        return Integer.parseInt(factorMultiplicador);

    }

    /*Funcion toString para devolver el resultado obtenido de las operaciones*/
    @Override
    public String toString() {

        if (this.getNumerador() != 0 && this.getDenominador() != 0) {
            simplificar(this);
            if (this.getDenominador() == 1) {
                return String.valueOf(getNumerador());
            } else {
                return getNumerador() + "/" + getDenominador();
            }
        } else {
            return "0";
        }

    }

}
