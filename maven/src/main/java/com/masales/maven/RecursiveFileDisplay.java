package com.masales.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.function.Consumer;
import java.util.concurrent.*;

public class RecursiveFileDisplay {

    public static void main(String[] args) {
        File currentDir = new File("."); // current directory
        displayDirectoryContents(currentDir);
    }

    public static void displayDirectoryContents(File dir) {
        try {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (containsPom(file)) {
                        executeCommand(file, "mvn clean package");
                        executeCommand(file, "mvn clean");
                    } else {
                      displayDirectoryContents(file);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void executeCommand(File file, String command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", command);
        builder.directory(file);
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        System.out.println(exitCode);
    }

    public static boolean containsPom(File base) {
        File pom = new File(base, "pom.xml");
        return pom.exists();
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }

}
