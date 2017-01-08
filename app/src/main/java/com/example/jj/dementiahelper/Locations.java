package com.example.jj.dementiahelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileInputStream;
import java.util.ArrayList;

public class Locations extends AppCompatActivity {

    ArrayList<Button> buttons = new ArrayList<Button>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        String res = loadFile("savedFile.txt");
        String[] info = res.split("\n");
        LinearLayout layout = (LinearLayout) findViewById(R.id.myList);
        for (String s : info) {
            final Button rowButton = new Button(this);

            //Set name of the button
            rowButton.setText(s);

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = displaymetrics.widthPixels;
            int height = displaymetrics.heightPixels;
            rowButton.setWidth(width);
            rowButton.setHeight(height/10);

            //Add button to the linear layout
            layout.addView(rowButton);

            //save reference to the button
            buttons.add(rowButton);
        }

        ImageButton addNewLocation = (ImageButton) findViewById(R.id.addLocation);
        addNewLocation.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                //setContentView(R.layout.content_add_location);
                finish();
                startActivity(new Intent(getApplicationContext(),AddLocation.class));
            }

        });
    }

    public String loadFile(String file) {
        String data = "";
        FileInputStream inputStream;
        try {
            inputStream = openFileInput(file);
            byte[] bytes = new byte[4096];
            inputStream.read(bytes);
            data = new String(bytes);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
