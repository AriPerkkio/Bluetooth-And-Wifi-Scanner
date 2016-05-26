package com.example.ariperkkio.btwifiscan;

import java.util.List;

/**
 * Created by AriPerkkio on 03/05/16.
 * Interface to handle HTTP responses' data
 */
public interface HttpResponsePass {
    // Ping servers and databases
    // Get result counts
    void onResponseRead(String response, String method);

    // Get all results
    // Synchronize results
    void scanResultPass(String method, List<scanResult> results);
}
