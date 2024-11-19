import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.awt.event.*;

public class SpotipaiGUI {
    private JLabel albumArt;
    private JLabel songTitle;
    private JLabel songArtist;
    private JList<Song> songList;

    private boolean isPlaying = false;
    private Clip audioClip;
    private long pausedPosition = 0;
    Timer timer;
    private Thread playThread;
    private final Object lock = new Object();

    private JButton playButton;
    private JFrame frame;
    JSlider songSlider;
    JLabel startTime;
    JLabel endTime;

    private Timer updateTimer;
    private JButton nextButton;
    private JLabel nowPlaying;

    public SpotipaiGUI() {
        JFrame frame = new JFrame("Spotipai");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(new Color(40, 40, 40));
        leftPanel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<Song> songs = AmbilLaguDB.getLagu();
        songList = new JList<>(songs.toArray(new Song[0]));
        songList.setCellRenderer(new SongListRenderer());
        songList.setSelectionBackground(new Color(90, 90, 90));
        songList.setSelectionForeground(Color.WHITE);
        songList.setFixedCellHeight(80);
        songList.setFixedCellWidth(300);
        songList.setBackground(new Color(50, 50, 50));
        songList.setFont(new Font("Arial", Font.PLAIN, 16));

        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = songList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Song selectedSong = songList.getModel().getElementAt(index);
                        songTitle.setText(selectedSong.getTitle());
                        songArtist.setText(selectedSong.getArtist());
                        nowPlaying.setText("N O W    P L A Y I N G");
                        ImageIcon imgAlbum = new ImageIcon(new ImageIcon(selectedSong.getThumbnailPath()).getImage().getScaledInstance(320, 320, Image.SCALE_SMOOTH));
                        albumArt.setIcon(imgAlbum);
                        playSelectedSong();
                    }
                }
            }
        });

        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(songList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(50, 50, 50));

        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.setBackground(new Color(40, 40, 40));
        scrollPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPanel.add(scrollPane, BorderLayout.CENTER);

        leftPanel.add(scrollPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBackground(Color.DARK_GRAY);

        albumArt = new JLabel();
        albumArt.setHorizontalAlignment(JLabel.CENTER);
        ImageIcon imgAlbum = new ImageIcon(new ImageIcon("./res/images/none.png").getImage().getScaledInstance(320, 320, Image.SCALE_SMOOTH));
        albumArt.setIcon(imgAlbum);
        albumArt.setPreferredSize(new Dimension(320, 320));
        albumArt.setBackground(Color.DARK_GRAY);
        rightPanel.add(albumArt, BorderLayout.CENTER);

        JPanel songDetailPanel = new JPanel();
        songDetailPanel.setLayout(new BoxLayout(songDetailPanel, BoxLayout.Y_AXIS));
        songDetailPanel.setBackground(Color.DARK_GRAY);
        songDetailPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        songTitle = new JLabel("Pilihlah sebuah lagu dari playlist yang ada!");
        songTitle.setForeground(Color.WHITE);
        songTitle.setFont(new Font("Arial", Font.BOLD, 20));
        songTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        songArtist = new JLabel("Pilihan lagunya banyak kok");
        songArtist.setForeground(Color.LIGHT_GRAY);
        songArtist.setFont(new Font("Arial", Font.PLAIN, 16));
        songArtist.setAlignmentX(Component.CENTER_ALIGNMENT);

        nowPlaying = new JLabel("");
        nowPlaying.setForeground(Color.LIGHT_GRAY);
        nowPlaying.setFont(new Font("Arial", Font.PLAIN, 12));
        nowPlaying.setAlignmentX(Component.CENTER_ALIGNMENT);

        songDetailPanel.add(nowPlaying);
        songDetailPanel.add(songTitle);
        songDetailPanel.add(songArtist);
        rightPanel.add(songDetailPanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBackground(new Color(30, 30, 30));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BorderLayout());
        sliderPanel.setBackground(new Color(30, 30, 30));

        songSlider = new JSlider();
        songSlider.setValue(0);
        songSlider.setBackground(new Color(30, 30, 30));
        songSlider.setForeground(new Color(255, 165, 0));
        songSlider.setOpaque(true);

        startTime = new JLabel("00:00");
        startTime.setForeground(Color.LIGHT_GRAY);

        endTime = new JLabel("00:00");
        endTime.setForeground(Color.LIGHT_GRAY);

        startTime.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        endTime.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        sliderPanel.add(startTime, BorderLayout.WEST);
        sliderPanel.add(songSlider, BorderLayout.CENTER);
        sliderPanel.add(endTime, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));

        JButton prevButton = createControlButton("./res/images/icon/previous.png");
        prevButton.setForeground(Color.YELLOW);
        prevButton.setPreferredSize(new Dimension(50, 50));
        prevButton.setBackground(new Color(30,30,30));

        playButton = createControlButton("./res/images/icon/play.png");
        playButton.setForeground(Color.ORANGE);
        playButton.setPreferredSize(new Dimension(50, 50));
        playButton.setBackground(new Color(30,30,30));

        nextButton = createControlButton("./res/images/icon/next.png");
        nextButton.setForeground(Color.YELLOW);
        nextButton.setPreferredSize(new Dimension(50, 50));
        nextButton.setBackground(new Color(30,30,30));

        buttonPanel.add(prevButton);
        buttonPanel.add(playButton);
        buttonPanel.add(nextButton);

        controlPanel.add(sliderPanel, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        rightPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);
        
        
        playButton.addActionListener(e -> {
            Song selectedSong = songList.getSelectedValue();
            if (selectedSong != null) {
                if (!isPlaying) { 
                    if (playThread == null || !playThread.isAlive()) {
                        playThread = new Thread(() -> {
                            try {
                                String songPath = selectedSong.getSongPath();
                                File audioFile = new File(songPath);
                                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                                audioClip = AudioSystem.getClip();
                                audioClip.open(audioStream);
        
                                synchronized (lock) {
                                    while (true) {
                                        if (pausedPosition > 0) {
                                            audioClip.setMicrosecondPosition(pausedPosition);
                                            pausedPosition = 0;
                                        }
        
                                        audioClip.start();
                                        isPlaying = true;
                                        SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/pause.png")));
        
                                        long durationInMicroseconds = audioClip.getMicrosecondLength();
                                        long durationInMillis = durationInMicroseconds / 1000;
                                        songSlider.setMaximum((int) durationInMillis);
        
                                        timer = new Timer(200, e1 -> {
                                            if (audioClip.isActive()) {
                                                long currentPosition = audioClip.getMicrosecondPosition() / 1000;
                                                SwingUtilities.invokeLater(() -> {
                                                    songSlider.setValue((int) currentPosition);
                                                    startTime.setText(formatTime(currentPosition / 1000));
                                                    endTime.setText(formatTime(durationInMillis / 1000));
                                                });
                                            }
                                            else if (audioClip.getMicrosecondPosition() >= audioClip.getMicrosecondLength()) {
                                                stopCurrentSong();
                                                nextButton.doClick();
                                                updateTimer.stop();
                                            }
                                        });
                                        timer.start();
        
                                        // Thread utama nunggu 100 ms untuk sinkron
                                        while (audioClip.isActive()) {
                                            lock.wait(100);
                                        }
        
                                        timer.stop();
                                        isPlaying = false;
                                        SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/play.png")));
        
                                        lock.wait();
                                    }
                                }
                            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        });
                        playThread.start();
                        playThread.setPriority(Thread.MAX_PRIORITY);
                    }
                    else {
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                }
                else {
                    audioClip.stop();
                    pausedPosition = audioClip.getMicrosecondPosition();
                    isPlaying = false;
                    SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/play.png")));
                    timer.stop();
                }
            } 
            else {
                JOptionPane.showMessageDialog(frame, "Liat sebelah kiri, banyak lagunya", "Pilih salah satu lagu dari playlist!", JOptionPane.WARNING_MESSAGE);
            }
        });

        songSlider.addChangeListener(e -> {
            if (songSlider.getValueIsAdjusting()) {
                long newPositionInMillis = songSlider.getValue();
                if (audioClip != null) {
                    audioClip.setMicrosecondPosition(newPositionInMillis * 1000);
                    if (isPlaying) {
                        SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/pause.png")));
                    }
                    pausedPosition = newPositionInMillis * 1000;
                }
            }
        });
        
        nextButton.addActionListener(e -> {
            if (timer != null) {
                timer.stop();
            }
            songSlider.setValue(0);
            int currentIndex = songList.getSelectedIndex();
            int totalSongs = songList.getModel().getSize();
            
            if (currentIndex < totalSongs - 1) {
                int newIndex = currentIndex + 1;
                stopCurrentSong();
                songList.setSelectedIndex(newIndex);
                songList.ensureIndexIsVisible(newIndex);
                playSelectedSong();
            }
            else {
                stopCurrentSong();
                songList.setSelectedIndex(0);
                songList.ensureIndexIsVisible(0);
                playSelectedSong();
            }
        });
        
        prevButton.addActionListener(e -> {
            if (timer != null) {
                timer.stop();
            }
            songSlider.setValue(0);
            int currentIndex = songList.getSelectedIndex();
            int totalSongs = songList.getModel().getSize();
            if (currentIndex > 0) {
                int newIndex = currentIndex - 1;
                stopCurrentSong();
                songList.setSelectedIndex(newIndex);
                songList.ensureIndexIsVisible(newIndex);
                playSelectedSong();
            }
            else {
                stopCurrentSong();
                songList.setSelectedIndex(totalSongs-1);
                songList.ensureIndexIsVisible(totalSongs-1);
                playSelectedSong();
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpotipaiGUI::new);
    }

    private String formatTime(long detik) {
        long menit = detik / 60;
        long sisa = detik % 60;
        return String.format("%02d:%02d", menit, sisa);
    }

    private void playSelectedSong() {
        Song selectedSong = songList.getSelectedValue();
        if (selectedSong != null) {
            stopCurrentSong();
            pausedPosition = 0;
    
            songTitle.setText(selectedSong.getTitle());
            songArtist.setText(selectedSong.getArtist());
            albumArt.setIcon(new ImageIcon(selectedSong.getThumbnailPath()));
    
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        String songPath = selectedSong.getSongPath();
                        File audioFile = new File(songPath);
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                        audioClip = AudioSystem.getClip();
                        audioClip.open(audioStream);
    
                        long durationInMicroseconds = audioClip.getMicrosecondLength();
                        long durationInMillis = durationInMicroseconds / 1000;
    
                        SwingUtilities.invokeLater(() -> {
                            songSlider.setMaximum((int) durationInMillis);
                            songSlider.setValue(0);
                            startTime.setText(formatTime(0));
                            endTime.setText(formatTime(durationInMillis / 1000));
                        });
    
                        audioClip.start();
                        isPlaying = true;
    
                        SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/pause.png")));
    
                        updateTimer = new Timer(100, e -> {
                            if (audioClip.isActive()) {
                                long currentPosition = audioClip.getMicrosecondPosition() / 1000;
                                SwingUtilities.invokeLater(() -> {
                                    songSlider.setValue((int) currentPosition);
                                    startTime.setText(formatTime(currentPosition / 1000));
                                });
                            }
                            else if (audioClip.getMicrosecondPosition() >= audioClip.getMicrosecondLength()) {
                                stopCurrentSong();
                                nextButton.doClick();
                                updateTimer.stop();
                            }
                            else {
                                updateTimer.stop();
                            }
                        });
                        updateTimer.start();
    
                    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            worker.execute();
        }
        else {
            JOptionPane.showMessageDialog(frame, "Liat sebelah kiri, banyak lagunya", "Pilih salah satu lagu dari playlist!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void stopCurrentSong() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
            audioClip.close();
            isPlaying = false;
            pausedPosition = 0;
            if (timer != null) {
                timer.stop();
            }
            SwingUtilities.invokeLater(() -> {
                songSlider.setValue(0);
            });
        }
    }
        
    private JButton createControlButton(String icon) {
        JButton btn = new JButton();
        btn.setIcon(new ImageIcon(icon));
        btn.setBackground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(50, 50));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(50, 50, 50));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(30,30,30));
            }
        });
        return btn;
    }
}