package co.highfive.petrolstation.fuelsale.dto;

public class FuelCustomerDto {
    public int id;
    public String name;
    public int account_id;
    public String campaign_name;
    public double remaining_amount;

    public boolean is_offline = false;
    public long local_id = 0;


    public String mobile;

}
