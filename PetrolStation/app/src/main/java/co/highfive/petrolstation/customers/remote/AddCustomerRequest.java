package co.highfive.petrolstation.customers.remote;

public class AddCustomerRequest {
    public String name;              // required
    public String mobile;            // optional/required حسب السيرفر
    public Integer customer_classify;
    public Integer type_customer;
    public Integer status;
    public String address;
    public String asseal_no;
}
