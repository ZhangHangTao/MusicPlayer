package cn.localioutils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import cn.pojo.PlayBean;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

//处理本地音乐
public class LocalMusicUtils {

	public final static String LOCAL_LRC_DIR = System.getProperty("user.dir") + "/LocalMusic/Lrc/";

	public final static String LOCAL_MUSIC_DIR = System.getProperty("user.dir") + "/LocalMusic/Music/";

	static {
		Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		Logger.getLogger("org.jaudiotagger.audio").setLevel(Level.OFF);
	}

//获取本地歌词
	public static String getLrc(String lrcPath) {
		String lrc = null;
		File lrcFile = new File(lrcPath);
		if (!lrcFile.exists()) {
			lrc = "[00:00.00]暂无歌词";
		} else {
			try {
				byte[] filecontent = Files.readAllBytes(Paths.get(lrcFile.toURI()));
				lrc = new String(filecontent, StandardCharsets.UTF_8);
			} catch (Exception e) {
				lrc = "[00:00.00]暂无歌词";
				return lrc;
			}
		}
		return lrc;
	}

	
	// 获取本地音乐的信息,包括本地音乐的头文件信息,专辑图片

	public static void getLocalMusicInf(List<PlayBean> list) {
		if (list.size() != 0) {
			list.clear();
		}
	
		File[] filelist = new File(LOCAL_MUSIC_DIR).listFiles();
		for (File file : filelist) {
			PlayBean playBean = new PlayBean(file.getName());
			// 解析文件
			AudioFile audioFile = null;
			try {
				audioFile = AudioFileIO.read(file);
			} catch (Exception e) {
				System.err.println(e);
				continue;
			}
			Tag tag = audioFile.getTag();
			String songName = tag.getFirst(FieldKey.TITLE);//歌名
			String artist = tag.getFirst(FieldKey.ARTIST);// 演唱者
			String album = tag.getFirst(FieldKey.ALBUM);// 专辑名称
			String fileName = null;
			try {
				fileName = playBean.getMusicName().substring(0, playBean.getMusicName().lastIndexOf('.'));
			} catch (Exception e) {
				System.err.println(e);
				fileName = "无名歌曲";
			}
			// 为PlayBean赋值
			if (artist != null && !artist.equals("")) {
				playBean.setArtistName(artist);
			}
			if (album != null && !album.equals("")) {
				playBean.setAlbum(album);
			}
			if (songName != null && !songName.equals("")) {
				playBean.setMusicName(songName);
			} else {
				playBean.setMusicName(fileName);
			}
			playBean.setMp3Url(file.toURI().toString());
			String lrcPath = LOCAL_LRC_DIR + fileName + ".lrc";
			playBean.setLocalLrcPath(lrcPath);//记录本地lrc文件所在位置
			list.add(playBean);
		}
	}

	// 获取封面
	public static WritableImage getLocalMusicArtwork(File file) {
		// 解析文件
		AudioFile audioFile = null;
		try {
			audioFile = AudioFileIO.read(file);
		} catch (Exception e) {
			// 无专辑照片
			return null;
		}
		Tag tag = audioFile.getTag();
		// 获取封面
		try {
			BufferedImage artwork = tag.getFirstArtwork().getImage();
			return SwingFXUtils.toFXImage(artwork, null);
		} catch (Exception e) {
			return null;
		}
	}

}
