package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers.dto.InvoiceDto;

public class PosInvoicesOfflineDataDto {

    @SerializedName("fuel_invoices")
    public List<InvoiceDto> fuel_invoices;

    @SerializedName("invoices")
    public List<InvoiceDto> invoices;

    @SerializedName("setting")
    public SettingDto setting;
}
