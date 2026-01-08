package co.highfive.petrolstation.fragments;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.listener.EditNameListener;

public class EditNameDialog extends DialogFragment {
    Context context;
    EditNameListener editNameListener;
    AppCompatEditText name;
    String account_name;
    public EditNameDialog() {
        // Required empty public constructor
    }
    public EditNameDialog(String account_name) {
        this.account_name=account_name;
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
        View customView = inflater.inflate(R.layout.edit_name_dialog_layout, container, false);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        AppCompatImageView close=  (AppCompatImageView) customView.findViewById(R.id.close);
        AppCompatTextView save=  (AppCompatTextView) customView.findViewById(R.id.save);
        name=  (AppCompatEditText) customView.findViewById(R.id.name);

        name.setText(account_name);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditNameDialog.this.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String validate = validate();
                if(validate == null){
                    editNameListener.editName(name.getText().toString().trim());
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),validate,Toast.LENGTH_LONG).show();
                }


            }
        });
        return customView;
    }

    private String validate() {
        if(name.getText().toString().trim().isEmpty()){
            return getString(R.string.enter_name);
        }
        return null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);

        }
    }

    public void setEditNameListener(EditNameListener editNameListener){
        this.editNameListener=editNameListener;
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




