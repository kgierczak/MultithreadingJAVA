package org.example;

import java.io.IOException;
import java.util.logging.*;

public class AppLogger {
    private static final Logger logger = Logger.getLogger("AppLogger");

    public static void init() {
        try {
            FileHandler fileHandler = new FileHandler("app_log.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            logger.info("Uruchomienie aplikacji");
        } catch (IOException e) {
            System.err.println("Nie udało się zainicjować loggera.");
        }
    }

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logError(String message, Throwable e) {
        logger.log(Level.SEVERE, message, e);
    }
}