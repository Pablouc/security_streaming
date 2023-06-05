package com.embebidos.image_streaming;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.io.IOException;



public class MainActivity extends AppCompatActivity {
    //private UdpClient UdpClient;
   private static final String TAG = "APIresponse";
    Button alert_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UdpClient = new UdpClient();
        //UdpClient.startListening();

        alert_btn = (Button) findViewById(R.id.alert_btn);

        alert_btn.setOnClickListener(v -> {
            performPostRequest();
            //Toast.makeText(getBaseContext(),"Se ha generado una alerta", Toast.LENGTH_SHORT).show();

        });

    }

   /* @Override
    protected void onDestroy() {
        super.onDestroy();
        if (UdpClient != null) {
            UdpClient.stopListening();
        }
    }*/

   private void performPostRequest() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String apiUrl = "http://192.168.100.5:5000/books"; // Replace with your Flask API endpoint

                try {

                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);


                    String requestBody = "{\"id\": 3, \"title\": \"Book 3\", \"author\": \"Author 3\"}"; // JSON request body

                    Log.d(TAG, "Nivel 1");
                    try {
                        Log.d(TAG, "Me mame 1");
                        OutputStream outputStream = connection.getOutputStream();
                        Log.d(TAG, "Me mame 2");
                        outputStream.write(requestBody.getBytes());
                        Log.d(TAG, "Me mame 3");
                        outputStream.flush();

                        Log.d(TAG, "Me mame 4");
                        outputStream.close();

                        Log.d(TAG, "Me mame 1.1");

                        // Rest of the code
                    } catch (IOException e) {
                        Log.e(TAG, "Error while getting output stream: " + e.getMessage());
                        e.printStackTrace();
                    }


                    Log.d(TAG, "mame new ");
                    int responseCode = connection.getResponseCode();

                    Log.d(TAG, "responde code: " + Integer.toString(responseCode));
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        Log.d(TAG, "Me mame 4");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        String result = response.toString();
                        Log.d(TAG, "Alarm sent: " + result);

                        // Handle the response here
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(getBaseContext(),result, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.d(TAG, "Me mame 5");
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }




}