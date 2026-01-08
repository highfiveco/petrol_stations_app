package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;

public class FuelPriceAddJsonResponseData {

    @SerializedName("success_count")
    public Integer success_count;

    @SerializedName("fail_count")
    public Integer fail_count;

    @SerializedName("added_invoices")
    public List<InvoiceDto> added_invoices;

    @SerializedName("errors")
    public List<FuelPriceAddJsonErrorDto> errors;
}
