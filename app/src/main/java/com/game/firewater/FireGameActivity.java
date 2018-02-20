package com.game.firewater;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gameClasses.AudioPlay;
import gameClasses.AudioRecordingThread;
import gameClasses.GameSurface;
import utils.BaseGameUtils;
import utils.Constants;
import utils.MyUtils;

public class FireGameActivity extends Activity implements Constants, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener, View.OnTouchListener {

    // Google play service VARIABLES
    // Score of other participants.
    // We update this as we receive their scores from the network.
//    private Map<String, Integer> mParticipantScore;
    // Participants who sent us their final score.
//    private Set<String> mFinishedParticipants;
    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;
    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;
    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;
    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;
    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    private String mRoomId = null;
    // The participants in the currently active game
    private ArrayList<Participant> mParticipantsArray = null;
    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    private String mIncomingInvitationId = null;

    // GAME logic VARIABLES
    // user's current score
    private int myScore = 0;
    private int participantScore = 0;
    private TextView participantTextView;
    private TextView myScoreTextView;
    // are we already playing?
    private boolean isPlaying = false;
    private boolean isGameInEndedPhase = false;
    private boolean amIWaitingToPlayAgain = false;
    private boolean doesOtherWantToPLayAgain = false;
    private int curScreenId = -1;
    // Are we playing in multiplayer mode?
    private boolean isMultiplayerMode = false;
    // My participant ID in the currently active game
    private String mMyId = null;
    private String mParticipantId = null;
    private String myPlayerName = null;
    private String participantPlayerName = null;
    // Message buffer for sending messages
    private byte[] mMsgBuf = new byte[2];
    private long[] timeSynchroArray = new long[5];
    // variables to access the game surface
    private SurfaceHolder surfaceHolder;
    private GameSurface gameSurface;
    // to indicate if current player is fire or water
    private boolean amIFire = false;
    // seed used to generate falling objects positions
    private int theSeed = 0;
    // the surface view container in game screen
    private FrameLayout surfaceview_container;
    // AudioRecordingThread thread
    private AudioRecordingThread audioRecordingThread;
    // audioRecordingThread read thread
    private AudioPlay audioPlayThread;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private Context fireGameContext;
    private ImageView moveLeftBtn, moveRightBtn, talkBtn;
    private Typeface custom_font;
    // dialog variable to be used when dialog is needed
    private Dialog dialog;
    private Dialog gameFinishDialog;
    private Dialog oneOptionDialog;
    private Animation scalAnim;


    /**
     * To Handle Requested Audio Permission
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set fullscreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Set No Title
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // set layout file
        setContentView(R.layout.activity_fire_game);
        // audio record permission request
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        initialization();

    }

    /**
     * To initialize component in the activity
     */
    private void initialization() {
//        mParticipantScore = new HashMap<>();
//        mFinishedParticipants = new HashSet<>();
        // font creation from assets
        custom_font = Typeface.createFromAsset(this.getAssets(), "fonts/trajan_pro_bold.ttf");
        // custom dialog
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        // make background transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // game finish dialog
        gameFinishDialog = new Dialog(this);
        gameFinishDialog.setCanceledOnTouchOutside(false);
        gameFinishDialog.setCancelable(false);
        gameFinishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        gameFinishDialog.setContentView(R.layout.game_finish_dialog);
        gameFinishDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        // one option dialog
        oneOptionDialog = new Dialog(this);
        oneOptionDialog.setCanceledOnTouchOutside(false);
        oneOptionDialog.setCancelable(false);
        oneOptionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        oneOptionDialog.setContentView(R.layout.one_option_dialog);
        oneOptionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        // animation scall
        scalAnim = AnimationUtils.loadAnimation(this, R.anim.scal_animation);
        scalAnim.setFillAfter(true);
        // get context
        fireGameContext = this;
        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        // set font to buttons

        setFontToButtons();
        // set up a click listener for everything we care about
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);

        }
        participantTextView = (TextView) findViewById(R.id.participantScoreTextView);
        myScoreTextView = (TextView) findViewById(R.id.myScoreTextView);

        // find right and left buttons and add touch events on them
        moveLeftBtn = (ImageView) findViewById(R.id.btnLeft);
        moveRightBtn = (ImageView) findViewById(R.id.btnRight);
        moveLeftBtn.setOnTouchListener(this);
        moveRightBtn.setOnTouchListener(this);

        // record voice
        talkBtn = (ImageView) findViewById(R.id.btnTalk);
        talkBtn.setOnTouchListener(this);


        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                getString(R.string.leaderboard_best_score),
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                    @Override
                    public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                        LeaderboardScore c = arg0.getScore();
                        if (c != null) {
                            long score = c.getRawScore();
                            Log.d("scoree", "" + score);
                        }
                    }
                });


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean toReturn = false;
        switch (v.getId()) {
            case R.id.btnTalk:
                onTalkBtnClick(v, event);
                toReturn = true;
                break;
            case R.id.btnLeft:
                onMoveLeftBtnClick(v, event);
                toReturn = true;
                break;
            case R.id.btnRight:
                onMoveRightBtnClick(v, event);
                toReturn = true;
                break;
        }
        return toReturn;
    }

    /**
     * handle move right button touch
     *
     * @param v
     * @param event
     */
    private void onMoveRightBtnClick(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (surfaceHolder.getSurface() != null) {
                    if (amIFire)
                        v.setBackgroundResource(R.drawable.btn_right_fire_clicked1);
                    else
                        v.setBackgroundResource(R.drawable.btn_right_water_clicked1);
                    v.startAnimation(scalAnim);

                    gameSurface.playSoundCharRunning(amIFire);
                    gameSurface.movePlayerTo(mMyId, 0.7f);
                    moveMyCharachterOnParticipantScreen(0.7f, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (surfaceHolder.getSurface() != null) {
                           /* v.setSelected(false);
                            v.setPressed(false);*/
                    v.clearAnimation();
                    if (amIFire)
                        v.setBackgroundResource(R.drawable.btn_right_fire1);
                    else
                        v.setBackgroundResource(R.drawable.btn_right_water1);

                    gameSurface.stopSoundCharRunning(amIFire);
                    gameSurface.movePlayerTo(mMyId, 0);
                    moveMyCharachterOnParticipantScreen(0, gameSurface.getMyPlayerXPosition(mMyId));
                }
                break;
        }
    }

    /**
     * handle move left button touch
     *
     * @param v
     * @param event
     */
    private void onMoveLeftBtnClick(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (surfaceHolder.getSurface() != null) {

                    if (amIFire) {
                        v.setBackgroundResource(R.drawable.btn_leftt_fire_clicked1);
                    } else {
                        v.setBackgroundResource(R.drawable.btn_leftt_water_clicked1);
                    }
                    v.startAnimation(scalAnim);
                    gameSurface.playSoundCharRunning(amIFire);
                    gameSurface.movePlayerTo(mMyId, -0.7f);
                    // SEND message to other player
                    moveMyCharachterOnParticipantScreen(-0.7f, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (surfaceHolder.getSurface() != null) {

                    v.clearAnimation();
                    if (amIFire) {
                        v.setBackgroundResource(R.drawable.btn_leftt_fire1);
                    } else {
                        v.setBackgroundResource(R.drawable.btn_leftt_water1);
                    }
                    gameSurface.stopSoundCharRunning(amIFire);
                    gameSurface.movePlayerTo(mMyId, 0);
                    gameSurface.getMyPlayerXPosition(mMyId);
                    moveMyCharachterOnParticipantScreen(0, gameSurface.getMyPlayerXPosition(mMyId));
                }
                break;
        }
    }

    /**
     * handle talk button touch
     *
     * @param v
     * @param event
     */
    private void onTalkBtnClick(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // start recording

                if (amIFire)
                    v.setBackgroundResource(R.drawable.mic_orange_clicked);
                else
                    v.setBackgroundResource(R.drawable.mic_blue_clicked);
                v.startAnimation(scalAnim);
                audioRecordingThread = new AudioRecordingThread((FireGameActivity) fireGameContext);
                break;
            case MotionEvent.ACTION_UP:
                v.clearAnimation();

                if (amIFire)
                    v.setBackgroundResource(R.drawable.mic_orange);
                else
                    v.setBackgroundResource(R.drawable.mic_blue);

                audioRecordingThread.close();
                break;
        }
    }

    /**
     * set the font on buttons
     */
    private void setFontToButtons() {

        ((Button) findViewById(R.id.button_single_player_2)).setTypeface(custom_font);
        ((Button) findViewById(R.id.button_quick_game)).setTypeface(custom_font);
        ((Button) findViewById(R.id.button_invite_players)).setTypeface(custom_font);
        ((Button) findViewById(R.id.button_see_invitations)).setTypeface(custom_font);

    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.button_single_player:
            case R.id.button_single_player_2:
                // play a single-player game
                keepScreenOn();
//                resetGameVars();
                startGame(false);
                break;
            case R.id.button_sign_in:
                // user wants to sign in
                // Check to see the developer who's running this sample code read the instructions :-)
                // NOTE: this check is here only because this is a sample! Don't include this
                // check in your actual production app.
//                if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
//                    Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
//                }

                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                showSigningOutDialog();
                break;
            case R.id.button_invite_players:
                // show list of invitable players
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 2);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
        }
    }

    /**
     * show confirmation dialog when user tries to leave the game during session
     */
    private void showLeaveGameDialog() {
        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.textViewTitle);
        text.setTypeface(custom_font);
        text.setText("Leave The Game !!");
        Button dialogOkButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogOkButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user wants to sign out
                // sign out.
                Log.d(TAG, "leave the game during session");
                dialog.dismiss();
                leaveRoom();


            }
        });
        Button dialogCancelButton = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        dialogCancelButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * show confirmation dialog when user tries to exit the game
     */
    private void showExitGameDialog() {
        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.textViewTitle);
        text.setTypeface(custom_font);
        text.setText("Exist Game !!");
        Button dialogOkButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogOkButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Exist the game");
                dialog.dismiss();
                FireGameActivity.this.finish();


            }
        });
        Button dialogCancelButton = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        dialogCancelButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showOneOptionDialog(String title, String msg, int timeScore) {

        // set the custom dialog components - text, image and button
        TextView text = (TextView) oneOptionDialog.findViewById(R.id.textViewTitle);
        text.setTypeface(custom_font);
        text.setText(title);

        // set score
        TextView scoreText = (TextView) oneOptionDialog.findViewById(R.id.textViewMessage);
        String messageToView = msg;
        if (timeScore != -1) {
            messageToView += "\n Your Score: " + timeScore;
        }
        scoreText.setText(messageToView);

//        Button dialogOkButton = (Button) oneOptionDialog.findViewById(R.id.dialogButtonOK);
//        dialogOkButton.setTypeface(custom_font);
//        // if button is clicked, close the custom dialog
//        dialogOkButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // user wants to sign out
//                // sign out.
//                Log.d(TAG, "play again with the same participiant");
////                oneOptionDialog.dismiss();
//
//
//            }
//        });
        Button dialogCancelButton = (Button) oneOptionDialog.findViewById(R.id.dialogButtonCancel);
        dialogCancelButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oneOptionDialog.dismiss();
                leaveRoom();
            }
        });

        oneOptionDialog.show();
    }

    /**
     * show score dialoge when game finishes or ended
     *
     * @param isWin
     * @param secondsScrore
     */
    private void showGameFinishDialog(boolean isWin, int secondsScrore) {

        // set the custom dialog components - text, image and button
        TextView text = (TextView) gameFinishDialog.findViewById(R.id.textViewTitle);
        text.setTypeface(custom_font);
        if (isWin) {
            text.setText("Winning");
        } else {
            text.setText("Bad Luck");
        }
        // set score
        TextView scoreText = (TextView) gameFinishDialog.findViewById(R.id.textViewMessage);
        scoreText.setText("Time Score : " + secondsScrore);

        Button dialogOkButton = (Button) gameFinishDialog.findViewById(R.id.dialogButtonOK);
        dialogOkButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user want to play again with the same player
                Log.d(TAG, "play again with the same participiant");
                gameFinishDialog.dismiss();

                switchToScreen(R.id.screen_wait);
                amIWaitingToPlayAgain = true;
                if (doesOtherWantToPLayAgain) {
                    // I want to play and he wants to play
                    // he already confirmed that
                    // send him message that I want it too
                    sendPlayAgainToOtherPlayer();
                    // play the game because he already said he wants to play
                    startGameAgain();
                } else {
                    // Send message to other player to play again
                    sendPlayAgainToOtherPlayer();
                }

            }
        });
        Button dialogCancelButton = (Button) gameFinishDialog.findViewById(R.id.dialogButtonCancel);
        dialogCancelButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameFinishDialog.dismiss();
                isGameInEndedPhase = false;
                leaveRoom();
            }
        });

        gameFinishDialog.show();
    }

    /**
     * show sign out confirmation dialog
     */
    private void showSigningOutDialog() {
        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.textViewTitle);
        text.setTypeface(custom_font);
        text.setText("Signing Out !!");
        Button dialogOkButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogOkButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Sign-out button clicked");
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                switchToScreen(R.id.screen_sign_in);
                dialog.dismiss();
            }
        });
        Button dialogCancelButton = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        dialogCancelButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * change game screen to water or fire style
     *
     * @param isFire
     */
    private void switchStyleToWater(boolean isFire) {

        LinearLayout myPLayerView = (LinearLayout) findViewById(R.id.myViewContainer);
        LinearLayout participantPLayerView = (LinearLayout) findViewById(R.id.participantContainer);
        ImageView myCharacterImageView = (ImageView) findViewById(R.id.myCharacterImageView);
        ImageView participantImageView = (ImageView) findViewById(R.id.participantCharacterImageView);
        ImageView myStarImageView = (ImageView) findViewById(R.id.mStarIcon);
        ImageView participantStarImageView = (ImageView) findViewById(R.id.participantStarIcon);

        TextView myNameTextView = (TextView) findViewById(R.id.myNameTextView);
        TextView participantNameTextView = (TextView) findViewById(R.id.participantNameTextView);
        if (isMultiplayerMode) {
            myNameTextView.setText(myPlayerName);
            participantNameTextView.setText(participantPlayerName);
        }


        LinearLayout controlBar = (LinearLayout) findViewById(R.id.controlBtns);

        if (isFire) {
            // players view
           /* myPLayerView.setBackgroundColor(getResources().getColor(R.color.FireBackgroundColor));*/
            myPLayerView.setBackgroundResource(R.drawable.orange_gradiant_color);
          /*  participantPLayerView.setBackgroundColor(getResources().getColor(R.color.WaterBackgroundColor));*/
            participantPLayerView.setBackgroundResource(R.drawable.blue_gradient_color);
            myCharacterImageView.setBackgroundResource(R.drawable.fire_character_1);
            participantImageView.setBackgroundResource(R.drawable.water_character_1);
            myStarImageView.setBackgroundResource(R.drawable.red_star);
            participantStarImageView.setBackgroundResource(R.drawable.blue_star);

            // control bar
            controlBar.setBackgroundResource(R.drawable.orange_gradiant_color);


            moveLeftBtn.setBackgroundResource(R.drawable.btn_leftt_fire1);
            moveRightBtn.setBackgroundResource(R.drawable.btn_right_fire1);
            talkBtn.setBackgroundResource(R.drawable.mic_orange);


        } else {
            myPLayerView.setBackgroundResource(R.drawable.blue_gradient_color);
            participantPLayerView.setBackgroundResource(R.drawable.orange_gradiant_color);
            myCharacterImageView.setBackgroundResource(R.drawable.water_character_1);
            participantImageView.setBackgroundResource(R.drawable.fire_character_1);
            myStarImageView.setBackgroundResource(R.drawable.blue_star);
            participantStarImageView.setBackgroundResource(R.drawable.red_star);
            // control bar
            controlBar.setBackgroundResource(R.drawable.blue_gradient_color);

            moveLeftBtn.setBackgroundResource(R.drawable.btn_leftt_water1);
            moveRightBtn.setBackgroundResource(R.drawable.btn_right_water1);
            talkBtn.setBackgroundResource(R.drawable.mic_blue);
        }

    }

    /**
     * function to start a quick game
     */
    private void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // quick-start a game with 1 randomly selected opponent
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1/*MIN_OPPONENTS*/,
                1 /*MAX_OPPONENTS*/, 0 /*bit_mask*/);
        // build the room config:
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);

        RoomConfig roomConfig = rtmConfigBuilder.build();
        // prevent screen from sleeping during handshake
        keepScreenOn();
//        resetGameVars();
        // create room:
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);
        switchToScreen(R.id.screen_wait);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                //If you use the waiting room UI, you do not need to implement
                // additional logic to decide when the game should be started or canceled,
                // as seen earlier in shouldStartGame() and shouldCancelGame().
                // When you obtain an Activity.RESULT_OK result, you can start
                // right away since the required number of participants have been connected.
                // Likewise, when you get an error result from the waiting room UI,
                // you can simply leave the room.
                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    startGame(true);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d("invite", "Invitee count: " + invitees.size());

        Log.d("invite", "Invitee Id: " + invitees.get(0));

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
//        resetGameVars();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        // get the selected invitation
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        if (inv != null) {
            acceptInviteToRoom(inv.getInvitationId());
        }
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    // Accept the given invitation.
    private void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
//        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToMainScreen();
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
        super.onStop();
    }

    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
        if (mGoogleApiClient == null) {
            switchToScreen(R.id.screen_sign_in);
        } else if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Connecting client.");
            switchToScreen(R.id.screen_wait);
            mGoogleApiClient.connect();
        } else {
            Log.w(TAG,
                    "GameHelper: client was already connected on onStart()");
        }
        super.onStart();
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && curScreenId == R.id.screen_game) {
            // pressing back while we are in screen game
            showLeaveGameDialog();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && curScreenId == R.id.screen_main) {
            showExitGameDialog();
            return false;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    public void leaveRoom() {
        Log.d(TAG, "Leaving room.");

        if (surfaceview_container != null)
            surfaceview_container.removeAllViews();
        if (surfaceHolder != null && gameSurface != null)
            gameSurface.surfaceDestroyed(surfaceHolder);

        stopKeepingScreenOn();
        // close audio threads
        if (isMultiplayerMode) {
            if (audioPlayThread != null)
                audioPlayThread.close();
            if (audioRecordingThread != null)
                audioRecordingThread.close();
        }


        if (mRoomId != null) {
            // in this example, we take the simple approach and just leave the room:
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    private void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);
        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(R.string.is_inviting_you));
        switchToScreen(curScreenId); // This will show the invitation popup
    }

    @Override
    public void onInvitationRemoved(String invitationId) {

        if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
            mIncomingInvitationId = null;
            switchToScreen(curScreenId); // This will hide the invitation popup
        }

    }

    private void sendSeedToOtherPlayer(int theSeed, String mParticipantId) {
//        if (!isMultiplayerMode)
//            return; // playing single-player mode

        mMsgBuf[0] = (byte) (SEED_RANDOM_CHAR_MSG);
        mMsgBuf[1] = (byte) theSeed;
        // Send to every other participant.
        for (Participant p : mParticipantsArray) {
            if (p.getParticipantId().equals(mParticipantId) && p.getStatus() == Participant.STATUS_JOINED) {
                Log.d("sending seed", theSeed + "");
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    private void sendPlayAgainToOtherPlayer() {
        if (!isMultiplayerMode)
            return; // playing single-player mode

        mMsgBuf[0] = (byte) (PLAY_AGAIN_CHAR_MSG);

        // Send to every other participant.
        for (Participant p : mParticipantsArray) {
            if (p.getParticipantId().equals(mParticipantId) && p.getStatus() == Participant.STATUS_JOINED) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                        mRoomId, p.getParticipantId());
                break;
            }
        }
    }


    // Show error message about game being cancelled and return to main screen.
    private void showGameError() {
        switchToMainScreen();
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
    }


    private void synchronizePlayersToStartGame(Room room) {
        //get participants and my ID:
        mParticipantsArray = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        String playerId = "";
        //TODO: need to handle if more than two players
        for (Participant p : mParticipantsArray) {
            if (!p.getParticipantId().equals(mMyId)) {
                mParticipantId = p.getParticipantId();
                participantPlayerName = p.getDisplayName();
//                playerId = room.getParticipant(mParticipantId).getPlayer().getPlayerId();
            } else {
                myPlayerName = p.getDisplayName();

            }
        }
        Log.d("namess me", myPlayerName);
        Log.d("invite", "namess o" + participantPlayerName);
        // the first Participant is always the fire
        // the second Participant is the water
        Random generator = new Random();
        if (mMyId.equalsIgnoreCase(mParticipantsArray.get(0).getParticipantId())) {
            amIFire = true;
            theSeed = generator.nextInt(125) + 1; // 1 byte can not hold more than 127
            Log.d("generated seed", theSeed + "");
            sendSeedToOtherPlayer(theSeed, mParticipantId);
        } else {
            amIFire = false;
        }


//        mParticipantId = mParticipantsArray.get(0).getParticipantId();
        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
        if (mRoomId == null)
            mRoomId = room.getRoomId();

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "My mParticipantId ID " + mParticipantId);
        Log.d(TAG, "<< CONNECTED TO ROOM >>");

    }


    private void updateRoom(Room room) {
        if (room != null) {
            mParticipantsArray = room.getParticipants();
        }
//        if (mParticipantsArray != null) {
//            updatePeerScoresDisplay();
//        }
    }

    // Reset game variables in preparation for a new game.
    private void resetGameVars() {
//        mSecondsLeft = GAME_DURATION;
        myScore = 0;
        participantScore = 0;
        myScoreTextView.setText("0");
        participantTextView.setText("0");

        for (int i = 0; i < timeSynchroArray.length; i++) {
            timeSynchroArray[i] = -1;
        }

    }

    private void startGameAgain() {
        // clear surface
        if (surfaceview_container != null)
            surfaceview_container.removeAllViews();
        if (surfaceHolder != null && gameSurface != null)
            gameSurface.surfaceDestroyed(surfaceHolder);

        // reset flags
        isGameInEndedPhase = false;
        amIWaitingToPlayAgain = false;
        doesOtherWantToPLayAgain = false;
        startGame(true);

    }

    // Start the gameplay phase of the game.
    private void startGame(boolean multiplayer) {
        Log.d(TAG, "startGame called");
        isPlaying = true;
        isMultiplayerMode = multiplayer;
        resetGameVars();
//        updateScoreDisplay();
//        broadcastScore(false);
        switchToScreen(R.id.screen_game);

        surfaceview_container =
                (FrameLayout) findViewById(R.id.surfaceViewContainer);
        SurfaceView surfaceView = new GameSurface(this, amIFire);
        surfaceHolder = surfaceView.getHolder();
        gameSurface = (GameSurface) surfaceView;
        if (surfaceview_container != null)
            surfaceview_container.removeAllViews();
        // add our surface view
        surfaceview_container.addView(surfaceView);

        if (isMultiplayerMode) {
            audioPlayThread = new AudioPlay();
            Log.d("The seed Value", theSeed + "");
        }

        gameSurface.startGame(mMyId, mParticipantId, amIFire, theSeed);
    }


    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
//        String sender = rtm.getSenderParticipantId();
//        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

        if ((char) buf[0] == MOVE_PLAYER_CHAR_MSG) {
            // message to move player
            byte[] floatValue = new byte[4];
            System.arraycopy(buf, 1, floatValue, 0, 4);

            float distence = MyUtils.toFloat(floatValue);
//            int distence = (int) buf[1];
            // resume sounds if destince is not zero
            // pause  sounds if distence is zero
            if (distence == 0) {
                gameSurface.stopSoundCharRunning(!amIFire);
            } else {
                gameSurface.playSoundCharRunning(!amIFire);
            }
            gameSurface.movePlayerTo(mParticipantId, distence);

        } else if ((char) buf[0] == TIME_SYNCHRO_CHAR_MSG) {
            int msgCountId = (int) buf[1];
            if (msgCountId > 0 && msgCountId < timeSynchroArray.length)
                timeSynchroArray[msgCountId] = System.currentTimeMillis();

            // if the last one
            if(msgCountId==timeSynchroArray.length){
                if(!amIFire){
                    updateMyGameTimer();
                }

            }
        } else if ((char) buf[0] == FIX_PLAYER_POSI_CHAR_MSG) {
            // stop moving the player character and update x position to be as recieved
            // message to move player
            byte[] floatValue = new byte[4];
            System.arraycopy(buf, 1, floatValue, 0, 4);

            float distence = 0;
            float xPosition = MyUtils.toFloat(floatValue);
            // resume sounds if destince is not zero
            // pause  sounds if distence is zero
            gameSurface.stopSoundCharRunning(!amIFire);
            gameSurface.movePlayerTo(mParticipantId, distence);
            gameSurface.updatePlayerXPosition(mParticipantId, xPosition);


        } else if ((char) buf[0] == PLAY_AGAIN_CHAR_MSG) {
            if (amIWaitingToPlayAgain) {
                // I want to play again and other player wants too
                startGameAgain();
            } else if (isGameInEndedPhase) {
                // game ended ,, other player wants to play again with me
                // set flag to know that
                doesOtherWantToPLayAgain = true;
            }
        } else if ((char) buf[0] == SEED_RANDOM_CHAR_MSG) {
            // seeds received
            theSeed = (int) buf[1];
        } else if ((char) buf[0] == KILL_PLAYER_CHAR_MSG) {
            int characterId = (int) buf[1];
            String type = WATER_TYPE;
            if (characterId == FIRE_TYPE_ID) {
                type = FIRE_TYPE;
            }
            gameSurface.killCharacter(type);
        } else if ((char) buf[0] == EAT_OBJECT_CHAR_MSG) {
            int characterId = (int) buf[1];
            String type = WATER_TYPE;
            if (characterId == FIRE_TYPE_ID) {
                type = FIRE_TYPE;
            }
            gameSurface.showStarForCharacter(type);
            incrementParticipantScore();
        } else if ((char) buf[0] == VOICE_DATA_CHAR_MSG) {
            // voice came
            byte[] msgBuf = new byte[buf.length - 1];
            System.arraycopy(buf, 1, msgBuf, 0, buf.length - 1);
            // to turn bytes to shorts as either big endian or little endian.
            short[] shorts = new short[msgBuf.length / 2];
            ByteBuffer.wrap(msgBuf).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

            playReceivedVoice(shorts);


        }
    }

    /**
     * got the left time from Fire, ready to update mine for synchronization
     */
    private void updateMyGameTimer() {
        if( timeSynchroArray[4] == -1 ) return;

        long time1 = 0,time2=0, time3=0;
        int validCounter =0;
        if( timeSynchroArray[1] != -1 && timeSynchroArray[0]!=-1) {
            time1 = timeSynchroArray[1] - timeSynchroArray[0];
            validCounter++;
        }
        if( timeSynchroArray[1] != -1 && timeSynchroArray[2]!=-1){
            time2 = timeSynchroArray[2]-timeSynchroArray[1];
            validCounter++;
        }

        if( timeSynchroArray[2] != -1 && timeSynchroArray[3]!=-1) {
            time3 = timeSynchroArray[3] - timeSynchroArray[2];
            validCounter++;
        }


        if(validCounter < 2){
            // one time and two miss , ignore update
            return;
        }
        long totalTime =time1+time2+time3;
        long avgTime =totalTime/validCounter;

        long updateMsTime = (timeSynchroArray[4]*1000)-avgTime;
        Log.d("timesynch reci Ms",""+updateMsTime);
        gameSurface.updateMyTimerToSynchronize(updateMsTime);
    }


    private void playReceivedVoice(short[] shorts) {

        Log.d(TAG, "voice data received" + shorts.length);
        audioPlayThread.writeToBuffer(shorts);

//        int mBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        if (mBufferSize == AudioTrack.ERROR || mBufferSize == AudioTrack.ERROR_BAD_VALUE) {
//            // For some reason we couldn't obtain a buffer size
////            mBufferSize = SAMPLE_RATE *1 /*CHANNELS*/ * 2;
//            mBufferSize = shorts.length;
//        }
//        final boolean mShouldContinue = true;
//        final ShortBuffer mSamples = ShortBuffer.wrap(shorts); // the samples to play
//        final int mNumSamples = shorts.length; // number of samples to play
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT);
//                if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
////                    bufferSize = SAMPLE_RATE *
//                    bufferSize = shorts.length;
//                }
//
//                AudioTrack audioTrack = new AudioTrack(
//                        AudioManager.STREAM_MUSIC,
//                        SAMPLE_RATE,
//                        AudioFormat.CHANNEL_OUT_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT,
//                        bufferSize,
//                        AudioTrack.MODE_STREAM);
//
//                audioTrack.play();
//                audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
//                    @Override
//                    public void onPeriodicNotification(AudioTrack track) {
//                        if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
//                            int currentFrame = track.getPlaybackHeadPosition();
//                            int elapsedSeconds = (currentFrame * 1000) / SAMPLE_RATE;
//                        }
//                    }
//                    @Override
//                    public void onMarkerReached(AudioTrack track) {
//                        Log.v(TAG, "AudioRecordingThread file end reached");
//                        track.release();
//                    }
//                });
//                audioTrack.setPositionNotificationPeriod(SAMPLE_RATE / 30); // 30 times per second
//                audioTrack.setNotificationMarkerPosition(mNumSamples);
//
//
//                Log.v(TAG, "AudioRecordingThread streaming started");
//
//                short[] buffer = new short[bufferSize];
//                mSamples.rewind();
//                int limit = mNumSamples;
//                int totalWritten = 0;
//                while (mSamples.position() < limit && mShouldContinue) {
//                    int numSamplesLeft = limit - mSamples.position();
//                    int samplesToWrite;
//                    if (numSamplesLeft >= buffer.length) {
//                        mSamples.get(buffer);
//                        samplesToWrite = buffer.length;
//                    } else {
//                        for (int i = numSamplesLeft; i < buffer.length; i++) {
//                            buffer[i] = 0;
//                        }
//                        mSamples.get(buffer, 0, numSamplesLeft);
//                        samplesToWrite = numSamplesLeft;
//                    }
//                    totalWritten += samplesToWrite;
//                    audioTrack.write(buffer, 0, samplesToWrite);
//                }
//
//                if (!mShouldContinue) {
//                    audioTrack.release();
//                }
//
//                Log.v(TAG, "AudioRecordingThread streaming finished. Samples written: " + totalWritten);
//            }
//        }).start();


//

    }

    void playAudio() {
//        ShortBuffer mSamples; // the samples to play
//        int mNumSamples; // number of samples to play
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT);
//                if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
//                    bufferSize = SAMPLE_RATE * 2;
//                }
//
//                AudioTrack audioTrack = new AudioTrack(
//                        AudioManager.STREAM_MUSIC,
//                        SAMPLE_RATE,
//                        AudioFormat.CHANNEL_OUT_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT,
//                        bufferSize,
//                        AudioTrack.MODE_STREAM);
//
//                audioTrack.play();
//
//                Log.v(LOG_TAG, "AudioRecordingThread streaming started");
//
//                short[] buffer = new short[bufferSize];
//                mSamples.rewind();
//                int limit = mNumSamples;
//                int totalWritten = 0;
//                while (mSamples.position() < limit && mShouldContinue) {
//                    int numSamplesLeft = limit - mSamples.position();
//                    int samplesToWrite;
//                    if (numSamplesLeft >= buffer.length) {
//                        mSamples.get(buffer);
//                        samplesToWrite = buffer.length;
//                    } else {
//                        for (int i = numSamplesLeft; i < buffer.length; i++) {
//                            buffer[i] = 0;
//                        }
//                        mSamples.get(buffer, 0, numSamplesLeft);
//                        samplesToWrite = numSamplesLeft;
//                    }
//                    totalWritten += samplesToWrite;
//                    audioTrack.write(buffer, 0, samplesToWrite);
//                }
//
//                if (!mShouldContinue) {
//                    audioTrack.release();
//                }
//
//                Log.v(LOG_TAG, "AudioRecordingThread streaming finished. Samples written: " + totalWritten);
//            }
//        }).start();
    }

    /**
     * When a character eats object in user screen , a message should be send to the other user screen
     * this method should not do changes on UI , because it is called from other thread
     *
     * @param type
     */
    public void showStarForCharacterOnParticipantScreen(String type) {
        if (!isMultiplayerMode)
            return; // playing single-player mode
        int characterId = WATER_TYPE_ID;
        if (type.equals(FIRE_TYPE)) {
            characterId = FIRE_TYPE_ID;
        }
        byte[] mMsgBuf = new byte[2];
        mMsgBuf[0] = (byte) (EAT_OBJECT_CHAR_MSG);
        mMsgBuf[1] = (byte) characterId;
        // Send to every other participant.
        for (Participant p : mParticipantsArray) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            // it's an interim score notification, so we can use unreliable
            Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                    p.getParticipantId());
        }


    }

    private void incrementParticipantScore() {
        participantScore++;
        // update my score
        runOnUiThread(new Runnable() {
            public void run() {
                participantTextView.setText("" + participantScore);
            }
        });
    }

    public void incrementMyScore() {
        myScore++;
        // update my score
        runOnUiThread(new Runnable() {
            public void run() {
                myScoreTextView.setText("" + myScore);

            }
        });

    }

    /**
     * Fire character should send 5 messages to synchronize time between players
     */

    public void sendTimeSynchroMessages() {
        if (!isMultiplayerMode)
            return; // playing single-player mode

        final byte[] mMsgBuf = new byte[3];
        mMsgBuf[0] = (byte) (TIME_SYNCHRO_CHAR_MSG);

        CountDownTimer countDownTimer = new CountDownTimer(5 * 500, 500) {
            int msgCounterId = 0;

            public void onTick(long millisUntilFinished) {

                mMsgBuf[1] = (byte) msgCounterId;
                mMsgBuf[2] = (byte) 0;
                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                        mParticipantId);
                Log.d("timesynch", msgCounterId + "");
                msgCounterId++;


            }

            public void onFinish() {
//                msgCounterId++;
                mMsgBuf[1] = (byte) msgCounterId;
                mMsgBuf[2] = (byte) gameSurface.getLeftTime();
                Log.d("timesynch last", msgCounterId + "");
                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                        mParticipantId);
            }
        };
        countDownTimer.start();

    }

    /**
     * When a character is distroyed in user screen , a message should be send to distroy the character on the other user screen
     * this method should not do changes on UI , because it is called from other thread
     *
     * @param type
     */
    public void distroyCharacterOnParticipantScreen(String type) {
        if (!isMultiplayerMode)
            return; // playing single-player mode
        int characterId = WATER_TYPE_ID;
        if (type.equals(FIRE_TYPE)) {
            characterId = FIRE_TYPE_ID;
        }
        byte[] mMsgBuf = new byte[2];
        mMsgBuf[0] = (byte) (KILL_PLAYER_CHAR_MSG);
        mMsgBuf[1] = (byte) characterId;
//        mMsgBuf[2] = (byte) lives;

        // Send to every other participant.
        for (Participant p : mParticipantsArray) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
//            if (finalScore) {
            // final score notification must be sent via reliable message
//            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
//                    mRoomId, p.getParticipantId());
//            } else {
            // it's an interim score notification, so we can use unreliable
            Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                    p.getParticipantId());
//            }
        }

    }

    /**
     * to send a message for moving or stop moving character on other screen
     *
     * @param distance  value to move or zero to stop
     * @param Xposition used only when stop
     */
    public void moveMyCharachterOnParticipantScreen(float distance, float Xposition) {
        if (!isMultiplayerMode)
            return; // playing single-player mode

        if (distance != 0) {
            // Send message to move character
            byte[] mMsgBuf = new byte[5];
            mMsgBuf[0] = (byte) (MOVE_PLAYER_CHAR_MSG);
            byte[] floatValue = new byte[4];
            floatValue = MyUtils.float2ByteArray(distance);
            System.arraycopy(floatValue, 0, mMsgBuf, 1, floatValue.length);
            // Send to every other participant.
            for (Participant p : mParticipantsArray) {
                if (p.getParticipantId().equals(mMyId))
                    continue;
                if (p.getStatus() != Participant.STATUS_JOINED)
                    continue;

                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        } else {
            // send stop moving message at final Xposition
            byte[] mMsgBuf = new byte[5];
            mMsgBuf[0] = (byte) (FIX_PLAYER_POSI_CHAR_MSG);
            byte[] floatValue = new byte[4];
            floatValue = MyUtils.float2ByteArray(Xposition);
            System.arraycopy(floatValue, 0, mMsgBuf, 1, floatValue.length);
            // Send to every other participant.
            for (Participant p : mParticipantsArray) {
                if (p.getParticipantId().equals(mMyId))
                    continue;
                if (p.getStatus() != Participant.STATUS_JOINED)
                    continue;
                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }











     /*
     * UI SECTION. Methods that implement the game's UI.
     */


    // Broadcast my score to everybody else.
//    void broadcastScore(boolean finalScore) {
//        if (!isMultiplayerMode)
//            return; // playing single-player mode
//
//        // First byte in message indicates whether it's a final score or not
//        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');
//
//        // Second byte is the score.
//        mMsgBuf[1] = (byte) mScore;
//
//        // Send to every other participant.
//        for (Participant p : mParticipantsArray) {
//            if (p.getParticipantId().equals(mMyId))
//                continue;
//            if (p.getStatus() != Participant.STATUS_JOINED)
//                continue;
//            if (finalScore) {
//                // final score notification must be sent via reliable message
//                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
//                        mRoomId, p.getParticipantId());
//            } else {
//                // it's an interim score notification, so we can use unreliable
//                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
//                        p.getParticipantId());
//            }
//        }
//    }

    public void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.

        if (screenId == R.id.screen_game) {
            switchStyleToWater(amIFire);
        }

        for (int id : SCREENS) {
            try {
                findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
            } catch (Exception e) {
                Log.d("ayham Error", "exception " + e);
            }

        }


        curScreenId = screenId;


        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (isMultiplayerMode) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (curScreenId == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (curScreenId == R.id.screen_main || curScreenId == R.id.screen_game);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    private void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    // updates the label that shows my score
//    private void updateScoreDisplay() {
////        ((TextView) findViewById(R.id.my_score)).setText(formatScore(mScore));
//    }

    // formats a score as a three-digit number
//    private String formatScore(int i) {
//        if (i < 0)
//            i = 0;
//        String s = String.valueOf(i);
//        return s.length() == 1 ? "00" + s : s.length() == 2 ? "0" + s : s;
//    }


    /*
     * MISC SECTION. Miscellaneous methods.
     */


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    private void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public void sendVoice(byte[] bytes) {
        if (!isMultiplayerMode)
            return; // playing single-player mode

        byte[] letterBuffer = new byte[1];
        letterBuffer[0] = (byte) (VOICE_DATA_CHAR_MSG);
        byte[] msgBuf = new byte[bytes.length + 1];
        System.arraycopy(letterBuffer, 0, msgBuf, 0, letterBuffer.length);
        System.arraycopy(bytes, 0, msgBuf, letterBuffer.length, bytes.length);

        // Send to every other participant.
        for (Participant p : mParticipantsArray) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            // it's an interim score notification, so we can use unreliable
            Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, msgBuf, mRoomId,
                    p.getParticipantId());
        }

    }

    // returns whether there are enough players to start the game
    boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    boolean shouldCancelGame(Room room) {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        return false;
    }

    /**
     * click handler to open leaderboard UI
     *
     * @param v
     */
    public void showLeaderboard(View v) {
        startActivityForResult(
                Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                        getString(R.string.leaderboard_best_score)), RC_SHOW_LEADERBOARD);
    }

    /**
     * called when game finishes by winning or loosing
     *
     * @param isWinning
     * @param secondsScrore
     */
    public void showGameFinishScreen(final boolean isWinning, final int secondsScrore) {

        // submit leaderboard score
        Games.Leaderboards.submitScore(mGoogleApiClient,
                getString(R.string.leaderboard_best_score),
                secondsScrore);

        // changes in the UI should be made on main UI thread
        // because this method is called from surface view thread
        runOnUiThread(new Runnable() {
            public void run() {
                isPlaying = false;
                isGameInEndedPhase = true;
                showGameFinishDialog(isWinning, secondsScrore);

            }
        });


    }

    /*
      Listeners from Google Api
     */
/*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    /*
    Handling room creation errors (RoomUpdateCallback)
    */

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(CONTAG, "*** Error: onRoomCreated, status " + statusCode);
            // let screen go to sleep
            stopKeepingScreenOn();

            // type of error
            switch (statusCode) {
                case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // the client needs to reconnect to the service to access this data
                    Log.e(CONTAG, "*** Error: onRoomCreated, needs to reconnect");
                    break;
                case GamesStatusCodes.STATUS_REAL_TIME_CONNECTION_FAILED:
                case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED: //6 no internet
                    //the client failed to connect to the network
                    Log.e(CONTAG, "*** Error: onRoomCreated, needs to reconnect to the network");
                    break;
                case GamesStatusCodes.STATUS_MULTIPLAYER_DISABLED:
                    //the game does not support multiplayer
                case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                    //an unexpected error occurred in the service
                default:
                    // show error message, return to main screen
                    Log.e(CONTAG, "*** Error: onRoomCreated, other");
                    showGameError();

            }
            return;
        }
        Log.d(CONTAG, "succsefull onRoomCreated(" + statusCode + ", " + room + ")");
        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();
        // show the waiting room UI
        showWaitingRoom(room);
    }

    // When I join the room
    @Override
    public void onJoinedRoom(int statusCode, Room room) {

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(CONTAG, "*** Error: onJoinedRoom, status " + statusCode);
            // let screen go to sleep
            stopKeepingScreenOn();
            showGameError();
            return;
        }
        Log.d(CONTAG, "succsefull onJoinedRoom(" + statusCode + ", " + room + ")");

        // show the waiting room UI
        // get waiting room intent
        showWaitingRoom(room);
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get on Disconnected From Room()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        // left room. Ready to start or join another room.
        Log.d(CONTAG, "onLeftRoom, code " + statusCode);
        switchToMainScreen();
    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(CONTAG, "*** Error: onRoomConnected, status " + statusCode);
            // let screen go to sleep
            stopKeepingScreenOn();
            showGameError();
            return;
        }
        Log.d(CONTAG, "succsefull onRoomConnected(" + statusCode + ", " + room + ")");
        updateRoom(room);
        synchronizePlayersToStartGame(room);
    }

    /*
    Connecting players  ( RoomStatusUpdateCallback )
     */

    @Override
    public void onRoomConnecting(Room room) {
        Log.d(CONTAG, "onRoomConnecting");
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        Log.d(CONTAG, "onRoomAutoMatching");
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        Log.d(CONTAG, "onPeerInvitedToRoom");
        updateRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        Log.d(CONTAG, "onRoomAutoMatching");
        updateRoom(room);
        // peer declined invitation -- see if game should be canceled
        //TODO: handle peer declined
//        if (!isPlaying && shouldCancelGame(room)) {
//            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        Log.d(CONTAG, "onPeerJoined");
        updateRoom(room);
    }

    // called when other player leaves room
    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
        // peer left -- see if game should be canceled
        Log.d(CONTAG, "onPeerLeft " + peersWhoLeft.size());
        // called when other player leaves room
        if (isPlaying) {
            isPlaying = false;
            gameSurface.freezGame();
            int timeScore = gameSurface.getTimeScore();
            // submit leaderboard score
            Games.Leaderboards.submitScore(mGoogleApiClient,
                    getString(R.string.leaderboard_best_score),
                    timeScore);
            showOneOptionDialog("Game Ended", "Your Friend Left !!", timeScore);
        } else if (isGameInEndedPhase) {
            // game ended already and other player left
            isPlaying = false;
            isGameInEndedPhase = false;
            // remove Game Ended dialog
            gameFinishDialog.dismiss();
            showOneOptionDialog("Game Ended", "Your Friend Left !!", -1);
        }
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(CONTAG, "onConnectedToRoom.");
//        synchronizePlayersToStartGame(room);
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        Log.d(CONTAG, "onDisconnectedFromRoom");
        // called when other player leaves room , so we got disconnected
        if (isPlaying) {
            isPlaying = false;
            mRoomId = null; // we already left the room
            gameSurface.freezGame();
            int timeScore = gameSurface.getTimeScore();
            // submit leaderboard score
            Games.Leaderboards.submitScore(mGoogleApiClient,
                    getString(R.string.leaderboard_best_score),
                    timeScore);
            showOneOptionDialog("Game Ended", "Your Friend Left !!", timeScore);
        } else if (isGameInEndedPhase) {
            // game ended already and other player left
            isPlaying = false;
            isGameInEndedPhase = false;
            // remove Game Ended dialog
            gameFinishDialog.dismiss();
            showOneOptionDialog("Game Ended", "Your Friend Left !!", -1);
        }
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        Log.d(CONTAG, "onPeersConnected");
        if (isPlaying) {
            // add new player to an ongoing game
        } else if (shouldStartGame(room)) {
            // start game!
            updateRoom(room);
        }
    }

    /**
     * @param room
     * @param peers
     */
    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        // For now if peer is disconnected we should end the game
        updateRoom(room);
        Log.d(CONTAG, "onPeersDisconnected");
        // called when other player leaves room
        if (isPlaying) {
            isPlaying = false;
            gameSurface.freezGame();
            int timeScore = gameSurface.getTimeScore();
            // submit leaderboard score
            Games.Leaderboards.submitScore(mGoogleApiClient,
                    getString(R.string.leaderboard_best_score),
                    timeScore);
            showOneOptionDialog("Game Ended", "Your Friend Left !!", timeScore);
        } else if (isGameInEndedPhase) {
            // game ended already and other player left
            isPlaying = false;
            isGameInEndedPhase = false;
            // remove Game Ended dialog
            gameFinishDialog.dismiss();
            showOneOptionDialog("Game Ended", "Your Friend Left !!", -1);
        }

    }

    @Override
    public void onP2PConnected(String participant) {
        Log.d(CONTAG, "onP2PConnected");
    }

    @Override
    public void onP2PDisconnected(String participant) {
        Log.d(CONTAG, "onP2PDisconnected");
    }




    /*
     * GAME LOGIC SECTION. Methods that implement Sign in and connections.
     */

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG, "onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
        switchToMainScreen();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, 101010 /*getString(R.string.signin_other_error)*/);
        }

        switchToScreen(R.id.screen_sign_in);
    }


}
