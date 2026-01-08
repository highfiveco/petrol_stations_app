package co.highfive.petrolstation.models;

import com.google.gson.annotations.SerializedName;

public class Transactions {

    @SerializedName("id")
    private String id;

    @SerializedName("type_statement")
    private String type_statement;

    @SerializedName("created_at")
    private String created_at;

    @SerializedName("currency_name")
    private String currency_name;

    @SerializedName("amount")
    private Double amount; // IMPORTANT: number

    @SerializedName("is_print")
    private String is_print; // sometimes 0/1 as number or string -> keep String

    public String getId() { return id; }
    public String getType_statement() { return type_statement; }
    public String getCreated_at() { return created_at; }
    public String getCurrency_name() { return currency_name; }
    public Double getAmount() { return amount; }
    public String getIs_print() { return is_print; }
}
