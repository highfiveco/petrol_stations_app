package co.highfive.petrolstation.network;

import java.util.HashMap;
import java.util.Map;

public class ApiParams {

    private final Map<String, String> params = new HashMap<>();

    public ApiParams add(String key, String value) {
        if (key != null && value != null) {
            params.put(key, value);
        }
        return this;
    }

    public Map<String, String> build() {
        return params;
    }
}
