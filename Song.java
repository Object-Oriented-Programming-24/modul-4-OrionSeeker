// Class ini adalah class yang digunakan untuk menyimpan detail dari lagu yang diambil dari database
public class Song{
    // Seluruh atribut di sini modifiernya private, sehingga perlu setter,getter jika mau diakses dari class lain, atau kalo cuma perlu diset, cukup lewat constructor saja
    private int id;
    private String title;
    private String artist;
    private String songPath;
    private String thumbnailPath;

    // Method di bawah ini adalah konstruktor, yang digunakan untuk mengatur value dari masing-masing atribut ketika membuat objek Song
    public Song(int id, String title, String artist, String songPath, String thumbnailPath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.songPath = songPath;
        this.thumbnailPath = thumbnailPath;
    }

    // Method getId ini digunakan untuk mengambil nilai dari atribut id jika diakses dari class lain
    public int getId() {
        return id;
    }

    // Method getTitle ini digunakan untuk mengambil nilai dari atribut title jika diakses dari class lain
    public String getTitle() {
        return title;
    }

    // Method getArtist ini digunakan untuk mengambil nilai dari atribut artist jika diakses dari class lain
    public String getArtist() {
        return artist;
    }

    // Method getSongPath ini digunakan untuk mengambil nilai dari atribut songPath jika diakses dari class lain
    public String getSongPath() {
        return songPath;
    }

    // Method getThumbnailPath ini digunakan untuk mengambil nilai dari atribut thumbnailpath jika diakses dari class lain
    public String getThumbnailPath() {
        return thumbnailPath;
    }
}