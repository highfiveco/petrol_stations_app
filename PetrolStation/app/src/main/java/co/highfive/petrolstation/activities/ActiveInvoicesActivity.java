package co.highfive.petrolstation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import co.highfive.petrolstation.adapters.ActiveInvoicesAdapter;
import co.highfive.petrolstation.databinding.ActivityActiveInvoicesBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;

public class ActiveInvoicesActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_DRAFT_ID = "selected_draft_id";

    private ActivityActiveInvoicesBinding binding;
    private ActiveInvoicesAdapter adapter;

    private static final String SESSION_KEY_FUEL_SALE_DRAFTS = "SESSION_KEY_FUEL_SALE_DRAFTS";
    private final ArrayList<FuelSaleDraft> drafts = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityActiveInvoicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.getRoot());

        binding.icBack.setOnClickListener(v -> finish());

        adapter = new ActiveInvoicesAdapter(drafts, draft -> {
            if (draft == null) return;
            Intent i = new Intent();
            i.putExtra(EXTRA_SELECTED_DRAFT_ID, draft.local_id);
            setResult(RESULT_OK, i);
            finish();
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        loadDrafts();
    }

    private void loadDrafts() {
        drafts.clear();
        drafts.addAll(readDraftsFromSession());

        binding.emptyState.setVisibility(drafts.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    private ArrayList<FuelSaleDraft> readDraftsFromSession() {
        try {
            String raw = getSessionManager().getString(SESSION_KEY_FUEL_SALE_DRAFTS);
            if (raw == null || raw.trim().isEmpty()) return new ArrayList<>();

            java.lang.reflect.Type type =
                    new com.google.gson.reflect.TypeToken<ArrayList<FuelSaleDraft>>() {}.getType();

            ArrayList<FuelSaleDraft> list = getGson().fromJson(raw, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
