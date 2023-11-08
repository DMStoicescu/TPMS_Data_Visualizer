package com.example.tpms_data_visualizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Date;

public class DeviceDetailsActivity extends AppCompatActivity {

    EditText titleDeviceText, descriptionDeviceText;
    //TODO add the content
    ImageButton saveDeviceButton;
    FloatingActionButton deleteDeviceButton;
    TextView titleDeviceTextView;
    String title,content,docId;
    boolean isViewMode = false;

    String[] testArray = {"Sensor1","Sensor2","Sensor3","Sensor4","Sensor5","Sensor6"};
    ListView sensorListView;
    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        titleDeviceText = findViewById(R.id.device_title);
        descriptionDeviceText = findViewById(R.id.device_description);
        saveDeviceButton = findViewById(R.id.save_device_btn);
        titleDeviceTextView = findViewById(R.id.new_device);
        deleteDeviceButton = findViewById(R.id.delete_device_btn);
        sensorListView = findViewById(R.id.sensor_list_options);

        //Receive data logic
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        if(docId != null && !docId.isEmpty()){
            isViewMode = true;
        }

        titleDeviceText.setText(title);
        descriptionDeviceText.setText(content);

        if(isViewMode){
            titleDeviceTextView.setText("Device: " + title);
            deleteDeviceButton.setVisibility(View.VISIBLE);
            sensorListView.setVisibility(View.GONE);
        }
        else {
            listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, testArray);
            sensorListView.setAdapter(listAdapter);
        }

        //Buttons logic
        saveDeviceButton.setOnClickListener((v -> saveDevice()));
        deleteDeviceButton.setOnClickListener((v -> deleteDeviceFromFirebase()));
    }

    void saveDevice(){
        String device_title = titleDeviceText.getText().toString();
        String device_description = descriptionDeviceText.getText().toString();
        //TODO add the content here

        if (device_title == null || device_title.isEmpty()){
            titleDeviceText.setError(("Title is required!"));
            return;
        }

        //TODO: Once sensors are added, do validation


        Device device = new Device();

        device.setTitle(device_title);
        device.setContent(device_description);
        device.setTimestamp(Timestamp.now());

        if(!isViewMode) {
            ArrayList<String> checkedSensors = new ArrayList<>();
            for (int i = 0; i < sensorListView.getCount(); i++) {
                if (sensorListView.isItemChecked(i)) {
                    checkedSensors.add(sensorListView.getItemAtPosition(i).toString());
                }
            }

            device.setCheckedSensorsArray(checkedSensors);
        }

        saveDeviceToFirebase(device);
    }

    void saveDeviceToFirebase(Device device){
        DocumentReference documentRef;
        if(isViewMode){
            //Work on existing document
            documentRef = Utility.getCollectionReferenceForNotes().document(docId);
        }
        else{
            //Create new reference for new document
            documentRef = Utility.getCollectionReferenceForNotes().document();
        }
        documentRef.set(device).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Which means that device is added
                    Utility.showToast(DeviceDetailsActivity.this, "Device configured successfully!");
                    finish();
                }
                else {
                    Utility.showToast(DeviceDetailsActivity.this, "Failed to configure device!");
                }
            }
        });
    }

        void deleteDeviceFromFirebase(){
            DocumentReference documentRef;
            documentRef = Utility.getCollectionReferenceForNotes().document(docId);

            documentRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Which means that device is deleted
                        Utility.showToast(DeviceDetailsActivity.this, "Device deleted!");
                        finish();
                    }
                    else {
                        Utility.showToast(DeviceDetailsActivity.this, "Failed to delete device!");
                    }
                }
            });
        }

}