package com.example.hospitalappointment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {

    // Callback for time selection
    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }

    private final Context context;
    private final List<String> timeList;
    private final OnTimeSelectedListener listener;

    private int selectedPosition = RecyclerView.NO_POSITION; // no item selected initially

    public TimeAdapter(Context context, List<String> timeList, OnTimeSelectedListener listener) {
        this.context = context;
        this.timeList = timeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.time_card, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String time = timeList.get(position);
        holder.tvTime.setText(time);

        // Apply selection UI
        if (selectedPosition == position) {
            holder.timeCard.setBackgroundResource(R.drawable.bg_date_selected);
            holder.tvTime.setTextColor(0xFFFFFFFF); // White
        } else {
            holder.timeCard.setBackgroundResource(R.drawable.bg_date_unselected);
            holder.tvTime.setTextColor(0xFF4D6D68); // Default greenish text
        }

        holder.itemView.setOnClickListener(v -> {
            int prevPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (prevPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(prevPosition); // unselect previous
            }
            notifyItemChanged(selectedPosition); // select new

            if (listener != null) {
                listener.onTimeSelected(time);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeList == null ? 0 : timeList.size();
    }

    // --- ViewHolder ---
    static class TimeViewHolder extends RecyclerView.ViewHolder {
        LinearLayout timeCard;
        TextView tvTime;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            timeCard = itemView.findViewById(R.id.timeCard);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
