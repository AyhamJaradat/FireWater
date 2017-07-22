package com.game.firewater;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }


    public void openClickSinglePlayerGame(View v) {
        Intent intent = new Intent(this, SingleClickGame.class);
        startActivity(intent);
    }
    public void openClickMultiPlayerGame(View v) {
        Intent intent = new Intent(this, MultiPlayerClickGame.class);
        startActivity(intent);
    }
}
