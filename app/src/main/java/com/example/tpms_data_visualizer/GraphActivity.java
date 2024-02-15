package com.example.tpms_data_visualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;


public class GraphActivity extends AppCompatActivity {

    XYPlot temperaturePlot1,temperaturePlot2,temperaturePlot3,temperaturePlot4,temperaturePlot5,temperaturePlot6;
    String title,content,docId;
    ArrayList<String> checkedSensors;
    Connection connectToDb = null;
    SensorHashMap temperature_map;
    HashMap<String, SensorHashMap> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        temperaturePlot1 = findViewById(R.id.temperature_plot_1);
//        temperaturePlot2 = findViewById(R.id.temperature_plot_2);
//        temperaturePlot3 = findViewById(R.id.temperature_plot_3);
//        temperaturePlot4 = findViewById(R.id.temperature_plot_4);
//        temperaturePlot5 = findViewById(R.id.temperature_plot_5);
//        temperaturePlot6 = findViewById(R.id.temperature_plot_6);
//
//        ArrayList<XYPlot> plot_arr = new ArrayList<>();
//        plot_arr.add(temperaturePlot1);
//        plot_arr.add(temperaturePlot2);
//        plot_arr.add(temperaturePlot3);
//        plot_arr.add(temperaturePlot4);
//        plot_arr.add(temperaturePlot5);
//        plot_arr.add(temperaturePlot6);



        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        checkedSensors = getIntent().getStringArrayListExtra("checkedSensors");

        getTemperatureData(checkedSensors);

        int idx = -1;
        for (String key: map.keySet()) {
            idx ++;
            Log.e("ERROR", "NUIELEEEE");
            Log.e("ERROR", key);

            temperaturePlot1.setVisibility(View.VISIBLE);

            for (Float value: map.get(key).values) {
                System.out.println(map);

                Log.e("ERROR", key + " " + String.valueOf(value));
            }

            temperaturePlot1.addSeries(new SimpleXYSeries(map.get(key).values,
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,key),new LineAndPointFormatter(generateNewColor(),generateNewColor(),null,null));

            temperaturePlot1.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    int i = Math.round( ((Number)obj).floatValue());
                    return toAppendTo.append(map.get(key).dates.get(i));
                }

                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            });


        }


//        final Number[] domainLabels1 = {100,200,300,600,700,800,900,100,130,140};
//        Number[] series1Numbers1 = {1,4,2,8,88,16,8,32,16,64};
//
//        Number[] series1Numbers2 = {10,40,20,80,880,160,80,320,160,640};
//
//        // Turn the above arrays into XYSeries
//        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers1),
//                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Series 1");
//
//        LineAndPointFormatter series1Format1 = new LineAndPointFormatter(Color.RED,Color.GREEN,null,null);
//
//        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series1Numbers2),
//                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Series 2");
//
//        LineAndPointFormatter series1Format2 = new LineAndPointFormatter(Color.WHITE,Color.BLUE,null,null);
//
//
//        temperaturePlot.addSeries(series1,series1Format1);
//        temperaturePlot.addSeries(series2,series1Format2);

//        temperaturePlot1.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
//            @Override
//            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
//                int i = Math.round( ((Number)obj).floatValue() );
//                return toAppendTo.append(domainLabels1[i]);
//            }
//
//            @Override
//            public Object parseObject(String source, ParsePosition pos) {
//                return null;
//            }
//        });

        PanZoom.attach(temperaturePlot1);

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
                temperature_map = new SensorHashMap();

                String dbGetSensorProtocolQuery = "SELECT DISTINCT time, temperature_C, id FROM TPMSData WHERE id ='"+ checkedSensors.get(i) +"'";
                dbGetSensorProtocolQuery += " ORDER BY time ASC";
                Statement st = connectToDb.createStatement();
                ResultSet resSet = st.executeQuery(dbGetSensorProtocolQuery);

                String id = "sample";
                //Add last seen time to corresponding variable
                while(resSet.next()){
                    temperature_map.values.add(resSet.getFloat("temperature_C"));
                    temperature_map.dates.add(resSet.getString("time"));
                    id = resSet.getString("id");
                }

                this.map.put(id,temperature_map);
            }

            connectToDb.close();

        }
        catch (Exception ex){
            Log.e("ERROR", String.valueOf(ex));
            Utility.showToast(GraphActivity.this, "Could not retrieve last seen data, check internet connection");
        }
    }
}