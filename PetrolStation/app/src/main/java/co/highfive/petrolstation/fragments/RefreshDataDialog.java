package co.highfive.petrolstation.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tuyenmonkey.mkloader.MKLoader;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.listener.EditNameListener;
import java.util.ArrayList;
import java.util.List;
public class RefreshDataDialog extends DialogFragment {
    Context context;
    EditNameListener editNameListener;
    AppCompatTextView close;
    AppCompatImageView ic_update_setting;
    MKLoader loader_update_setting;
    AppCompatImageView ic_update_company_setting;
    MKLoader loader_update_company_setting;
    AppCompatImageView ic_update_customers;
    MKLoader loader_update_customers;

    AppCompatImageView ic_update_items;
    MKLoader loader_update_items;


    private boolean viewsReady = false;
    private final List<Runnable> pendingUiActions = new ArrayList<>();

    private void runWhenReady(Runnable r) {
        if (viewsReady) {
            r.run();
        } else {
            pendingUiActions.add(r);
        }
    }

    public RefreshDataDialog() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View customView = inflater.inflate(R.layout.refresh_data_dialog_layout, container, false);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ic_update_setting=  (AppCompatImageView) customView.findViewById(R.id.ic_update_setting);
        loader_update_setting=  (MKLoader) customView.findViewById(R.id.loader_update_setting);
        ic_update_company_setting=  (AppCompatImageView) customView.findViewById(R.id.ic_update_company_setting);
        loader_update_company_setting=  (MKLoader) customView.findViewById(R.id.loader_update_company_setting);
        ic_update_customers=  (AppCompatImageView) customView.findViewById(R.id.ic_update_customers);
        loader_update_customers=  (MKLoader) customView.findViewById(R.id.loader_update_customers);

        ic_update_items = customView.findViewById(R.id.ic_update_items);
        loader_update_items = customView.findViewById(R.id.loader_update_items);


        close=  (AppCompatTextView) customView.findViewById(R.id.close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RefreshDataDialog.this.dismiss();
            }
        });
        lockCloseButton();
        viewsReady = true;
        for (Runnable r : pendingUiActions) {
            try { r.run(); } catch (Exception ignored) {}
        }
        pendingUiActions.clear();

        return customView;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);

        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e("ABSDIALOGFRAG", "Exception", e);

            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void showUpdateSetting(){
        runWhenReady(() -> {
            if (loader_update_setting != null) loader_update_setting.setVisibility(View.VISIBLE);
            if (ic_update_setting != null) ic_update_setting.setVisibility(View.GONE);
        });
    }

    public void changeUpdateSetting(boolean status){
        runWhenReady(() -> {
            if (loader_update_setting != null) loader_update_setting.setVisibility(View.GONE);
            if (ic_update_setting != null) {
                ic_update_setting.setVisibility(View.VISIBLE);
                ic_update_setting.setBackgroundResource(status ? R.drawable.ic_true : R.drawable.ic_false);
                ic_update_setting.setScaleType(ImageView.ScaleType.CENTER);
            }
        });
    }

    public void showUpdateCompanySetting(){
        runWhenReady(() -> {
            if (loader_update_company_setting != null) loader_update_company_setting.setVisibility(View.VISIBLE);
            if (ic_update_company_setting != null) ic_update_company_setting.setVisibility(View.GONE);
        });
    }

    public void changeUpdateCompanySetting(boolean status){
        runWhenReady(() -> {
            if (loader_update_company_setting != null) loader_update_company_setting.setVisibility(View.GONE);
            if (ic_update_company_setting != null) {
                ic_update_company_setting.setVisibility(View.VISIBLE);
                ic_update_company_setting.setBackgroundResource(status ? R.drawable.ic_true : R.drawable.ic_false);
                ic_update_company_setting.setScaleType(ImageView.ScaleType.CENTER);
            }
        });
    }

    public void showUpdateCustomers(){
        runWhenReady(() -> {
            if (loader_update_customers != null) loader_update_customers.setVisibility(View.VISIBLE);
            if (ic_update_customers != null) ic_update_customers.setVisibility(View.GONE);
        });
    }

    public void changeCustomers(boolean status){
        runWhenReady(() -> {
            if (loader_update_customers != null) loader_update_customers.setVisibility(View.GONE);
            if (ic_update_customers != null) {
                ic_update_customers.setVisibility(View.VISIBLE);
                ic_update_customers.setBackgroundResource(status ? R.drawable.ic_true : R.drawable.ic_false);
                ic_update_customers.setScaleType(ImageView.ScaleType.CENTER);
            }
        });
    }

    // ✅ NEW: Items UI
    public void showUpdateItems() {
        runWhenReady(() -> {
            if (loader_update_items != null) loader_update_items.setVisibility(View.VISIBLE);
            if (ic_update_items != null) ic_update_items.setVisibility(View.GONE);
        });
    }

    public void changeUpdateItems(boolean status) {
        runWhenReady(() -> {
            if (loader_update_items != null) loader_update_items.setVisibility(View.GONE);
            if (ic_update_items != null) {
                ic_update_items.setVisibility(View.VISIBLE);
                ic_update_items.setBackgroundResource(status ? R.drawable.ic_true : R.drawable.ic_false);
                ic_update_items.setScaleType(ImageView.ScaleType.CENTER);
            }
        });
    }

    public void activateCloseButton(){
        runWhenReady(() -> {
            if (close != null) {
                close.setEnabled(true);
                close.setAlpha(1f);
            }
        });
    }

    public void lockCloseButton(){
        runWhenReady(() -> {
            if (close != null) {
                close.setEnabled(false);
                close.setAlpha(0.5f);
            }
        });
    }

    public void unlockCloseButton(){
        runWhenReady(() -> {
            if (close != null) {
                close.setEnabled(true);
                close.setAlpha(1f);
            }
        });
    }




}


