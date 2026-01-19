package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PosItemsPage {

    @SerializedName("current_page")
    public int currentPage;

    @SerializedName("data")
    public List<PosItemDto> data;

    @SerializedName("last_page")
    public int lastPage;

    @SerializedName("next_page_url")
    public String nextPageUrl;

    @SerializedName("prev_page_url")
    public String prevPageUrl;

    @SerializedName("per_page")
    public int perPage;

    @SerializedName("total")
    public int total;
}
