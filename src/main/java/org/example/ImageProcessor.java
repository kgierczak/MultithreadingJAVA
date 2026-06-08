package org.example;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
import java.util.concurrent.*;

public class ImageProcessor {
    // Maksymalnie 4 wątki jednocześnie
    private static final int THREAD_COUNT = 4;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    public static Image createNegative(Image original) throws Exception {
        return processParallel(original, (reader, writer, x, y) -> {
            Color color = reader.getColor(x, y);
            writer.setColor(x, y, new Color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), color.getOpacity()));
        });
    }

    public static Image applyThresholding(Image original, int threshold) throws Exception {
        double thresholdNormalized = threshold / 255.0;
        return processParallel(original, (reader, writer, x, y) -> {
            Color color = reader.getColor(x, y);
            double brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
            Color newColor = brightness > thresholdNormalized ? Color.WHITE : Color.BLACK;
            writer.setColor(x, y, newColor);
        });
    }

    public static Image applyContour(Image original) throws Exception {
        return processParallel(original, (reader, writer, x, y) -> {
            if (x < original.getWidth() - 1 && y < original.getHeight() - 1) {
                Color c1 = reader.getColor(x, y);
                Color c2 = reader.getColor(x + 1, y + 1);
                double diff = Math.abs(c1.getBrightness() - c2.getBrightness());
                writer.setColor(x, y, diff > 0.1 ? Color.BLACK : Color.WHITE);
            } else {
                writer.setColor(x, y, Color.WHITE);
            }
        });
    }

    // Mechanizm zrównoleglenia zadań na obrazie
    private static Image processParallel(Image original, PixelAction action) throws Exception {
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        WritableImage result = new WritableImage(width, height);
        PixelReader reader = original.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        int chunkHeight = height / THREAD_COUNT;
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int startY = i * chunkHeight;
            final int endY = (i == THREAD_COUNT - 1) ? height : (i + 1) * chunkHeight;

            executor.submit(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        action.process(reader, writer, x, y);
                    }
                }
                latch.countDown();
            });
        }
        latch.await();
        return result;
    }

    public static void shutdown() {
        executor.shutdown();
    }

    @FunctionalInterface
    interface PixelAction {
        void process(PixelReader reader, PixelWriter writer, int x, int y);
    }
}