package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.models.Transactions;

public class CustomerFinancialAccountResponse {

    @SerializedName("account")
    public Account account;

    @SerializedName("delete_financial")
    public int delete_financial;

    @SerializedName("transactions")
    public TransactionsPage transactions;

    @SerializedName("setting")
    public Setting setting;

    public static class TransactionsPage {

        @SerializedName("current_page")
        public int current_page;

        @SerializedName("data")
        public List<Transactions> data;

        @SerializedName("last_page")
        public int last_page;

        @SerializedName("next_page_url")
        public String next_page_url;

        @SerializedName("prev_page_url")
        public String prev_page_url;

        @SerializedName("per_page")
        public int per_page;

        @SerializedName("total")
        public int total;
    }
}
