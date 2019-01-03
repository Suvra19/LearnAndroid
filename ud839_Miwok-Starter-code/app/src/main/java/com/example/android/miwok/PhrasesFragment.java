package com.example.android.miwok;


import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhrasesFragment extends Fragment {

    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private AudioAttributes mPlaybackAttributes;

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            switch(i) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mMediaPlayer.pause();
                    mMediaPlayer.seekTo(0);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    mMediaPlayer.start();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseMediaPlayer();
                    break;

            }
        }
    };

    public PhrasesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        TextView textView = new TextView(getActivity());
//        textView.setText(R.string.hello_blank_fragment);
//        return textView;
        View rootView = inflater.inflate(R.layout.word_list, container, false);

        final List<Word> words = new ArrayList<>();
        words.add(new Word("lutti", "one", R.raw.phrase_are_you_coming));
        words.add(new Word("otiiko", "two", R.raw.phrase_how_are_you_feeling));
        words.add(new Word("tolookosu", "three", R.raw.phrase_come_here));
        words.add(new Word("oyyisa", "four", R.raw.phrase_where_are_you_going));
        words.add(new Word("massokka", "five", R.raw.phrase_im_feeling_good));
        words.add(new Word("temmokka", "six", R.raw.phrase_lets_go));
        words.add(new Word("kenekaku", "seven", R.raw.phrase_my_name_is));
        words.add(new Word("kawinta", "eight", R.raw.phrase_what_is_your_name));
        words.add(new Word("wo'e", "nine", R.raw.phrase_yes_im_coming));
        words.add(new Word("na'aacha", "ten", R.raw.phrase_how_are_you_feeling));


        WordAdapter adapter = new WordAdapter(getActivity(), words, R.color.category_phrases);
        ListView listView = (ListView) rootView.findViewById(R.id.list);
        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;

            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

}
