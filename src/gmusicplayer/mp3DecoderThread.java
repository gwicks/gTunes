package gmusicplayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.FloatControl;
import org.mp3transform.Decoder;

/**
 *
 * @author merau_000
 */
public class mp3DecoderThread implements Runnable {

    public Decoder decoder = new Decoder(); //Actual audio decoder
    public Thread thread;
    public File currentFile = null; //Actual file object
    public String currentStream; //If it's a google play song, this is the stream url
    private boolean stop;
    private boolean streaming = false; //Determines if its a google play song or not.
    public float volume; //Actually a gain control, but close enough
    private BufferedInputStream bin = null;
    
    public void stopPlaying() {
        stop = true; //Let the thread know we are stopping
        decoder.stop(); //Actually kill the decoder
        
        if (thread != null) {
            try {
                System.out.println("Joined thread");
                //if (bin != null) {
                    //bin.close();
                //}
                thread.join();
                
            } catch (Exception e) {
                // ignore any problems.
                System.out.println(e.getMessage());
            }
        }
    }

    //The playing probably doesn't need a seperate method for streaming and local file, but whatever, my code, my rules.
    public mp3DecoderThread startPlaying(File file, float volume) {
        mp3DecoderThread t = new mp3DecoderThread();
        t.currentFile = file;
        t.volume = volume;
        t.streaming = false; //We aren't streaming for this particular instance.
        t.currentStream = "";
        Thread thread = new Thread(t);

        t.thread = thread;
        thread.start();
        return t;
    }

    public mp3DecoderThread startPlayingStream(String url, float volume) {
        mp3DecoderThread t = new mp3DecoderThread();
        t.currentFile = null;
        t.volume = volume;
        t.streaming = true;
        t.currentStream = url;
        Thread thread = new Thread(t);

        t.thread = thread;
        thread.start();
        return t;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                //If it's a local file...
                if (!streaming) {
                    //If our file is not there, don't even try to play, just stop
                    if (currentFile == null) {
                        break;
                    }
                    play(currentFile, volume);
                } else {
                    //Make sure we actually have a URL of some sort.
                    //The erroredout boolean tells us if we're having over 800 errors in the decode process
                    //This stops it from freezing when a streamed song ends.
                    if (currentStream.equals("") || decoder.erroredout) {
                        break;
                    }
                    playStream(currentStream, volume);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public void play(File file, float avolume) throws IOException {
        stop = false;
        
        streaming = false;
        //Be sure we're actually playing an mp3
        if (!file.getName().endsWith(".mp3")) {
            return;
        }
        //Set the current file we're playing
        currentFile = file;
        System.out.println("playing: " + file);
        FileInputStream in = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(in);
        decoder.play(file.getName(), bin, avolume);
    }

    public void playStream(String url, float svolume) throws IOException {
        stop = false;
        streaming = true;
        
        try {
            bin = new BufferedInputStream(new URL(url).openStream());

        } catch (IOException ex) {
            if (bin != null) {
                bin.close();
                
            }
            
            decoder.stop();
            Logger.getLogger(mp3DecoderThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (bin != null) {
            decoder.play(url, bin, svolume);
            
        }
    }
}
