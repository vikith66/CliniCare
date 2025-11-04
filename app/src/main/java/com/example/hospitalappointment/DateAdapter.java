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

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    public interface OnDateSelectedListener {
        void onDateSelected(String date); // callback to activity
    }

    private final Context context;
    private final List<DateItem> dateList;
    private int selectedPosition = -1;
    private final OnDateSelectedListener listener;

    public DateAdapter(Context context, List<DateItem> dateList, OnDateSelectedListener listener) {
        this.context = context;
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.date_card, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateItem dateItem = dateList.get(position);

        holder.tvDay.setText(dateItem.getDay());
        holder.tvDate.setText(dateItem.getDate());

        // Set equal width for 7 items visible
        int totalWidth = holder.itemView.getResources().getDisplayMetrics().widthPixels;
        int boxWidth = totalWidth / 6;
        holder.dateCard.getLayoutParams().width = boxWidth;

        // Set appearance based on selection
        if (selectedPosition == position) {
            holder.dateCard.setBackgroundResource(R.drawable.bg_date_selected);
            holder.tvDay.setTextColor(0xFFFFFFFF); // white
            holder.tvDate.setTextColor(0xFFFFFFFF);
        } else {
            holder.dateCard.setBackgroundResource(R.drawable.bg_date_unselected); // only stroke
            holder.tvDay.setTextColor(0xFF4D6D68); // unselected color
            holder.tvDate.setTextColor(0xFF4D6D68);
        }

        // Click listener
        holder.dateCard.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                selectedPosition = adapterPos;
                notifyDataSetChanged();
                if (listener != null) {
                    String selectedDate = dateList.get(adapterPos).getDay() + " " + dateList.get(adapterPos).getDate();
                    listener.onDateSelected(selectedDate);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        LinearLayout dateCard;
        TextView tvDay, tvDate;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateCard = itemView.findViewById(R.id.dateCard);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
