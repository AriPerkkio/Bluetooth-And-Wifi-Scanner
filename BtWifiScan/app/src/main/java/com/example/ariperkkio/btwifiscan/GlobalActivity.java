package com.example.ariperkkio.btwifiscan;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class GlobalActivity extends Activity implements View.OnClickListener{

    // UI Elements
    private Button btnGetAll;
    private Button btnMap;
    private ListView resultList;
    private TextView servOneText;
    private TextView servTwoText;
    private String activeServer;
    private TextView dbOneText;
    private TextView dbTwoText;
    private TextView dbThreeText;

    private customAdapter listAdapter;
    private ArrayList<scanResult> results;
    private GlobalDbConnection globalDbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_global);
        globalDbConnection = new GlobalDbConnection(this);
        btnGetAll = (Button) findViewById(R.id.globalBtnGetAll);
        btnMap = (Button) findViewById(R.id.globalBtnMap);
        btnGetAll.setOnClickListener(this);
        btnMap.setOnClickListener(this);
        servOneText = (TextView) findViewById(R.id.globalServOne);
        servTwoText = (TextView) findViewById(R.id.globalServTwo);
        dbOneText = (TextView) findViewById(R.id.globalDbOne);
        dbTwoText = (TextView) findViewById(R.id.globalDbTwo);
        dbThreeText = (TextView) findViewById(R.id.globalDbThree);
        results = new ArrayList<scanResult>();
        resultList = (ListView) findViewById(R.id.globalListResults);
        listAdapter = new customAdapter(this, results);
        resultList.setAdapter(listAdapter);

        if(globalDbConnection.pingServerOne()){
            servOneText.setText(R.string.servOneOnline);
            servOneText.setTextColor(getResources().getColor(R.color.ONLINE));
            activeServer = getString(R.string.servOne);
        }
        if(globalDbConnection.pingServerTwo()){
            servTwoText.setText(R.string.servTwoOnline);
            servTwoText.setTextColor(getResources().getColor(R.color.ONLINE));
            if(activeServer==null) activeServer = getString(R.string.servTwo);
        }
        if(globalDbConnection.pingDbOne(activeServer)){
            dbOneText.setText(R.string.dbOneOnline);
            dbOneText.setTextColor(getResources().getColor(R.color.ONLINE));
        }
        if(globalDbConnection.pingDbTwo(activeServer)){
            dbTwoText.setText(R.string.dbTwoOnline);
            dbTwoText.setTextColor(getResources().getColor(R.color.ONLINE));
        }
        if(globalDbConnection.pingDbThree(activeServer)){
            dbThreeText.setText(R.string.dbThreeOnline);
            dbThreeText.setTextColor(getResources().getColor(R.color.ONLINE));
        }
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.globalBtnGetAll:
                List<scanResult> tempList = globalDbConnection.getAllBt(activeServer);
                for(int i=0;i<tempList.size();i++)
                    results.add(tempList.get(i));
                listAdapter.notifyDataSetChanged();
            break;

            case R.id.globalBtnMap:

            break;
        }
    }
}
