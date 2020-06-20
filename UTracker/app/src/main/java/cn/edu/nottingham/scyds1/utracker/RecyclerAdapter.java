package cn.edu.nottingham.scyds1.utracker;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 *  RecyclerView class which is responsible for showing the card as a list using adapter.
 *  OnBindViewHolder() is one of the most important functions here as for the user perspective of the card elements
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    //region globals
    private ArrayList<CardItem> mList;
    private OnItemClickListener mListener;
    private Context context;
    //endregion

    //region interface
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }
    //endregion

    //setter for lister
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    //ViewHolder class to maintain the display of the card elements and button clicks (delete, view more)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        //globals for this class
        public ImageView mImageView;
        public TextView mDateTextView;
        public TextView mDistanceTextView;
        public TextView mAvgSpeedTextView;
        public ImageView mDeleteImage;
        public TextView mDurationTextView;
        public Button mViewMore;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            // UI elemets
            mImageView = itemView.findViewById(R.id.imageView);
            mDateTextView = itemView.findViewById(R.id.tvDate);
            mDistanceTextView = itemView.findViewById(R.id.tvDistance);
            mAvgSpeedTextView = itemView.findViewById(R.id.tvAvgspeed);
            mDeleteImage = itemView.findViewById(R.id.image_delete);
            mDurationTextView = itemView.findViewById(R.id.tvDuration);
            mViewMore = itemView.findViewById(R.id.btn_viewMore);

            //if view more button was clicked send it back to activity function
            mViewMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            //if delete button was clicked send it back to activity function
            mDeleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    //setter for list of elements and context (needed context for getting Strings from strings.xml)
    public RecyclerAdapter(ArrayList<CardItem> exampleList, Context context) {
        mList = exampleList;
        this.context = context;
    }

    //initializer for ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item, parent, false);
        return new ViewHolder(v, mListener);
    }

    //Format and display every element (or almost every) on the card
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CardItem currentItem = mList.get(position);
        holder.mImageView.setImageDrawable(currentItem.getFormattedImage(currentItem.getActivityType(),context));
        holder.mDateTextView.setText(currentItem.getFormattedDate(currentItem.getDate(),context));
        holder.mDistanceTextView.setText(currentItem.getFormattedDistance(currentItem.getDistance(),context));
        holder.mAvgSpeedTextView.setText(currentItem.getFormattedSpeed(currentItem.getAvgSpeed(),context));
        holder.mDurationTextView.setText(currentItem.getFormattedDuration(currentItem.getDuration(),context));
    }

    //same as number of rows in the table
    @Override
    public int getItemCount() {
        return mList.size();
    }

}