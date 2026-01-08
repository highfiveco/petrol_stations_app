package co.highfive.petrolstation.network;

import com.google.gson.annotations.SerializedName;

public class PaginationLinkDto {
    @SerializedName("url") public String url;
    @SerializedName("label") public String label;
    @SerializedName("active") public Boolean active;
}
