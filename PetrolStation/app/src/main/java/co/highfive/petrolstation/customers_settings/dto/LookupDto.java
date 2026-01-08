package co.highfive.petrolstation.customers_settings.dto;

import com.google.gson.annotations.SerializedName;

public class LookupDto {
    @SerializedName("id") public int id;
    @SerializedName("value2") public String value2; // ممكن null
    @SerializedName("name") public String name;
    @SerializedName("en_name") public String en_name; // ممكن null
}
