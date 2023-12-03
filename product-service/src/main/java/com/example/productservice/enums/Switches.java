package com.example.productservice.enums;


public enum Switches {
    // GATERON SWITCHES
    GRED("Gateron Red"),
    GBROWN("Gateron Brown"),
    GBLUE("Gateron Blue"),
    GBLACK("Gateron Black"),
    GYELLOW("Gateron Yellow"),
    GGREEN("Gateron Green"),
    GWHITE("Gateron White"),

    // CHERRY MX SWITCHES
    MXRED("Cherry MX Red"),
    MXBLACK("Cherry MX Black"),
    MXSILVER("Cherry MX Speed Silver"),
    MXSILENTRED("Cherry MX Silent Red"),
    MXGREEN("Cherry MX Green"),
    MXBROWN("Cherry MX Brown"),
    MXBLUE("Cherry MX Blue");


    public final String elaboration;

    private Switches(String elaboration){
        this.elaboration = elaboration;
    }
}
