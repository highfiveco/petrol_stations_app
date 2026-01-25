package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pumps")
public class PumpEntity {
    @PrimaryKey public int id;
    public String name;
}
