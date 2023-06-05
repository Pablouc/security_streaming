package com.embebidos.image_streaming;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UdpClient {
    private static final String TAG = "UdpClient";
    private static final int BUFFER_SIZE = 1024;
    private static final int PORT = 8889; // Replace with the server's port
    private boolean isRunning;
    private Thread receiveThread;

    public void startListening() {
        isRunning = true;

        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socket = null;

                try {
                    socket = new DatagramSocket(PORT);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    Log.d(TAG, "Start listening");
                    System.out.println("Mamani pelona!");

                    while (isRunning) {
                        socket.receive(packet);
                        byte[] receivedData = packet.getData();
                        int receivedLength = packet.getLength();
                        String receivedMessage = new String(receivedData, 0, receivedLength);
                        if (receivedLength > 0) {
                            Log.d(TAG, "Received message: " + receivedMessage);
                        } else {
                            Log.d(TAG, "No data received from the socket");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
            }
        });

        receiveThread.start();
    }

    public void stopListening() {
        isRunning = false;

        if (receiveThread != null) {
            try {
                receiveThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}