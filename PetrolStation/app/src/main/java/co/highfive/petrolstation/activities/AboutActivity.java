package co.highfive.petrolstation.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.DataBindingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.databinding.ActivityAboutBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest.RequestAsyncTask;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.AsyncResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.hazemhamadaqa.app.AppConfig;
import co.highfive.petrolstation.models.AppData;

public class AboutActivity extends BaseActivity {

    private ActivityAboutBinding binding;
    private AppData appData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        setupUI(binding.mainLayout);
        initViews();
    }

    private void initViews() {
        appData = getGson().fromJson(getSessionManager().getString(getSessionKeys().app_data), AppData.class);

        if (appData != null && appData.getAbout_app() != null) {
            binding.title.setText(getHtmlText(appData.getAbout_app().getTitle()));
            binding.about.setText(getHtmlText(appData.getAbout_app().getDetails()));
        }

        binding.icBack.setOnClickListener(v -> finish());
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
    }
}
