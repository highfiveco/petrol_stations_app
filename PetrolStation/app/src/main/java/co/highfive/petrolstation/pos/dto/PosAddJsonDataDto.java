package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;

public class PosAddJsonDataDto {

    @SerializedName("success_count")
    public int success_count;

    @SerializedName("fail_count")
    public int fail_count;

    @SerializedName("added_invoices")
    public List<InvoiceDto> added_invoices;

    @SerializedName("errors")
    public List<PosAddJsonErrorDto> errors;
}
