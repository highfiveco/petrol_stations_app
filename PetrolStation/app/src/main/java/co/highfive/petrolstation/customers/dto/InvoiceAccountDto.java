package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class InvoiceAccountDto {
    @SerializedName("id") public Integer id;
    @SerializedName("account_name") public String account_name;
    @SerializedName("mobile") public String mobile;

    @SerializedName("pdf_url") public String pdf_url;
    @SerializedName("excel_url") public String excel_url;
}
