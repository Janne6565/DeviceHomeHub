package com.janne.webserverraspberrypi.services.ambilight;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AlbumPaletteTest {

    @Test
    void keepsColorsDropsBlackBorderAndWhiteText() {
        BufferedImage cover = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = cover.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 300, 300);
        g.setColor(new Color(220, 40, 60));      // red, largest area
        g.fillRect(30, 30, 240, 150);
        g.setColor(new Color(30, 90, 220));      // blue, smaller
        g.fillRect(30, 180, 240, 90);
        g.setColor(Color.WHITE);                 // "title text"
        g.fillRect(100, 100, 100, 15);
        g.dispose();

        List<AlbumPalette.Swatch> palette = AlbumPalette.extract(cover);

        assertThat(palette).hasSize(2);
        assertThat(hue(palette.get(0).color())).isCloseTo(hue(new Color(220, 40, 60)), within(0.02f));
        assertThat(hue(palette.get(1).color())).isCloseTo(hue(new Color(30, 90, 220)), within(0.02f));
        assertThat(palette.get(0).weight()).isGreaterThan(palette.get(1).weight());
        assertThat(palette.stream().mapToDouble(AlbumPalette.Swatch::weight).sum()).isCloseTo(1.0, within(1e-9));
    }

    @Test
    void greyscaleCoverStillYieldsOneColor() {
        BufferedImage cover = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = cover.createGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        assertThat(AlbumPalette.extract(cover)).hasSize(1);
    }

    @Test
    void renderStartsWithDominantColorAtTopCentre() {
        List<AlbumPalette.Swatch> palette = List.of(
                new AlbumPalette.Swatch(Color.RED, 0.5),
                new AlbumPalette.Swatch(Color.BLUE, 0.5));

        BufferedImage frame = AlbumPalette.render(palette);

        assertThat(frame.getWidth()).isEqualTo(AlbumPalette.FRAME_WIDTH);
        assertThat(frame.getHeight()).isEqualTo(AlbumPalette.FRAME_HEIGHT);
        Color top = new Color(frame.getRGB(AlbumPalette.FRAME_WIDTH / 2 - 1, 0));
        Color bottom = new Color(frame.getRGB(AlbumPalette.FRAME_WIDTH / 2 - 1, AlbumPalette.FRAME_HEIGHT - 1));
        assertThat(top.getRed()).isGreaterThan(200);
        assertThat(bottom.getBlue()).isGreaterThan(200);
    }

    private static float hue(Color c) {
        return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[0];
    }
}
