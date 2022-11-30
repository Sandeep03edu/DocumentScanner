package com.scanner.document.Scanning;

import static android.content.Context.CLIPBOARD_SERVICE;
import static java.lang.Thread.sleep;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.scanner.document.Model.GenericDocument;
import com.scanner.document.Model.OcrDocument;
import com.scanner.document.R;
import com.scanner.document.Utils.Constants;

import java.util.ArrayList;

public class DisplayOcrAdapter extends RecyclerView.Adapter<DisplayOcrAdapter.ViewHolder> {

    private static final String TAG = "DisplayOcrAdapterTag";
    Context mContext;
    GenericDocument genericDocument;

    public DisplayOcrAdapter(Context mContext, GenericDocument genericDocument) {
        this.mContext = mContext;
        this.genericDocument = genericDocument;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cart_ocr, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OcrDocument ocrDocument = genericDocument.getOcrDocument();
        if (ocrDocument == null) return;

        holder.setData(ocrDocument, position);
    }

    @Override
    public int getItemCount() {
        return genericDocument.getOcrDocument().getFilePhotos().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 viewPager2;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPager2 = itemView.findViewById(R.id.cart_ocr_pager);
            textView = itemView.findViewById(R.id.cart_ocr_text);
        }

        public void setData(OcrDocument ocrDocument, int pos) {
            textView.setOnClickListener(new View.OnClickListener()  {
                @Override
                public void onClick(View view) {
//                    viewPager2.setCurrentItem(1);
                    Intent showCompText = new Intent(mContext, OcrCompleteText.class);
                    showCompText.putExtra(Constants.TEXT, ocrDocument.getFileTexts().get(pos));
                    mContext.startActivity(showCompText);
                }
            });

            ArrayList<String> imageTextList = new ArrayList<>();
            imageTextList.add(ocrDocument.getFilePhotos().get(pos));
            imageTextList.add(ocrDocument.getFileTexts().get(pos));
            Log.d(TAG, "setData: Text: " + ocrDocument.getFileTexts().get(pos));
            textView.setText("DETECT OCR TEXT");

            DisplayOcrPagerAdapter adapter = new DisplayOcrPagerAdapter(mContext, imageTextList, new DisplayOcrPagerAdapter.ClickActionListener() {
                @Override
                public void onBack() {
                    viewPager2.setCurrentItem(0);
                }

                @Override
                public void onCopy(String copiedText) {
                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Ocr Text", copiedText);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(mContext, "Text Copied Successfully", Toast.LENGTH_SHORT).show();
                }
            });
            viewPager2.setAdapter(adapter);
            viewPager2.setOffscreenPageLimit(2);
            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (position == 0) {
                        textView.setVisibility(View.VISIBLE);
                    } else if (position == 1) {
                        textView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
