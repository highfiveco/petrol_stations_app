package co.highfive.petrolstation.listener;

public interface AddFinanceAccountListener {
    void addFinanceAccount(String type_id,String account_type_id,String select_customer_id,String select_user_id,String account_name,String currency_id,String notes);
}
