package com.example.rdeskinsfinal;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.Observable;
import java.util.Random;

public class PlayArea extends View implements TimeAnimator.TimeListener {
    private static final int brickWidth = 18;
    private static final int brickHeight = 6;
    private static final int bricksPerRow = 10;
    private static final int bricksPerColumn = 10;
    private static final int viewWidth = (brickWidth*bricksPerRow);
    private static final int viewHeight = brickHeight*bricksPerColumn*2;
    private static final int paddleHeight = 2;
    private static final double maxPaddleAngle = 67.5;
    private static final int ballRadius = brickHeight/2;
    private static final float[] initialBallPos = {(float)viewWidth/2, (float)viewHeight*3/4};
    private static final float initialBallVel = ((float)viewHeight/1000)*(float).42;
    private static final int green = Color.rgb(141, 242, 78);
    private static final int yellow = Color.rgb(242, 234, 78);
    private static final int orange = Color.rgb(242, 160, 78);
    private static final int red = Color.rgb(242, 78, 78);
    private static final int[] stateColors = {0, red, orange, yellow, green};
    private static final int darkBlue = Color.rgb(0, 0, 136);
    private static final int ballColor = Color.rgb(41, 192, 230);
    private static final int faintBlack = Color.argb(50,0,0,0);
    //End design parameters

    private int cWidth, cHeight;
    private Rect rect;
    private Paint paint;

    private int mStartBrickNum, mStartBallCount;
    private int mMaxBrickHits;
    private int[][] mBrickStates;
    private int mPaddleOffset;
    private boolean mPaddleMoveLeft,mPaddleMoveRight;
    private boolean mPaused;
    private int mPaddleWidth;
    private float mPaddleVelocity;
    private float mBallXPos, mBallYPos;
    private float mBallXVelocity, mBallYVelocity;
    private TimeAnimator mTimer;
    private GameObservable mObservable;
    private SoundPool soundPlayer;
    private int hitSound, lostSound;

    public PlayArea(Context context) {
        super(context);
        init();
    }

    public PlayArea(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaused = true;
        rect = new Rect();
        mTimer = new TimeAnimator();
        mTimer.setTimeListener(this);
        mObservable = new GameObservable();
        mTimer.start();
        soundPlayer = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        hitSound = soundPlayer.load(getContext(), R.raw.ball_hit, 1);
        lostSound = soundPlayer.load(getContext(), R.raw.ball_lost, 1);
    }

    private void createFirstLevel() {
        pauseMenu();
        mObservable.setLevel(1);
        mObservable.setScore(0);
        mObservable.setBrickNum(mStartBrickNum);
        mObservable.setBallCount(mStartBallCount);
        createBrickStates();
        mPaddleOffset = viewWidth/2;
        setPaddleMovingLeft(false);
        setPaddleMovingRight(false);
        mBallXVelocity = 0;
        mBallYVelocity = initialBallVel;
        mBallXPos = initialBallPos[0];
        mBallYPos = initialBallPos[1];
    }

    private void createNextLevel() {
        pauseMenu();
        mObservable.setLevel(mObservable.getLevel() + 1);
        mObservable.setBrickNum(mStartBrickNum);
        mObservable.setBallCount(mStartBallCount);
        createBrickStates();
        mPaddleOffset = viewWidth/2;
        setPaddleMovingLeft(false);
        setPaddleMovingRight(false);
        float ballVelocity = (float)Math.sqrt(Math.pow(mBallXVelocity,2) + Math.pow(mBallYVelocity,2));
        mBallXVelocity = 0;
        mBallYVelocity = (float)(1.33)*ballVelocity;
        mBallXPos = initialBallPos[0];
        mBallYPos = initialBallPos[1];
    }

    private void createBrickStates() {
        mBrickStates = new int[bricksPerRow][bricksPerColumn];
        Random rand = new Random();
        int row, col;
        for (int i = 0; i < mObservable.getBrickNum(); i ++) {
            row = rand.nextInt(bricksPerRow);
            col = rand.nextInt(bricksPerColumn);
            if (mBrickStates[row][col] == 0) {
                mBrickStates[row][col] = mMaxBrickHits;
            }
            else {
                i--;
            }
        }
    }

    public void setPreferences(int startBricks, int brickHits, int startBalls, int paddleSens, int paddleWidth) {
        boolean restart = false;
        if (startBricks != mStartBrickNum || brickHits != mMaxBrickHits || startBalls != mStartBallCount) {
            restart = true;
        }
        mStartBrickNum = startBricks;
        mMaxBrickHits = brickHits;
        mStartBallCount = startBalls;
        double decimal = paddleSens*.01;
        mPaddleVelocity = (float)(decimal*viewWidth/1000);
        mPaddleWidth = paddleWidth;
        if (restart)
            createFirstLevel();
    }

    @Override
    public void onDraw(Canvas canvas) {
        float x = ((float) cWidth)/viewWidth;
        float y = ((float) cHeight)/viewHeight;
        canvas.scale(x,y);
        canvas.drawRGB(255,255,255); //background
        drawWall(canvas);
        drawPaddle(canvas);
        drawBall(canvas);
        if (mPaused) {
            drawPauseScreen(canvas);
        }
    }

    private void drawWall(Canvas canvas) {
        paint = new Paint();
        rect = new Rect(0,0,brickWidth,brickHeight);
        for (int i = 0; i < bricksPerRow; i++) {
            for (int j = 0; j < bricksPerColumn; j++) {
                int brickState = mBrickStates[i][j];
                if (brickState > 0) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(stateColors[brickState]);
                    canvas.drawRect(rect, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(rect, paint);
                }
                rect.offset(brickWidth,0);
            }
            rect.offset(-brickWidth*bricksPerRow,brickHeight);
        }
    }

    private void drawPaddle(Canvas canvas) {
        paint = new Paint();
        rect = new Rect(-mPaddleWidth /2,0, mPaddleWidth /2, paddleHeight);
        rect.offset(mPaddleOffset,viewHeight-paddleHeight);
        paint.setColor(darkBlue);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(rect, paint);
    }

    private void drawBall(Canvas canvas) {
        paint = new Paint();
        paint.setColor(ballColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(mBallXPos, mBallYPos, ballRadius, paint);
    }

    private void drawPauseScreen(Canvas canvas) {
        paint = new Paint();
        rect = new Rect(0,0, viewWidth, viewHeight);
        paint.setColor(faintBlack);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Paused",(float)viewWidth/2,(float)viewHeight/2,paint);
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        cWidth = w;
        cHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Enforce 3:2 ratio.
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        int possibleHeight = (wSize/3)*2;
        int possibleWidth = (hSize/2)*3;

        if (possibleWidth > wSize) {
            //Calculated width is too large. Set width to actual width, use calculated height
            width = wSize;
            height = possibleHeight;
        }
        else { //Otherwise calculated height is too large
            height = hSize;
            width = possibleWidth;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState",super.onSaveInstanceState());
        //put stuff you need to save
        bundle.putInt("defaultBrick", mStartBrickNum);
        bundle.putInt("defaultBall", mStartBallCount);
        bundle.putInt("level",mObservable.getLevel());
        bundle.putInt("score",mObservable.getScore());
        bundle.putInt("brickNum",mObservable.getBrickNum());
        bundle.putInt("maxBrickHits",mMaxBrickHits);
        bundle.putInt("ballCount",mObservable.getBallCount());
        for (int i = 0; i < bricksPerColumn; i++) {
            bundle.putIntArray("brickState" + i, mBrickStates[i]);
        }
        bundle.putInt("paddleOffset",mPaddleOffset);
        bundle.putFloat("ballX",mBallXPos);
        bundle.putFloat("ballY",mBallYPos);
        bundle.putFloat("ballXVel",mBallXVelocity);
        bundle.putFloat("ballYVel",mBallYVelocity);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        state = bundle.getParcelable("instanceState");
        mStartBrickNum = bundle.getInt("defaultBrick");
        mStartBallCount = bundle.getInt("defaultBall");
        mMaxBrickHits = bundle.getInt("maxBrickHits");
        int level = bundle.getInt("level");
        int ballCount = bundle.getInt("ballCount");
        int score = bundle.getInt("score");
        int brickNum = bundle.getInt("brickNum");
        mObservable = new GameObservable(level, ballCount, score, brickNum);
        mBrickStates = new int[bricksPerColumn][bricksPerRow];
        for (int i=0; i <bricksPerColumn; i++) {
            mBrickStates[i] = bundle.getIntArray("brickState" + i);
        }
        mPaddleOffset = bundle.getInt("paddleOffset");
        mBallXPos = bundle.getFloat("ballX");
        mBallYPos = bundle.getFloat("ballY");
        mBallXVelocity = bundle.getFloat("ballXVel");
        mBallYVelocity = bundle.getFloat("ballYVel");
        super.onRestoreInstanceState(state);
    }

    public void pauseAll() {
        if (mTimer != null && mTimer.isRunning()) {
            mTimer.pause();
        }
        mPaused = true;
    }

    public void pauseMenu() {
        if (mTimer != null && mTimer.isPaused()) {
            mTimer.start(); //Timer still runs to allow paddle movement
        }
        mPaused = true;
        invalidate();
    }

    public void resume() {
        if (mTimer != null && mTimer.isPaused()) {
            mTimer.start();
        }
        mPaused = false;
    }

    public boolean getPaused() {
        return mPaused;
    }

    public void setPaddleMovingLeft(boolean moving) {
        mPaddleMoveLeft = moving;
    }

    public void setPaddleMovingRight(boolean moving) {
        mPaddleMoveRight = moving;
    }

    private void movePaddle(int offset) {
        int possibleOffset = mPaddleOffset + offset;
        if (possibleOffset < 0) {
            mPaddleOffset = 0;
        }
        else mPaddleOffset = Math.min(possibleOffset, viewWidth);
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        int paddleOffset = (int)(mPaddleVelocity*deltaTime);
        if (mPaddleMoveLeft) {
            movePaddle(-1*paddleOffset);
        }
        else if (mPaddleMoveRight) {
            movePaddle(paddleOffset);
        }

        if (!mPaused) {
            handleCollision();
            mBallXPos += mBallXVelocity*deltaTime;
            mBallYPos += mBallYVelocity*deltaTime;
        }
        invalidate();
    }

    private void handleCollision() {
        if (paddleCollision()) {
            float ballVelocity = (float)Math.sqrt(Math.pow(mBallXVelocity,2) + Math.pow(mBallYVelocity,2));
            double angle = maxPaddleAngle*2*(mBallXPos-mPaddleOffset)/ mPaddleWidth;
            double radians = Math.toRadians(angle);
            mBallXVelocity = (float)(ballVelocity*Math.sin(radians));
            mBallYVelocity = (float)(-1*ballVelocity*Math.cos(radians));
            ballHitNoise();
        }
        else if (bottomWallCollision()) {
            int ballCount = mObservable.getBallCount() -1;
            mObservable.setBallCount(ballCount);
            if (ballCount == 0) {
                Toast.makeText(getContext(),
                        "Game Over! Score = " + mObservable.getScore(),
                        Toast.LENGTH_LONG)
                        .show();
                createFirstLevel();
            }
            else {
                mBallYVelocity = (float)Math.sqrt(Math.pow(mBallXVelocity,2) + Math.pow(mBallYVelocity,2));
                mBallXVelocity = 0;
                mBallXPos = initialBallPos[0];
                mBallYPos = initialBallPos[1];
                pauseMenu();
            }
            ballLostNoise();
        }
        else if (topWallCollision()) {
            mBallYVelocity *= -1;
            ballHitNoise();
        }
        else if (leftWallCollision() || rightWallCollision()) {
            mBallXVelocity *= -1;
            ballHitNoise();
        }
        else {
            checkBrickCollision();
        }
    }

    private void ballHitNoise() {
        soundPlayer.play(hitSound, 0.5f, 0.5f, 1, 0, 1);
    }

    private void ballLostNoise() {
        soundPlayer.play(lostSound, 0.5f, 0.5f, 1, 0, 1);
    }

    private boolean leftWallCollision() {
        float ballLeft = mBallXPos - ballRadius;
        return ballLeft <= 0 && mBallXVelocity <= 0;
    }

    private boolean rightWallCollision() {
        float ballRight = mBallXPos + ballRadius;
        return ballRight >= viewWidth && mBallXVelocity >= 0;
    }

    private boolean topWallCollision() {
        float ballTop = mBallYPos - ballRadius;
        return ballTop <= 0 && mBallYVelocity <= 0;
    }

    private boolean bottomWallCollision() {
        float ballBottom = mBallYPos + ballRadius;
        return ballBottom >= viewHeight && mBallYVelocity >= 0;
    }

    private boolean paddleCollision() {
        float ballBottom = mBallYPos + ballRadius;
        return (mBallYVelocity > 0) && ballBottom >= (viewHeight-paddleHeight) &&  inPaddleXRange();
    }

    private boolean inPaddleXRange() {
        int leftPaddle = mPaddleOffset - (mPaddleWidth /2);
        int rightPaddle = mPaddleOffset + (mPaddleWidth /2);
        return (mBallXPos >= leftPaddle && mBallXPos <= rightPaddle);
    }

    private void checkBrickCollision() {
        int leftCol = getCol(mBallXPos - ballRadius);
        int ballCol = getCol(mBallXPos);
        int rightCol = getCol(mBallXPos + ballRadius);
        int topRow = getRow(mBallYPos - ballRadius);
        int ballRow = getRow(mBallYPos);
        int bottomRow = getRow(mBallYPos + ballRadius);
        if (inBrickBounds(ballRow,leftCol) && mBallXVelocity < 0 && mBrickStates[ballRow][leftCol] > 0) {
            mBallXVelocity *= -1;
            hitBrick(ballRow,leftCol);
        }
        else if (inBrickBounds(topRow,ballCol) && mBallYVelocity < 0 && mBrickStates[topRow][ballCol] > 0) {
            mBallYVelocity *= -1;
            hitBrick(topRow,ballCol);
        }
        else if (inBrickBounds(ballRow, rightCol) && mBallXVelocity > 0 && mBrickStates[ballRow][rightCol] > 0) {
            mBallXVelocity *= -1;
            hitBrick(ballRow, rightCol);
        }
        else if (inBrickBounds(bottomRow,ballCol) && mBallYVelocity > 0 && mBrickStates[bottomRow][ballCol] > 0) {
            mBallYVelocity *= -1;
            hitBrick(bottomRow, ballCol);
        }
    }

    private void hitBrick(int row, int col) {
        mBrickStates[row][col]--;
        if (mBrickStates[row][col] == 0) {
            int brickNum = mObservable.getBrickNum() -1;
            mObservable.setBrickNum(brickNum);

            int score = mObservable.getScore() + 1;
            mObservable.setScore(score);
        }

        if (mObservable.getBrickNum() == 0) {
            createNextLevel();
        }

        ballHitNoise();
    }

    private int getCol(float x) {
        return (int)(x/brickWidth);
    }

    private int getRow(float y) {
        return (int)(y/brickHeight);
    }

    private boolean inBrickBounds(int row, int col) {
        return (row >= 0 && row < bricksPerRow && col >= 0 && col < bricksPerColumn);
    }

    public GameObservable getObservable() {
        return mObservable;
    }

    static class GameObservable extends Observable {
        private int mLevel, mBalls, mScore, mBrickNum;

        GameObservable() {
            mLevel = 0;
            mBalls = 0;
            mScore = 0;
            mBrickNum = 0;
        }

        GameObservable(int level, int balls, int score, int bricks) {
            mLevel = level;
            mBalls = balls;
            mScore = score;
            mBrickNum = bricks;
        }

        void setLevel(int level) {
            mLevel = level;
            setChanged();
            notifyObservers();
        }

        void setBallCount(int balls) {
            mBalls = balls;
            setChanged();
            notifyObservers();
        }

        void setScore(int score) {
            mScore = score;
            setChanged();
            notifyObservers();
        }

        void setBrickNum(int bricks) {
            mBrickNum = bricks;
            setChanged();
            notifyObservers();
        }

        int getLevel() {
            return mLevel;
        }

        int getBallCount() {
            return mBalls;
        }

        int getScore() {
            return mScore;
        }

        int getBrickNum() {
            return mBrickNum;
        }
    } //End nested observable class
}
