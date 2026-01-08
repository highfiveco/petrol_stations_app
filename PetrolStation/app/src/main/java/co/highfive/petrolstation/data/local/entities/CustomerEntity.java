package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "customers",
        indices = {
                @Index(value = {"name"}),
                @Index(value = {"mobile"})
        }
)
public class CustomerEntity {

    @PrimaryKey
    public int id;

    public Integer typeCustomer;
    public Integer customerClassify;

    public String name;
    public Integer accountId;
    public String mobile;

    public Integer status;
    public String customerStatus;

    public String customerClassifyName;
    public String typeCustomerName;

    public Double balance;

    public String campaignName;
    public Double remainingAmount;

    public String address;
    public String assealNo;

    public String accountNo;

}
