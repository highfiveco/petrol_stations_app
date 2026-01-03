package co.highfive.petrolstation.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public class AudioPlayer {

    private MediaPlayer mediaPlayer;
    Player player;
    int flag;

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void play(Context c, int rid, final AudioPlayerEvent audioPlayerEvent) {
        stop();

        mediaPlayer = MediaPlayer.create(c, rid);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stop();
                if (audioPlayerEvent != null)
                    audioPlayerEvent.onCompleted();
            }
        });

        mediaPlayer.start();
    }

    public void play(Context c, String url , final AudioPlayerEvent audioPlayerEvent) {
        stop();
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

            }
        });

        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                return false;
            }
        });

        if (flag==0){
            //  new Player().execute(URL);
            player=new Player();
            player.execute(url);

        }
        else {
            if (mediaPlayer!=null){

                playAudio();

            }
        }

    }

    public int getAudioSessionId() {
        if (mediaPlayer == null)
            return -1;
        return mediaPlayer.getAudioSessionId();
    }

    public interface AudioPlayerEvent {
        void onCompleted();
    }

    class Player extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            Boolean prepared;

            try
            {
                mediaPlayer.setDataSource(params[0]);
                mediaPlayer.prepare();
//                lengthOfAudio = mediaPlayer.getDuration();

                prepared = true;

            }
            catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                Log.d("IllegarArgument", e.getMessage());
                prepared = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            }

            return prepared;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog.show();

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
//            progressDialog.dismiss();
            if (aBoolean){
                flag=1;
            }
            else {
                flag=0;
            }
            playAudio();

        }
    }

    public void playAudio() {

        if(mediaPlayer!=null)
        {
            mediaPlayer.start();
//            updateSeekProgress();
        }
    }

    public void pauseAudio() {
        if(mediaPlayer!=null)
        {
            mediaPlayer.pause();
        }
    }

}
