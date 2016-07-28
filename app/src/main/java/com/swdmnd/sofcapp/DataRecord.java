package com.swdmnd.sofcapp;

/**
 * Created by Arief on 5/17/2016.
 */
public class DataRecord {
    // Column names
    public static final int ID = 0;
    public static final int DATE = 1;
    public static final int TIME = 2;
    public static final int VOLTAGE = 3;
    public static final int CURRENT = 4;
    public static final int TEMPERATURE = 5;
    public static final int RESISTANCE = 6;
    private Integer id;
    private String date;
    private String time;
    private Double voltage;
    private Double current;
    private Double temperature;
    private Double resistance;

    public DataRecord(){}

    public DataRecord(Integer id, String date, String time, Double voltage, Double current, Double temperature,  Double resistance){
        this.id = id;
        this.date = date;
        this.time = time;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
        this.resistance = resistance;
    }

    public Integer getId(){
        return this.id;
    }

    public String getDate(){
        return this.date;
    }

    public String getTime(){
        return this.time;
    }

    public Double getVoltage(){
        return this.voltage;
    }

    public Double getCurrent(){
        return this.current;
    }

    public Double getTemperature(){
        return this.temperature;
    }

    public Double getResistance() { return this.resistance; }
}
