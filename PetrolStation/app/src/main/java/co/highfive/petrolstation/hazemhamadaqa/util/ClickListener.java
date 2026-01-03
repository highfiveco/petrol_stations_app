package co.highfive.petrolstation.hazemhamadaqa.util;

import android.view.View;

/**
 * Created by Eng. Hazem Hamadaqa on 3/28/2017.
 */

public interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}