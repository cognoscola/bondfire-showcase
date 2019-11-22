package com.bondfire.app.android.data;

public class Definition {

    private String type;
    private String name;
    private String stringValue;
    private byte byteValue;
    private short shortValue;
    private int intValue;
    private long longValue;
    private float floatValue;
    private double doubleValue;
    private boolean booleanValue;


    public Definition(String type, String name, String value){
        this.name = name;
        this.type = type;
        this.stringValue = value;
    }
    public Definition(String type, String name, byte value){
        this.name = name;
        this.type = type;
        this.byteValue = value;
    }
    public Definition(String type, String name, short value){
        this.name = name;
        this.type = type;
        this.shortValue = value;
    }
    public  Definition(String type, String name, int value){
        this.name = name;
        this.type = type;
        this.intValue = value;
    }
    public  Definition(String type, String name, float value){
        this.name = name;
        this.type = type;
        this.floatValue = value;
    }
    public  Definition(String type, String name, double value){
        this.name = name;
        this.type = type;
        this.doubleValue = value;
    }
    public Definition(String type, String name, boolean value){
        this.name = name;
        this.type = type;
        this.booleanValue = value;
    }
    public String Name(){
        return this.name;
    }

    public String type(){
        return this.type;
    }
}
