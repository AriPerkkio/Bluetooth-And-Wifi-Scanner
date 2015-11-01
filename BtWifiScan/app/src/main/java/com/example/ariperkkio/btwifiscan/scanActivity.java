package com.example.ariperkkio.btwifiscan;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class scanActivity extends Activity implements View.OnClickListener{

    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_scan);

        back = (Button) findViewById(R.id.scanBack);
        back.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case (R.id.scanBack):
                finish();
            break;
        }

    }

}
