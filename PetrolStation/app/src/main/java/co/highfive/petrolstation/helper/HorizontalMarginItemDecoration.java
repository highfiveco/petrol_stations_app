package co.highfive.petrolstation.helper;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class HorizontalMarginItemDecoration extends RecyclerView.ItemDecoration {
    private final int horizontalMarginInPx;

    public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
//        Intrinsics.checkNotNullParameter(outRect, "outRect");
//        Intrinsics.checkNotNullParameter(view, "view");
//        Intrinsics.checkNotNullParameter(parent, "parent");
//        Intrinsics.checkNotNullParameter(state, "state");
        outRect.right = this.horizontalMarginInPx;
        outRect.left = this.horizontalMarginInPx;
    }

    public HorizontalMarginItemDecoration(@NotNull Context context, @DimenRes int horizontalMarginInDp) {
//        Intrinsics.checkNotNullParameter(context, "context");
        this.horizontalMarginInPx = (int)context.getResources().getDimension(horizontalMarginInDp);
    }
}

