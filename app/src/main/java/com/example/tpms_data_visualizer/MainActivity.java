package com.example.tpms_data_visualizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addDeviceButton;
    RecyclerView listOfDevices;
    ImageButton menuButton;
    DeviceAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addDeviceButton = findViewById(R.id.add_new_device_btn);
        listOfDevices = findViewById(R.id.device_list_recycler_view);

        addDeviceButton.setOnClickListener((v) -> startActivity(new Intent(MainActivity.this, DeviceDetailsActivity.class)));

        populateDeviceList();
    }

    void populateDeviceList(){
        Query query = Utility.getCollectionReferenceForNotes().orderBy("title",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Device> options = new FirestoreRecyclerOptions.Builder<Device>()
                .setQuery(query, Device.class).build();

        listOfDevices.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new DeviceAdapter(options, this);
        listOfDevices.setAdapter(deviceAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        deviceAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceAdapter.notifyDataSetChanged();
    }
}