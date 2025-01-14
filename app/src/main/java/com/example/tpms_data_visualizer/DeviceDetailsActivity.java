package com.example.tpms_data_visualizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DeviceDetailsActivity extends AppCompatActivity {

    //General variables
    EditText titleDeviceText, descriptionDeviceText;
    ImageButton saveDeviceButton;
    FloatingActionButton deleteDeviceButton;
    Button pressureGraphButton, temperatureGraphButton;
    TextView titleDeviceTextView, sensorListTitle, sensorListModulation, sensorListProtocol, vehicleLastSeen;
    String title,content,docId;
    boolean isViewMode = false;

    //Database and population related variables
    Connection connectToDb = null;
    String[] testArray = {"Sensor1","Sensor2","Sensor3","Sensor4","Sensor5","Sensor6"};
    ArrayList <String> receivedSensorsId = new ArrayList<String>();
    String receivedSensorsModulation = "";
    String receivedSensorsProtocol = "";
    String receivedLastSeenTime = "";
    ListView sensorListOptionsView;
    ArrayAdapter<String> listAdapter;

    ArrayList<String> checkedSensors;

    //Plot related variables
    LinearLayout scrollableInfoLayout, temperatureContentLayout, pressureContentLayout;
    XYPlot temperature_plot;

    Handler handler = new Handler();
     Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            // Your existing code to update data
            getLastSeenTime(checkedSensors);
            vehicleLastSeen.setText(receivedLastSeenTime);
            // Schedule this runnable again in 60000 milliseconds (1 minute)
            handler.postDelayed(this, 60000);
        }
     };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        titleDeviceText = findViewById(R.id.device_title);
        descriptionDeviceText = findViewById(R.id.device_description);
        saveDeviceButton = findViewById(R.id.save_device_btn);
        titleDeviceTextView = findViewById(R.id.new_device);
        deleteDeviceButton = findViewById(R.id.delete_device_btn);
        pressureGraphButton = findViewById(R.id.pressure_graph_btn);
        temperatureGraphButton = findViewById(R.id.temperature_graph_btn);
        sensorListTitle = findViewById(R.id.sensor_list_title);
        sensorListOptionsView = findViewById(R.id.sensor_list_options);
        scrollableInfoLayout = findViewById(R.id.scrollable_information_content);
        sensorListModulation = findViewById(R.id.sensor_list_modulations);
        sensorListProtocol = findViewById(R.id.sensor_list_protocol);
        vehicleLastSeen = findViewById(R.id.vehicle_last_seen);

        //Receive data logic
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        //Setter for view mode
        if(docId != null && !docId.isEmpty()){
            isViewMode = true;
        }

        titleDeviceText.setText(title);
        descriptionDeviceText.setText(content);

        //View mode enabled logic
        if(isViewMode){
            titleDeviceTextView.setText("Vehicle: " + title);
            deleteDeviceButton.setVisibility(View.VISIBLE);
            sensorListTitle.setVisibility(View.GONE);
            sensorListOptionsView.setVisibility(View.GONE);

            //Retrieve sensor data
            checkedSensors = getIntent().getStringArrayListExtra("checkedSensorsArray");

            //Sort sensors ID so data is consistent
            Collections.sort(checkedSensors);

            //Get modulation and update its corresponding ListView
            getModulationData(checkedSensors);
            sensorListModulation.setText(receivedSensorsModulation);

            //Get protocol and update its corresponding ListView
            getProtocolData(checkedSensors);
            sensorListProtocol.setText(receivedSensorsProtocol);

            //Get last seen time and update its corresponding TextView
            handler.postDelayed(updateTask, 1);

        }
        //Create mode enabled logic
        else {
            scrollableInfoLayout.setVisibility(View.GONE);

            //Logic for database query to retrieve unique sensor id:
            try {
                //Connect to database and query it for unique sensor id
                ConnectionHelper connectionHelper = new ConnectionHelper();
                connectToDb = connectionHelper.getConnection();
                String dbGetSensorsIdQuery = "SELECT DISTINCT id FROM TPMSData";
                Statement st = connectToDb.createStatement();
                ResultSet resSet = st.executeQuery(dbGetSensorsIdQuery);
                //Append the sensors to the sensorID list
                while(resSet.next()){
                    receivedSensorsId.add(resSet.getString(1));
                }
            }
            catch (Exception ex){
                Utility.showToast(DeviceDetailsActivity.this, "Could not retrieve data, check internet connection!");
            }

            //If there are any received sensorsID display them otherwise display dummy data
            if (!receivedSensorsId.isEmpty()) {
                listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, receivedSensorsId);
            }
            else {
                listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, testArray);

            }

            //Update the sensorListView with the right ListAdapter
            sensorListOptionsView.setAdapter(listAdapter);

        }

        //Buttons function calls
        saveDeviceButton.setOnClickListener((v -> saveDevice()));
        pressureGraphButton.setOnClickListener((v) -> pressureGraphOnClick());
        temperatureGraphButton.setOnClickListener((v) -> temperatureGraphOnClick());
        deleteDeviceButton.setOnClickListener((v -> deleteDeviceFromFirebase()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to avoid memory leaks
        handler.removeCallbacks(updateTask);
    }

    void saveDevice(){
        String device_title = titleDeviceText.getText().toString();
        String device_description = descriptionDeviceText.getText().toString();

        if (device_title == null || device_title.isEmpty()){
            titleDeviceText.setError(("Title is required!"));
            return;
        }

        Device device = new Device();

        device.setTitle(device_title);
        device.setContent(device_description);
        device.setTimestamp(Timestamp.now());

        if(!isViewMode) {
            checkedSensors = new ArrayList<>();
            int sensor_counter = 0;

            for (int i = 0; i < sensorListOptionsView.getCount(); i++) {
                if (sensorListOptionsView.isItemChecked(i)) {
                    sensor_counter += 1;
                    checkedSensors.add(sensorListOptionsView.getItemAtPosition(i).toString());
                }
            }

            if(sensor_counter < 2 || sensor_counter > 6){
                titleDeviceText.setError("Number of selected sensors must be between 2 and 6!");
                return;
            }
        }

        device.setCheckedSensorsArray(checkedSensors);


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
                    Utility.showToast(DeviceDetailsActivity.this, "Vehicle configured successfully!");
                    finish();
                }
                else {
                    Utility.showToast(DeviceDetailsActivity.this, "Failed to configure vehicle!");
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
                        Utility.showToast(DeviceDetailsActivity.this, "Vehicle deleted!");
                        finish();
                    }
                    else {
                        Utility.showToast(DeviceDetailsActivity.this, "Failed to delete vehicle!");
                    }
                }
            });
        }

    void pressureGraphOnClick(){
        Intent intent = new Intent(DeviceDetailsActivity.this, GraphActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putStringArrayListExtra("checkedSensors", checkedSensors);
        intent.putExtra("docId", docId);
        intent.putExtra("type", "pressure");
        startActivity(intent);
    }

    void temperatureGraphOnClick(){
        Intent intent = new Intent(DeviceDetailsActivity.this, GraphActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putStringArrayListExtra("checkedSensors", checkedSensors);
        intent.putExtra("docId", docId);
        intent.putExtra("type", "temperature");
        startActivity(intent);
    }
    void getModulationData(ArrayList<String> checkedSensors){
        //Logic for database query to retrieve modulation for each sensor id configured in the vehicle:

        try {
            //Connect to database and query it for modulations for all sensors in the vehicle
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connectToDb = connectionHelper.getConnection();

            //Create the query
            String dbGetSensorsModulationQuery = "SELECT modulation FROM TPMSData WHERE id IN " + "(";
            for (int i = 0;  i<checkedSensors.size(); i++){
                if(i==checkedSensors.size()-1){
                    dbGetSensorsModulationQuery += "'" + checkedSensors.get(i) + "'" +") ";
                }
                else {
                    dbGetSensorsModulationQuery += "'" + checkedSensors.get(i) + "'" +",";
                }
            }
            dbGetSensorsModulationQuery += " GROUP BY id";

            Statement st = connectToDb.createStatement();
            ResultSet resSet = st.executeQuery(dbGetSensorsModulationQuery);
            //Append the sensors to the sensorID list
            int idx = 0;
            while(resSet.next()){
                receivedSensorsModulation  += checkedSensors.get(idx) +" - " + resSet.getString(1) + "\n";
                idx++;
            }
            connectToDb.close();

        }
        catch (Exception ex){
            Utility.showToast(DeviceDetailsActivity.this, "Could not retrieve modulation data, check internet connection!");
        }
    }

    void getProtocolData(ArrayList<String> checkedSensors){
        //Logic for database query to retrieve protocol for each sensor id configured in the vehicle:

        try {
            //Connect to database and query it for protocols for all sensors in the vehicle
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connectToDb = connectionHelper.getConnection();

            //Create the query
            String dbGetSensorProtocolQuery = "SELECT protocol FROM TPMSData WHERE id IN " + "(";
            for (int i = 0;  i<checkedSensors.size(); i++){
                if(i==checkedSensors.size()-1){
                    dbGetSensorProtocolQuery += "'" + checkedSensors.get(i) + "'" +") ";
                }
                else {
                    dbGetSensorProtocolQuery += "'" + checkedSensors.get(i) + "'" +",";
                }
            }
            dbGetSensorProtocolQuery += " GROUP BY id";

            Statement st = connectToDb.createStatement();
            ResultSet resSet = st.executeQuery(dbGetSensorProtocolQuery);
            //Append the sensors to the sensorID list
            int idx = 0;
            while(resSet.next()){
                receivedSensorsProtocol += checkedSensors.get(idx) +" - " + resSet.getString(1) + "\n";
                idx++;
            }

            connectToDb.close();
        }
        catch (Exception ex){
            Utility.showToast(DeviceDetailsActivity.this, "Could not retrieve protocol data, check internet connection!");
        }
    }

    void getLastSeenTime(ArrayList<String> checkedSensors){
        try {
            //Connect to database and query it for protocols for all sensors in the vehicle
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connectToDb = connectionHelper.getConnection();

            //Create the query
            String dbGetSensorProtocolQuery = "SELECT MAX(time) FROM TPMSData WHERE id IN " + "(";
            for (int i = 0;  i<checkedSensors.size(); i++){
                if(i==checkedSensors.size()-1){
                    dbGetSensorProtocolQuery += "'" + checkedSensors.get(i) + "'" +") ";
                }
                else {
                    dbGetSensorProtocolQuery += "'" + checkedSensors.get(i) + "'" +",";
                }
            }

            Statement st = connectToDb.createStatement();
            ResultSet resSet = st.executeQuery(dbGetSensorProtocolQuery);

            //Add last seen time to corresponding variable
            while(resSet.next()){
                receivedLastSeenTime = resSet.getString(1) + "\n";
            }

            connectToDb.close();
        }
        catch (Exception ex){
            Utility.showToast(DeviceDetailsActivity.this, "Could not retrieve last seen data, check internet connection!");
        }
    }

}