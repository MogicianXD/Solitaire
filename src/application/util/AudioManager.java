package application.util;


import javafx.scene.media.AudioClip;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AudioManager
{
    ArrayList<Pair<AudioType, AudioClip>> audioList = new ArrayList<>();

    public AudioManager()
    {
        //init
        AudioClip audioClip = new AudioClip
                (getClass().getResource("../../res/audio/flip.wav").toString());
        audioList.add(new Pair<>(AudioType.Send, audioClip));

        audioClip = new AudioClip
                (getClass().getResource("../../res/audio/send.wav").toString());
        audioList.add(new Pair<>(AudioType.SendHeavy, audioClip));

        audioClip = new AudioClip
                (getClass().getResource("../../res/audio/send_heavy.wav").toString());
        audioList.add(new Pair<>(AudioType.Flip, audioClip));

        audioClip = new AudioClip
                (getClass().getResource("../../res/audio/shuffle.wav").toString());
        audioList.add(new Pair<>(AudioType.Shuffle, audioClip));

        audioClip = new AudioClip
                (getClass().getResource("../../res/audio/victory.wav").toString());
        audioList.add(new Pair<>(AudioType.Flip, audioClip));

        for (Pair<AudioType, AudioClip> pair : audioList)
        {
            pair.getValue().setCycleCount(1);
            pair.getValue().setVolume(1);
        }
    }

    public void playAudio(AudioType audioType)
    {
        for (Pair<AudioType, AudioClip> pair : audioList)
        {
            if(pair.getKey() == audioType)
            {
                pair.getValue().play();
                return;
            }
        }
    }

    public void playAudio(AudioType audioType, long delay)
    {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                playAudio(audioType);
                cancel();
            }
        };
        timer.schedule(timerTask, delay, 1000);
    }
}

