package co.highfive.petrolstation.pos;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.pos.dto.PosAddJsonInvoiceDto;

public class PosJsonRequests {

    public static class BuiltForm {
        public final ArrayList<String> keys;
        public final ArrayList<String> values;

        public BuiltForm(ArrayList<String> keys, ArrayList<String> values) {
            this.keys = keys;
            this.values = values;
        }
    }

    public static BuiltForm buildPosAddJsonForm(List<PosAddJsonInvoiceDto> invoices, Gson gson) {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        keys.add("data");
        values.add(gson.toJson(invoices != null ? invoices : new ArrayList<>()));

        return new BuiltForm(keys, values);
    }
}
