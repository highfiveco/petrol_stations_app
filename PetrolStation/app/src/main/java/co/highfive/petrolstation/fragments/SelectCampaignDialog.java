package co.highfive.petrolstation.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import co.highfive.petrolstation.adapters.SelectCampaignAdapter;
import co.highfive.petrolstation.databinding.DialogSelectCampaignBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelCampaignDto;

public class SelectCampaignDialog extends DialogFragment {

    public interface Listener {
        void onSelected(FuelCampaignDto campaign);
    }

    private static final String ARG_SELECTED_ID = "selected_id";

    private DialogSelectCampaignBinding binding;
    private Listener listener;

    private ArrayList<FuelCampaignDto> campaigns = new ArrayList<>();
    private int selectedId = 0;

    public static SelectCampaignDialog newInstance(ArrayList<FuelCampaignDto> campaigns, int selectedId) {
        SelectCampaignDialog d = new SelectCampaignDialog();
        d.campaigns = (campaigns != null) ? campaigns : new ArrayList<>();
        Bundle b = new Bundle();
        b.putInt(ARG_SELECTED_ID, selectedId);
        d.setArguments(b);
        return d;
    }

    public void setListener(Listener l) { this.listener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSelectCampaignBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        selectedId = getArguments() != null ? getArguments().getInt(ARG_SELECTED_ID, 0) : 0;

        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        SelectCampaignAdapter adapter = new SelectCampaignAdapter(campaigns, selectedId, campaign -> {
            if (listener != null) listener.onSelected(campaign);
            dismissAllowingStateLoss();
        });

        binding.recycler.setAdapter(adapter);

        binding.close.setOnClickListener(v -> dismissAllowingStateLoss());

        return binding.getRoot();
    }
}
