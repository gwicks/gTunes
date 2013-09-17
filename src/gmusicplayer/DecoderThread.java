/*
 * Name: jTunes Media Player
 * Author: Greg Wicks
 * Date: 1/18/2013
 * School: Washington-Lee High School
 * Computer Systems Used: Toshiba Tecra A9/Sony VAIO AVE14A27CXH
 * IDE: Netbeans IDE 7.2.1
 * Purpose of Program: To play iTunes format AAC audio files and display metadata, 
 * and to stream the user's Google Play Music (Skyjam) songs.
 * Purpose of this File: To decode the AAC audio and send it to the line out.
*/
package gmusicplayer;

import java.awt.Window;
import java.io.RandomAccessFile;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;

/**
 *
 * @author merau_000
 */
public class DecoderThread {

    public String songPath; //File path reference.
    public String[] songPathList; //Unused as of now
    public Thread mainThread; //The main object everything runs on
    private static SourceDataLine line = null; //The actual audio output line, as exposed by the Java Sound API
    public FloatControl volume; //Volume Control
    //private MainWindow mainwindow = (MainWindow) Window.getWindows()[0]; //This object holds the parent window 
    //private SettingsStorage settings = mainwindow.settings;
    
    //These objects are used by the decoding process.
    private byte[] b;
    private MP4Container cont;
    private Movie movie;
    private List<Track> tracks;
    private AudioTrack track;
    private AudioFormat aufmt;
    private SampleBuffer buf;
    
    
    public float currVol;
    

    private class runDecode implements Runnable {

        @Override
        public void run() {
            try {

                try {
                    //create container
                    cont = new MP4Container(new RandomAccessFile(songPath, "r"));
                    movie = cont.getMovie();
                    //find AAC track
                    tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
                    if (tracks.isEmpty()) {
                        throw new Exception("movie does not contain any AAC track");
                    }
                    track = (AudioTrack) tracks.get(0);
                    
                    //create audio format
                    aufmt = new AudioFormat(track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);
                    line = AudioSystem.getSourceDataLine(aufmt);

                    line.open();
                    line.start();
                    
                    volume = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                    
                    //volume.setValue(mainwindow.gain);
                    
                    
                    
                    //create AAC decoder
                    final Decoder dec = new Decoder(track.getDecoderSpecificInfo());

                    //decode
                    Frame frame;
                    buf = new SampleBuffer();
                    
                    System.out.println(Float.toString(volume.getValue()));
                    
                    while (track.hasMoreFrames()) {
                        frame = track.readNextFrame();
                        
                        try {
                            dec.decodeFrame(frame.getData(), buf);

                            b = buf.getData();
                            
                            line.write(b, 0, b.length);
                        } catch (AACException e) {
                            e.printStackTrace(System.out);
                            //since the frames are separate, decoding can continue if one fails
                        }
                        
                    }
                    
                    
                } finally {
                    //Once we're done playing, close the audio line
                    if (line != null) {
                        
                        line.stop();
                        line.close();
                        
                    }
                }
            } catch (InterruptedException interrupt) {
                //Handle interrupts by killing the audio
                line.stop();
                line.close();
                
            } catch (Exception e) {
            }
        }
    }

    public void start() {
        //The clear function is so we don't end up with bizarre song audio data mix ups. 
        //This creates such monstrosities as a Bon Jovi song's frames being mixed up with Bob Seger song's frames
        //However, we don't want to close the since were starting a song.
        clearOldUselessShit();
        mainThread = new Thread(new runDecode());
        mainThread.start();
        
    }

    public void stop() {
        System.out.println("Interrupted Thread");
        //Clear all the objects and buffers
        b = null;
        cont = null;
        movie = null;
        track = null;
        aufmt = null;
        buf = null;
        //Stop and close the line
        line.stop();
        line.close();
    }
    
    public void clearOldUselessShit() {
        b = null;
        cont = null;
        movie = null;
        tracks = null;
        track = null;
        aufmt = null;
        buf = null;
        
    }
}
