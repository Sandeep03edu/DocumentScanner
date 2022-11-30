package com.scanner.document.Scanning;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.scanner.document.R;
import com.scanner.document.Utils.AppUtils;

import java.util.ArrayList;

public class DisplayOcrPagerAdapter extends RecyclerView.Adapter<DisplayOcrPagerAdapter.ViewHolder> {

    private static final String TAG = "DisplayOcrPagerAdapTag";
    private Context mContext;
    private ArrayList<String> imageTextList;
    ClickActionListener clickActionListener;

    public DisplayOcrPagerAdapter(Context mContext, ArrayList<String> imageTextList, ClickActionListener clickActionListener) {
        this.mContext = mContext;
        this.imageTextList = imageTextList;
        this.clickActionListener = clickActionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cart_ocr_pager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.textLl.setVisibility(View.GONE);

            holder.imageView.setImageURI(Uri.parse(imageTextList.get(position)));
        } else if (position == 1) {
            holder.imageView.setVisibility(View.GONE);
            holder.textLl.setVisibility(View.VISIBLE);

            holder.textView.setText(imageTextList.get(position));
        }

        holder.textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clickActionListener.onCopy(holder.textView.getText().toString().trim());
                return false;
            }
        });

        holder.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickActionListener.onBack();
            }
        });

        holder.copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickActionListener.onCopy(holder.textView.getText().toString().trim());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageTextList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        LinearLayout textLl;
        ImageView back;
        MaterialCardView copyText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cart_ocr_pager_image);
            textView = itemView.findViewById(R.id.cart_ocr_pager_text);

            textLl = itemView.findViewById(R.id.cart_ocr_pager_text_ll);
            back = itemView.findViewById(R.id.cart_ocr_pager_back);
            copyText = itemView.findViewById(R.id.cart_ocr_pager_copy_text);
        }
    }

    interface ClickActionListener{
        void onBack();
        void onCopy(String copiedText);
    }
}
