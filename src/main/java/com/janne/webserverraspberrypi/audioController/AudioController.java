package com.janne.webserverraspberrypi.audioController;

import com.janne.webserverraspberrypi.audioController.models.Song;

/**
 * Interface for audio controller configurations to read and make changes to the current playback state etc.
 */
public interface AudioController {

    /**
     * Get currently playing song
     * @return currently playing song
     */
    Song getCurrentPlayingSong();

    /**
     * Searches for a certain song and starts playing that song
     * @param query query parameter to search for
     */
    void playSong(String query);

    /**
     * Play/Pause playback
     */
    void togglePlayback();

    /**
     * Is the currently playing song liked or not
     * @return if the song is liked or not
     */
    boolean isCurrentSongLiked();

    /**
     * Like/Unlike currently playing song
     */
    void toggleLikeCurrentSong();

    /**
     * Jump to the beginning of  a song / the previous song
     */
    void previousSong();

    /**
     * Skip to the next song
     */
    void nextSong();

    /**
     * Set the current volume
     * @param volume value to set to (float between 0-1)
     */
    void setVolume(float volume);

    /**
     * Get the current volume
     * @return current volume (float between 0-1)
     */
    float getVolume();

}