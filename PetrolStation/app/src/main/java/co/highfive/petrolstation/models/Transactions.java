package co.highfive.petrolstation.models;

import com.google.gson.annotations.SerializedName;

public class Transactions {

    @SerializedName("id")
    private String id;

    @SerializedName("notes")
    private String notes;

    @SerializedName("currency_name")
    private String currency_name;

    @SerializedName("sanad_type")
    private Integer sanad_type;

    @SerializedName("sanad_id")
    private Integer sanad_id;

    @SerializedName("sanad_no")
    private String sanad_no;

    // بعض السيرفرات ترجع sand_no بدل sanad_no
    @SerializedName("sand_no")
    private String sand_no;

    @SerializedName("exchange_rate")
    private Double exchange_rate;

    @SerializedName("total_price")
    private Double total_price;

    @SerializedName("type")
    private Integer type;

    @SerializedName("action_type")
    private Integer action_type;

    @SerializedName("payment_name")
    private String payment_name;

    @SerializedName("amount")
    private Double amount;

    @SerializedName("bank_name")
    private String bank_name;

    @SerializedName("account_id")
    private Integer account_id;

    @SerializedName("bank_id")
    private Integer bank_id;

    @SerializedName("cheque_no")
    private String cheque_no;

    @SerializedName("due_date")
    private String due_date;

    @SerializedName("account_name")
    private String account_name;

    @SerializedName("type_statement")
    private String type_statement;

    @SerializedName("created_at")
    private String created_at;

    @SerializedName("title")
    private String title;

    @SerializedName("printed_by")
    private String printed_by;

    @SerializedName("printed_at")
    private String printed_at;

    @SerializedName("balance")
    private Double balance;

    @SerializedName("amountLetter")
    private String amountLetter;

    // sometimes 0/1 as number or string -> keep String
    @SerializedName("is_print")
    private String is_print;

    // ======================
    // Getters
    // ======================
    public String getId() { return id; }
    public String getNotes() { return notes; }
    public String getCurrency_name() { return currency_name; }

    public Integer getSanad_type() { return sanad_type; }
    public Integer getSanad_id() { return sanad_id; }

    public String getSanad_no() { return sanad_no; }
    public String getSand_no() { return sand_no; }

    public Double getExchange_rate() { return exchange_rate; }
    public Double getTotal_price() { return total_price; }

    public Integer getType() { return type; }
    public Integer getAction_type() { return action_type; }

    public String getPayment_name() { return payment_name; }

    public Double getAmount() { return amount; }

    public String getBank_name() { return bank_name; }
    public Integer getAccount_id() { return account_id; }
    public Integer getBank_id() { return bank_id; }

    public String getCheque_no() { return cheque_no; }
    public String getDue_date() { return due_date; }

    public String getAccount_name() { return account_name; }
    public String getType_statement() { return type_statement; }
    public String getCreated_at() { return created_at; }

    public String getTitle() { return title; }

    public String getPrinted_by() { return printed_by; }
    public String getPrinted_at() { return printed_at; }

    public Double getBalance() { return balance; }

    public String getAmountLetter() { return amountLetter; }

    public String getIs_print() { return is_print; }

    // ======================
    // Helpers (اختياري)
    // ======================
    public String getBestSanadNo() {
        if (sanad_no != null && !sanad_no.trim().isEmpty()) return sanad_no;
        return sand_no;
    }
}
