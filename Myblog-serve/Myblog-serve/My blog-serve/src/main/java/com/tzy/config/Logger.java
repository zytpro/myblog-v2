package com.tzy.config;

import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "app";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();
    private static volatile boolean isRunning = true;

    static {
        // 创建日志目录
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            log.error("Failed to create log directory", e);
        }

        // 启动日志写入线程
        Thread logWriter = new Thread(() -> {
            while (isRunning || !logQueue.isEmpty()) {
                try {
                    LogEntry entry = logQueue.take();
                    writeLog(entry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        logWriter.setDaemon(true);
        logWriter.start();
    }

    private Logger() {}

    public static void log(LogLevel level, String message) {
        try {
            logQueue.put(new LogEntry(level, message));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to queue log message", e);
        }
    }

    private static void writeLog(LogEntry entry) {
        Path logFile = getCurrentLogFile();
        String logMessage = formatLogMessage(entry);

        try {
            Files.write(logFile,
                       logMessage.getBytes(),
                       StandardOpenOption.CREATE,
                       StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to write log", e);
        }
    }

    private static Path getCurrentLogFile() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Path logFile = Paths.get(LOG_DIR, LOG_FILE_PREFIX + "-" + date + ".log");

        try {
            if (Files.exists(logFile) && Files.size(logFile) > MAX_FILE_SIZE) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
                Path rotatedFile = Paths.get(LOG_DIR,
                    LOG_FILE_PREFIX + "-" + date + "-" + timestamp + ".log");
                Files.move(logFile, rotatedFile);
            }
        } catch (IOException e) {
            log.error("Failed to rotate log file", e);
        }

        return logFile;
    }

    private static String formatLogMessage(LogEntry entry) {
        return String.format("%s [%s] %s%n",
            entry.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            entry.level,
            entry.message);
    }

    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }

    // 优雅关闭
    public static void shutdown() {
        isRunning = false;
    }

    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }

    private static class LogEntry {
        final LogLevel level;
        final String message;
        final LocalDateTime timestamp;

        LogEntry(LogLevel level, String message) {
            this.level = level;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }
}
