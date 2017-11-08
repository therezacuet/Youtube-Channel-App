package com.ins.floating;

/**
 * Created by Nicholas Camargo on 16/2/16.
 */
public class Constants {
    public  static  int playerType = 0;
    public  static  int linkType = 0;
    public  static  int repeatType = 0;
    public  static  int noOfRepeats = 0;
    public  static  int playbackQuality = 3;
    public static boolean finishOnEnd = false;


    private static String strPlaybackQuality = "large";
    public static String getPlaybackQuality() {
        if(playbackQuality == 0){
            strPlaybackQuality = "auto";
        }
        else if (playbackQuality == 1){
            strPlaybackQuality = "hd1080";
        }
        else if (playbackQuality == 2){
            strPlaybackQuality = "hd720";
        }
        else if (playbackQuality == 3){
            strPlaybackQuality = "large";
        }
        else if (playbackQuality == 4){
            strPlaybackQuality = "medium";
        }
        else if (playbackQuality == 5){
            strPlaybackQuality = "small";
        }
        else{
            strPlaybackQuality = "tiny";
        }
        return strPlaybackQuality;
    }


    //Actions
    public interface ACTION {
        public static String PREV_ACTION = "com.ins.floating.ytube.action.prev";
        public static String PAUSE_PLAY_ACTION = "com.ins.floating.ytube.action.play";
        public static String NEXT_ACTION = "com.ins.floating.ytube.action.next";
        public static String STARTFOREGROUND_WEB_ACTION = "com.ins.floating.ytube.action.playingweb";
        public static String STOPFOREGROUND_WEB_ACTION = "com.ins.floating.ytube.action.stopplayingweb";
        public static String STARTFOREGROUND_YTUBE_ACTION = "com.ins.floating.ytube.action.playingytube";
        public static String STOPFOREGROUND_YTUBE_ACTION = "com.ins.floating.ytube.action.stopplayingytube";
    }

    //Notification Id
    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

}
