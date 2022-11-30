package com.scanner.document.Scanning;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.BiMap;
import com.scanner.document.R;

import java.util.ArrayList;

public class DisplayGridAdapter extends RecyclerView.Adapter<DisplayGridAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<String> uriArrayList;
    private DisplayGridAdapter.ClickAction clickAction;
    int screenWidth;

    public DisplayGridAdapter(Context mContext, ArrayList<String> uriArrayList, ClickAction clickAction) {
        this.mContext = mContext;
        this.uriArrayList = uriArrayList;
        this.clickAction = clickAction;
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cart_grid_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imgUriStr = uriArrayList.get(position);
        holder.imageView.setImageURI(Uri.parse(imgUriStr));

        holder.imageView.getLayoutParams().width = screenWidth/2;
        holder.imageView.getLayoutParams().height = screenWidth/2;
    }

    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cart_grid_display_image);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public interface ClickAction{
        void onItemClick(int pos);
    }

}
