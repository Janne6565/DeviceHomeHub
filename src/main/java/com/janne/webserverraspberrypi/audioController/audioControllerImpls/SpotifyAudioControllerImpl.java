package com.janne.webserverraspberrypi.audioController.audioControllerImpls;

import com.janne.webserverraspberrypi.audioController.AudioController;
import com.janne.webserverraspberrypi.audioController.models.Song;

public class SpotifyAudioControllerImpl implements AudioController {

    private final String CONTROLLER_ENDPOINT;

    public SpotifyAudioControllerImpl(String endpointURL) {
        this.CONTROLLER_ENDPOINT = endpointURL;
    }

    @Override
    public Song getCurrentPlayingSong() {
        return null;
    }

    @Override
    public void playSong(String query) {

    }

    @Override
    public void togglePlayback() {

    }

    @Override
    public boolean isCurrentSongLiked() {
        return false;
    }

    @Override
    public void toggleLikeCurrentSong() {

    }

    @Override
    public void previousSong() {

    }

    @Override
    public void nextSong() {

    }

    @Override
    public void setVolume(float volume) {

    }

    @Override
    public float getVolume() {
        return 0;
    }
}
