package com.example.tpms_data_visualizer;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class DeviceAdapter extends FirestoreRecyclerAdapter<Device, DeviceAdapter.DeviceViewHolder> {
    Context context;
    public DeviceAdapter(@NonNull FirestoreRecyclerOptions<Device> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull DeviceViewHolder holder, int position, @NonNull Device device) {
        holder.titleTextView.setText(device.title);
        holder.descriptionTextView.setText(device.content);
        holder.timeStampTextView.setText(Utility.timestampToString(device.timestamp));

        holder.itemView.setOnClickListener((v) -> {
            Intent intent = new Intent(context, DeviceDetailsActivity.class);
            intent.putExtra("title", device.title);
            intent.putExtra("content", device.content);
            String docId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("docId", docId);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, descriptionTextView, timeStampTextView;
        //TODO ADD LIST OF SENSORS
        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.recycle_device_title);
            descriptionTextView = itemView.findViewById(R.id.recycle_device_description);
            timeStampTextView = itemView.findViewById(R.id.recycle_device_timestamp);
            //TODO ADD LIST OF SENSORS
        }
    }

}
