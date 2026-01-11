package co.highfive.petrolstation.models;

import java.util.ArrayList;

public class CustomersSettingData {

    private Setting setting;

    private ArrayList<CustomerStatus> customer_status;
    private ArrayList<CustomerStatus> customer_classify;
    private ArrayList<CustomerStatus> type_customer;

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public ArrayList<CustomerStatus> getCustomer_status() {
        return customer_status;
    }

    public void setCustomer_status(ArrayList<CustomerStatus> customer_status) {
        this.customer_status = customer_status;
    }

    public ArrayList<CustomerStatus> getCustomer_classify() {
        return customer_classify;
    }

    public void setCustomer_classify(ArrayList<CustomerStatus> customer_classify) {
        this.customer_classify = customer_classify;
    }

    public ArrayList<CustomerStatus> getType_customer() {
        return type_customer;
    }

    public void setType_customer(ArrayList<CustomerStatus> type_customer) {
        this.type_customer = type_customer;
    }
}
