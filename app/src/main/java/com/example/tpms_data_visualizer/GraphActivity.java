package com.example.tpms_data_visualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;


public class GraphActivity extends AppCompatActivity {

    XYPlot data_plot;
    TextView titleTextView;
    String title,content,docId, graph_type;
    ArrayList<String> checkedSensors;
    Connection connectToDb = null;
    SensorHashMap data_map;
    HashMap<String, SensorHashMap> map;
    ArrayList<ArrayList<Integer>> colours = new ArrayList<ArrayList<Integer>>();
    Handler handler = new Handler();
    Runnable updateGraph = new Runnable() {
        @Override
        public void run() {
            // Your existing code to update data
            plotGraphData();
            data_plot.getGraph().setDomainGridLinePaint(null);
            // Schedule this runnable again in 60000 milliseconds (1 minute)
            handler.postDelayed(this, 60000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        titleTextView = findViewById(R.id.data_text);
        data_plot = findViewById(R.id.data_plot);


        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        checkedSensors = getIntent().getStringArrayListExtra("checkedSensors");
        graph_type = getIntent().getStringExtra("type");

        if (graph_type.equals("pressure")) {
            titleTextView.setText("Pressure Plot");
            getPressureData(checkedSensors);
        } else if (graph_type.equals("temperature")) {
            titleTextView.setText("Temperature Plot");
            getTemperatureData(checkedSensors);
        } else {
            Utility.showToast(GraphActivity.this, "Something went wrong in getting type of the graph");
            finish();
        }

        //Generate colors for consistency when refreshing
        for (String key: map.keySet()) {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            temp.add(generateNewColor());
            temp.add(generateNewColor());
            colours.add(temp);
        }

        handler.postDelayed(updateGraph, 1);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to avoid memory leaks
        handler.removeCallbacks(updateGraph);
    }
    public static int generateNewColor() {
        Random random = new Random();
        // 255 for fully opaque
        int alpha = 255;
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        return Color.argb(alpha, red, green, blue);
    }

    void getTemperatureData(ArrayList<String> checkedSensors){
        try {
            //Redo maps
            map = new HashMap<>();

            //Connect to database and query it for protocols for all sensors in the vehicle
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connectToDb = connectionHelper.getConnection();

            //Create the query
            for (int i = 0;  i<checkedSensors.size(); i++){
                data_map = new SensorHashMap();

                String dbGetSensorProtocolQuery = "SELECT DISTINCT time, temperature_C, id FROM TPMSData WHERE id ='"+ checkedSensors.get(i) +"'";
                dbGetSensorProtocolQuery += " ORDER BY time ASC";
                Statement st = connectToDb.createStatement();
                ResultSet resSet = st.executeQuery(dbGetSensorProtocolQuery);

                String id = "sample";
                //Add last seen time to corresponding variable
                while(resSet.next()){
                    data_map.values.add(resSet.getFloat("temperature_C"));
                    data_map.dates.add(resSet.getString("time"));
                    id = resSet.getString("id");
                }

                this.map.put(id,data_map);
            }

            connectToDb.close();

        }
        catch (Exception ex){
            Log.e("ERROR", String.valueOf(ex));
            Utility.showToast(GraphActivity.this, "Could not retrieve last seen data, check internet connection");
        }
    }

    void getPressureData(ArrayList<String> checkedSensors){
        try {
            //Redo maps
            map = new HashMap<>();

            //Connect to database and query it for protocols for all sensors in the vehicle
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connectToDb = connectionHelper.getConnection();

            //Create the query
            for (int i = 0;  i<checkedSensors.size(); i++){
                data_map = new SensorHashMap();

                String dbGetSensorProtocolQuery = "SELECT DISTINCT time, pressure_KPa, id FROM TPMSData WHERE id ='"+ checkedSensors.get(i) +"'";
                dbGetSensorProtocolQuery += " ORDER BY time ASC";
                Statement st = connectToDb.createStatement();
                ResultSet resSet = st.executeQuery(dbGetSensorProtocolQuery);

                String id = "sample";
                //Add last seen time to corresponding variable
                while(resSet.next()){
                    data_map.values.add(resSet.getFloat("pressure_KPa"));
                    data_map.dates.add(resSet.getString("time"));
                    id = resSet.getString("id");
                }

                this.map.put(id,data_map);
            }

            connectToDb.close();

        }
        catch (Exception ex){
            Log.e("ERROR", String.valueOf(ex));
            Utility.showToast(GraphActivity.this, "Could not retrieve last seen data, check internet connection");
        }
    }


    void plotGraphData(){
        if (graph_type.equals("pressure")) {
            getPressureData(checkedSensors);
        } else if (graph_type.equals("temperature")) {
            getTemperatureData(checkedSensors);
        } else {
            Utility.showToast(GraphActivity.this, "Something went wrong in getting type of the graph");
            finish();
        }
        data_plot.clear();
        int idx = -1;
        for (String key: map.keySet()) {
            idx ++;
            data_plot.setVisibility(View.VISIBLE);


            data_plot.addSeries(new SimpleXYSeries(map.get(key).values,
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,key),new LineAndPointFormatter(colours.get(idx).get(0),colours.get(idx).get(1),null,null));

            data_plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    int i = (int) Math.round(((Number) obj).doubleValue());
                    String[] time = map.get(key).dates.get(i).split(" ");
                    return toAppendTo.append(time[1]);
                }

                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            });


        }


        PanZoom.attach(data_plot, PanZoom.Pan.VERTICAL, PanZoom.Zoom.STRETCH_VERTICAL);

    }
}