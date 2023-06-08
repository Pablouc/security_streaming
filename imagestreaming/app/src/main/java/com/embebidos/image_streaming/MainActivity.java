package com.embebidos.image_streaming;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.io.IOException;
import com.bumptech.glide.Glide;


public class MainActivity extends AppCompatActivity {
    //private UdpClient UdpClient;
   private static final String TAG = "APIresponse";
    private static final int INTERVAL_MS = 1000; // Interval between each GET request in milliseconds
    Button alert_btn;

    Button picture_btn;
    private ImageView imageView;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageView = findViewById(R.id.imageView);
        handler = new Handler();

        alert_btn = (Button) findViewById(R.id.alert_btn);


        alert_btn.setOnClickListener(v -> {
            performPostRequest(true);
            //Toast.makeText(getBaseContext(),"Se ha generado una alerta", Toast.LENGTH_SHORT).show();

        });

        picture_btn = (Button) findViewById(R.id.takePic);


        picture_btn.setOnClickListener(v -> {
            picturePostRequest();
            //Toast.makeText(getBaseContext(),"Se ha generado una alerta", Toast.LENGTH_SHORT).show();

        });

        // Schedule a task to fetch the image every 1 second
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchImage();
            }
        }, 0, INTERVAL_MS);

    }


    private void fetchImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Perform the GET request to retrieve the image
                    byte[] imageData = HttpUtils.get("http://192.168.18.21:5000/image");


                    // Update the UI on the main thread
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Display the image using Glide library
                            Glide.with(MainActivity.this)
                                    .load(imageData)
                                    .into(imageView);
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Error while fetching image: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

   private void performPostRequest(boolean signal) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String apiUrl = "http://192.168.18.21:5000/signal"; // Replace with your Flask API endpoint

                try {

                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);


                    String requestBody = "{\"signal\": " + signal + "}"; // JSON request body

                    Log.d(TAG, "Nivel 1");
                    try {
                        OutputStream outputStream = connection.getOutputStream();
                        outputStream.write(requestBody.getBytes());
                        outputStream.flush();
                        outputStream.close();

                        // Rest of the code
                    } catch (IOException e) {
                        Log.e(TAG, "Error while getting output stream: " + e.getMessage());
                        e.printStackTrace();
                    }



                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
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
                        Log.d(TAG, "Error updating signal");
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void picturePostRequest() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String apiUrl = "http://192.168.18.21:5000/picture"; // Replace with your Flask API endpoint

                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Handle the successful response here
                        // For example, display a Toast message
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "POST request successful", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle the unsuccessful response here
                        // For example, display an error message
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "POST request failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }




}