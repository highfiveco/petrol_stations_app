package co.highfive.petrolstation.fragments;


import android.content.ClipboardManager;
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
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;


public class ViewJsonDialog extends DialogFragment {
    Context context;

    AppCompatImageView close;
    AppCompatTextView copy;
    AppCompatTextView json_value;

    String  json_txt;
    BaseActivity baseActivity;
    public ViewJsonDialog() {
        // Required empty public constructor
    }
    public ViewJsonDialog(BaseActivity baseActivity, String json_txt) {
        this.baseActivity=baseActivity;
        this.json_txt=json_txt;
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
        View customView = inflater.inflate(R.layout.view_json_dialog_layout, container, false);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));



        close=  (AppCompatImageView) customView.findViewById(R.id.close);
        copy=  (AppCompatTextView) customView.findViewById(R.id.copy);
        json_value=  (AppCompatTextView) customView.findViewById(R.id.json_value);


        json_value.setText(baseActivity.formatJson(json_txt));

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewJsonDialog.this.dismiss();
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baseActivity.setClipboard(baseActivity.formatJson(json_txt));
                baseActivity.toast(R.string.added_to_clipboard);
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
