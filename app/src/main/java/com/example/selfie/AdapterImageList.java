package com.example.selfie;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class AdapterImageList extends RecyclerView.Adapter<AdapterImageList.ViewHolder> {
    private List<Image> listImages;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private int height, width;


    public AdapterImageList(Context context, List<Image> listImages) {
        super();
        Log.d("AdapterImageList", "Create Adapter: ");
        this.context = context;
        this.listImages = listImages;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public AdapterImageList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) parent.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.image_item_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterImageList.ViewHolder holder, int position) {
        if (listImages == null) return;
        if (position < 0) return;
        if (position >= listImages.size()) return;

        if (listImages.get(position) != null && listImages.get(position).getPath() != null)
            Picasso.get().load(new File(listImages.get(position).getPath())).placeholder(R.drawable.progress_animation).resize(width / 3, width / 3).centerCrop().into(holder.imvItem);

        if (listImages.size() > 0) {
            Log.d("FIX", "onBindViewHolder: " + listImages.size());
            if (listImages.get(position).isSelected()) {
                holder.imvSelectedItem.setVisibility(View.VISIBLE);
            } else {
                holder.imvSelectedItem.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return listImages.size();
    }

    public Image getItemByPosition(int position) {
        return listImages.get(position);
    }

    public void setListData(List<Image> listData) {
        listImages.clear();
        listImages.addAll(listData);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imvItem, imvSelectedItem;

        public ViewHolder(View itemView) {
            super(itemView);


            imvItem = itemView.findViewById(R.id.imvItemScreenshot);
            imvSelectedItem = itemView.findViewById(R.id.imvSelectedItem);

//            imvItem.setLayoutParams(new ConstraintLayout.LayoutParams(width / 3, width / 3));

            imvItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() < 0 || getAdapterPosition() >= listImages.size())
                        return;
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClick(getAdapterPosition());
                }
            });

//            imvSelectedItem.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    initAlertDialog(getAdapterPosition());
//                    alertDialog.show();
//                }
//            });

            imvItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (getAdapterPosition() < 0 || getAdapterPosition() >= listImages.size())
                        return false;
                    if (onItemClickListener != null)
                        onItemClickListener.onItemLongClick(getAdapterPosition());
                    return false;
                }
            });
        }


        public void showSelected() {
            if (getAdapterPosition() > -1) {
                if (listImages.get(getAdapterPosition()) != null) {
                    if (listImages.get(getAdapterPosition()).isSelected())
                        imvSelectedItem.setVisibility(View.VISIBLE);
                    else
                        imvSelectedItem.setVisibility(View.GONE);
                }
            }

        }
    }
}
