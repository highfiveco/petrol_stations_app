package co.highfive.petrolstation.listener;

public interface CustomerListener {
    void setCustomerFilter(String name_val,String drum_number_val,String region1_id_val, String region2_id_val,String order_val,String balance,boolean isCollected,String status_id_val);

}
