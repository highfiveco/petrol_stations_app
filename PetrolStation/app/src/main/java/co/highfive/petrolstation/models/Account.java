package co.highfive.petrolstation.models;

import com.google.gson.annotations.SerializedName;

public class Account {

    @SerializedName("id")
    private String id;

    @SerializedName("account_name")
    private String account_name;


    @SerializedName("name_currency")
    private String name_currency;

    @SerializedName("account_currency")
    private String account_currency;

    @SerializedName("name")
    private String name;

    @SerializedName("account_type")
    private String account_type;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("balance")
    private Double balance;

    @SerializedName("depit")
    private Double depit;

    @SerializedName("credit")
    private Double credit;

    @SerializedName("pdf_url")
    private String pdf_url;

    @SerializedName("excel_url")
    private String excel_url;

    public String getId() { return id; }
    public String getAccount_name() { return account_name; }
    public String getMobile() { return mobile; }
    public Double getBalance() { return balance; }
    public String getPdf_url() { return pdf_url; }
    public String getExcel_url() { return excel_url; }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_currency() {
        return name_currency;
    }

    public void setName_currency(String name_currency) {
        this.name_currency = name_currency;
    }

    public Double getDepit() {
        return depit;
    }

    public void setDepit(Double depit) {
        this.depit = depit;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public String getAccount_currency() {
        return account_currency;
    }

    public void setAccount_currency(String account_currency) {
        this.account_currency = account_currency;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setId(String id) {
        this.id = id;
    }
}
