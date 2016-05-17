package com.example.ariperkkio.btwifiscan;

import java.util.List;

/**
 * Created by AriPerkkio on 17/05/16.
 * Holder class for 1000's of scanresults, since intent cannot hold more than ~500KB (~2k results)
 * Used when passing results to google maps
 */
public class ResultListHolder {
    static ResultListHolder instance;
    private List<scanResult> results;

    private ResultListHolder(){
        //
    }

    static public ResultListHolder getInstance(){
        if(instance==null) instance = new ResultListHolder();
        return instance;
    }

    public void setResults(List<scanResult> _list){
        results = _list;
    }

    public List<scanResult> getResults(){
        return results;
    }
}
