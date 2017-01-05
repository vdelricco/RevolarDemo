package com.delricco.vince.revolardemo;

public class RevolarContact {

    private String name;
    private String number;

    private RevolarContact() {}

    public RevolarContact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return name;
    }
}
