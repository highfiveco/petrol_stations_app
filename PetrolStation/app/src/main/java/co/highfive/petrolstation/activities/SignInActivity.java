package co.highfive.petrolstation.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest.RequestAsyncTask;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.AsyncResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.hazemhamadaqa.app.AppConfig;

public class SignInActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

    }

}