package com.example.ariperkkio.btwifiscan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button buttonNew;
    private Button buttonHistory;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_main);

        buttonNew = (Button) findViewById(R.id.mainBtnNewScan);
        buttonHistory = (Button) findViewById(R.id.mainBtnHistory);
        buttonNew.setOnClickListener(this);
        buttonHistory.setOnClickListener(this);

    }

    public void onClick (View v) {
        switch(v.getId()) {
            case (R.id.mainBtnNewScan):
                //do stuff
                //Toast.makeText(this, "New scan", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, newScanActivity.class); //Create intent for newScanActivity
                startActivity(intent);
            break;

            case (R.id.mainBtnHistory):
                //do stuff
                //Toast.makeText(this, "History", Toast.LENGTH_SHORT).show();
            break;
        }
    }
}
