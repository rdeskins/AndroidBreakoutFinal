package com.example.rdeskinsfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener, Observer {
    private PlayArea playArea;
    private PlayArea.GameObservable observable;
    private TextView levelTView, ballTView, scoreTView, brickTView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playArea = findViewById(R.id.playArea);
        playArea.setTag("playArea");
        playArea.setOnClickListener(this);

        View leftButton = findViewById(R.id.left);
        View rightButton = findViewById(R.id.right);
        leftButton.setTag("left");
        rightButton.setTag("right");
        leftButton.setOnTouchListener(this);
        rightButton.setOnTouchListener(this);

        levelTView = findViewById(R.id.levelText);
        ballTView = findViewById(R.id.ballText);
        scoreTView = findViewById(R.id.scoreText);
        brickTView = findViewById(R.id.brickText);
    }

    @Override
    protected void onPause() {
        playArea.pauseAll();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        observable = playArea.getObservable();
        observable.addObserver(this);
        setText();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstUse = prefs.getBoolean("first_use",true);
        if (firstUse) {
            prefs.edit().putBoolean("first_use", false).apply();
            prefs.edit().putString("brickCount", "5").apply();
            prefs.edit().putString("brickHits", "2").apply();
            prefs.edit().putString("balls", "3").apply();
            prefs.edit().putString("paddleSens", "50").apply();
            prefs.edit().putString("paddleWidth", "30").apply();
        }
        int brickCount = Integer.parseInt(prefs.getString("brickCount", "5"));
        int brickHits = Integer.parseInt(prefs.getString("brickHits", "2"));
        int balls = Integer.parseInt(prefs.getString("balls", "3"));
        int paddleSens = Integer.parseInt(prefs.getString("paddleSens", "50"));
        int paddleWidth = Integer.parseInt(prefs.getString("paddleWidth", "30"));
        playArea.setPreferences(brickCount, brickHits, balls, paddleSens, paddleWidth);
        playArea.pauseMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onAbout(MenuItem item) {
        Toast.makeText(this,
                "Breakout Project, Spring 2020, Robin R Deskins",
                Toast.LENGTH_SHORT)
                .show();
    }

    public void onSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        String tag = v.getTag().toString();
        if (tag.equals("playArea")) {
            if (playArea.getPaused()) {
                playArea.resume();
            }
            else {
                playArea.pauseMenu();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean moving = false;
        int motionEvent = event.getAction();
        if (motionEvent == MotionEvent.ACTION_DOWN) {
            moving = true;
        }

        String tag = v.getTag().toString();
        if (tag.equals("left")) {
            playArea.setPaddleMovingLeft(moving);
        }
        else if (tag.equals("right")) {
            playArea.setPaddleMovingRight(moving);
        }

        return true;
    }

    @Override
    public void update(Observable o, Object arg) {
        setText();
    }

    private void setText() {
        String levelText = "Level = " + observable.getLevel();
        String ballText = "Balls = " + observable.getBallCount();
        String scoreText = "Score = " + observable.getScore();
        String brickText = "Bricks = " + observable.getBrickNum();
        levelTView.setText(levelText);
        ballTView.setText(ballText);
        scoreTView.setText(scoreText);
        brickTView.setText(brickText);
    }
}
