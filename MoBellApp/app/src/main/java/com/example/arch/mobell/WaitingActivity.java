package com.example.arch.mobell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WaitingActivity extends AppCompatActivity {
//TODO add image capabilities
    private boolean foundOthers = false;
    List peers;
    Intent tempint1;
    Intent tempint2;
    BroadcastReceiver recv;
    TextView waitingText;
    public Handler handler;
    public int dots = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((FloatingActionButton)findViewById(R.id.fab)).setVisibility(View.INVISIBLE);

        waitingText = (TextView)findViewById(R.id.textView3);
        handler = new Handler();
        Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (handler != null) {
                    dots++;
                    if (dots>3) {dots = 1;}
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waitingText.setText("Waiting for other users" + new String(new char[dots]).replace("\0", ".") + new String(new char[3 - dots]).replace("\0", " "));
                        }
                    });
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(tick, 1000);

        peers = new ArrayList();
        tempint1 = (new Intent(getApplicationContext(), P2pService.class).putExtra("message", P2pService.Intents.startSession));
        tempint2 = new Intent(getApplicationContext(), SessionActivity.class);

        recv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                synchronized (this) {
                    P2pService.Broadcasts command = (P2pService.Broadcasts) intent.getSerializableExtra("message");
                    if (command == P2pService.Broadcasts.onDeviceFound) {
                        Peer p = new Peer();p.name = intent.getStringExtra("name");p.mac = intent.getStringExtra("mac");
                        peers.add(p);
                    }
                    if (command == P2pService.Broadcasts.onStartSession) {
                        Intent intnt = new Intent(WaitingActivity.this, SessionActivity.class);
                        String[] macs = new String[peers.size()];
                        String[] names = new String[peers.size()];
                        for(int i=0;i<peers.size();i++){
                            macs[i] = ((Peer)peers.get(i)).mac;
                            names[i] = ((Peer)peers.get(i)).name;
                        }
                        intnt.putExtra("macs", macs);
                        intnt.putExtra("names", names);
                        startActivity(intnt);
                        finish();
                    }
                    // TODO: ondevicelost, onabourt
                    redraw();
                }

            }
        };
        registerReceiver(recv, new IntentFilter("p2pservice"));

        Log.e("Start", "thefing service");
        startService(new Intent(this, P2pService.class).putExtra("name", getIntent().getStringExtra("name")));

    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        unregisterReceiver(recv);
        if (!foundOthers) {
            startService(new Intent(getApplicationContext(), P2pService.class).putExtra("message", P2pService.Intents.abort));
        }
    }

    private void redraw() {
        TableLayout table = (TableLayout)findViewById(R.id.mytable);
        table.removeAllViews();
        table.setStretchAllColumns(true);
        table.bringToFront();
        boolean dark = true;
        for(int i = 0; i < peers.size(); i++){
            TableRow tr =  new TableRow(this);
            TextView c1 = new TextView(this);
            c1.setTextColor(Color.WHITE);
            c1.setTextSize(TypedValue.COMPLEX_UNIT_PT, 20);
            c1.setTypeface(null, Typeface.BOLD);
            c1.setText(((Peer)peers.get(i)).name);
            if(dark) {c1.setBackgroundColor(Color.rgb(0x02, 0x77, 0xbd));}
            else {c1.setBackgroundColor(Color.rgb(0x4f, 0xc3, 0xf7));}
            TextView c2 = new TextView(this);
            c2.setTextColor(Color.WHITE);
            c2.setTextSize(TypedValue.COMPLEX_UNIT_PT, 20);
            c2.setText(((Peer) peers.get(i)).mac);
            if(dark) {c2.setBackgroundColor(Color.rgb(0x02, 0x77, 0xbd));}
            else {c2.setBackgroundColor(Color.rgb(0x4f, 0xc3, 0xf7));}
            tr.addView(c1);
            tr.addView(c2);
            table.addView(tr);
            dark = !dark;
        }
        TableRow tr = new TableRow(this);
        ((FloatingActionButton)findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] macs = new String[peers.size()];
                String[] names = new String[peers.size()];
                for (int i = 0; i < peers.size(); i++) {
                    macs[i] = ((Peer) peers.get(i)).mac;
                    names[i] = ((Peer) peers.get(i)).name;
                }
                tempint1.putExtra("macs", macs.clone());
                tempint1.putExtra("names", names.clone());
                tempint2.putExtra("macs", macs.clone());
                tempint2.putExtra("names", names.clone());
                startService(tempint1);
                startActivity(tempint2);
                finish();
            }
        });
        ((FloatingActionButton)findViewById(R.id.fab)).setVisibility(View.VISIBLE);
        table.addView(tr);
    }

}