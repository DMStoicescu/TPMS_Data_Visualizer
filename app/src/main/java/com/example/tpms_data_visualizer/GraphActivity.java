package com.example.tpms_data_visualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;

public class GraphActivity extends AppCompatActivity {

    XYPlot temperaturePlot;
    String title,content,docId;
    ArrayList<String> checkedSensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        temperaturePlot = findViewById(R.id.temperature_plot);

        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        checkedSensors = getIntent().getStringArrayListExtra("checkedSensorsArray");


        //Populate temperature graph
        //So far this is dummy data:
        final Number[] domainLabels = {1,2,3,6,7,8,9,10,13,14};
        Number[] series1Numbers = {1,4,2,8,88,16,8,32,16,64};

        // Turn the above arrays into XYSeries
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Series 1");

        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.GRAY,Color.RED,null,null);


        temperaturePlot.addSeries(series1,series1Format);

        temperaturePlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round( ((Number)obj).floatValue() );
                return toAppendTo.append(domainLabels[i]);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        PanZoom.attach(temperaturePlot);
    }
}