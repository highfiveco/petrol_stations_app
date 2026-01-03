package co.highfive.petrolstation.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.R;

import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.listener.SuccessListener;

public class UpdateAppDialog extends DialogFragment {
    Context context;

    SuccessListener successListener;

    String update_title;
    String update_description;
    BaseActivity baseActivity;



    private int totalUpdateReadingsCount = 0;
    private int totalUpdatePhonesCount = 0;
    private int totalMoveTransactionsCount = 0;
    private int totalLoadTransactionsCount = 0;
    private int totaldiscountTransactionsCount = 0;

    int totalSyncTasks = 0;

    public UpdateAppDialog(BaseActivity baseActivity, String update_title, String update_description) {
        this.update_title=update_title;
        this.update_description=update_description;
        this.baseActivity=baseActivity;
    }

    public UpdateAppDialog( ) {
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
        View customView = inflater.inflate(R.layout.update_app_dialog_layout, container, false);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        AppCompatTextView save=  (AppCompatTextView) customView.findViewById(R.id.save);
        AppCompatTextView sync=  (AppCompatTextView) customView.findViewById(R.id.sync);
        AppCompatTextView title=  (AppCompatTextView) customView.findViewById(R.id.title);
        AppCompatTextView description=  (AppCompatTextView) customView.findViewById(R.id.description);
        LinearLayout sync_layout=  (LinearLayout) customView.findViewById(R.id.sync_layout);

            title.setText(update_title);
            description.setText(baseActivity.getHtmlText(update_description));


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successListener.success(true);
            }
        });

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateAppDialog.this.dismiss();
//                baseActivity.moveToActivity(baseActivity, SyncOfflineDataActivity.class,null,false);
            }
        });





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

    public void setSuccessListener(SuccessListener successListener){
        this.successListener=successListener;
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


}


