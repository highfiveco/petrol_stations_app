package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;


public class FuelSalesPagingDto {

    @SerializedName("current_page")
    public int current_page;

    // ✅ هذا هو سبب الخطأ: لازم List مش Object
    @SerializedName("data")
    public List<FuelSaleDto> data;

    @SerializedName("first_page_url")
    public String first_page_url;

    @SerializedName("from")
    public Integer from;

    @SerializedName("last_page")
    public int last_page;

    @SerializedName("last_page_url")
    public String last_page_url;

    @SerializedName("next_page_url")
    public String next_page_url;

    @SerializedName("path")
    public String path;

    @SerializedName("per_page")
    public int per_page;

    @SerializedName("prev_page_url")
    public String prev_page_url;

    @SerializedName("to")
    public Integer to;

    @SerializedName("total")
    public int total;
}
