package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users_lite")
public class UserLiteEntity {
    @PrimaryKey public int id;

    public String name;
}
