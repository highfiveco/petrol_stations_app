package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CustomerDto {

    @SerializedName("id") public int id;
    @SerializedName("type_customer") public Integer type_customer;
    @SerializedName("customer_classify") public Integer customer_classify;
    @SerializedName("name") public String name;
    @SerializedName("account_id") public Integer account_id;
    @SerializedName("mobile") public String mobile;
    @SerializedName("status") public Integer status;


    @SerializedName("customer_status") public String customer_status;
    @SerializedName("customer_classify_name") public String customer_classify_name;
    @SerializedName("type_customer_name") public String type_customer_name;

    @SerializedName("balance") public Double balance;
    @SerializedName("campaign_name") public String campaign_name;
    @SerializedName("remaining_amount") public Double remaining_amount;

    // ===== offline extras =====
    @SerializedName("vehicles") public List<CustomerVehicleDto> vehicles;

    @SerializedName("fuel_invoices") public List<InvoiceDto> fuel_invoices;
    @SerializedName("invoices") public List<InvoiceDto> invoices;

    @SerializedName("total_invoices_count") public Integer total_invoices_count;

    @SerializedName("address")
    public String address;

    @SerializedName("asseal_no")
    public String asseal_no;

    @SerializedName("currency_name")
    public String currency_name;

    @SerializedName("account_currency")
    public String account_currency;
}
