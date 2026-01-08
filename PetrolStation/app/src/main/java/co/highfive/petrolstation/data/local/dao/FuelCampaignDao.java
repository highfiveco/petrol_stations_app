package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import java.util.List;
import co.highfive.petrolstation.data.local.entities.FuelCampaignEntity;

@Dao
public interface FuelCampaignDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<FuelCampaignEntity> list);

    @Query("SELECT * FROM fuel_campaigns ORDER BY id DESC")
    List<FuelCampaignEntity> getAll();

    @Query("DELETE FROM fuel_campaigns")
    void clear();
}
