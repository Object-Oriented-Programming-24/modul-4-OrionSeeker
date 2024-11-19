import java.sql.*;
import java.util.*;

// Class ini digunakan untuk mengambil detail-detail lagu dari database mysql
public class AmbilLaguDB {
    
    // Class getLagu ini static, tipe datanya List<Song>, digunakan untuk mereturn sebuah list berisi daftar detail lagu
    public static List<Song> getLagu() {
        
        // List yang bertipe data Song ini nantinya akan menampung seluruh detail lagu yang ada, yang diambil dari query select. Select dilakukan dengan mengambil seluruh data yang ada dari tabel lagu.
        List<Song> songs = new ArrayList<>();
        String query = "SELECT * FROM lagu";

        // Kode di bawah digunakan untuk melakukan koneksi dengan database mysql, yang ada di localhost dengan port 3306, username root, dan password kosong.
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbomodul4", "root", "");
            
            // Kode di bawah adalah pembuatan statement dan eksekusi query untuk mengambil data lagu
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Kode di bawah ini digunakan untuk mengambil seluruh baris query dari awal sampai tidak ada data lagi di dalam rs
            while (rs.next()) {
                
                // Data yang diambil adalah id bertipe int, serta title, artist, songpath, thumbnailpath bertipe string.
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String artist = rs.getString("artist");
                String songPath = rs.getString("songPath");
                String thumbnailPath = rs.getString("thumbnailPath");

                // Seluruh data yang diambil tadi dimasukkan ke dalam objek song yang baru dibuat kemudian objek tersebut dimasukkan ke dalam list songs
                songs.add(new Song(id, title, artist, songPath, thumbnailPath));
            }

        // Kode di bawah adalah catch yang akan berjalan ketika gagal untuk konek ke DB atau melakukan query pengambilan data, kode ini akan mencetak trace errornya.
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // songs direturn untuk digunakan di class utama yaitu SpotipaiGUI yang memanggil fungsi getLagu()
        return songs;
    }
}
