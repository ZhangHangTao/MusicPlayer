package cn.pojo;


public class PlayBean {

    private String musicName;

    private String mp3Url;

    private String artistName;

    private String album;

    private String localLrcPath;

    private String lrc;

    public PlayBean() {

    }

    public PlayBean(String musicName, String mp3Url, String artistName, String album, String localLrcPath) {
        this.musicName = musicName;
        this.mp3Url = mp3Url;
        this.artistName = artistName;
        this.album = album;
        this.localLrcPath = localLrcPath;
    }

    public PlayBean(String musicName, String mp3Url, String artistName, String album, String localLrcPath, String lrc) {
        this.musicName = musicName;
        this.mp3Url = mp3Url;
        this.artistName = artistName;
        this.album = album;
        this.localLrcPath = localLrcPath;
        this.lrc = lrc;
    }

    public PlayBean(String musicName) {
        this.musicName = musicName;
    }


    public String getMp3Url() {
        return mp3Url;
    }

    public void setMp3Url(String mp3Url) {
        this.mp3Url = mp3Url;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getLocalLrcPath() {
        return localLrcPath;
    }

    public void setLocalLrcPath(String localLrcPath) {
        this.localLrcPath = localLrcPath;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }


}
