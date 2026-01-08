package co.highfive.petrolstation.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaginatedResponse<T> {

    @SerializedName("current_page")
    public Integer current_page;

    @SerializedName("data")
    public List<T> data;

    @SerializedName("first_page_url")
    public String first_page_url;

    @SerializedName("from")
    public Integer from;

    @SerializedName("last_page")
    public Integer last_page;

    @SerializedName("last_page_url")
    public String last_page_url;

    @SerializedName("links")
    public List<PaginationLinkDto> links;

    @SerializedName("next_page_url")
    public String next_page_url;

    @SerializedName("path")
    public String path;

    @SerializedName("per_page")
    public Integer per_page;

    @SerializedName("prev_page_url")
    public String prev_page_url;

    @SerializedName("to")
    public Integer to;

    @SerializedName("total")
    public Integer total;
}
