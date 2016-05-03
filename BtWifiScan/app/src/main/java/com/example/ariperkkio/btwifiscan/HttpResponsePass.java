package com.example.ariperkkio.btwifiscan;

import java.util.List;

/**
 * Created by AriPerkkio on 03/05/16.
 * Interface to handle HTTP responses' data
 */
public interface HttpResponsePass {
    void onResponseRead(String response); // TODO: Remove later
    void onResponseRead(String response, String method);
    void scanResultPass(String method, List<scanResult> results);
}
