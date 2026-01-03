package co.highfive.petrolstation.listener;

public interface FinancialFundFilter {
    void filter(String currency,String boxStatus,String userId,String date_from,String date_to);
}
