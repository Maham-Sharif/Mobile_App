package com.example.phonely;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder> {
    List<PhoneModel> phoneList;
    Context context;

    public PhoneAdapter(List<PhoneModel> phoneList, Context context) {
        this.phoneList = phoneList;
        this.context = context;
    }

    @NonNull
    @Override
    public PhoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.phone_item, parent, false);
        return new PhoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneViewHolder holder, int position) {
        PhoneModel phone = phoneList.get(position);
        holder.name.setText(phone.name);
        holder.price.setText(phone.price);
        Glide.with(context).load(phone.imageUrl).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return phoneList.size();
    }

    public static class PhoneViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price;

        public PhoneViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.phoneImage);
            name = itemView.findViewById(R.id.phoneName);
            price = itemView.findViewById(R.id.phonePrice);
        }
    }
}

