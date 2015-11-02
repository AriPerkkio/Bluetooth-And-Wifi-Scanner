package com.example.ariperkkio.btwifiscan;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class scanActivity extends Activity implements View.OnClickListener{

    Button back;

    // booleans to check options for bluetooth
    boolean btStatus;
    boolean btDevName;
    boolean btDevAddr;
    boolean btDevType;
    boolean btRSSI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_scan);

        back = (Button) findViewById(R.id.scanBack);
        back.setOnClickListener(this);

        // Get options for Bluetooth scanning
        if(btStatus = getIntent().getExtras().getBoolean("btStatus")) {
            btDevName = getIntent().getExtras().getBoolean("btDevName");
            btDevAddr = getIntent().getExtras().getBoolean("btDevAddr");
            btDevType = getIntent().getExtras().getBoolean("btDevType");
            btRSSI = getIntent().getExtras().getBoolean("btRSSI");
        }

    }

    public void onClick(View v) {
        switch(v.getId()) {
            case (R.id.scanBack):
                finish();
            break;
        }

    }

}
