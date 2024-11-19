public class Song{
    private int id;
    private String title;
    private String artist;
    private String songPath;
    private String thumbnailPath;

    public Song(int id, String title, String artist, String songPath, String thumbnailPath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.songPath = songPath;
        this.thumbnailPath = thumbnailPath;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getSongPath() {
        return songPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }
}