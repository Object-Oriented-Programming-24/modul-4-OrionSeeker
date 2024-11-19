import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.awt.event.*;

// Class ini adalah class utama dari Spotipai. Di sini terdapat fungsi main, GUI, dan berbagai actionListener dan MouseListener
public class SpotipaiGUI {
    // Kode di bawah ini digunakan untuk mendeklarasikan albumArt, songTitle, songArtist yang bertipe JLabel, kemudian ada songList yaitu sebuah JList dengan tipe data Song. Modifiernya private karena cuma di class ini aja dipakenya, ga perlu diakses class lain.
    private JLabel albumArt;
    private JLabel songTitle;
    private JLabel songArtist;
    private JList<Song> songList;

    // Kode di bawah ini digunakan untuk menyimpan status lagu sedang diputar atau tidak
    private boolean isPlaying = false;

    // Kode di bawah ini adalah objek Clip yang digunakan untuk memutar audio
    private Clip audioClip;

    // Kode di bawah ini digunakan untuk menyimpan posisi terakhir audio ketika dipause, default valuenya 0
    private long pausedPosition = 0;

    // Kode di bawah ini digunakan untuk mengatur slider dan durasi audio
    Timer timer;

    // Kode di bawah ini adalah thread yang digunakan untuk menjalankan audio di background, terpisah dari thread utama GUI
    private Thread playThread;

    // Kode di bawah ini adalah objek lock untuk sinkronasi untuk mengontrol akses ke Thread yang memutar audio
    private final Object lock = new Object();

    // Kode di bawah ini adalah button play, frame utama, slider, durasi waktu berjalan, dan total durasi waktu yang didefinsikan secara global agar dapat digunakan oleh seluruh method dalam class ini
    private JButton playButton;
    private JFrame frame;
    JSlider songSlider;
    JLabel startTime;
    JLabel endTime;

    // Kode di bawah ini digunakan untuk memperbarui songSlider sesuai durasi yang sudah berjalan
    private Timer updateTimer;

    // Kode di bawah ini adalah button untuk next, dan tulisan now playing, dibuat global agar bisa diakses seluruh method dalam class ini
    private JButton nextButton;
    private JLabel nowPlaying;

    // Method di bawah ini adalah konstruktor
    public SpotipaiGUI() {

        // Kode di bawah ini membuat frame bernama Spotipai, yang ukurannya 800x600px, dan layoutnya borderlayout
        JFrame frame = new JFrame("Spotipai");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Kode di bawah ini membuat panel sebelah kiri, yang layoutnya borderlayout, warna backgroundnya RGB(40,40,40), ada dibuat garis border ketebalan 2cm dengan warna RGB(80,80,80), dan padding 10px di semua sisi.
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(new Color(40, 40, 40));
        leftPanel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Kode di bawah ini adalah pengambilan data lagu dari database melalui method getLagu milik class AmbilLaguDB yang disimpan di sebuah List songs
        List<Song> songs = AmbilLaguDB.getLagu();

        // Kode di bawah ini adalah pembuatan JList dari list songs 
        songList = new JList<>(songs.toArray(new Song[0]));

        // Kode di bawah ini adlaah pengaturan renderer JList menggunakan SongListRenderer (ada class terpisah), warna backgroundnya untuk item terpilih RGB(90,90,90), foregroundnya putih, tinggi cellnya 80, lebarnya 300
        // untuk background utamanya RGB(50,50,50), fontnya Arial 16pt
        songList.setCellRenderer(new SongListRenderer());
        songList.setSelectionBackground(new Color(90, 90, 90));
        songList.setSelectionForeground(Color.WHITE);
        songList.setFixedCellHeight(80);
        songList.setFixedCellWidth(300);
        songList.setBackground(new Color(50, 50, 50));
        songList.setFont(new Font("Arial", Font.PLAIN, 16));

        // Kode di bawah ini adalah MouseListener dengan method mouseClicked untuk menanggapi klik yang dilakukan pengguna pada cell di JList
        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                // Kode di bawah ini digunakan ketika cell diklik 1x
                if (e.getClickCount() == 1) {

                    // Kode di bawah ini digunakan untuk mengambil indeks lagu yang diklik, lagu yang mana
                    int index = songList.locationToIndex(e.getPoint());
                    // Kalo indeksnya >=0 (valid), maka ambil lagunya, ubah title, artist, dan image album mengikuti lagu yang diambil, kemudian tampilkan now playing dan panggil method playSelectedSong untuk memutar lagu
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

        // Kode di bawah ini digunakan untuk memastikan hanya 1 cell JList yang dapat diklik atau dipilih dalam satu waktu
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Kode di bawah ini adalah scrollPane yang digunakan untuk scrolling daftar lagu-lagu yang ada di JList songList
        // diberikan garis border RGB(80,80,80) dengan ketebalan 1, setiap kali mouse discroll bergeser 16px, dan warna backgroundnya RGB(50,50,50)
        JScrollPane scrollPane = new JScrollPane(songList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(50, 50, 50));

        // Kode di bawah ini digunakan untuk membuat panel dengan layout BorderLayout, warna backgroundnya RGB(40,40,40), padding segala sisi 10px, dan dimasukkan scrollPane di posisi center
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.setBackground(new Color(40, 40, 40));
        scrollPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPanel.add(scrollPane, BorderLayout.CENTER);

        // Kode di bawah ini digunakan untuk memasukkan scrollPanel ke dalam leftPanel di posisi center
        leftPanel.add(scrollPanel, BorderLayout.CENTER);

        // Kode di bawah ini digunakan untuk membuat panel untuk menyimpan panel atau label lainnya di sebelah kiri, layoutnya borderLayout, backgroundnya dark gray
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBackground(Color.DARK_GRAY);

        // Kode di bawah ini adalah pembuatan label bernama AlbumArt, yang diset alignmentnya center, diisi icon none (ketika lagu belum ada yang diputar, ketka sudah ada, akan direplace album lagu)
        // ukurannya 320x320px, warna backgroundnya dark gray, dan ditambahkan ke dalam rightPanel di posisi center
        albumArt = new JLabel();
        albumArt.setHorizontalAlignment(JLabel.CENTER);
        ImageIcon imgAlbum = new ImageIcon(new ImageIcon("./res/images/none.png").getImage().getScaledInstance(320, 320, Image.SCALE_SMOOTH));
        albumArt.setIcon(imgAlbum);
        albumArt.setPreferredSize(new Dimension(320, 320));
        albumArt.setBackground(Color.DARK_GRAY);
        rightPanel.add(albumArt, BorderLayout.CENTER);

        // Kode di bawah ini adalah panel yang menampung title dan artist dari suatu lagu, layoutnya box layout (Y_Axis / vertikal), warna backgroundnya dark gray, dan diberikan padding atas 25px 
        JPanel songDetailPanel = new JPanel();
        songDetailPanel.setLayout(new BoxLayout(songDetailPanel, BoxLayout.Y_AXIS));
        songDetailPanel.setBackground(Color.DARK_GRAY);
        songDetailPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        // Kode di bawah ini adalah label yang digunakan untuk judul lagu, warnanya putih, fontnya arial, bold, 20pt, align center. Awalnya diberi value "Pilihlah sebuah lagu dari playlist yang ada!"
        songTitle = new JLabel("Pilihlah sebuah lagu dari playlist yang ada!");
        songTitle.setForeground(Color.WHITE);
        songTitle.setFont(new Font("Arial", Font.BOLD, 20));
        songTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Kode di bawah ini adalah label yang digunakan untuk artis lagu, warnanya light gray, fontnya arial, 16pt, align center. Awalnya diberi value "Pilihan lagunya banyak kok"
        songArtist = new JLabel("Pilihan lagunya banyak kok");
        songArtist.setForeground(Color.LIGHT_GRAY);
        songArtist.setFont(new Font("Arial", Font.PLAIN, 16));
        songArtist.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Kode di bawah ini adalah label yang digunakan untuk menampilkan tulisan NOW PLAYING ketika ada lagu yang diputar, warnanya light gray, arial, 12pt, align center
        nowPlaying = new JLabel("");
        nowPlaying.setForeground(Color.LIGHT_GRAY);
        nowPlaying.setFont(new Font("Arial", Font.PLAIN, 12));
        nowPlaying.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Kode di bawah ini digunakan untuk memasukkan nowPlaying, songTitle, dan songArtist ke dalam songDetailPanel, kemudian panel ini dimasukkan ke right panel posisi north
        songDetailPanel.add(nowPlaying);
        songDetailPanel.add(songTitle);
        songDetailPanel.add(songArtist);
        rightPanel.add(songDetailPanel, BorderLayout.NORTH);

        // Kode di bawah ini digunakan untuk membuat panel yang akan digunakan untuk kontrol, layoutnya borderlayout, warna backgroundnya RGB(30,30,30), diberi padding segala arah 10px
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBackground(new Color(30, 30, 30));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Kode di bawah ini adalah panel dengan layout borderlayout, warna background RGB(30,30,30) yang digunakan untuk menyimpan songSlider
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BorderLayout());
        sliderPanel.setBackground(new Color(30, 30, 30));

        // Kode di bawah ini adalah pembuatan JSlider yang akan digunakan sebagai slide durasi lagu. Value awalnya 0, warna backgroundnya RGB(30,30,30), foregroundnya RGB(255,165,0) dan dipastikan tidak transparan
        songSlider = new JSlider();
        songSlider.setValue(0);
        songSlider.setBackground(new Color(30, 30, 30));
        songSlider.setForeground(new Color(255, 165, 0));
        songSlider.setOpaque(true);

        // Kode di bawah ini adalah pembuatan label untuk durasi lagu yang diputar dan durasi maksimal lagu, keduanya diberi warna light gray
        startTime = new JLabel("00:00");
        startTime.setForeground(Color.LIGHT_GRAY);
        endTime = new JLabel("00:00");
        endTime.setForeground(Color.LIGHT_GRAY);

        // Kode di bawah ini digunakan untuk memberikan padding kiri kanan 10px pada startTime dan endTIme
        startTime.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        endTime.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Kode di bawah ini digunakan untuk memasukkan startTime, songSlider, dan endTime ke dalam sliderPanel di posisi west, center, dan east
        sliderPanel.add(startTime, BorderLayout.WEST);
        sliderPanel.add(songSlider, BorderLayout.CENTER);
        sliderPanel.add(endTime, BorderLayout.EAST);

        // Kode di bawah ini adalah panel yang akan digunakan untuk menyimpan button prev, play, dan next. Layoutnya pakai flowlayout center dengan padding horizontal 20px dan warna background RGB(30,30,30)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));

        // Kode di bawah ini adalah pembuatan prevButton dengan icon di path, warna foregroundnya kuning, ukuran 50x50px, dan warna backgroudnnya RGB(30,30,30)
        JButton prevButton = createControlButton("./res/images/icon/previous.png");
        prevButton.setForeground(Color.YELLOW);
        prevButton.setPreferredSize(new Dimension(50, 50));
        prevButton.setBackground(new Color(30,30,30));

        // Kode di bawah ini adalah pembuatan playButton dengan icon di path, warna foregroundnya kuning, ukuran 50x50px, dan warna backgroudnnya RGB(30,30,30)
        playButton = createControlButton("./res/images/icon/play.png");
        playButton.setForeground(Color.ORANGE);
        playButton.setPreferredSize(new Dimension(50, 50));
        playButton.setBackground(new Color(30,30,30));

        // Kode di bawah ini adalah pembuatan nextBUtton dengan icon di path, warna foregroundnya kuning, ukuran 50x50px, dan warna backgroudnnya RGB(30,30,30)
        nextButton = createControlButton("./res/images/icon/next.png");
        nextButton.setForeground(Color.YELLOW);
        nextButton.setPreferredSize(new Dimension(50, 50));
        nextButton.setBackground(new Color(30,30,30));

        // Kode di bawah ini digunakan untuk memasukkan semua button tadi ke dalam buttonPanel
        buttonPanel.add(prevButton);
        buttonPanel.add(playButton);
        buttonPanel.add(nextButton);

        // Kode di bawah ini digunakan untuk memasukkan sliderPanel dan buttonPanel ke dalam controlPanel di posisi north dan south
        controlPanel.add(sliderPanel, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Kode di bawah ini digunakan untuk memasukkan controlPanel ke dalam rightPanel di posisi south
        rightPanel.add(controlPanel, BorderLayout.SOUTH);

        // Kode di bawah ini digunakan untuk memasukkan leftPanel dan rightPanel ke dalam frame di posisi west dan center
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);
        
        // Kode di bawah ini digunaakn untuk mengatur listener untuk tombol play/pause ketika tombol diklik
        playButton.addActionListener(e -> {

            // Kode di bawah ini digunakan untuk mengambil lagu dari JList
            Song selectedSong = songList.getSelectedValue();
            // Jika lagu yang dipilih tidak null, dan isplaying false
            if (selectedSong != null) {
                if (!isPlaying) { 
                    // cek apakah playThread null atau tidak aktif
                    if (playThread == null || !playThread.isAlive()) {
                        // membuat thread baru untuk memutar musik
                        playThread = new Thread(() -> {
                            try {
                                // mengambil songpath, audio filenya, dimasukkan ke dalam AudioInputStream, diClip, kemudian diopen audio itu
                                String songPath = selectedSong.getSongPath();
                                File audioFile = new File(songPath);
                                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                                audioClip = AudioSystem.getClip();
                                audioClip.open(audioStream);
        
                                // lock di sini memastikan hanya satu thread yang dapat mengakses dan memodifikasi sumber daya bersama secara bersamaan
                                synchronized (lock) {
                                    while (true) {
                                        // Jika dipause ketika lagu sudah mulai, pindah posisi audio ke posisi sebelum dipause, kemudian posisi pause direset
                                        if (pausedPosition > 0) {
                                            audioClip.setMicrosecondPosition(pausedPosition);
                                            pausedPosition = 0;
                                        }
        
                                        // Mulai mainkan audio, atur isplaying ke true, dan ubah play button jadi pause
                                        audioClip.start();
                                        isPlaying = true;
                                        SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/pause.png")));
        
                                        // Ambil durasi lagu, konversi ke milidetik, kemudian atur maksimum dari slider sesuai durasi lagu
                                        long durationInMicroseconds = audioClip.getMicrosecondLength();
                                        long durationInMillis = durationInMicroseconds / 1000;
                                        songSlider.setMaximum((int) durationInMillis);
        
                                        // Timer di sini digunakan untuk sinkronasi UI, berjalan tiap 200 milidetik
                                        timer = new Timer(200, e1 -> {
                                            // Jika audio sedang aktif, maka posisi slider diupdate sesuai berapa lama lagu berputar, durasi berjalan dan akhir juga diupdate
                                            if (audioClip.isActive()) {
                                                long currentPosition = audioClip.getMicrosecondPosition() / 1000;
                                                SwingUtilities.invokeLater(() -> {
                                                    songSlider.setValue((int) currentPosition);
                                                    startTime.setText(formatTime(currentPosition / 1000));
                                                    endTime.setText(formatTime(durationInMillis / 1000));
                                                });
                                            }
                                            // Kalo lagu dah berakhir, maka lagu diberhentiin, button next diklik, timer distop
                                            else if (audioClip.getMicrosecondPosition() >= audioClip.getMicrosecondLength()) {
                                                stopCurrentSong();
                                                nextButton.doClick();
                                                updateTimer.stop();
                                            }
                                        });
                                        // timer di sini dimulai untuk mengupdate UI
                                        timer.start();
        
                                        // Thread utama nunggu 100 ms untuk sinkron
                                        while (audioClip.isActive()) {
                                            lock.wait(100);
                                        }
        
                                        // timer distop, isplaying diset ke false, button diubah ke play
                                        timer.stop();
                                        isPlaying = false;
                                        SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/play.png")));
        
                                        // Thread pemutar lagu dihentikan sementara sampai disuruh lanjut lagi
                                        lock.wait();
                                    }
                                }
                            // Kode di bawah adalah solusi untuk error ketika audio gagal diputar, sehingga tidak menghentikan program secara keseluruhan, tapi ngeprint error trace
                            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        });
                        // Kode di bawah ini digunakan untuk memulai thread dan mengatur prioritasnya ke paling tinggi
                        playThread.start();
                        playThread.setPriority(Thread.MAX_PRIORITY);
                    }
                    else {
                        // Saat playThread sedang hidup, maka thread diaktifkan lagi
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                }
                // Kalo sedang ga play musik, maka stop audioclip, simpan posisi terakhir, ubah isplaying ke false, ubah icon jadi play, dan stop timer
                else {
                    audioClip.stop();
                    pausedPosition = audioClip.getMicrosecondPosition();
                    isPlaying = false;
                    SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/play.png")));
                    timer.stop();
                }
            } 
            // berjalan ketika play ditekan tapi belum milih lagu
            else {
                JOptionPane.showMessageDialog(frame, "Liat sebelah kiri, banyak lagunya", "Pilih salah satu lagu dari playlist!", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Listener untuk slider lagu, bisa geser geser utnuk pindah durasi lagu
        songSlider.addChangeListener(e -> {
            // Ketika slider sedang digeser geser
            if (songSlider.getValueIsAdjusting()) {
                // catat posisi barunya di mana
                long newPositionInMillis = songSlider.getValue();
                if (audioClip != null) {
                    // Ketika audioClip sedang ga null, ubah posisi putar audio ngikutin slider
                    audioClip.setMicrosecondPosition(newPositionInMillis * 1000);
                    // Kalo sedang mutar lagu, ubah icon jadi pause
                    if (isPlaying) {
                        SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/pause.png")));
                    }
                    // posisi pause diisi oleh posisi baru
                    pausedPosition = newPositionInMillis * 1000;
                }
            }
        });
        
        // Listener untuk nextButton
        nextButton.addActionListener(e -> {
            // Kalo timer ga null, stop dulu
            if (timer != null) {
                timer.stop();
            }
            // Reset slider ke 0
            songSlider.setValue(0);
            // Ambil indeks saat ini, ambil juga total lagu yang ada dalam JList
            int currentIndex = songList.getSelectedIndex();
            int totalSongs = songList.getModel().getSize();
            
            // Kalo posisinya bukan lagu paling terakhir, tambahkan indeks dengan 1, stop lagu yang sekarang, ubah fokus di JList ke lagu yang baru, kemudian play lagu baru itu
            if (currentIndex < totalSongs - 1) {
                int newIndex = currentIndex + 1;
                stopCurrentSong();
                songList.setSelectedIndex(newIndex);
                songList.ensureIndexIsVisible(newIndex);
                playSelectedSong();
            }
            // Kalo posisinya lagu paling terakhir, stop lagu yang sekarang, ubah fokus di JList ke lagu pertama, kemudian play lagu baru itu
            else {
                stopCurrentSong();
                songList.setSelectedIndex(0);
                songList.ensureIndexIsVisible(0);
                playSelectedSong();
            }
        });
        
        // Listener untuk prevButton
        prevButton.addActionListener(e -> {
            // Kalo timer ga null, stop dulu
            if (timer != null) {
                timer.stop();
            }
            // Reset slider ke 0
            songSlider.setValue(0);
            // Ambil indeks saat ini, ambil juga total lagu yang ada dalam JList
            int currentIndex = songList.getSelectedIndex();
            int totalSongs = songList.getModel().getSize();
            // Kalo posisinya bukan lagu paling pertama, kurangi indeks dengan 1, stop lagu yang sekarang, ubah fokus di JList ke lagu yang baru, kemudian play lagu baru itu
            if (currentIndex > 0) {
                int newIndex = currentIndex - 1;
                stopCurrentSong();
                songList.setSelectedIndex(newIndex);
                songList.ensureIndexIsVisible(newIndex);
                playSelectedSong();
            }
            // Kalo posisinya lagu paling pertama, stop lagu yang sekarang, ubah fokus di JList ke lagu terakhir, kemudian play lagu baru itu
            else {
                stopCurrentSong();
                songList.setSelectedIndex(totalSongs-1);
                songList.ensureIndexIsVisible(totalSongs-1);
                playSelectedSong();
            }
        });

        // Atur agar frame terlihat, mirip dengan frame.show()
        frame.setVisible(true);
    }

    // Method main untuk run program
    public static void main(String[] args) {
        // Menjalankan constructor SpotipaiGUI dalam EDT (Thread yagn digunakan oleh Swing untuk manipulasi komponen GUI)
        SwingUtilities.invokeLater(SpotipaiGUI::new);
    }

    // Method untuk konversi format detik menjadi menit:detik
    private String formatTime(long detik) {
        long menit = detik / 60;
        long sisa = detik % 60;
        return String.format("%02d:%02d", menit, sisa);
    }

    // Method untuk memutar suatu lagu
    private void playSelectedSong() {

        // Ambil lagu dari JList Songlist, jika dia tidak null, maka stop lagu sebelumnya, atur posisi pause ke 0
        Song selectedSong = songList.getSelectedValue();
        if (selectedSong != null) {
            stopCurrentSong();
            pausedPosition = 0;
    
            // Atur title, artist, dan albumart dari lagu yang baru
            songTitle.setText(selectedSong.getTitle());
            songArtist.setText(selectedSong.getArtist());
            albumArt.setIcon(new ImageIcon(selectedSong.getThumbnailPath()));
    
            // SwingWorker di sini berfungsi sebagai Thread, yang memutar lagu di background agar UI tetap responsif. Jadi sama saja seperti Thread
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        // Kode-kode di bawah ini digunakan untuk membaca file audio dari suatu path dari JList
                        String songPath = selectedSong.getSongPath();
                        File audioFile = new File(songPath);
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                        audioClip = AudioSystem.getClip();
                        audioClip.open(audioStream);
    
                        // Menghitung durasi lagu
                        long durationInMicroseconds = audioClip.getMicrosecondLength();
                        long durationInMillis = durationInMicroseconds / 1000;
    
                        // Mengatur slidernya maksimumnya sesuai durasi lagu, diberi value awal 0, mengatur durasi waktu yang akan diputar awalnya 0, kemudian durasi total lagunya mengikuti lagunya
                        SwingUtilities.invokeLater(() -> {
                            songSlider.setMaximum((int) durationInMillis);
                            songSlider.setValue(0);
                            startTime.setText(formatTime(0));
                            endTime.setText(formatTime(durationInMillis / 1000));
                        });
    
                        // Memutar lagu dan set isplaying ke true
                        audioClip.start();
                        isPlaying = true;
    
                        // Mengubah tombol play menjadi tombol pause
                        SwingUtilities.invokeLater(() -> playButton.setIcon(new ImageIcon("./res/images/icon/pause.png")));
    
                        // Timer yang digunakan untuk mengecek setiap 100 milidetik
                        updateTimer = new Timer(100, e -> {
                            // Jika audio aktif, maka catat posisi saat ini, ubah posisi slider dan durasi berjalannya lagu
                            if (audioClip.isActive()) {
                                long currentPosition = audioClip.getMicrosecondPosition() / 1000;
                                SwingUtilities.invokeLater(() -> {
                                    songSlider.setValue((int) currentPosition);
                                    startTime.setText(formatTime(currentPosition / 1000));
                                });
                            }
                            // Jika audio sudah selesai diputar, maka stop lagu, klik tombol next, dan stop timer
                            else if (audioClip.getMicrosecondPosition() >= audioClip.getMicrosecondLength()) {
                                stopCurrentSong();
                                nextButton.doClick();
                                updateTimer.stop();
                            }
                            // Jika audio mati, maka stop timer
                            else {
                                updateTimer.stop();
                            }
                        });
                        // timer diaktivasi lagi untuk memperbarui UI
                        updateTimer.start();
    
                    // catch untuk menangani error saat mencoba memutar audio
                    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                        e.printStackTrace();
                    }
                    // tidak ada data yang perlu direturn dari eksekusi di background
                    return null;
                }
            };
            // menjalankan tugas yang ada di dalam SwingWorker
            worker.execute();
        }
        // jika memutar lagu ketika belum ada lagu yang dipilih
        else {
            JOptionPane.showMessageDialog(frame, "Liat sebelah kiri, banyak lagunya", "Pilih salah satu lagu dari playlist!", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Method ini digunakan untuk menghentikan lagu yang sedang diputar
    private void stopCurrentSong() {
        // Jika audioclip tidak kosong dan sedang berjalan, maka stop dan close, ubah isplaying ke pause, reset posisi pause ke 0, timer distop, dan ubah slider ke posisi 0
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
        
    // Method di bawah ini digunakan untuk membuat button yang dipakai untuk kontrol (play, prev, dan next)
    private JButton createControlButton(String icon) {
        // Jadi button dari JButton dibuat, kemudian diatur iconnya, backgroundnya black, fontnya Arial, Bold, 20pt, ukurannya 50px, tidak transparan
        JButton btn = new JButton();
        btn.setIcon(new ImageIcon(icon));
        btn.setBackground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(50, 50));
        btn.setOpaque(true);

        // Kode di bawah ini adalah listener mouse untuk button-button yang dibuat dengan method ini, dalam kasus ini untuk hover mouse
        btn.addMouseListener(new MouseAdapter() {
            @Override
            // Jadi ketika mouse masuk ke zona button, ubah backgroundnya jadi RGB(50,50,50)
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(50, 50, 50));
            }

            @Override
            // Jadi ketika mouse keluar dari zona button, ubah backgroundnya jadi RGB(30,30,30)
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(30,30,30));
            }
        });
        // button yang sudah dibuat direturn
        return btn;
    }
}