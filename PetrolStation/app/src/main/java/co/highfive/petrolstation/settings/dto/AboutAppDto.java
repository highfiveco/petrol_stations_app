package co.highfive.petrolstation.settings.dto;

import com.google.gson.annotations.SerializedName;

public class AboutAppDto {
    @SerializedName("id") public int id;
    @SerializedName("title") public String title;
    @SerializedName("details") public String details; // HTML
}
