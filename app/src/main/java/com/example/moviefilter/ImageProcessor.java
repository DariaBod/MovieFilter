package com.example.moviefilter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ImageProcessor {
    public static int[][] splitImage(Bitmap bitmap, int rows, int cols) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[][] averageColors = new int[rows][cols];

        int cellWidth = width / cols;
        int cellHeight = height / rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int startX = j * cellWidth;
                int startY = i * cellHeight;

                int endX = startX + cellWidth;
                int endY = startY + cellHeight;

                averageColors[i][j] = calculateAverageColor(bitmap, startX, startY, endX, endY);
            }
        }

        return averageColors;
    }

    private static int calculateAverageColor(Bitmap bitmap, int startX, int startY, int endX, int endY) {
        int totalPixels = 0;
        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int pixel = bitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                redSum += red;
                greenSum += green;
                blueSum += blue;

                totalPixels++;
            }
        }

        if (totalPixels == 0) {
            return Color.BLACK; // or any default color
        }

        int averageRed = redSum / totalPixels;
        int averageGreen = greenSum / totalPixels;
        int averageBlue = blueSum / totalPixels;

        return Color.rgb(averageRed, averageGreen, averageBlue);
    }
}
