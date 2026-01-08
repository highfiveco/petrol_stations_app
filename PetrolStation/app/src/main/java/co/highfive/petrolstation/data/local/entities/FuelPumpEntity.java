package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_pumps")
public class FuelPumpEntity {
    @PrimaryKey public int id;

    public String name;
    public String icon;
}
