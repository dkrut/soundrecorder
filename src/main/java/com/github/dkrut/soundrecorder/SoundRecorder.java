package com.github.dkrut.soundrecorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SoundRecorder
{
    private static final Logger log = LoggerFactory.getLogger(SoundRecorder.class);
    private static final String DROPBOX = "dropbox";
    private static final String GOOGLE_DISK = "google";
    private AudioFileFormat.Type fileType;
    private TargetDataLine line;
    private AudioFormat audioFormat;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'_'HHmmss");

    public SoundRecorder() {
        fileType = AudioFileFormat.Type.WAVE;
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            log.info("Initializing Audio System");
            line = (TargetDataLine) AudioSystem.getLine(info);
        } catch (Exception e) {
            log.error("Error while Audio System initializing: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void recordSound(long milliseconds, String cloud, boolean deleteAfter) {
        File file = new File(formatter.format(new Date(System.currentTimeMillis())) + ".wav");
        start(file);
        delayFinish(file, milliseconds, cloud, deleteAfter);
    }

    private void start(File file) {
        new Thread(() ->
        {
            try {
                log.debug("Open line");
                line.open(audioFormat);
                log.debug("Start line");
                line.start();
                AudioInputStream ais = new AudioInputStream(line);
                log.info("Start recording file '{}'", file.getName());
                AudioSystem.write(ais, fileType, file);
                log.info("Recording file '{}' finished", file.getName());
            } catch (Exception ex) {
                log.error("Error during recording '{}': " + ex.getMessage(), file.getName());
                ex.printStackTrace();
            }
        }).start();
    }

    private void delayFinish(File fileName, long delayTime, String cloud, boolean deleteAfter) {
        new Thread(() ->
        {
            try
            {
                Thread.sleep(delayTime);
                log.debug("Line stop");
                line.stop();
                log.debug("Line close");
                line.close();
                if (cloud.equals(DROPBOX)) {
                    DiskDropbox diskDropbox = new DiskDropbox();
                    diskDropbox.uploadFile(fileName, deleteAfter);
                    return;
                }
                if (cloud.equals(GOOGLE_DISK)) {
                    DiskGoogle diskGoogle = new DiskGoogle();
                    diskGoogle.uploadFile(fileName, deleteAfter);
                }
                else {
                    log.warn("File '{}' not uploaded to cloud. Check 'cloud' value in 'app.properties'. " +
                            "\nValid value: 'dropbox' or 'google'" +
                            "\nCurrent value: '{}'", fileName.getName(), cloud);
                    log.warn("File left at '{}'", fileName.getAbsolutePath());
                }
            }
            catch (InterruptedException e) {
                log.error("Error in thread sleeping: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}