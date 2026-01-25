package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.CampaignEntity;

@Dao
public interface CampaignDao {

    @Upsert
    void upsert(CampaignEntity entity);

    @Upsert
    void upsertAll(List<CampaignEntity> list);

    @Query("SELECT * FROM campaigns ORDER BY name ASC")
    List<CampaignEntity> getAll();

    @Query("SELECT * FROM campaigns WHERE id = :id LIMIT 1")
    CampaignEntity getById(int id);

    @Query("DELETE FROM campaigns")
    void clear();
}
