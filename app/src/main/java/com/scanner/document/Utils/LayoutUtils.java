package com.scanner.document.Utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class LayoutUtils {

    public static void setTextColor(Context context, TextView[] textViews, TextView textView, int colorCode, int primaryColor){
        for(TextView view : textViews){
            view.setTextColor(context.getResources().getColor(primaryColor));
        }
        textView.setTextColor(context.getResources().getColor(colorCode));
    }
}
