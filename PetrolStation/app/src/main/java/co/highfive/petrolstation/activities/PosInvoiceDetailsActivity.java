package co.highfive.petrolstation.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.databinding.ActivityActiveInvoicesBinding;
import co.highfive.petrolstation.databinding.ActivityPosInvoicesListBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.pos.data.PosInvoiceDbHelper;
import co.highfive.petrolstation.pos.dto.PosActiveInvoice;
import co.highfive.petrolstation.pos.ui.SimplePosInvoicesAdapter;

public class PosInvoiceDetailsActivity extends BaseActivity {

    ActivityPosInvoicesListBinding binding;
    private PosInvoiceDbHelper db;
    private SimplePosInvoicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pos_invoices_list);
        setupUI(findViewById(android.R.id.content));

        binding = ActivityPosInvoicesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new PosInvoiceDbHelper(this);

        binding.icBack.setOnClickListener(v -> finish());

        bindingSetup();
        loadInvoices();
    }

    private void bindingSetup() {
        androidx.recyclerview.widget.RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SimplePosInvoicesAdapter(null, invoiceId -> {
            Intent i = new Intent();
            i.putExtra("pos_invoice_id", invoiceId);
            setResult(RESULT_OK, i);
            finish();
        });

        rv.setAdapter(adapter);
    }

    private void loadInvoices() {
        List<PosActiveInvoice> list = db.listInvoices();
        adapter.submitList(list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInvoices();
    }
}
