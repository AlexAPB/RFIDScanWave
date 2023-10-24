package com.fatec.rfidscanwave.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageUtil {
    public static Image getRoundImage(Image image, int radius) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage wImage = new WritableImage(radius * 2, radius * 2);
        PixelWriter pixelWriter = wImage.getPixelWriter();
        PixelReader pixelReader = image.getPixelReader();
        Color c1 = new Color(0, 0, 0, 0);

        int w = (width / 2);
        int h = (height / 2);
        int r = radius * radius;

        for (int i = (width / 2) - radius, k = 0; i < w + radius; i++, k++)
            for (int j = (height / 2) - radius, b = 0; j < h + radius; j++, b++) {
                if ((i - w) * (i - w) + (j - h) * (j - h) > r)
                    pixelWriter.setColor(k, b, c1);
                else
                    pixelWriter.setColor(k, b, pixelReader.getColor(i, j));
            }
        return wImage;
    }
}
