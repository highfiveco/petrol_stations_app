package co.highfive.petrolstation.pos;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.pos.dto.PosAddItemDto;
import co.highfive.petrolstation.pos.dto.PosPaymentMethodDto;

public class PosRequests {

    public static class BuiltForm {
        public final ArrayList<String> keys;
        public final ArrayList<String> values;

        public BuiltForm(ArrayList<String> keys, ArrayList<String> values) {
            this.keys = keys;
            this.values = values;
        }
    }

    public static BuiltForm buildPosAddForm(
            int accountId,
            List<PosAddItemDto> items,
            List<PosPaymentMethodDto> paymentMethods,
            String notes,
            Gson gson
    ) {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        // arrays: item_id[] price[] count[]
        if (items != null) {
            for (PosAddItemDto it : items) {
                keys.add("item_id[]"); values.add(String.valueOf(it.itemId));
                keys.add("price[]");   values.add(String.valueOf(it.price));
                keys.add("count[]");   values.add(String.valueOf(it.count));
            }
        }

        keys.add("account_id");
        values.add(String.valueOf(accountId));

        // payment_methods JSON string
        keys.add("payment_methods");
        values.add(gson.toJson(paymentMethods != null ? paymentMethods : new ArrayList<>()));

        // notes (اختياري)
        if (notes != null) {
            keys.add("notes");
            values.add(notes);
        }

        return new BuiltForm(keys, values);
    }
}
