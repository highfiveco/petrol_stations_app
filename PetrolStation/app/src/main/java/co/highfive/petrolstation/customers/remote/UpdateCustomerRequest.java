package co.highfive.petrolstation.customers.remote;

public class UpdateCustomerRequest {
    public int id;                  // required
    public String name;             // required
    public String mobile;           // optional/required حسب السيرفر
    public Integer status;          // 1/2
    public Integer type_customer;
    public Integer customer_classify;
    public String address;
    public String asseal_no;
}
