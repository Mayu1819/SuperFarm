package com.example.superfarm.models;


public enum ENUM_Days {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String strVersion;

    ENUM_Days(String strVersion) {
        this.strVersion = strVersion;
    }

    @Override
    public String toString() {
        return strVersion;
    }
}
