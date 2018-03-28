package com.game.firewater;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
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
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import gameClasses.AudioPlay;
import gameClasses.AudioRecordingThread;
import gameClasses.GameSurface;
import utils.Constants;
import utils.MyUtils;
import web_rtc.AppRTCAudioManager;
import web_rtc.AppRTCClient;
import web_rtc.PeerConnectionClient;
import web_rtc.WebSocketRTCClient;

import static utils.ConstantFields.STAT_CALLBACK_PERIOD;

public class FireGameActivity extends Activity implements Constants,
        View.OnClickListener, View.OnTouchListener, AppRTCClient.SignalingEvents, PeerConnectionClient.PeerConnectionEvents, OnCallEvents {


    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    private final AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();
    private int gameTestMode = VoiceMessageOnClick /*OpenVoiceCall*/;
    /*
    * fields for RTC implementation
     */
    private PeerConnectionClient peerConnectionClient;
    private AppRTCClient appRtcClient;
    private AppRTCClient.SignalingParameters signalingParameters;
    private AppRTCAudioManager audioManager;
    private boolean activityRunning;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected;
    private boolean isError;
    private long callStartedTimeMs;
    private boolean micEnabled = true;
    private boolean isOtherPlayerMuted = false;
    // Google play service VARIABLES
    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    private GoogleSignInAccount mSignedInAccount = null;
    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;
    // Client used to interact with the real time multiplayer system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;
    // Client variables
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    // Client used to interact with the Invitation system.
    private InvitationsClient mInvitationsClient = null;
    // Holds the configuration of the current room.
    private RoomConfig mRoomConfig;
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
    private long[] timeSynchroArray = new long[4];
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
    private ImageView moveLeftBtn, moveRightBtn, myTtalkBtn, participantTalkBtn, sendGoLeftBtn, sendGoRightBtn;
    private ImageView waitingWaterIV, waitingFireIV, holdToTalkBtn;
    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage rtm) {
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
                if (!amIFire) {
                    int msgCountId = (int) buf[1];
                    Log.d("timesynch recieved", "msgCountId" + msgCountId);
                    if (msgCountId >= 0 && msgCountId < timeSynchroArray.length ) {
                        // for indexes 0 , 1, 2, 3
                        timeSynchroArray[msgCountId] = System.currentTimeMillis();
                    } else if (msgCountId == timeSynchroArray.length ) {
                        // if the last one
                        // for index 4 , read left time
                        updateMyGameTimer(((int) buf[2]) + 60);
                    }
                }
            } else if ((char) buf[0] == GO_TO_CHAR_MSG) {
                // other player is telling me to move
                // show move animation on my character
                int direction = (int) buf[1];
                Log.d("Send GO MSG", "on recive msg");
                gameSurface.showGoToAnimiation(direction);

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
                connectToAudioRoom(false);
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
    };
    private Typeface custom_font;
    // dialog variable to be used when dialog is needed
    private Dialog dialog;
    private Dialog gameFinishDialog;
    private Dialog oneOptionDialog;
    private Animation scalAnim;
    private InvitationCallback mInvitationCallback = new InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
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
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (mIncomingInvitationId != null && mIncomingInvitationId.equals(invitationId)) {
                mIncomingInvitationId = null;
                switchToScreen(curScreenId); // This will hide the invitation popup
            }
        }
    };
    private String mPlayerId;
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {

        // Called when room has been created
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            //if (statusCode != GamesStatusCodes.STATUS_OK) {
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(CONTAG, "*** Error: onRoomCreated, status " + statusCode);
                String description = GamesCallbackStatusCodes.getStatusCodeString(statusCode);
                Log.e(CONTAG, "*** Error: onRoomCreated, description " + description);
                stopKeepingScreenOn();
                if (statusCode == 6) {
                    showGameError("No internet connection, Please connect");
                } else {
                    showGameError();
                }

                // type of error
//                switch (statusCode) {
//                    case GamesCallbackStatusCodes.INTERNAL_ERROR:
//                        //	An unspecified error occurred; no more specific information is available.
//                        break;
//                    case GamesCallbackStatusCodes.MULTIPLAYER_DISABLED:
//                        // This game does not support multiplayer. This could occur if the linked app is not configured appropriately in the developer console.
//                        break;
//                    case GamesCallbackStatusCodes.REAL_TIME_CONNECTION_FAILED:
//                        //Failed to initialize the network connection for a real-time room
//                        break;
//                    case GamesCallbackStatusCodes.REAL_TIME_MESSAGE_SEND_FAILED:
//                        //Failed to send message to the peer participant for a real-time room.
//                        break;
//                    case GamesCallbackStatusCodes.REAL_TIME_ROOM_NOT_JOINED:
//                        //Failed to send message to the peer participant for a real-time room, because the user has not joined the room.
//                    default:
//                        // show error message, return to main screen
//                        Log.e(CONTAG, "*** Error: onRoomCreated, other");
//                        showGameError();
//
//                }

                return;
            }
            Log.d(CONTAG, "succsefull onRoomCreated(" + statusCode + ", " + room + ")");
            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room.getRoomId();

            // show the waiting room UI
            showWaitingRoom(room);

        }

        // Called when room is fully connected.
        @Override
        public void onRoomConnected(int statusCode, Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            //if (statusCode != GamesStatusCodes.STATUS_OK) {
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(CONTAG, "*** Error: onRoomConnected, status " + statusCode);
                //stopKeepingScreenOn();
                showGameError();
                return;
            }
            Log.d(CONTAG, "succsefull onRoomConnected(" + statusCode + ", " + room + ")");
            updateRoom(room);
            synchronizePlayersToStartGame(room);
        }

        // When I join the room
        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(CONTAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            //if (statusCode != GamesStatusCodes.STATUS_OK) {
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(CONTAG, "*** Error: onJoinedRoom, status " + statusCode);
//                stopKeepingScreenOn();
                showGameError();
                return;
            }
            Log.d(CONTAG, "succsefull onJoinedRoom(" + statusCode + ", " + room + ")");
            // show the waiting room UI
            // get waiting room intent
            showWaitingRoom(room);
        }

        // Called when we've successfully left the room (this happens a result of voluntarily leaving
        // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            // we have left the room; return to main screen.

            Log.d(CONTAG, "onLeftRoom, code " + statusCode);
            switchToMainScreen();
        }
    };
    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onConnectedToRoom(Room room) {
            Log.d(CONTAG, "onConnectedToRoom.");

            //get participants and my ID:
            mParticipantsArray = room.getParticipants();
            mMyId = room.getParticipantId(mPlayerId);

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room.getRoomId();
            }
            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: " + mRoomId);
            Log.d(TAG, "My ID " + mMyId);
            Log.d(TAG, "<< CONNECTED TO ROOM>>");
        }

        // Called when we get disconnected from the room. We return to the main screen.
        @Override
        public void onDisconnectedFromRoom(Room room) {
//            mRoomId = null;
//            mRoomConfig = null;
//            showGameError();
            Log.d(CONTAG, "onDisconnectedFromRoom");
            //disconnectAudioChannel audio
            runOnUiThread(() -> {
                Log.d(RTC_TAG, "Remote end hung up; dropping PeerConnection");
                disconnectAudioChannel();
            });


            // called when other player leaves room , so we got disconnected
            if (isPlaying) {
                isPlaying = false;
                mRoomId = null; // we already left the room
                gameSurface.freezGame();
                int timeScore = gameSurface.getTimeScore();
                // submit leaderboard score
//                Games.Leaderboards.submitScore(mGoogleApiClient,
//                        getString(R.string.leaderboard_best_score),
//                        timeScore);
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


        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        @Override
        public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
            Log.d(CONTAG, "onPeerDeclined");
            updateRoom(room);
            // peer declined invitation -- see if game should be canceled
            //TODO: handle peer declined
//        if (!isPlaying && shouldCancelGame(room)) {
//            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
        }

        @Override
        public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
            Log.d(CONTAG, "onPeerInvitedToRoom");
            updateRoom(room);
        }

        @Override
        public void onP2PDisconnected(@NonNull String participant) {
            Log.d(CONTAG, "onP2PDisconnected");
        }

        @Override
        public void onP2PConnected(@NonNull String participant) {
            Log.d(CONTAG, "onP2PConnected");
        }

        @Override
        public void onPeerJoined(Room room, @NonNull List<String> arg1) {
            Log.d(CONTAG, "onPeerJoined");
            updateRoom(room);
        }

        // called when other player leaves room
        @Override
        public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
            updateRoom(room);
            // peer left -- see if game should be canceled
            Log.d(CONTAG, "onPeerLeft " + peersWhoLeft.size());
            // called when other player leaves room
            if (isPlaying) {
                isPlaying = false;
                gameSurface.freezGame();
                int timeScore = gameSurface.getTimeScore();
                // submit leaderboard score
//                Games.Leaderboards.submitScore(mGoogleApiClient,
//                        getString(R.string.leaderboard_best_score),
//                        timeScore);
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
        public void onRoomAutoMatching(Room room) {
            Log.d(CONTAG, "onRoomAutoMatching");
            updateRoom(room);
        }

        @Override
        public void onRoomConnecting(Room room) {
            Log.d(CONTAG, "onRoomConnecting");
            updateRoom(room);
        }

        @Override
        public void onPeersConnected(Room room, @NonNull List<String> peers) {
            Log.d(CONTAG, "onPeersConnected");
            if (isPlaying) {
                // add new player to an ongoing game
            } else if (shouldStartGame(room)) {
                // start game!
                updateRoom(room);
            }
//            updateRoom(room);
        }

        @Override
        public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
            // For now if peer is disconnected we should end the game
            updateRoom(room);
            Log.d(CONTAG, "onPeersDisconnected");
            // called when other player leaves room
            if (isPlaying) {
                isPlaying = false;
                gameSurface.freezGame();
                int timeScore = gameSurface.getTimeScore();
                // submit leaderboard score
//                Games.Leaderboards.submitScore(mGoogleApiClient,
//                        getString(R.string.leaderboard_best_score),
//                        timeScore);
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
    };

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // set layout file
        setContentView(R.layout.activity_fire_game);
        // audio record permission request
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        // Create the client used to sign in.
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        initialization();
//        RTCInitialization();

    }

    private void RTCInitialization() {
        // If capturing format is not specified for screencapture, use screen resolution.
        peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters.createDefault();

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        appRtcClient = new WebSocketRTCClient(this);


    }

    private void startAudioConnection(String audioRoomId) {
        RTCInitialization();
        // Create connection parameters.
        roomConnectionParameters = new AppRTCClient.RoomConnectionParameters("https://appr.tc", audioRoomId, false);

        setupListeners();

        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(this, peerConnectionParameters, this);
        // my voice is not muted by defult
        peerConnectionClient.setEnableAudio(true);
       /* if(gameTestMode == OpenVoiceCall) {
            peerConnectionClient.setEnableRemoteAudio(true);
        }else*/
        if (gameTestMode == VoiceMessageOnClick) {
            peerConnectionClient.setEnableAudio(false);
        }
        startCall();
    }

    private void setupListeners() {
        //TODO: add buttons on UI for audio settings
//        binding.buttonCallDisconnect.setOnClickListener(view -> onCallHangUp());

//        binding.buttonCallSwitchCamera.setOnClickListener(view -> onCameraSwitch());

//        binding.buttonCallToggleMic.setOnClickListener(view -> {
//            boolean enabled = onToggleMic();
//            binding.buttonCallToggleMic.setAlpha(enabled ? 1.0f : 0.3f);
//        });
    }

    private void startCall() {
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
//        logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        Log.d(RTC_TAG, "Connecting to: " + roomConnectionParameters.roomUrl);
        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(this);
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(RTC_TAG, "Starting the audio manager...");
        audioManager.start(this::onAudioManagerDevicesChanged);

    }

    private void muteUnMuteOtherPlayerVoice(boolean isMute) {


        if (isMute) {
            peerConnectionClient.setRemoteAudioEnabled(false);
        } else {
            peerConnectionClient.setRemoteAudioEnabled(true);
        }
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(RTC_TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    private void reportError(final String description) {
        runOnUiThread(() -> {
            if (!isError) {
                isError = true;
                disconnectWithErrorMessage(description);
            }
        });
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (!activityRunning) {
            Log.e(RTC_TAG, "Critical error: " + errorMessage);
            runOnUiThread(() -> {
                Log.d(RTC_TAG, "Remote end hung up; dropping PeerConnection");
                disconnectAudioChannel();
            });

        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(R.string.ok,
                            (dialog, id) -> {
                                dialog.cancel();
                                disconnectAudioChannel();
                            })
                    .create()
                    .show();
        }
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnectAudioChannel() {
//        activityRunning = false;
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
//        binding.localVideoView.release();
//        binding.remoteVideoView.release();
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (iceConnected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        //finish();
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        Log.d(RTC_TAG, "Creating peer connection, delay=" + delta + "ms");
        VideoCapturer videoCapturer = null;
//        if (peerConnectionParameters.videoCallEnabled) {
//            videoCapturer = createVideoCapturer();
//        }
        peerConnectionClient.createPeerConnection(null/*rootEglBase.getEglBaseContext()*/, null/* binding.localVideoView*/,
                null /*remoteRenderers*/, videoCapturer, signalingParameters);

        if (signalingParameters.initiator) {
            Log.d(RTC_TAG, "Creating OFFER...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                Log.d(RTC_TAG, "Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(RTC_TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(RTC_TAG, "Call is connected in closed or error state");
            return;
        }
        // Update video view.
//        updateVideoView();
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
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

        waitingWaterIV = (ImageView) findViewById(R.id.waitingWaterIV);
        waitingFireIV = (ImageView) findViewById(R.id.waitingFireIV);
        waitingFireIV.setBackgroundResource(R.drawable.moving_fire_animation);
        waitingWaterIV.setBackgroundResource(R.drawable.moving_water_animation);

        // record voice
        myTtalkBtn = (ImageView) findViewById(R.id.btnMyTalk);
        participantTalkBtn = (ImageView) findViewById(R.id.btnParticipantTalk);
        sendGoLeftBtn = (ImageView) findViewById(R.id.btnSendLeftMsg);
        sendGoRightBtn = (ImageView) findViewById(R.id.btnSendRightMsg);
        holdToTalkBtn = (ImageView) findViewById(R.id.btnHoldToTalk);

        sendGoLeftBtn.setOnTouchListener(this);
        sendGoRightBtn.setOnTouchListener(this);
        // set touch listner for google play service use
        holdToTalkBtn.setOnTouchListener(this);


//        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
//                getString(R.string.leaderboard_best_score),
//                LeaderboardVariant.TIME_SPAN_ALL_TIME,
//                LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
//                new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
//
//                    @Override
//                    public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
//                        LeaderboardScore c = arg0.getScore();
//                        if (c != null) {
//                            long score = c.getRawScore();
//                            Log.d("scoree", "" + score);
//                        }
//                    }
//                });


    }

    /**
     * on touch listener for buttons on buttom bar
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean toReturn = false;
        switch (v.getId()) {
            case R.id.btnHoldToTalk:
                if (gameTestMode == VoiceMessageOnClick) {
                    // only on voice messages hold test mode
                    onTalkBtnClick(v, event);
                }
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
            case R.id.btnSendLeftMsg:
                if (!isMultiplayerMode) {
                    onMoveOtherLeftBtnClick(v, event);
                    toReturn = true;
                }
                break;
            case R.id.btnSendRightMsg:
                if (!isMultiplayerMode) {
                    onMoveOtherRightBtnClick(v, event);
                    toReturn = true;
                }
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
    private void onMoveOtherRightBtnClick(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (surfaceHolder.getSurface() != null) {
                    if (amIFire)
                        v.setBackgroundResource(R.drawable.btn_right_water_clicked1);
                    else
                        v.setBackgroundResource(R.drawable.btn_right_fire_clicked1);
                    v.startAnimation(scalAnim);

                    gameSurface.playSoundCharRunning(!amIFire);
                    gameSurface.moveOtherPlayerTo(0.7f);

                }
                break;
            case MotionEvent.ACTION_UP:
                if (surfaceHolder.getSurface() != null) {
                           /* v.setSelected(false);
                            v.setPressed(false);*/
                    v.clearAnimation();
                    if (amIFire)
                        v.setBackgroundResource(R.drawable.btn_right_water1);
                    else
                        v.setBackgroundResource(R.drawable.btn_right_fire1);

                    gameSurface.stopSoundCharRunning(!amIFire);
                    gameSurface.moveOtherPlayerTo(0);
                }
                break;
        }
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

    private void onMoveOtherLeftBtnClick(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (surfaceHolder.getSurface() != null) {
                    if (amIFire) {
                        v.setBackgroundResource(R.drawable.btn_leftt_water_clicked1);
                    } else {
                        v.setBackgroundResource(R.drawable.btn_leftt_fire_clicked1);
                    }
                    v.startAnimation(scalAnim);
                    gameSurface.playSoundCharRunning(!amIFire);
                    gameSurface.moveOtherPlayerTo(-0.7f);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (surfaceHolder.getSurface() != null) {

                    v.clearAnimation();
                    if (amIFire) {
                        v.setBackgroundResource(R.drawable.btn_leftt_water1);
                    } else {
                        v.setBackgroundResource(R.drawable.btn_leftt_fire1);
                    }
                    gameSurface.stopSoundCharRunning(!amIFire);
                    gameSurface.moveOtherPlayerTo(0);
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
                // enable mic if its disabled
                if (micEnabled == false)
                    onToggleMic();
                //audioRecordingThread = new AudioRecordingThread((FireGameActivity) fireGameContext);
                break;
            case MotionEvent.ACTION_UP:
                v.clearAnimation();

                if (amIFire)
                    v.setBackgroundResource(R.drawable.mic_orange);
                else
                    v.setBackgroundResource(R.drawable.mic_blue);

//                audioRecordingThread.close();
                // disable mic if its enabled
                if (micEnabled == true)
                    onToggleMic();
                break;
        }
    }

    /**
     * set the font on buttons
     */
    private void setFontToButtons() {

        ((Button) findViewById(R.id.button_single_player)).setTypeface(custom_font);
        ((Button) findViewById(R.id.button_quick_game)).setTypeface(custom_font);
        ((Button) findViewById(R.id.button_invite_players)).setTypeface(custom_font);
        ((Button) findViewById(R.id.button_see_invitations)).setTypeface(custom_font);

    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {

            case R.id.button_single_player:
                // play a single-player game
                keepScreenOn();
//                resetGameVars();
                startGame(false);
                break;
            case R.id.button_sign_in:
                // user wants to sign in
                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                startSignInIntent();

                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                showSigningOutDialog();
                break;
            case R.id.button_invite_players:
                if (mRealTimeMultiplayerClient == null) {
                    showPleaseSignInDialog();
                    return;
                }
                // show list of invitable players
                switchToScreen(R.id.screen_wait);
                // show list of invitable players
                mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 1, false).addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_SELECT_PLAYERS);
                            }
                        }
                ).addOnFailureListener(
                        createFailureListener("There was a problem selecting opponents."));
                break;
            case R.id.button_see_invitations:
                if (mRealTimeMultiplayerClient == null) {
                    showPleaseSignInDialog();
                    return;
                }
                // show list of pending invitations
                switchToScreen(R.id.screen_wait);
                // show list of pending invitations
                mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_INVITATION_INBOX);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
                break;

            case R.id.button_accept_popup_invitation:
                if (mRealTimeMultiplayerClient == null) {
                    showPleaseSignInDialog();
                    return;
                }
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_cancel_popup_invitation:
                mIncomingInvitationId = null;
                switchToScreen(curScreenId); // This will hide the invitation popup
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
            case R.id.btnMyTalk:
                // to mute mic or unmute it
                if (gameTestMode == OpenVoiceCall) {
                    boolean enabled = onToggleMic();
//                   ((ImageView) v).setAlpha(enabled ? 1.0f : 0.3f);
                    if (enabled) {
                        if (amIFire) {
                            v.setBackgroundResource(R.drawable.mic_orange_clicked);
                        } else {
                            v.setBackgroundResource(R.drawable.mic_blue_clicked);
                        }

                    } else {
                        if (amIFire) {
                            v.setBackgroundResource(R.drawable.mic_orange);

                        } else {
                            v.setBackgroundResource(R.drawable.mic_blue);
                        }
                    }


                }

                break;
            case R.id.btnParticipantTalk:
                if (isMultiplayerMode && peerConnectionClient != null) {
                    isOtherPlayerMuted = !isOtherPlayerMuted;

                    if (isOtherPlayerMuted) {
                        // set muted image
                        if (amIFire) {
                            // other is water
                            participantTalkBtn.setBackgroundResource(R.drawable.water_mute);
                        } else {
                            // other is fire
                            participantTalkBtn.setBackgroundResource(R.drawable.fire_mute);
                        }
                        // mute other player
                        muteUnMuteOtherPlayerVoice(true);
                    } else {
                        // set unmuted image
                        if (amIFire) {
                            // other is water
                            participantTalkBtn.setBackgroundResource(R.drawable.water_voice);
                        } else {
                            // other is fire
                            participantTalkBtn.setBackgroundResource(R.drawable.fire_voice);
                        }
//                    unmute other player
                        muteUnMuteOtherPlayerVoice(false);
                    }
                }

                break;
            case R.id.btnSendLeftMsg:
                sendGoLeftRightMsgToOtherPlayer(false);
                break;
            case R.id.btnSendRightMsg:
                sendGoLeftRightMsgToOtherPlayer(true);
                break;
        }
    }

    /**
     * Send message to other player to move , instead of voice
     *
     * @param isRight
     */
    public void sendGoLeftRightMsgToOtherPlayer(boolean isRight) {

        Log.d("Send GO MSG", "on click send msg");
        if (!isMultiplayerMode) {
            // playing single-player mode
            return;
        }
        // playing multiple-player mode

        int directionToGo = isRight ? 0 : 1;
        byte[] mMsgBuf = new byte[2];
        mMsgBuf[0] = (byte) (GO_TO_CHAR_MSG);
        mMsgBuf[1] = (byte) directionToGo;
        // Send to every other participant.
        for (Participant p : mParticipantsArray) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            // it's an interim score notification, so we can use unreliable
            mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                    p.getParticipantId());
        }


    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity's onActivityResult function
     */
    public void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
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
        dialogOkButton.setText("Play again");
        // if button is clicked, close the custom dialog
        dialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user want to play again with the same player
                Log.d(TAG, "play again with the same participiant");
                gameFinishDialog.dismiss();
                if (isMultiplayerMode) {
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
                } else {
                    // single player mode, play again
                    startGame(false);
                }

            }
        });
        Button dialogCancelButton = (Button) gameFinishDialog.findViewById(R.id.dialogButtonCancel);
        dialogCancelButton.setText("Main Menu");
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

    @Override
    public void startActivityForResult(@RequiresPermission Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
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
                signOut();
                switchToMainScreen();
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

    public void signOut() {
        Log.d(TAG, "signOut()");
        if (!isSignedIn()) {
            Log.w(TAG, "signOut() called, but was not signed in!");
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signOut(): success");
                        } else {
                            handleException(task.getException(), "signOut() failed!");
                        }

                        onDisconnected();
                    }
                });
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    public void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mRealTimeMultiplayerClient = null;
        mInvitationsClient = null;
        mAchievementsClient = null;
        mLeaderboardsClient = null;
        setSignInOutButtonStyle(false);
        switchToMainScreen();
    }

    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;
        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new AlertDialog.Builder(FireGameActivity.this)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show();
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
            participantTalkBtn.setVisibility(View.VISIBLE);
        } else {
            // in single play mode hide all multiplayer buttons
            myTtalkBtn.setVisibility(View.GONE);
            participantTalkBtn.setVisibility(View.GONE);
            holdToTalkBtn.setVisibility(View.GONE);
            if (amIFire) {
                myNameTextView.setText("Mr. Fire");
                participantNameTextView.setText("Mr. Water");
            } else {
                myNameTextView.setText("Mr. Water");
                participantNameTextView.setText("Mr. Fire");
            }
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

            sendGoRightBtn.setBackgroundResource(R.drawable.water_right_btn);
            sendGoLeftBtn.setBackgroundResource(R.drawable.water_left_btn);
            myTtalkBtn.setBackgroundResource(R.drawable.mic_orange);
            participantTalkBtn.setBackgroundResource(R.drawable.water_voice);
            holdToTalkBtn.setBackgroundResource(R.drawable.mic_orange);
            if (isMultiplayerMode) {
                if (gameTestMode == OpenVoiceCall) {
                    // on open voice channel start with enabled mic
                    myTtalkBtn.setBackgroundResource(R.drawable.mic_orange_clicked);
                    participantTalkBtn.setBackgroundResource(R.drawable.water_voice);
                    holdToTalkBtn.setVisibility(View.GONE);
                    myTtalkBtn.setVisibility(View.VISIBLE);

                } else if (gameTestMode == VoiceMessageOnClick) {
                    holdToTalkBtn.setVisibility(View.VISIBLE);
                    myTtalkBtn.setVisibility(View.GONE);
                }
            }


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
            sendGoRightBtn.setBackgroundResource(R.drawable.fire_button_right);
            sendGoLeftBtn.setBackgroundResource(R.drawable.fire_button_left);
            myTtalkBtn.setBackgroundResource(R.drawable.mic_blue);
            participantTalkBtn.setBackgroundResource(R.drawable.fire_voice);
            holdToTalkBtn.setBackgroundResource(R.drawable.mic_blue);
            if (isMultiplayerMode) {
                if (gameTestMode == OpenVoiceCall) {
                    // on open voice channel start with enabled mic
                    myTtalkBtn.setBackgroundResource(R.drawable.mic_blue_clicked);
                    participantTalkBtn.setBackgroundResource(R.drawable.fire_voice);
                    holdToTalkBtn.setVisibility(View.GONE);
                    myTtalkBtn.setVisibility(View.VISIBLE);
                } else if (gameTestMode == VoiceMessageOnClick) {
                    holdToTalkBtn.setVisibility(View.VISIBLE);
                    myTtalkBtn.setVisibility(View.GONE);
                }
            }
        }

    }

    /**
     * function to start a quick game
     */
    private void startQuickGame() {

        if (mRealTimeMultiplayerClient == null) {
            showPleaseSignInDialog();
            return;
        }
        // auto-match criteria to invite one random automatch opponent.
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0x0);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
// build the room config:
        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        // create room
        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    private void showPleaseSignInDialog() {
//        new AlertDialog.Builder(FireGameActivity.this)
//                .setTitle("Sign in required")
//                .setMessage("Please sign in to play with friends")
//                .setNeutralButton(android.R.string.ok, null)
//                .show();

        // set the custom dialog components - text, image and button
        TextView text = (TextView) gameFinishDialog.findViewById(R.id.textViewTitle);
        text.setTypeface(custom_font);
        text.setText("Sign-in is required");
        // set score
        TextView scoreText = (TextView) gameFinishDialog.findViewById(R.id.textViewMessage);
        scoreText.setText("Sign-in to play with\nfriends and others");

        Button dialogOkButton = (Button) gameFinishDialog.findViewById(R.id.dialogButtonOK);
        dialogOkButton.setText("Sign in");
        dialogOkButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignInIntent();
                gameFinishDialog.dismiss();
            }
        });
        Button dialogCancelButton = (Button) gameFinishDialog.findViewById(R.id.dialogButtonCancel);
        dialogCancelButton.setText("Cancel");
        dialogCancelButton.setTypeface(custom_font);
        // if button is clicked, close the custom dialog
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameFinishDialog.dismiss();
            }
        });

        gameFinishDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode /*resultCode*/,
                                 Intent intent) {

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
                } else {
                    // What ever else , leave the room
                    // if we're in a room, leave it.
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:


                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                Task<GoogleSignInAccount> task =
                        GoogleSignIn.getSignedInAccountFromIntent(intent);

                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    onConnected(account);
                } catch (ApiException apiException) {
                    String message = apiException.getMessage();
                    if (message == null || message.isEmpty()) {
                        message = getString(R.string.signin_other_error);
                    }
                    Log.d("error in sign in m:", "" + message);
                    onDisconnected();
                    if (message.equals("4: ")) {
                        showGameError("No internet Connection, Please Connect!");
                    } else {
                        showGameError(getString(R.string.signin_other_error));
                    }
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
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
//        resetGameVars();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .addPlayersToInvite(invitees)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria).build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
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

    // Accept the given invitation.
    private void acceptInviteToRoom(String invitationId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invitationId);
        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .build();

        switchToScreen(R.id.screen_wait);
        keepScreenOn();
//        resetGameVars();

        mRealTimeMultiplayerClient.join(mRoomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room Joined Successfully!");
                    }
                });
    }

    // Activity interfaces
    @Override
    public void onPause() {
        super.onPause();
        activityRunning = false;
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null) {
            peerConnectionClient.stopVideoSource();
        }
        // unregister our listeners.  They will be re-registered via onResume->signInSilently->onConnected.
        if (mInvitationsClient != null) {
            mInvitationsClient.unregisterInvitationCallback(mInvitationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();
    }
    // Called when we get an invitation to play a game. We react by showing that to the user.
//    @Override
//    public void onInvitationReceived(Invitation invitation) {
//        // We got an invitation to play a game! So, store it in
//        // mIncomingInvitationId
//        // and show the popup on the screen.
//        mIncomingInvitationId = invitation.getInvitationId();
//        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
//                invitation.getInviter().getDisplayName() + " " +
//                        getString(R.string.is_inviting_you));
//        switchToScreen(curScreenId); // This will show the invitation popup
//    }

//    @Override
//    public void onInvitationRemoved(String invitationId) {
//
//        if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
//            mIncomingInvitationId = null;
//            switchToScreen(curScreenId); // This will hide the invitation popup
//        }
//
//    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        switchToMainScreen();

        super.onStop();
    }

    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
//        if (mGoogleApiClient == null) {
//            switchToScreen(R.id.screen_sign_in);
//        } else if (!mGoogleApiClient.isConnected()) {
//            Log.d(TAG, "Connecting client.");
//            switchToScreen(R.id.screen_wait);
//            mGoogleApiClient.connect();
//        } else {
//            Log.w(TAG,
//                    "GameHelper: client was already connected on onStart()");
//        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        // if we're in a room, leave it.
        leaveRoom();

        activityRunning = false;
//        rootEglBase.release();
        super.onDestroy();
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
        // disconnectAudioChannel audio
//        disconnectAudioChannel();

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
            mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRoomId = null;
                            mRoomConfig = null;
                        }
                    });

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
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
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
                mRealTimeMultiplayerClient.sendReliableMessage(mMsgBuf,
                        mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                                Log.d(TAG, "RealTime message sent");
                                Log.d(TAG, "  statusCode: " + statusCode);
                                Log.d(TAG, "  tokenId: " + tokenId);
                                Log.d(TAG, "  recipientParticipantId: " + recipientParticipantId);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                                Log.d(TAG, "Created a reliable message with tokenId: " + tokenId);
                            }
                        });

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
                mRealTimeMultiplayerClient.sendReliableMessage(mMsgBuf,
                        mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                                Log.d(TAG, "RealTime message sent");
                                Log.d(TAG, "  statusCode: " + statusCode);
                                Log.d(TAG, "  tokenId: " + tokenId);
                                Log.d(TAG, "  recipientParticipantId: " + recipientParticipantId);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                                Log.d(TAG, "Created a reliable message with tokenId: " + tokenId);
                            }
                        });
                break;
            }
        }
    }

    // Show error message about game being cancelled and return to main screen.
    private void showGameError() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.game_problem))
                .setNeutralButton(android.R.string.ok, null).show();
        switchToMainScreen();

    }

    private void showGameError(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setNeutralButton(android.R.string.ok, null).show();
        switchToMainScreen();

    }

    private void synchronizePlayersToStartGame(Room room) {
        //get participants and my ID:
        mParticipantsArray = room.getParticipants();
        mMyId = room.getParticipantId(mPlayerId);
//        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

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
            // connect fire to audio room
            connectToAudioRoom(true);
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

    private void connectToAudioRoom(boolean isFire) {
        String audioRoomId = theSeed + "";
        if (isFire) {
            // I am fire , audio room contains my id
            audioRoomId += mMyId;
        } else {
            // I am water, audio room contains other player id
            audioRoomId += mParticipantId;
        }
        startAudioConnection(audioRoomId);

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

        checkForAchievements(TRAINING_ACHIEV);
        pushAccomplishments();
        isPlaying = true;
        isMultiplayerMode = multiplayer;
        resetGameVars();
//        updateScoreDisplay();
//        broadcastScore(false);
        switchToScreen(R.id.screen_game);

        surfaceview_container =
                (FrameLayout) findViewById(R.id.surfaceViewContainer);
        SurfaceView surfaceView = new GameSurface(this, amIFire, isMultiplayerMode);
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

    /**
     * got the left time from Fire, ready to update mine for synchronization
     */
    private void updateMyGameTimer(int leftTime) {

        Log.d("timesynch avgTime ", "after updateMyGameTimer " + leftTime);
        if (leftTime > 180 || leftTime < 120) return;

        long time1 = 0, time2 = 0, time3 = 0;
        int validCounter = 0;
        if (timeSynchroArray[1] != -1 && timeSynchroArray[0] != -1) {
            time1 = timeSynchroArray[1] - timeSynchroArray[0];
            validCounter++;
        }
        if (timeSynchroArray[1] != -1 && timeSynchroArray[2] != -1) {
            time2 = timeSynchroArray[2] - timeSynchroArray[1];
            validCounter++;
        }

        if (timeSynchroArray[2] != -1 && timeSynchroArray[3] != -1) {
            time3 = timeSynchroArray[3] - timeSynchroArray[2];
            validCounter++;
        }


        if (validCounter < 2) {
            // one time and two miss , ignore update
            return;
        }
        long totalTime = time1 + time2 + time3;
        long avgTime = totalTime / validCounter;
        Log.d("timesynch avgTime ", "" + avgTime);
        long updateMsTime = (leftTime * 1000) - avgTime;
        Log.d("timesynch reci Ms", "" + updateMsTime);
        gameSurface.updateMyTimerToSynchronize(updateMsTime);
    }

    /**
     * play recieved voice from google play service
     *
     * @param shorts
     */
    private void playReceivedVoice(short[] shorts) {

        Log.d(TAG, "voice data received" + shorts.length);
        audioPlayThread.writeToBuffer(shorts);
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
            mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                    p.getParticipantId());
        }


    }

    public void incrementParticipantScore() {
        participantScore++;
        // update my score
        runOnUiThread(new Runnable() {
            public void run() {
                participantTextView.setText("" + participantScore);
            }
        });
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
        // send five messages 0,1,2,3,4
        // message index4 should contain the leftTime
        CountDownTimer countDownTimer = new CountDownTimer(5 * 500, 500) {
            int msgCounterId = 0;

            public void onTick(long millisUntilFinished) {

                mMsgBuf[1] = (byte) msgCounterId;
                if (msgCounterId == 4) {
                    // time left
                    mMsgBuf[2] = (byte) (gameSurface.getLeftTime() - 60);
                } else {
                    //ignore this value
                    mMsgBuf[2] = (byte) 0;
                }

                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                        mParticipantId);

                Log.d("timesynch", msgCounterId + "");
                msgCounterId++;


            }

            public void onFinish() {
                mMsgBuf[1] = (byte) msgCounterId;
                if (msgCounterId == 4) {
//                The byte data type is an 8-bit signed two's complement integer.
//                  It has a minimum value of -128 and a maximum value of 127 (inclusive).
                    // time left should be less than 60+60+60 = 180 , I decrement 60 here and increment there
                    // to ensure 1 byte fits
                    mMsgBuf[2] = (byte) (gameSurface.getLeftTime() - 60);

                } else {
                    //ignore this value
                    mMsgBuf[2] = (byte) 0;
                }
                //Log.d("timesynch last", msgCounterId + "value" + gameSurface.getLeftTime());
                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                        mParticipantId);

            }
        };
        countDownTimer.start();

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
            // it's an interim score notification, so we can use unreliable
            mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                    p.getParticipantId());
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
                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
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
                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                        p.getParticipantId());

            }
        }
    }

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
        if (screenId == R.id.screen_wait) {
            // start animation of characters
            ((AnimationDrawable) waitingFireIV.getBackground()).start();
            ((AnimationDrawable) waitingWaterIV.getBackground()).start();
        } else {
            ((AnimationDrawable) waitingFireIV.getBackground()).stop();
            ((AnimationDrawable) waitingWaterIV.getBackground()).stop();
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
        switchToScreen(R.id.screen_main);

//        if (mRealTimeMultiplayerClient != null) {
//            setSignInOutButtonStyle(true);
//        } else {
//            setSignInOutButtonStyle(false);
//        }
    }

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

    /**
     * Send Voice using Google play services
     *
     * @param bytes
     */
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
            mRealTimeMultiplayerClient.sendUnreliableMessage(msgBuf, mRoomId,
                    p.getParticipantId());

        }

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

    // returns whether there are enough players to start the game
    boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    /*
    Connecting players  ( RoomStatusUpdateCallback )
     */

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
        // show all leader board in new code

        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getAllLeaderboardsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(intent, RC_SHOW_LEADERBOARD);
            }
        });
//        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
//                .getLeaderboardIntent(getString(R.string.leaderboard_best_single_score))
//                .addOnSuccessListener(new OnSuccessListener<Intent>() {
//                    @Override
//                    public void onSuccess(Intent intent) {
//                        startActivityForResult(intent, RC_SHOW_LEADERBOARD);
//                    }
//                });

    }

    public void showAchievements(View v) {
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_SHOW_ACHIEVEMENT);
                    }
                });
    }

    public void showTestSettings(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(FireGameActivity.this).create();
        alertDialog.setTitle("Voice Mode Settings");
        alertDialog.setMessage("Select one of the two Modes for Voice Using");
        // Alert dialog button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Voice as Call",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Alert dialog action goes here
                        // onClick button code here
                        gameTestMode = OpenVoiceCall;
                        micEnabled = true;
                        dialog.dismiss();// use dismiss to cancel alert dialog
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Voice as Hold-To-Talk",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Alert dialog action goes here
                        // onClick button code here
                        gameTestMode = VoiceMessageOnClick;
                        micEnabled = false;
                        dialog.dismiss();// use dismiss to cancel alert dialog
                    }
                });
        alertDialog.show();
    }

    /**
     * Update leaderboards with the user's score.
     *
     * @param finalScore The score the user got.
     * @param isWinning
     */
    private void updateLeaderboards(int finalScore, boolean isWinning) {
        if (isMultiplayerMode) {
            // multiplayer
            if (mOutbox.mMultiplePLayerModeScore < finalScore) {
                mOutbox.mMultiplePLayerModeScore = finalScore;
            }
            checkForAchievements(MULTIPLE_PLAY_ACHIEV);
            if (isWinning) {
                checkForAchievements(MULTIPLE_WIN_ACHIEV);
            }
        } else if (!isMultiplayerMode) {
            if (mOutbox.mSinglePLayerModeScore < finalScore) {
                mOutbox.mSinglePLayerModeScore = finalScore;
            }
            checkForAchievements(SINGLE_PLAY_ACHIEV);
            if (isWinning) {
                checkForAchievements(SINGLE_WIN_ACHIEV);
            }
        }

        pushAccomplishments();
    }


    /**
     * Check for achievements and unlock the appropriate ones.
     *
     * @param achievCode the achievement code user earns
     */
    private void checkForAchievements(int achievCode) {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
        if (achievCode == TRAINING_ACHIEV) {
            mOutbox.mPrimeAchievement = true;
//            achievementToast(getString(R.string.achievement_prime_toast_text));
        }
        if (achievCode == SINGLE_PLAY_ACHIEV) {
            mOutbox.mSingleModePlayAchievement = true;
//            achievementToast(getString(R.string.achievement_arrogant_toast_text));
        }
        if (achievCode == MULTIPLE_PLAY_ACHIEV) {
            mOutbox.mMultipleModePlayAchievement = true;
//            achievementToast(getString(R.string.achievement_humble_toast_text));
        }
        if (achievCode == SINGLE_WIN_ACHIEV) {
            mOutbox.mWinningSingleModeGameAchievement = true;
//            achievementToast(getString(R.string.achievement_leet_toast_text));
        }
        if (achievCode == MULTIPLE_WIN_ACHIEV) {
            mOutbox.mWinningMultipleModeGameAchievement = true;
//            achievementToast(getString(R.string.achievement_leet_toast_text));
        }
        mOutbox.mBoredSteps++;
    }

    private void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, try again later
            return;
        }

        if (mOutbox.mPrimeAchievement) {
            mAchievementsClient.unlock(getString(R.string.achievement_game_playing_master));
            mOutbox.mPrimeAchievement = false;
        }
        if (mOutbox.mSingleModePlayAchievement) {
            mAchievementsClient.unlock(getString(R.string.achievement_single_mode_playing_master));
            mOutbox.mSingleModePlayAchievement = false;
        }
        if (mOutbox.mMultipleModePlayAchievement) {
            mAchievementsClient.unlock(getString(R.string.achievement_multi_player_master));
            mOutbox.mMultipleModePlayAchievement = false;
        }
        if (mOutbox.mWinningSingleModeGameAchievement) {
            mAchievementsClient.unlock(getString(R.string.achievement_winning_single_mode_game));
            mOutbox.mWinningSingleModeGameAchievement = false;
        }
        if (mOutbox.mWinningMultipleModeGameAchievement) {
            mAchievementsClient.unlock(getString(R.string.achievement_winning_multiple_mode_game));
            mOutbox.mWinningMultipleModeGameAchievement = false;
        }
//        if (mOutbox.mBoredSteps > 0) {
//            mAchievementsClient.increment(getString(R.string.achievement_really_bored),
//                    mOutbox.mBoredSteps);
//            mAchievementsClient.increment(getString(R.string.achievement_bored),
//                    mOutbox.mBoredSteps);
//            mOutbox.mBoredSteps = 0;
//        }

        if (mOutbox.mSinglePLayerModeScore >= 0) {
            mLeaderboardsClient.submitScore(getString(R.string.leaderboard_best_single_score),
                    mOutbox.mSinglePLayerModeScore);
            mOutbox.mSinglePLayerModeScore = -1;
        }
        if (mOutbox.mMultiplePLayerModeScore >= 0) {
            mLeaderboardsClient.submitScore(getString(R.string.leaderboard_best_social_score),
                    mOutbox.mMultiplePLayerModeScore);
            mOutbox.mMultiplePLayerModeScore = -1;
        }
    }


    /**
     * called when game finishes by winning or loosing
     *
     * @param isWinning
     * @param secondsScrore
     */
    public void showGameFinishScreen(final boolean isWinning, final int secondsScrore) {

        // changes in the UI should be made on main UI thread
        // because this method is called from surface view thread
        runOnUiThread(new Runnable() {
            public void run() {

                isPlaying = false;
                isGameInEndedPhase = true;
                // submit leaderboard score
                // update leaderboards
                updateLeaderboards(secondsScrore, isWinning);
                showGameFinishDialog(isWinning, secondsScrore);

            }
        });


    }

    private void setSignInOutButtonStyle(boolean isSigned) {

        if (isSigned) {
            findViewById(R.id.button_sign_out).setVisibility(View.VISIBLE);
            findViewById(R.id.button_sign_in).setVisibility(View.GONE);
        } else {
            findViewById(R.id.button_sign_out).setVisibility(View.GONE);
            findViewById(R.id.button_sign_in).setVisibility(View.VISIBLE);
        }

    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        // user sign-in
        setSignInOutButtonStyle(true);

        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);

        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            // update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            mInvitationsClient = Games.getInvitationsClient(FireGameActivity.this, googleSignInAccount);

            // get the playerId from the PlayersClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mPlayerId = player.getPlayerId();

                            switchToMainScreen();
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the player id!"));
        }

        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
//            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
//                    Toast.LENGTH_LONG).show();
        }

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        mInvitationsClient.registerInvitationCallback(mInvitationCallback);

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        GamesClient gamesClient = Games.getGamesClient(FireGameActivity.this, googleSignInAccount);
//        Google Play Games achievement unlocked popup showing
        gamesClient.setViewForPopups(findViewById(R.id.gps_popup));
        gamesClient.getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle hint) {
                        if (hint != null) {
                            Invitation invitation =
                                    hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                            if (invitation != null && invitation.getInvitationId() != null) {
                                // retrieve and cache the invitation ID
                                Log.d(TAG, "onConnected: connection hint has a room invite!");
                                acceptInviteToRoom(invitation.getInvitationId());
                            }
                        }
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));
    }

    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }


    /*
     *   Methods for RTC implementation
     */

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onCallHangUp() {
        disconnectAudioChannel();
    }

    @Override
    public void onCameraSwitch() {
        // not used
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        // not used
    }

    @Override
    public boolean onToggleMic() {

        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);

        }
        return micEnabled;
//        return false;
    }

    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {
        runOnUiThread(() -> onConnectedToRoomInternal(params));
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(RTC_TAG, "Received remote SDP for non-initilized peer connection.");
                return;
            }
            Log.d(RTC_TAG, "Received remote " + sdp.type + ", delay=" + delta + "ms");
            peerConnectionClient.setRemoteDescription(sdp);
            if (!signalingParameters.initiator) {
                Log.d(RTC_TAG, "Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(RTC_TAG, "Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(candidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(RTC_TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates);
        });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(() -> {
            Log.d(RTC_TAG, "Remote end hung up; dropping PeerConnection");
            disconnectAudioChannel();
        });
    }

    @Override
    public void onChannelError(String description) {
        reportError(description);
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                Log.d(RTC_TAG, "Sending " + sdp.type + ", delay=" + delta + "ms");
                if (signalingParameters.initiator) {
                    appRtcClient.sendOfferSdp(sdp);
                } else {
                    appRtcClient.sendAnswerSdp(sdp);
                }
            }
            if (peerConnectionParameters.videoMaxBitrate > 0) {
                Log.d(RTC_TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                appRtcClient.sendLocalIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                appRtcClient.sendLocalIceCandidateRemovals(candidates);
            }
        });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            Log.d(RTC_TAG, "ICE connected, delay=" + delta + "ms");
            iceConnected = true;
            callConnected();
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(() -> {
            Log.d(RTC_TAG, "ICE disconnected");
            iceConnected = false;
            disconnectAudioChannel();
        });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }


    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    private class AccomplishmentsOutbox {
        boolean mPrimeAchievement = false;
        boolean mSingleModePlayAchievement = false;
        boolean mMultipleModePlayAchievement = false;
        boolean mWinningSingleModeGameAchievement = false;
        boolean mWinningMultipleModeGameAchievement = false;
        int mBoredSteps = 0;
        int mSinglePLayerModeScore = -1;
        int mMultiplePLayerModeScore = -1;

        boolean isEmpty() {
            return !mPrimeAchievement && !mSingleModePlayAchievement && !mMultipleModePlayAchievement &&
                    !mWinningSingleModeGameAchievement && !mWinningMultipleModeGameAchievement && mBoredSteps == 0 && mSinglePLayerModeScore < 0 &&
                    mMultiplePLayerModeScore < 0;
        }

    }

}
