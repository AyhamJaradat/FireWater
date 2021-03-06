package utils;

import com.game.firewater.R;

/**
 * Created by Ayham on 8/18/2017.
 */

public interface Constants {


    // game Tag
    String TAG = "fireGameTag";
    String CONTAG = "connectionTage";
    String RTC_TAG = "RTC audio";

    // minimum player required for a room
    int MIN_PLAYERS = 2;
    // This array lists everything that's clickable, so we can install click
    // event handlers.
    int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out,R.id.button_cancel_popup_invitation,
            R.id.btnMyTalk, R.id.btnParticipantTalk, R.id.btnSendLeftMsg, R.id.btnSendRightMsg,
            R.id.button_single_player

    };

    // This array lists all the individual screens our game has.
    int[] SCREENS = {
            R.id.screen_game, R.id.screen_main,
            R.id.screen_wait
    };

    // audio decoding sample rate
    int SAMPLE_RATE = 44100;
    int MAX_STREAMS = 100;


    // for audioRecordingThread permission
    int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Request codes for the UIs that we show with startActivityForResult:
    int RC_SELECT_PLAYERS = 10000;
    int RC_INVITATION_INBOX = 10001;
    // arbitrary request code for the waiting room UI.
    // This can be any integer that's unique in your Activity
    int RC_WAITING_ROOM = 10002;
    int RC_SHOW_LEADERBOARD = 10003;
    int RC_SHOW_ACHIEVEMENT = 10004;

    // Request code used to invoke sign in user interactions.
    int RC_SIGN_IN = 9001;

    int FIRE_TYPE_ID = 1;
    int WATER_TYPE_ID = 2;
    char MOVE_PLAYER_CHAR_MSG = 'M';
    char SEED_RANDOM_CHAR_MSG = 'S';
    char VOICE_DATA_CHAR_MSG = 'V';
    char KILL_PLAYER_CHAR_MSG = 'K';
    char EAT_OBJECT_CHAR_MSG = 'E';
    char FIX_PLAYER_POSI_CHAR_MSG = 'F';
    char PLAY_AGAIN_CHAR_MSG = 'P';
    char TIME_SYNCHRO_CHAR_MSG = 'T';
    char GO_TO_CHAR_MSG ='G';

    String FIRE_TYPE = "Fire";
    String WATER_TYPE = "Water";
    String WATER_STAR_TYPE = "waterStar";
    String FIRE_STAR_TYPE = "fireStar";
    String WATER_DISAPPEAR_TYPE = "waterDisappear";
    String FIRE_DISAPPEAR_TYPE = "fireDisappear";

    // achievments ids
    int SINGLE_PLAY_ACHIEV = 0;
    int MULTIPLE_PLAY_ACHIEV =1;
    int SINGLE_WIN_ACHIEV =2;
    int MULTIPLE_WIN_ACHIEV =3;
    int TRAINING_ACHIEV = 4;


    // for Testers - test modes
    int OpenVoiceCall =1;
    int VoiceMessageOnClick = 2;
//    int SinglePlayerMode =3;
}
