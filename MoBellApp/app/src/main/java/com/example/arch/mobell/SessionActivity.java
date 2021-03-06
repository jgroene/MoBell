package com.example.arch.mobell;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.datatype.Duration;

public class SessionActivity extends AppCompatActivity {

    List peers;
    BroadcastReceiver recv;
    TextToSpeech tts;
    Intent abortintent;
    List images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        String[] names = getIntent().getExtras().getStringArray("names");
        String[] macs = getIntent().getExtras().getStringArray("macs");
        peers = new ArrayList();
        images = new ArrayList();
        abortintent = new Intent(getApplicationContext(), P2pService.class).putExtra("message", P2pService.Intents.abort);

        Peer me = new Peer();me.name="You";me.mac="";
        images.add((Bitmap) getIntent().getParcelableExtra("image"));
        peers.add(me);
        for(int i=0;i<names.length;i++) {
            Peer p = new Peer(); p.name=names[i];p.mac=macs[i];p.lost=false;
            peers.add(p);
            images.add(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.contact).createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.contact), 200, 200, true));
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
                        // TODO: das muss in den service rein!!!!!
                        tts.speak(intent.getStringExtra("name")+" is out of range.", TextToSpeech.QUEUE_FLUSH, null);
                        for(int i=0; i<peers.size(); i++) {
                            if(((Peer)peers.get(i)).mac.equals(intent.getStringExtra("mac"))) {
                                peers.remove(i);
                                images.remove(i);
                            }
                        }
                        redraw();
                    }
                    /*if (command == P2pService.Broadcasts.) {
                        for(int i=0; i<peers.size(); i++) {
                            if(((Peer)peers.get(i)).mac.equals(intent.getStringExtra("mac"))) {
                               images.set(i, Intent.getParcable("image"));
                            }
                         }
                    }*/
                    // TODO: on image received broadcast!
                }

            }
        };

        startService(new Intent(getApplicationContext(), P2pService.class).putExtra("message", P2pService.Intents.requestDetails));

        FloatingActionButton butt = (FloatingActionButton)findViewById(R.id.fab);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                stopService(abortintent);
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setMessage("Are you sure you want to quit the group?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
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
            c1.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
            c1.setTypeface(null, Typeface.BOLD);
            c1.setText(((Peer) peers.get(i)).name);
            tr.addView(c1);
            ImageView c2 = new ImageView(this);
            c2.setImageBitmap(((Bitmap)images.get(i)));
            tr.addView(c2);
            View line = new View(this);
            line.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
            line.setBackgroundColor(Color.BLACK);
            tr.addView(line);
            table.addView(tr);
        }
    }

}
