package co.highfive.petrolstation.network;

import com.google.gson.Gson;

public class RefreshDataParser {

    private final Gson gson = new Gson();

    public BaseResponse<RefreshDataResponseData> parse(String json) {
        java.lang.reflect.Type type =
                new com.google.gson.reflect.TypeToken<BaseResponse<RefreshDataResponseData>>() {}.getType();

        return gson.fromJson(json, type);
    }
}
