package com.scanner.document.Scanning;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scanner.document.R;
import com.scanner.document.Utils.AppUtils;

import java.util.ArrayList;

public class GalleryPagerAdapter extends RecyclerView.Adapter<GalleryPagerAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Uri> imagesList;

    public GalleryPagerAdapter(Context mContext, ArrayList<Uri> imagesList) {
        this.mContext = mContext;
        this.imagesList = imagesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cart_gallery_edit, parent, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = imagesList.get(position);
        Bitmap bitmap = AppUtils.Uri2Bitmap(mContext,uri);
        holder.imageView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cart_gallery_edit_image);
        }
    }

    public Uri getUri(int pos) {
        return imagesList.get(pos);
    }
}
