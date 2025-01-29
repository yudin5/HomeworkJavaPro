package main.java.homework_1.annotations.support;

/**
 * Вспомогательный класс для хранения распарсенных значений
 */
public class CsvSourceData {

    private final int a;
    private final String b;
    private final int c;
    private final boolean d;

    public CsvSourceData(final int a, final String b, final int c, final boolean d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public int getA() {
        return a;
    }

    public String getB() {
        return b;
    }

    public int getC() {
        return c;
    }

    public boolean getD() {
        return d;
    }
}
