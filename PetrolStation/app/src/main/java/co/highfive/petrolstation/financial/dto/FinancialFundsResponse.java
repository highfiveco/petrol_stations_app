package co.highfive.petrolstation.financial.dto;

import java.util.List;

import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Fund;
import co.highfive.petrolstation.models.Setting;

/**
 * Response model for Financial Funds API
 */
public class FinancialFundsResponse {

    public int close_fund;
    public int report_fund;
    public int view_users;

    public FundPagination fund;

    public List<Currency> status_fund;
    public List<Currency> currency;
    public List<Currency> users;
    public List<Currency> fund_to;

    public Setting setting;

    /**
     * Pagination wrapper for funds list
     */
    public static class FundPagination {
        public List<Fund> data;
        public int current_page;
        public int last_page;
        public String next_page_url;
        public String prev_page_url;
        public int total;
    }
}
