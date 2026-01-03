package co.highfive.petrolstation.models;

import java.util.ArrayList;

public class Area {
    private String id;
    private String type;
    private String value;
    private String value2;
    private String name;
    ArrayList<Area> second_area;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Area> getSecond_area() {
        return second_area;
    }

    public void setSecond_area(ArrayList<Area> second_area) {
        this.second_area = second_area;
    }
}
