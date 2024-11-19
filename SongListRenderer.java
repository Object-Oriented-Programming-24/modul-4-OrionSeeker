import javax.swing.*;
import java.awt.*;

public class SongListRenderer extends JPanel implements ListCellRenderer<Song> {
    private JLabel thumbnailLabel;
    private JLabel titleLabel;
    private JLabel artistLabel;
    private JPanel textPanel;

    public SongListRenderer() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setBackground(new Color(45, 45, 45));

        thumbnailLabel = new JLabel();
        thumbnailLabel.setPreferredSize(new Dimension(60, 60));
        add(thumbnailLabel);

        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(45, 45, 45));

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        artistLabel = new JLabel();
        artistLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        artistLabel.setForeground(new Color(200, 200, 200));

        textPanel.add(titleLabel);
        textPanel.add(artistLabel);
        add(textPanel);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Song> list, Song song, int index, boolean isSelected, boolean cellHasFocus){
        titleLabel.setText(song.getTitle());
        artistLabel.setText(song.getArtist());

        ImageIcon thumbnail = new ImageIcon(song.getThumbnailPath());
        Image image = thumbnail.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        thumbnailLabel.setIcon(new ImageIcon(image));

        if (isSelected) {
            setBackground(new Color(70, 70, 70));
            textPanel.setBackground(new Color(70, 70, 70));
            titleLabel.setForeground(Color.WHITE);
            artistLabel.setForeground(new Color(200, 200, 200));
        }
        else {
            setBackground(new Color(45, 45, 45));
            textPanel.setBackground(new Color(45, 45, 45));
            titleLabel.setForeground(Color.WHITE);
            artistLabel.setForeground(new Color(200, 200, 200));
        }

        return this;
    }
}
