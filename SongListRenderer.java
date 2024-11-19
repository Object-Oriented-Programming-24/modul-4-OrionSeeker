import javax.swing.*;
import java.awt.*;

// Class ini digunakan untuk mengatur bagaimana daftar-daftar lagi di panel kiri (JList dalam JScrollPane disusun)
public class SongListRenderer extends JPanel implements ListCellRenderer<Song> {

    // Terdapat 3 buah label dan 1 buah panel dalam panel ini, yaitu label untuk thumbnail lagu, title, artist, dan panel textPanel untuk menyimpan titleLabel dan artistLabel tadi
    private JLabel thumbnailLabel;
    private JLabel titleLabel;
    private JLabel artistLabel;
    private JPanel textPanel;

    // Method ini adalah konstruktor dari class SongListRenderer
    public SongListRenderer() {
        // Layout yang digunakan panelnya adalah flowlayout dengan arah dari kiri, dengan padding 10px vertikal dan 10px horizontal. Diberikan warna background RGB(45,45,45)
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setBackground(new Color(45, 45, 45));

        // Kode di bawah adalah label yang digunakan untuk menyimpan thumbnail, ukurannya diatur 60x60px, kemudian dimasukkan ke dalam panel
        thumbnailLabel = new JLabel();
        thumbnailLabel.setPreferredSize(new Dimension(60, 60));
        add(thumbnailLabel);

        // Kode di bawah ini adalah panel yang akan menampung titleLabel dan artistLabel. Layoutnya boxlayout secara vertikal, backgroundnya RGB(45,45,45).
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(45, 45, 45));

        // Kode di bawah ini adalah label yang digunakan untuk menyimpan judul lagu, fontnya arial 14pt bold, warnanya putih.
        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        // Kode di bawah ini adalah label yang digunakan untuk menyimpan artis lagu, fontnya arial 12pt, warnanya RGB(200,200,200).
        artistLabel = new JLabel();
        artistLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        artistLabel.setForeground(new Color(200, 200, 200));

        // Kode di bawah ini memasukkan titleLabel dan artistLabel ke dalam textPanel, kemudian textPanel dimasukkan ke dalam panel utama
        textPanel.add(titleLabel);
        textPanel.add(artistLabel);
        add(textPanel);

        // Kode di bawah ini digunakan untuk memberikan padding masing-masing 5pt di segala arah untuk panel utama
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    // Kode di bawah ini digunakan untuk mengisi masing-masing cell lagu yang ada dalam JList
    @Override
    public Component getListCellRendererComponent(JList<? extends Song> list, Song song, int index, boolean isSelected, boolean cellHasFocus){

        // Kode di bawah ini digunakan untuk menset title dan artist berdasarkan objek song yang ada di dalam JList
        titleLabel.setText(song.getTitle());
        artistLabel.setText(song.getArtist());

        // Kode di bawah ini digunakan untuk mengambil ImageIcon untuk thumbnail. Kemudian diresize menjadi 60x60px dan diset untuk thumbnailLabel
        ImageIcon thumbnail = new ImageIcon(song.getThumbnailPath());
        Image image = thumbnail.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        thumbnailLabel.setIcon(new ImageIcon(image));

        // Kode di bawah ini digunakan untuk mengubah warna background dari cell lagu, textPanel, dan warna teks ketika cell diklik
        if (isSelected) {
            setBackground(new Color(70, 70, 70));
            textPanel.setBackground(new Color(70, 70, 70));
            titleLabel.setForeground(Color.WHITE);
            artistLabel.setForeground(new Color(200, 200, 200));
        }
        else {
            // Kode di bawah ini digunakan untuk mengubah warna background dari cell lagu, textPanel, dan warna teks ketika cell sedang tidak dklik
            setBackground(new Color(45, 45, 45));
            textPanel.setBackground(new Color(45, 45, 45));
            titleLabel.setForeground(Color.WHITE);
            artistLabel.setForeground(new Color(200, 200, 200));
        }

        // Kode di bawah ini mereturn panel utama di class ini
        return this;
    }
}
