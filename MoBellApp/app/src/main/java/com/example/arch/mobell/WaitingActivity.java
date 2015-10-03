package com.example.arch.mobell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

public class WaitingActivity extends AppCompatActivity {
//TODO add image capabilities
    private boolean foundOthers = false;
    List peers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        peers = new ArrayList();

        BroadcastReceiver recv = new BroadcastReceiver() {
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
        startService(new Intent(this, P2pService.class));

    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        if (!foundOthers) {
            startService(new Intent(getApplicationContext(), P2pService.class).putExtra("message", P2pService.Intents.abort));
        }
    }

    private void redraw() {
        ((TextView)findViewById(R.id.textView3)).setText("");
        TableLayout table = (TableLayout)findViewById(R.id.mytable);
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
        TableRow tr = new TableRow(this);
        Button butt = new Button(this);
        butt.setText("Go!");
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(getApplicationContext(), P2pService.class).putExtra("message", P2pService.Intents.startSession));
                Intent intent = new Intent(WaitingActivity.this, SessionActivity.class);
                startActivity(intent);
                finish();
            }
        });
        tr.addView(butt);
        table.addView(tr);
    }

}