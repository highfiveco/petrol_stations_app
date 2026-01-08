package co.highfive.petrolstation.network;

import java.util.ArrayList;
import java.util.List;

public class ParamsList {
    public final ArrayList<String> keys = new ArrayList<>();
    public final ArrayList<String> values = new ArrayList<>();

    public ParamsList add(String k, Object v) {
        if (k == null) return this;
        keys.add(k);
        values.add(v == null ? "" : String.valueOf(v));
        return this;
    }

    public ParamsList addRepeated(String key, List<?> list) {
        if (key == null || list == null) return this;
        for (Object v : list) add(key, v);
        return this;
    }
}
