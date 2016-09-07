package com.example.fenji.androidchat;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AndroidChatClient extends AppCompatActivity {
    final String HOST_ADDRESS = "10.0.0.139";
    final int PORT_NUMBER = 8005;
    Socket clientConnection = null;
    PrintWriter outToServer = null;
    BufferedReader inFromServer = null;
    ArrayAdapter<String> items;
    ListView list;
    EditText text;
    Button addButton, startButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_chat_client);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        list = (ListView) findViewById(R.id.listView);
        text = (EditText) findViewById(R.id.inputTextLine);
        addButton = (Button) findViewById(R.id.button);
        startButton = (Button) findViewById(R.id.button2);
        stopButton = (Button) findViewById(R.id.button3);
        items = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(items);
        // addButton.setOnClickListener(this);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    System.out.println("Connecting to " + HOST_ADDRESS + " on port " + PORT_NUMBER + " ...");
                    clientConnection = new Socket(HOST_ADDRESS, PORT_NUMBER);

                    System.out.println("Connected!");

                    // once we connect to the server, we also have an input and output stream
                    outToServer = new PrintWriter(clientConnection.getOutputStream(), true);
                    inFromServer = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
                    System.out.println("Input and Output Streams initialized");

                    // we could replace this with a name we get from the user
                    outToServer.println("name=" + "client-baseline");
                    // make sure we capture the response coming back from the server
                    String serverResponse = inFromServer.readLine();
                    System.out.println("Server says: " + serverResponse);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clientConnection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverResponse = "";
                String userMessage = text.getText().toString();
                try {
                    if (userMessage.equalsIgnoreCase("history")) {
                        outToServer.println(userMessage);
                        serverResponse = inFromServer.readLine();
                        items.add("** Message History **");
                        while (serverResponse != null && !serverResponse.equalsIgnoreCase("TX::HISTORY::END")) {
                            items.add(serverResponse);
                            serverResponse = inFromServer.readLine();
                        }
                        items.add("** End Message History **");
                        text.setText("");
                    } else {
                        outToServer.println(userMessage);
                        items.add(inFromServer.readLine());
                        text.setText("");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
