package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_payment_types")
public class FuelPaymentTypeEntity {
    @PrimaryKey public int id;

    public String value2;
    public String name;
    public String enName;
}
