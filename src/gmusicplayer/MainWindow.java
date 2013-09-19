package gmusicplayer;

import javax.swing.table.DefaultTableModel;
import gmusic.api.impl.GoogleMusicAPI;
import gmusic.api.impl.InvalidCredentialsException;
import gmusic.api.model.Song;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author merau_000
 */
public class MainWindow extends javax.swing.JFrame {

    public mp3DecoderThread decThread;
    public GoogleMusicAPI api;
    private DefaultTableModel tableModel;
    public List<Song> songList;
    public List<JTSong> displaySongList;
    public int selectedIndex;
    private SongTimer st = new SongTimer();

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        api = new GoogleMusicAPI();
        tableModel = new DefaultTableModel(new Object[]{"Title", "Artist", "Album", "Genre", "Year"}, 0);
        jTable1.setModel(tableModel);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                selectedIndex = jTable1.getSelectedRow();
                System.out.println(selectedIndex);

            }
        });
        decThread = new mp3DecoderThread();
        //tableModel.addRow(new Object[]{"I Appear Missing","Queens of the Stone Age","...Like Clockwork","Hard Rock","2013"});
    }

    int minutesprefix = 0;
    
    public int findMins(long seconds)
    {
        int count = 0;
        while (seconds >= 60)
        {
            seconds = seconds - 60;
            count = count + 1;
        }
        return count;
    }
    
    public String secondsToTimer(long seconds)
    {
        int sminutesprefix = 0;
        String timeString = "";
        
        if (seconds < 10) {
            timeString = "0:0" + Long.toString(seconds);
        } else if (seconds >= 10 && seconds < 60) {
            timeString = "0:" + Long.toString(seconds);
        } else {
            long remaindero = seconds % 60;
            sminutesprefix = findMins(seconds);
            if (remaindero == 0) {
                
                timeString = Integer.toString(sminutesprefix) + ":00";
            } else {
                long remainder = seconds % 60;

                if (remainder < 10) {
                    String finalnumber = Long.toString(remainder);
                    timeString = Integer.toString(sminutesprefix) + ":0" + finalnumber;
                } else {
                    String finalnumber = Long.toString(remainder);
                    timeString = Integer.toString(sminutesprefix) + ":" + finalnumber;
                }
            }
        }
        
        return timeString;
    }
    
    public void setSongTime(int seconds) {
        String timeString = "";
        if (seconds < 10) {
            timeString = "0:0" + Integer.toString(seconds);
        } else if (seconds >= 10 && seconds < 60) {
            timeString = "0:" + Integer.toString(seconds);
        } else {
            int remaindero = seconds % 60;
            if (remaindero == 0) {
                minutesprefix++;
                timeString = Integer.toString(minutesprefix) + ":00";
            } else {
                int remainder = seconds % 60;

                if (remainder < 10) {
                    String finalnumber = Integer.toString(remainder);
                    timeString = Integer.toString(minutesprefix) + ":0" + finalnumber;
                } else {
                    String finalnumber = Integer.toString(remainder);
                    timeString = Integer.toString(minutesprefix) + ":" + finalnumber;
                }
            }
        }
        
        songTime.setText(timeString);
        if (seconds < jProgressBar1.getMaximum())
        {
            jProgressBar1.setValue(seconds);
        }
        else
        {
            jProgressBar1.setValue(0);
            decThread.stopPlaying();
            st.timeThread.interrupt();
            st = new SongTimer();
            
        }
    }
    
    
    public BufferedImage scaleImage(BufferedImage img, int width, int height) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        if (imgWidth * height < imgHeight * width) {
            width = imgWidth * height / imgHeight;
        } else {
            height = imgHeight * width / imgWidth;
        }
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.clearRect(0, 0, width, height);
            g.drawImage(img, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return newImage;
    }

    public List<Song> sortSongs(List<Song> songs) {
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(final Song object1, final Song object2) {
                return object1.getTitle().compareTo(object2.getTitle());
            }
        });

        return songs;
    }

    public void GoogleLogin(String uname, String pwd) {
        try {
            api.login(uname, pwd);
            System.out.println("Login Successful");
            Collection<Song> songs = api.getAllSongs();

            System.out.println(songs.size() + " songs");
            Song[] sa = new Song[songs.size()];
            songs.toArray(sa);

            displaySongList = new ArrayList<JTSong>();
            songList = Arrays.asList(sa);
            songList = sortSongs(songList);

            for (Song s : songList) {
                String year = Integer.toString(s.getYear());
                long dur = s.getDurationMillis();
                if (year.equals("0"))
                {
                    year = "";
                
                }
                JTSong tempSong = new JTSong(s.getTitle(), s.getArtist(), s.getAlbum(), s.getGenre(), year);
                tempSong.duration = dur;
                if (s.getAlbumArtUrl() == null) {
                    tempSong.artworkURL = "http://s.iosfans.com/?u=http://i165.photobucket.com/albums/u61/deveelryuk/carrierrequests/noartplaceholder_darkgrey.png";
                } else {
                    tempSong.artworkURL = "http:" + s.getAlbumArtUrl();
                }
                displaySongList.add(tempSong);
            }

            System.out.println(displaySongList.get(0).title);

            for (JTSong js : displaySongList) {
                tableModel.addRow(js.toArray());
            }

        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidCredentialsException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton2 = new javax.swing.JButton();
        titleLabel = new javax.swing.JLabel();
        artistLabel = new javax.swing.JLabel();
        albumLabel = new javax.swing.JLabel();
        genreLabel = new javax.swing.JLabel();
        yearLabel = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        songTime = new javax.swing.JLabel();
        endTime = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jButton1.setText("Login");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Play");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        titleLabel.setText("Title:");

        artistLabel.setText("Artist:");

        albumLabel.setText("Album:");

        genreLabel.setText("Genre:");

        yearLabel.setText("Year:");

        jButton3.setText("Stop");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        songTime.setText("0:00");

        endTime.setText("0:00");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(artistLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(10, 10, 10)
                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(albumLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(genreLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(songTime)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(endTime))
                                            .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                                            .addComponent(yearLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(180, 180, 180)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(titleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(artistLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(albumLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(genreLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(yearLabel)
                        .addGap(27, 27, 27)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(songTime)
                            .addComponent(endTime))
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3)))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        LoginDialog dialog = new LoginDialog(this,true);
        dialog.parent = this;
        dialog.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        
        JTSong relatedSong = displaySongList.get(selectedIndex);
        try {

            BufferedImage currArt = ImageIO.read(new URL(relatedSong.artworkURL));
            currArt = scaleImage(currArt, 256, 256);
            jLabel1.setIcon(new ImageIcon(currArt));
            titleLabel.setText("Title:   " + relatedSong.title);
            artistLabel.setText("Artist:    " + relatedSong.artist);
            albumLabel.setText("Album:   " + relatedSong.album);
            genreLabel.setText("Genre:   " + relatedSong.genre);
            yearLabel.setText("Year:      " + relatedSong.year);
            
            

        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            String streamURL = api.getSongURL(songList.get(selectedIndex)).toString();
            decThread = decThread.startPlayingStream(streamURL, -15.0f);
            st.start();
            jProgressBar1.setMinimum(0);
            long lduration = relatedSong.duration;
            lduration = lduration / 1000;
            int duration = safeLongToInt(lduration);
            
            jProgressBar1.setMaximum(duration);
            endTime.setText(secondsToTimer(relatedSong.duration / 1000));
        } catch (URISyntaxException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    public static int safeLongToInt(long l) {
    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
        throw new IllegalArgumentException
            (l + " cannot be cast to int without changing its value.");
    }
    return (int) l;
}
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        System.out.println("Stop");
        decThread.stopPlaying();
        st.timeThread.interrupt();
        st = new SongTimer();
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel albumLabel;
    private javax.swing.JLabel artistLabel;
    private javax.swing.JLabel endTime;
    private javax.swing.JLabel genreLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel songTime;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel yearLabel;
    // End of variables declaration//GEN-END:variables
}
