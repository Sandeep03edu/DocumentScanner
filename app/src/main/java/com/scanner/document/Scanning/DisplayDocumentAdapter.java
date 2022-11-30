package com.scanner.document.Scanning;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scanner.document.R;

import java.util.ArrayList;

public class DisplayDocumentAdapter extends RecyclerView.Adapter<DisplayDocumentAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> imageArrayList;
    private ClickAction clickAction;

    public DisplayDocumentAdapter(Context mContext, ArrayList<String> imageArrayList, ClickAction clickAction) {
        this.mContext = mContext;
        this.imageArrayList = imageArrayList;
        this.clickAction = clickAction;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cart_display_scan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageStr = imageArrayList.get(position);
        Uri uri = Uri.parse(imageStr);
        holder.image.setImageURI(uri);
    }

    @Override
    public int getItemCount() {
        return imageArrayList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        LinearLayout reorder, crop, rotate, filter, delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.display_scan_page_image);

            reorder = itemView.findViewById(R.id.display_scan_reorder_page);
            crop = itemView.findViewById(R.id.display_scan_crop_page);
            rotate = itemView.findViewById(R.id.display_scan_rotate_page);
            filter = itemView.findViewById(R.id.display_scan_filter_page);
            delete = itemView.findViewById(R.id.display_scan_delete_page);

            reorder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction.onReorder(getAdapterPosition());
                }
            });

            crop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction.onCrop(getAdapterPosition());
                }
            });

            rotate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction.onRotate(getAdapterPosition());
                }
            });

            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction.onFilter(getAdapterPosition());
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction.onDelete(getAdapterPosition());
                }
            });

        }
    }

    public interface ClickAction{
        void onReorder(int pos);
        void onCrop(int pos);
        void onRotate(int pos);
        void onFilter(int pos);
        void onDelete(int pos);
    }

}
