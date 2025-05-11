package br.java.vt.infrastructure;

public enum DataEnum {

    DATA_1("Data 1"),
    DATA_2("Data 2"),
    DATA_3("Data 3"),
    DATA_4("Data 4"),
    DATA_5("Data 5");

    private final String value;

    DataEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
