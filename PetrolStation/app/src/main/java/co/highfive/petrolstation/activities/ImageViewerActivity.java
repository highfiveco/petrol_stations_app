package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
//import com.github.chrisbanes.photoview.PhotoView;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;

public class ImageViewerActivity extends BaseActivity {

    private float xCoOrdinate, yCoOrdinate;
    private double screenCenterX, screenCenterY;
    private int alpha;
//    PhotoView imageView;
    LinearLayout back;
    View view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer);

//        try{
//            back = (LinearLayout) findViewById(R.id.back);
//            imageView = (PhotoView) findViewById(R.id.imageView);
//            view = findViewById(R.id.layout);
//            view.getBackground().setAlpha(255);
//
//            if(getIntent().getExtras().getString("image_path",null) != null){
//                Glide.with(getApplicationContext()).load(getIntent().getExtras().getString("image_path")).into(imageView);
//            }
//
//            back.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    finish();
//                }
//            });
//        }catch (Exception e){
//
//        }

    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}