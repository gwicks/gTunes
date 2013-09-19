/*
 * Name: jTunes Media Player
 * Author: Greg Wicks
 * Date: 1/18/2013
 * School: Washington-Lee High School
 * Computer Systems Used: Toshiba Tecra A9/Sony VAIO AVE14A27CXH
 * IDE: Netbeans IDE 7.2.1
 * Purpose of Program: To play iTunes format AAC audio files and display metadata, 
 * and to stream the user's Google Play Music (Skyjam) songs.
 * Purpose of this File: To provide a count up timer for a song.
*/
package gmusicplayer;

/**
 *
 * @author merau_000
 */
public class SongTimer {

    public int currTime = 0;
    private boolean runIt = false;
    public Thread timeThread = null;

    private class runTimer implements Runnable {

        public void run() {
            try {
                MainWindow m = (MainWindow) MainWindow.getWindows()[0];
                
                while (runIt) {
                    currTime++;
                    Thread.sleep(1000);
                    //System.out.println(currTime);
                    m.setSongTime(currTime);
                }
            } catch (InterruptedException ie) {
                runIt = false;
            }
        }
    }

    public void start() {
        runIt = true;
        timeThread = new Thread(new runTimer());
        timeThread.start();
    }
}
