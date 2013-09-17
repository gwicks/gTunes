/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gmusicplayer;

/**
 *
 * @author merau_000
 */
public class JTSong {
    public String title;
    public String artist;
    public String album;
    public String genre;
    public String year;
    public String artworkURL;

    public JTSong(String title, String artist, String album, String genre, String year) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.year = year;
    }
    
    public String[] toArray()
    {
        return new String[]{this.title,this.artist,this.album,this.genre,this.year};
    }
}
