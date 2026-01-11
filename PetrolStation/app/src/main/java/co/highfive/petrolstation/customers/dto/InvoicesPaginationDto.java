package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InvoicesPaginationDto {
    @SerializedName("current_page") public Integer current_page;
    @SerializedName("data") public List<InvoiceDto> data;

    @SerializedName("last_page") public Integer last_page;
    @SerializedName("next_page_url") public String next_page_url;
    @SerializedName("prev_page_url") public String prev_page_url;

    @SerializedName("total") public Integer total;
    @SerializedName("per_page") public Integer per_page;
}
