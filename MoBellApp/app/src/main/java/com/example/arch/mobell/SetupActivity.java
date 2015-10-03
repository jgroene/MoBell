package com.example.arch.mobell;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class SetupActivity extends AppCompatActivity {

    ImageButton cameraButton;
    EditText nameField;
    Bitmap profilePicture = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nameField = (EditText) findViewById(R.id.nameInput);

        // TODO: set default image

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!nameField.getText().toString().isEmpty()) {
                    goToNextActivity();
                } else {
                    Snackbar.make(view, "Please enter your name to continue", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePicture = imageBitmap.createScaledBitmap(imageBitmap, 200, 200, true);
            cameraButton.setImageBitmap(profilePicture);
        }
    }

    private void goToNextActivity() {
        // TODO: check for language packages
        Intent intent = new Intent(this, WaitingActivity.class);
        intent.putExtra("name", nameField.getText().toString());
        startActivity(intent);
        finish();
    }

}
