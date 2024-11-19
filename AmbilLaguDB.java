import java.sql.*;
import java.util.*;

public class AmbilLaguDB {
    public static List<Song> getLagu() {
        List<Song> songs = new ArrayList<>();
        String query = "SELECT * FROM lagu";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbomodul4", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String artist = rs.getString("artist");
                String songPath = rs.getString("songPath");
                String thumbnailPath = rs.getString("thumbnailPath");

                songs.add(new Song(id, title, artist, songPath, thumbnailPath));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songs;
    }
}
