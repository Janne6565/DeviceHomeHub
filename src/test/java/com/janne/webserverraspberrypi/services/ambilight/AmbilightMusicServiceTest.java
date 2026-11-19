package com.janne.webserverraspberrypi.services.ambilight;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmbilightMusicServiceTest {

    @Test
    void originFitsHyperHdrSchema() {
        // HyperHDR's JSON schema caps "origin" at 20 characters and answers anything longer with
        // "Errors during specific message validation", so every image was silently dropped.
        assertThat(AmbilightMusicService.ORIGIN).hasSizeBetween(4, 20);
    }
}
