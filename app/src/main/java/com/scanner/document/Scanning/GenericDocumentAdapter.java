package com.scanner.document.Scanning;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.scanner.document.Model.Document;
import com.scanner.document.Model.GenericDocument;
import com.scanner.document.Model.OcrDocument;
import com.scanner.document.R;
import com.scanner.document.Utils.AppUtils;

public class GenericDocumentAdapter extends ListAdapter<GenericDocument, GenericDocumentAdapter.ViewHolder> {

    public static final int DOCUMENT_TYPE = 101;
    public static final int OCR_DOCUMENT_TYPE = 102;

    private static final String TAG = "DocumentsAdapterTag";
    public static final DiffUtil.ItemCallback<GenericDocument> Diff_CallBack = new DiffUtil.ItemCallback<GenericDocument>() {
        @Override
        public boolean areItemsTheSame(@NonNull GenericDocument oldItem, @NonNull GenericDocument newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull GenericDocument oldItem, @NonNull GenericDocument newItem) {
            if (oldItem.getDocument() != null) {
                return oldItem.getDocument().getTimeStamp() == newItem.getDocument().getTimeStamp()
                        && oldItem.getDocument().getFileName().equals(newItem.getDocument().getFileName())
                        && oldItem.getDocument().getSavedUri().equals(newItem.getDocument().getSavedUri())
                        && oldItem.getDocument().getFilePhotos().equals(newItem.getDocument().getFilePhotos());
            }
            if(oldItem.getOcrDocument()!=null){
                return oldItem.getOcrDocument().getTimeStamp() == newItem.getOcrDocument().getTimeStamp()
                        && oldItem.getOcrDocument().getFileName().equals(newItem.getOcrDocument().getFileName())
                        && oldItem.getOcrDocument().getSavedUri().equals(newItem.getOcrDocument().getSavedUri())
                        && oldItem.getOcrDocument().getFilePhotos().equals(newItem.getOcrDocument().getFilePhotos())
                        && oldItem.getOcrDocument().getFileTexts().equals(newItem.getOcrDocument().getFileTexts())
                        && oldItem.getOcrDocument().getFilePhotos().size()==newItem.getOcrDocument().getFilePhotos().size();
            }
            return true;
        }
    };

    @Override
    public int getItemViewType(int position) {
        GenericDocument genericDocument = getItem(position);
        if (genericDocument.getDocument() != null) {
            return DOCUMENT_TYPE;
        } else if (genericDocument.getOcrDocument() != null) {
            return OCR_DOCUMENT_TYPE;
        }
        return super.getItemViewType(position);
    }

    private OnItemClickListener listener;
    private OnItemClickListener shareListener;
    private OnItemClickListener moreListener;

    public GenericDocumentAdapter() {
        super(Diff_CallBack);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GenericDocument genericDocument = getItem(position);

        if (genericDocument.getDocument() != null && genericDocument.getDocument().getFilePhotos() != null && genericDocument.getDocument().getFilePhotos().size() != 0) {
            Document document = genericDocument.getDocument();
            String docThumbUriStr = document.getFilePhotos().get(0);
            Uri docThumbUri = Uri.parse(docThumbUriStr);

            holder.docThumb.setImageURI(docThumbUri);
            holder.name.setText(document.getFileName());
            holder.date.setText(AppUtils.formattedDate(document.getTimeStamp()));
        }
        else if(genericDocument.getOcrDocument() != null && genericDocument.getOcrDocument().getFilePhotos() != null && genericDocument.getOcrDocument().getFilePhotos().size() != 0) {
            OcrDocument ocrDocument = genericDocument.getOcrDocument();
            String docThumbUriStr = ocrDocument.getFilePhotos().get(0);
            Uri docThumbUri = Uri.parse(docThumbUriStr);

            holder.docThumb.setImageURI(docThumbUri);
            holder.name.setText(ocrDocument.getFileName());
            holder.date.setText(AppUtils.formattedDate(ocrDocument.getTimeStamp()));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView docThumb;
        TextView name, date, share, more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            docThumb = itemView.findViewById(R.id.cart_document_image);
            name = itemView.findViewById(R.id.cart_document_name);
            date = itemView.findViewById(R.id.cart_document_date);
            share = itemView.findViewById(R.id.cart_document_share);
            more = itemView.findViewById(R.id.cart_document_more);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (listener != null && pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(pos));
                    }
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (shareListener != null && pos != RecyclerView.NO_POSITION) {
                        shareListener.onItemClick(getItem(pos));
                    }
                }
            });

            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (moreListener != null && pos != RecyclerView.NO_POSITION) {
                        moreListener.onItemClick(getItem(pos));
                    }
                }
            });

        }
    }

    public interface OnItemClickListener {
        void onItemClick(GenericDocument document);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setShareListener(OnItemClickListener listener) {
        this.shareListener = listener;
    }

    public void setMoreListener(OnItemClickListener listener) {
        this.moreListener = listener;
    }
}
