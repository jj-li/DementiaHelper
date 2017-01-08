package com.example.jj.dementiahelper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Locations extends AppCompatActivity {

    ArrayList<Button> buttons = new ArrayList<Button>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        String res = loadFile("savedFile.txt");
        LinearLayout layout = (LinearLayout) findViewById(R.id.myList);
        if (res.equals("")) {
            final TextView rowText = new TextView(this);

            //Set name of the button
            rowText.setText("No Locations. \nClick the plus button to add a location.");
            rowText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 100);

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = displaymetrics.widthPixels;
            int height = displaymetrics.heightPixels;
            rowText.setWidth(width);
            rowText.setHeight(height);

            //Add button to the linear layout
            layout.addView(rowText);
        }
        else {
            String[] info = res.split("\n");

            for (final String s : info) {
                if (s.equals("")) {
                    continue;
                }
                final Button rowButton = new Button(this);
                String[] locationInfo = s.split(",");
                //Set name of the button
                rowButton.setText(locationInfo[0]);
                rowButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent nextPage = new Intent(getApplicationContext(),MapsActivity.class);

                        //Pass locationInfo over
                        Bundle bundle = new Bundle();
                        bundle.putString("data", s);
                        nextPage.putExtras(bundle);

                        //Go to the Maps Page
                        finish();
                        startActivity(nextPage);
                    }
                });

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width = displaymetrics.widthPixels;
                int height = displaymetrics.heightPixels;
                rowButton.setWidth(width);
                rowButton.setHeight(height / 10);

                //Add button to the linear layout
                layout.addView(rowButton);

                //save reference to the button
                buttons.add(rowButton);
            }
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

    protected void onStart(Bundle savedInstanceState) {
        onCreate(savedInstanceState);
    }

    public String loadFile(String file) {
        Context context = getApplicationContext();
        String data = "";
        try {
            FileInputStream fin = context.openFileInput(file);
            if (fin != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fin);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while (( line = bufferedReader.readLine() ) != null) {
                    data += line + "\n";
                }
                fin.close();
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
