package co.highfive.petrolstation.pos.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.highfive.petrolstation.databinding.RowPosItemBinding;
import co.highfive.petrolstation.pos.dto.PosItemDto;

public class PosItemsAdapter extends RecyclerView.Adapter<PosItemsAdapter.VH> {

    public interface Listener {
        void onSelectItem(int itemId);      // set qty=1 if not exists
        void onUnselectItem(int itemId);    // remove from map
        void onPlusClicked(int itemId);
        void onMinusClicked(int itemId);
        void onQtyChanged(int itemId, int qty); // set exact qty
        int getQty(int itemId);
    }

    private final Listener listener;
    private final List<PosItemDto> list = new ArrayList<>();
    private final Set<Integer> selectedIds = new HashSet<>();

    public PosItemsAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        PosItemDto item = list.get(position);
        return item != null ? item.id : super.getItemId(position);
    }

    public void submitList(@Nullable List<PosItemDto> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public void notifyQtyChanged(int itemId) {
        for (int i = 0; i < list.size(); i++) {
            PosItemDto it = list.get(i);
            if (it != null && it.id == itemId) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    // ======== أهم دالة: تزامن السلكشن من qtyMap (تستخدمها عند الرجوع من الفواتير النشطة) ========
    public void setSelectionFromQtyMap(@NonNull Map<Integer, Integer> qtyMap) {
        selectedIds.clear();
        for (Map.Entry<Integer, Integer> e : qtyMap.entrySet()) {
            Integer itemId = e.getKey();
            Integer qty = e.getValue();
            if (itemId != null && qty != null && qty > 0) {
                selectedIds.add(itemId);
            }
        }
        notifyDataSetChanged();
    }

    // ======== خيار إضافي: تزامن السلكشن من itemsJson مباشرة ========
    public void setSelectionFromItemsJson(@Nullable String itemsJson) {
        selectedIds.clear();

        if (itemsJson == null || itemsJson.trim().isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        try {
            com.google.gson.JsonArray arr = com.google.gson.JsonParser.parseString(itemsJson).getAsJsonArray();

            for (int i = 0; i < arr.size(); i++) {
                com.google.gson.JsonObject o = arr.get(i).getAsJsonObject();

                int itemId = getIntAny(o, "itemId", "item_id", "id");
                int qty = getIntAny(o, "qty", "quantity", "q");

                if (itemId > 0 && qty > 0) {
                    selectedIds.add(itemId);
                }
            }
        } catch (Exception ignored) {}

        notifyDataSetChanged();
    }

    private int getIntAny(com.google.gson.JsonObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k) && !o.get(k).isJsonNull()) {
                try {
                    return o.get(k).getAsInt();
                } catch (Exception e) {
                    try {
                        return Integer.parseInt(o.get(k).getAsString());
                    } catch (Exception ignored) {}
                }
            }
        }
        return 0;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPosItemBinding b = RowPosItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PosItemDto item = list.get(position);
        if (item == null) return;

        h.b.txtName.setText(item.name == null ? "" : item.name);
        h.b.txtPrice.setText(String.valueOf(item.price));

        // qty دائماً من الـ Activity عبر listener.getQty (من qtyMap)
        int qty = listener == null ? 0 : listener.getQty(item.id);
        h.b.txtQty.setText(String.valueOf(qty));

        // enable/disable minus (إذا بدك تسمح نزول لـ 0 خليها qty > 0)
        boolean canMinus = qty > 1;
        h.b.btnMinus.setEnabled(canMinus);
        h.b.btnMinus.setAlpha(canMinus ? 1f : 0.4f);

        // ====== السلكشن الحقيقي: من selectedIds ======
        boolean selected = selectedIds.contains(item.id);
        h.b.rootContent.setSelected(selected);

        // ===== Toggle select =====
        h.b.rootContent.setOnClickListener(v -> {
            if (selectedIds.contains(item.id)) {
                selectedIds.remove(item.id);
                if (listener != null) listener.onUnselectItem(item.id);
            } else {
                selectedIds.add(item.id);
                if (listener != null) listener.onSelectItem(item.id);
            }
            notifyQtyChanged(item.id);
        });

        // ===== Plus / Minus =====
        h.b.btnPlus.setOnClickListener(v -> {
            if (listener != null) listener.onPlusClicked(item.id);

            // أي زيادة = لازم يظل selected
            selectedIds.add(item.id);

            notifyQtyChanged(item.id);
        });

        h.b.btnMinus.setOnClickListener(v -> {
            if (listener != null) listener.onMinusClicked(item.id);

            // إذا صارت الكمية 0 بسبب decrease() في الـ Activity، شيل السلكشن
            int newQty = listener == null ? 0 : listener.getQty(item.id);
            if (newQty <= 0) selectedIds.remove(item.id);

            notifyQtyChanged(item.id);
        });

        // ===== Inline Qty Edit =====
        h.b.txtQty.setOnClickListener(v -> startQtyEdit(h));

        h.b.txtQty.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                finishQtyEdit(h, item);
                return true;
            }
            return false;
        });

        h.b.txtQty.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && h.isEditingQty) {
                finishQtyEdit(h, item);
            }
        });

        // icon
        if (item.icon != null && !item.icon.trim().isEmpty()) {
            h.b.imgIcon.setVisibility(View.VISIBLE);
            try {
                com.bumptech.glide.Glide.with(h.b.imgIcon.getContext())
                        .load(item.icon)
                        .into(h.b.imgIcon);
            } catch (Exception e) {
                h.b.imgIcon.setVisibility(View.GONE);
            }
        } else {
            h.b.imgIcon.setVisibility(View.GONE);
        }
    }

    private void startQtyEdit(@NonNull VH h) {
        h.isEditingQty = true;

        h.b.txtQty.setCursorVisible(true);
        h.b.txtQty.setFocusable(true);
        h.b.txtQty.setFocusableInTouchMode(true);

        h.b.txtQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        h.b.txtQty.setImeOptions(EditorInfo.IME_ACTION_DONE);
        h.b.txtQty.setSingleLine(true);

        String s = safeTrim(h.b.txtQty.getText());
        if (s.isEmpty() || "0".equals(s)) {
            h.b.txtQty.setText("1");
        }

        Editable ed = h.b.txtQty.getText();
        if (ed != null) {
            h.b.txtQty.setSelection(0, ed.length());
        }

        h.b.txtQty.requestFocus();
        showKeyboard(h.b.txtQty);
    }

    private void finishQtyEdit(@NonNull VH h, @NonNull PosItemDto item) {
        int qty = parseQty(safeTrim(h.b.txtQty.getText()));
        if (qty <= 0) qty = 1;

        if (listener != null) listener.onQtyChanged(item.id, qty);

        // أي qty > 0 => لازم يكون selected
        if (qty > 0) selectedIds.add(item.id);
        else selectedIds.remove(item.id);

        h.b.txtQty.setText(String.valueOf(qty));
        h.b.txtQty.clearFocus();

        h.b.txtQty.setCursorVisible(false);
        h.b.txtQty.setFocusable(false);
        h.b.txtQty.setFocusableInTouchMode(false);

        hideKeyboard(h.b.txtQty);

        h.isEditingQty = false;

        notifyQtyChanged(item.id);
    }

    private int parseQty(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 1;
        }
    }

    private String safeTrim(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    private void showKeyboard(View v) {
        try {
            Context c = v.getContext();
            InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception ignored) {}
    }

    private void hideKeyboard(View v) {
        try {
            Context c = v.getContext();
            InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        RowPosItemBinding b;
        boolean isEditingQty = false;

        VH(RowPosItemBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
