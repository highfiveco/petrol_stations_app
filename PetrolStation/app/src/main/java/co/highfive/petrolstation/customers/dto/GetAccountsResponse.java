package co.highfive.petrolstation.customers.dto;

import java.util.List;

import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.Setting;

public class GetAccountsResponse {
    public int add_financial;
    public int view_financial_move;
    public int update_account;
    public int print_financial_move;

    public Setting setting;
    public Paged<Account> accounts;

    public static class Paged<T> {
        public int current_page;
        public List<T> data;
        public int last_page;
        public String next_page_url;
        public String prev_page_url;
        public int total;
        public int per_page;
    }
}
