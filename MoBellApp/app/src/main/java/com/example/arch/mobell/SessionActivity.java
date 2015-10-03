package com.example.arch.mobell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionActivity extends AppCompatActivity {

    List peers;
    BroadcastReceiver recv;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String[] names = getIntent().getExtras().getStringArray("names");
        String[] macs = getIntent().getExtras().getStringArray("macs");
        peers = new ArrayList();

        for(int i=0;i<names.length;i++) {
            Peer p = new Peer(); p.name=names[i];p.mac=macs[i];p.lost=false;
            peers.add(p);
        }

        peers = new ArrayList();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });
        recv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                synchronized (this) {
                    P2pService.Broadcasts command = (P2pService.Broadcasts) intent.getSerializableExtra("message");
                    if (command == P2pService.Broadcasts.onDeviceLost) {
                        tts.speak(intent.getStringExtra("name")+" is out of range.", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }

            }
        };

        startService(new Intent(getApplicationContext(), P2pService.class).putExtra("message", P2pService.Intents.requestDetails));

        Button butt = (Button)findViewById(R.id.button);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(getApplicationContext(), P2pService.class).putExtra("message", P2pService.Intents.abort));
            }
        });

        redraw();
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        unregisterReceiver(recv);
    }

    private void redraw() {
        TableLayout table = (TableLayout)findViewById(R.id.mytable2);
        table.removeAllViews();
        table.setStretchAllColumns(true);
        table.bringToFront();
        for(int i = 0; i < peers.size(); i++){
            TableRow tr =  new TableRow(this);
            TextView c1 = new TextView(this);
            c1.setText(((Peer)peers.get(i)).name);
            TextView c2 = new TextView(this);
            c2.setText(((Peer) peers.get(i)).mac);
            tr.addView(c1);
            tr.addView(c2);
            table.addView(tr);
        }
    }

}
