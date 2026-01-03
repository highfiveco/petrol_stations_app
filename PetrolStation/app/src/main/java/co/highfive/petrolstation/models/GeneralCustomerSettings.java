package co.highfive.petrolstation.models;

import java.util.ArrayList;

public class GeneralCustomerSettings {
    private CustomerSetting setting;
    private ArrayList<CustomerStatus> type_customer_pay;
    private ArrayList<CustomerStatus> accounting_type;
    private ArrayList<CustomerStatus> customer_status;
    private ArrayList<CustomerStatus> subscription_type;
    private ArrayList<CustomerStatus> electrical_breakers;
    private ArrayList<CustomerStatus> tablon;
    private ArrayList<Area> first_area;
    private ArrayList<Constant> users;
    private ArrayList<Constant> type_maintenances;
    private ArrayList<Constant> status_maintenances;
    private ArrayList<Constant> orders;

    public CustomerSetting getSetting() {
        return setting;
    }

    public void setSetting(CustomerSetting setting) {
        this.setting = setting;
    }

    public ArrayList<CustomerStatus> getCustomer_status() {
        return customer_status;
    }

    public void setCustomer_status(ArrayList<CustomerStatus> customer_status) {
        this.customer_status = customer_status;
    }

    public ArrayList<CustomerStatus> getSubscription_type() {
        return subscription_type;
    }

    public void setSubscription_type(ArrayList<CustomerStatus> subscription_type) {
        this.subscription_type = subscription_type;
    }

    public ArrayList<CustomerStatus> getElectrical_breakers() {
        return electrical_breakers;
    }

    public void setElectrical_breakers(ArrayList<CustomerStatus> electrical_breakers) {
        this.electrical_breakers = electrical_breakers;
    }

    public ArrayList<CustomerStatus> getTablon() {
        return tablon;
    }

    public void setTablon(ArrayList<CustomerStatus> tablon) {
        this.tablon = tablon;
    }

    public ArrayList<Area> getFirst_area() {
        return first_area;
    }

    public void setFirst_area(ArrayList<Area> first_area) {
        this.first_area = first_area;
    }

    public ArrayList<Constant> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<Constant> users) {
        this.users = users;
    }

    public ArrayList<CustomerStatus> getType_customer_pay() {
        return type_customer_pay;
    }

    public void setType_customer_pay(ArrayList<CustomerStatus> type_customer_pay) {
        this.type_customer_pay = type_customer_pay;
    }

    public ArrayList<CustomerStatus> getAccounting_type() {
        return accounting_type;
    }

    public void setAccounting_type(ArrayList<CustomerStatus> accounting_type) {
        this.accounting_type = accounting_type;
    }

    public ArrayList<Constant> getType_maintenances() {
        return type_maintenances;
    }

    public void setType_maintenances(ArrayList<Constant> type_maintenances) {
        this.type_maintenances = type_maintenances;
    }

    public ArrayList<Constant> getStatus_maintenances() {
        return status_maintenances;
    }

    public void setStatus_maintenances(ArrayList<Constant> status_maintenances) {
        this.status_maintenances = status_maintenances;
    }

    public ArrayList<Constant> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Constant> orders) {
        this.orders = orders;
    }
}
