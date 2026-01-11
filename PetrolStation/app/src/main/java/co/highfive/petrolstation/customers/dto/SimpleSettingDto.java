package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class SimpleSettingDto {

    @SerializedName("id")
    public int id;

    @SerializedName("value2")
    public String value2;

    @SerializedName("name")
    public String name;

    @SerializedName("en_name")
    public String en_name;

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
