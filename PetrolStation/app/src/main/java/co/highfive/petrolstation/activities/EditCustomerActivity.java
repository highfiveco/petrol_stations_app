package co.highfive.petrolstation.activities;

import android.os.Bundle;
import co.highfive.petrolstation.R;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;

public class EditCustomerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);
        setupUI(findViewById(android.R.id.content));
    }
}