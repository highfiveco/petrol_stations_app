package co.highfive.petrolstation.customers.dto;

import java.util.List;

import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.CustomerLog;
import co.highfive.petrolstation.models.Setting;

public class CustomerLogResponse {
    public Account customer;
    public Paged<CustomerLog> logs;
    public Setting setting;

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
