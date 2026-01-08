package co.highfive.petrolstation.settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import co.highfive.petrolstation.data.local.dao.GetSettingCacheDao;
import co.highfive.petrolstation.data.local.entities.GetSettingCacheEntity;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.settings.dto.GetSettingData;

public class GetSettingRepository {

    private final GetSettingCacheDao dao;
    private final Gson gson;

    public GetSettingRepository(GetSettingCacheDao dao, Gson gson) {
        this.dao = dao;
        this.gson = gson;
    }

    /** خزّن JSON كما وصل من السيرفر (BaseResponse<GetSettingData>) */
    public void saveRawResponseJson(String rawJson) {
        GetSettingCacheEntity entity = new GetSettingCacheEntity();
        entity.id = 1;
        entity.json = rawJson;
        entity.updatedAt = System.currentTimeMillis();
        dao.upsert(entity);
    }

    public String getCachedRaw() {
        GetSettingCacheEntity entity = dao.get();
        return entity != null ? entity.json : null;
    }

    public GetSettingData getCachedData() {
        GetSettingCacheEntity entity = dao.get();
        if (entity == null || entity.json == null || entity.json.trim().isEmpty()) return null;

        Type type = new TypeToken<BaseResponse<GetSettingData>>() {}.getType();
        BaseResponse<GetSettingData> base = gson.fromJson(entity.json, type);
        return base != null ? base.data : null;
    }

    public long getCachedUpdatedAt() {
        GetSettingCacheEntity entity = dao.get();
        return entity != null ? entity.updatedAt : 0L;
    }

    public void clearCache() {
        dao.clear();
    }
}
