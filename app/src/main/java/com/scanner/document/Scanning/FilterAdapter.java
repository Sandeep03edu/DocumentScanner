package com.scanner.document.Scanning;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scanner.document.R;
import com.zomato.photofilters.imageprocessors.Filter;

import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private static final String TAG = "FilterAdapterTag";
    private Context mContext;
    private Bitmap bitmap;
    private List<Filter> filterArrayList;
    private FilterActionListener filterActionListener;


    public FilterAdapter(Context mContext, Bitmap bitmap, List<Filter> filterArrayList, FilterActionListener filterActionListener) {
        this.mContext = mContext;
        this.bitmap = bitmap;
        this.filterArrayList = filterArrayList;
        this.filterActionListener = filterActionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cart_filter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Filter filter = filterArrayList.get(position);
        Bitmap imgBitmap = bitmap;

        if(filter==null){
            holder.filterImage.setImageBitmap(imgBitmap);
            holder.filerName.setText("Original");
        }
        else if(imgBitmap!=null){
            imgBitmap = imgBitmap.copy(Bitmap.Config.ARGB_8888, true);
            imgBitmap = filter.processFilter(imgBitmap);
            holder.filterImage.setImageBitmap(imgBitmap);
            holder.filerName.setText(filter.getName());
        }

        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        holder.filterImage.getLayoutParams().width = screenWidth/5;
        holder.filterImage.getLayoutParams().height = screenWidth/5;
    }

    @Override
    public int getItemCount() {
        return filterArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView filterImage;
        TextView filerName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            filterImage = itemView.findViewById(R.id.cart_filer_image);
            filerName = itemView.findViewById(R.id.cart_filer_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    filterActionListener.setFilter(filterArrayList.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface FilterActionListener {
        void setFilter(Filter filter);
    }

}
