package com.example.jj.dementiahelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileInputStream;
import java.util.ArrayList;

public class Locations extends AppCompatActivity {

    ArrayList<Button> buttons = new ArrayList<Button>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        String res = loadFile("savedFile.txt");
        String[] info = res.split("\n");
        for (String s : info) {
            final Button rowButton = new Button(this);

            // set some properties of rowTextView or something

            rowButton.setText("This is row #" + i);

            // add the textview to the linearlayout
            myList.addView(rowTextView);

            // save a reference to the textview for later
            myTextViews[i] = rowTextView;
        }
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
