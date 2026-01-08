package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PagedResponse<T> {
    @SerializedName("current_page") public int current_page;
    @SerializedName("data") public List<T> data;

    @SerializedName("first_page_url") public String first_page_url;
    @SerializedName("last_page") public int last_page;
    @SerializedName("last_page_url") public String last_page_url;

    @SerializedName("next_page_url") public String next_page_url;
    @SerializedName("prev_page_url") public String prev_page_url;

    @SerializedName("per_page") public int per_page;
    @SerializedName("total") public int total;
}
