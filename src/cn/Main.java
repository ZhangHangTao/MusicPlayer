package cn;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cn.localioutils.LocalMusicUtils;
import cn.pojo.PlayBean;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class Main extends Application {

	private int listflag = 0;

	private int heartFlag = 1;

	private int soundImageFlag = 1;

	private int currentSecond;

	// 当前播放的时间的前一秒
	private int prevSecond;

	private double millis;

	// Vbox歌词容器
	private VBox lrcVBox;

	// ArrayList存储歌词时间
	private ArrayList<BigDecimal> lrcList;

	private Label labPlayTime;

	private SimpleDateFormat simpleDateFormat;

	private StackPane sound = new StackPane();

	private int isplay = 0; // 0表示当前没有音乐播放，1表示当前有音乐播放

	private Slider slider1;

	private Slider slider2;

	private Date date;

	private double min = 0;

	private double max = 0;

	private double currentVolume = 300;

	Random random = new Random();

	private int upper = 0;

	private int lower = 0;

	private double xOffSet = 0;

	private double yOffSet = 0;

	private int currentLrcIndex;

	private PlayBean currentPlayBean;

	boolean mouse = false;

	private ImageView songCoverImageView;

	private TranslateTransition tt;

	private Stage primaryStage;

	private MediaPlayer mediaPlayer;

	private RotateTransition rt;

	private ChangeListener<Duration> changeListener;// 播放进度监听器

	private ImageView iv_blur = null;

	private ImageView stop;

	private ImageView headview;

	private int musicPlayMode = 1;

	private float f;

	// 频谱矩形条数
	private final int waveNum = 96;
	Rectangle[] wave = new Rectangle[waveNum];
	HBox wavebox = new HBox(5);

	private ListView<String> listView;

	private List<PlayBean> playList;

	private int currentIndex = 0;

	ImageView volumeImage;

	ImageView logolight; // 光晕

	private Label singerLabel;// 用来显示歌手名的label

	private Label songNameLabel;// 歌曲名label

	private Label albumLabel; // 专辑名label

	private Image panDefaultImage; // 默认唱片背景

	private final Font font = Font.font("楷体", 14);
	private final Font boldFont = Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 18);

	Font font1;
	Font font2;
	Font font3;
	Font font4;

	private Timeline t1;

	public static void main(String[] args) {
		launch(args);

	}

	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;

		primaryStage.getIcons().add(new Image("cn/res/icon/Icon.png"));

		playList = new ArrayList<>();
		listView = new ListView<String>();
		lrcList = new ArrayList<>();
		simpleDateFormat = new SimpleDateFormat("mm:ss");
		date = new Date();
		changeListener = initChangeListener();
		readLocalMusic();

		// 刷新频率
		t1 = new Timeline(new KeyFrame(Duration.millis(30), event -> {
			lrcVBox.setLayoutY(lrcVBox.getLayoutY() - 9);
		}));
		t1.setCycleCount(5);// 执行1次

		listView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				int index = listView.getSelectionModel().getSelectedIndex();
				if (index == -1) {
					return;
				}
				PlayBean playBean = playList.get(index);
//				System.out.println(playBean);
				currentPlayBean = playBean;
				this.currentIndex = index;
				play();
				isplay = 1;
				tt.setFromX(0);
				tt.setToX(-250);
				tt.play();
				listflag = 1 - listflag;
			}
		});

		initRunnable();
		// 背景图=基准图+高斯模糊+圆角
		ImageView iv = new ImageView("cn/res/img/blur1.png");
		ImageView InitImage = new ImageView("cn/res/img/nest.jpg");
		InitImage.setFitHeight(640);
		InitImage.setFitWidth(1040);
		iv.setFitHeight(640);
		iv.setFitWidth(1040);
		iv.setSmooth(true);
		Rectangle rec = new Rectangle(iv.prefWidth(-1), iv.prefHeight(-1));
		Rectangle rec1 = new Rectangle(InitImage.prefWidth(-1), InitImage.prefHeight(-1));
		rec1.setArcHeight(40);
		rec1.setArcWidth(40);
		rec.setArcHeight(40);
		rec.setArcWidth(40);
		InitImage.setClip(rec1);
		iv.setClip(rec);
		iv.setEffect(new GaussianBlur(10)); // 待改
		iv.setOpacity(0);

		AnchorPane pane = new AnchorPane();
		pane.getChildren().addAll(iv, InitImage);

		HBox topview = getTopView();
		HBox middleview = getMiddleView();
		HBox bottomview = getBottomView();

		slider1 = new Slider(0, 1, 0.5);
		Label l = new Label((int) (slider1.getValue() * 100) + "%");
		l.setTranslateY(42);
		slider1.setOrientation(Orientation.VERTICAL);
		slider1.setMaxHeight(100);
		slider1.setTranslateY(-17);
		slider1.setTranslateX(-3);
		slider1.setOpacity(0.9);

		ImageView soundImage = new ImageView("cn/res/icon/sound.png");

		soundImage.setFitWidth(40);
		soundImage.setPreserveRatio(true);
		sound.getChildren().addAll(soundImage, slider1, l);
		sound.setOpacity(1);
		sound.setTranslateX(303);
		sound.setTranslateY(-520);
		sound.setOpacity(0);
		slider1.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				l.setText((int) (newValue.doubleValue() * 100) + "%");
				currentVolume = newValue.doubleValue() * 600;
				if (newValue.doubleValue() >= 0.7) {
					volumeImage.setImage(new Image("cn/res/icon/volume.png"));
				} else if (newValue.doubleValue() > 0.0) {
					volumeImage.setImage(new Image("cn/res/icon/volume1.png"));
				} else {
					volumeImage.setImage(new Image("cn/res/icon/volume3.png"));
				}
			}
		});

		slider2 = new Slider(0, 1, 0);
		slider2.setPrefWidth(950);
		slider2.setTranslateY(-615); // 待调整到适当位置
		slider2.setTranslateX(37);
		slider2.setShowTickLabels(true);
		slider2.setMajorTickUnit(60);
		slider2.setOpacity(0.6);
		slider2.setStyle("-fx-text-base-color: black;");
		slider2.setDisable(true); // 未选歌前不允许滑动进度条

		slider2.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(Double object) {
				if (object.intValue() == 0) {
					return "0:00";
				} else if (object.intValue() % 60 == 0) {
					return object.intValue() / 60 + ":00";
				}

				return null;
			}

			@Override
			public Double fromString(String string) {

				return null;
			}
		});
		slider2.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouse = true;
			}
		});
		slider2.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (currentPlayBean != null && mediaPlayer != null) {
					Duration duration = Duration.seconds(slider2.getValue());
					mediaPlayer.seek(duration);
					// 同时设置Label
					date.setTime((long) mediaPlayer.getCurrentTime().toMillis());
					labPlayTime.setText(simpleDateFormat.format(date));
					// 设置前一秒
					prevSecond = (int) duration.toSeconds() - 1;
				}
				mouse = false; // 鼠标标记，实现拖拽
			}
		});

		wavebox.setOpacity(0);
		wavebox.setTranslateX(14);
		wavebox.setTranslateY(-1196);

		// 频谱初始化
		for (int i = 1; i < waveNum; i++) {

			wave[i] = new Rectangle();

			wave[i].setWidth(4);
			wave[i].setHeight(0);
			wave[i].setArcHeight(8);

			wave[i].setArcWidth(8);
			wave[i].setFill(Color.BLANCHEDALMOND);
			wavebox.getChildren().addAll(wave[i]);
		}
		wavebox.setMouseTransparent(true);
		wavebox.setOpacity(0);
		wavebox.setAlignment(Pos.TOP_LEFT);

		// 光源效果
		wavebox.setEffect(new Lighting());
		wavebox.getEffect();

		VBox vboxpane = new VBox();
		vboxpane.setAlignment(Pos.CENTER);
		middleview.setAlignment(Pos.CENTER);
		bottomview.setAlignment(Pos.CENTER);

		// middleview.setPrefHeight(primaryStage.getHeight() - 130);
		vboxpane.getChildren().addAll(middleview, sound, bottomview, slider2, topview, wavebox);

		vboxpane.setOpacity(0);

		Node node = getView(primaryStage);

		AnchorPane root = new AnchorPane();
		root.getChildren().addAll(pane, vboxpane, node);

		root.setBackground(new Background(new BackgroundFill(Color.valueOf("#696969"), new CornerRadii(20), null)));

		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/cn/res/css/main.css").toExternalForm());
		scene.setFill(Color.TRANSPARENT);
		primaryStage.setScene(scene);
		primaryStage.setWidth(1040);
		primaryStage.setHeight(640);
		primaryStage.setTitle("Music Player");
		primaryStage.setResizable(false);
		primaryStage.centerOnScreen();
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.show();

		Timeline timeline = new Timeline();

		// 透明度动画
		KeyValue kv1 = new KeyValue(InitImage.opacityProperty(), 0);
		KeyFrame kf1 = new KeyFrame(Duration.seconds(0), "kf1", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

			}
		}, kv1);

		TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5), node);
		tt.setInterpolator(Interpolator.EASE_OUT);
		this.tt = tt;

		KeyValue kv2 = new KeyValue(InitImage.opacityProperty(), 1);
		// 2.5
		KeyFrame kf2 = new KeyFrame(Duration.seconds(2.5), "kf2", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

			}
		}, kv2);

		// 场景切换,列表拉出

		KeyFrame kf3 = new KeyFrame(Duration.seconds(3), "kf3", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				tt.setFromX(-200);
				tt.setToX(0);
				tt.play();

				RotateTransition rt1 = new RotateTransition(Duration.seconds(0.5), headview);
				rt1.setFromAngle(0);
				rt1.setToAngle(360);
				rt1.setCycleCount(2);
				rt1.play();
			}
		});

		KeyFrame kf4 = new KeyFrame(Duration.seconds(3), "kf4", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				InitImage.setOpacity(0);
				iv.setOpacity(1);
				vboxpane.setOpacity(1);

			}
		});

		timeline.getKeyFrames().addAll(kf1, kf2, kf3, kf4);
		timeline.play();

		node.translateXProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				int w = 200 - (-newValue.intValue());
				int h = (int) root.getHeight() - 125;
				if (w > 0) {
					WritableImage wi = new WritableImage(w, h);
					pane.snapshot(new SnapshotParameters(), wi);
					iv_blur.setImage(wi);
				}
			}
		});
//        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
//
//    		@Override
//    		public void handle(MouseEvent event) {
//    			xOffSet=event.getScreenX();
//    			yOffSet=event.getScreenY();
//    		}
//    	});
//    	
//    	scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
//    		@Override
//    		public void handle(MouseEvent event) {
//    			primaryStage.setX(event.getScreenX()-xOffSet);
//    			primaryStage.setY(event.getScreenY()-xOffSet);
//    			
//    		}
//    	});

		// 局部(上方)拖拽
		topview.setOnMousePressed(event -> {
			xOffSet = event.getSceneX();
			yOffSet = event.getSceneY();
			event.consume();
		});
		topview.setOnMouseDragged(event -> {
			primaryStage.setX(event.getScreenX() - xOffSet);
			primaryStage.setY(event.getScreenY() - yOffSet);
			event.consume();
		});
	}

	// 扫描读取本地音乐文件夹内的所有音乐信息,并把音乐信息保存到playList列表里
	private void readLocalMusic() {

		LocalMusicUtils.getLocalMusicInf(playList);
		ObservableList<String> items = listView.getItems();

		items.clear();
//		String record_string = "";
		for (PlayBean playbean : playList) {
			items.add(playbean.getMusicName());
		}
		items.add("");
		items.remove(items.size() - 1);

	}

	private void play() {
		stop.setImage(new Image("cn/res/icon/stop1.png")); // 播放标志换成暂停标志
		if (currentPlayBean == null) {
			return;
		}
		// MediaPlayer对象复用
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.currentTimeProperty().removeListener(changeListener);
			mediaPlayer.setOnEndOfMedia(null);
			mediaPlayer.dispose(); // 释放资源
			mediaPlayer = null;
		}

		labPlayTime.setText("00:00");
		prevSecond = 0;

		String mp3Url = currentPlayBean.getMp3Url();

		// 碟片旋转停止 滑块启用
		rt.stop();
		slider2.setDisable(false);

		mediaPlayer = new MediaPlayer(new Media(mp3Url));
		rt.play();

		songNameLabel.setText(currentPlayBean.getMusicName());
		singerLabel.setText("歌手:" + currentPlayBean.getArtistName());
		albumLabel.setText("专辑:" + currentPlayBean.getAlbum());
		new Thread(() -> mediaPlayer.play()).start();
		loadLrc();
		mediaPlayer.currentTimeProperty().addListener(changeListener);

		File file = null;
		try {
			file = new File(new URI(mp3Url));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		WritableImage writableImage = LocalMusicUtils.getLocalMusicArtwork(file);
		songCoverImageView.setImage(choose(writableImage, panDefaultImage));

		// 音乐加载完成时开始新线程
		mediaPlayer.setOnReady(new Runnable() {
			@Override
			public void run() {

				mediaPlayer.volumeProperty().bind(slider1.valueProperty());

				slider2.setValue(0);
				slider2.setMin(0);

				slider2.setMax(mediaPlayer.getTotalDuration().toSeconds());

				// 进度条拖动
				mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
					@Override
					public void changed(ObservableValue<? extends Duration> observable, Duration oldvalue,
							Duration newValue) {

						if (mouse == false) {
							slider2.setValue(newValue.toSeconds());

						}

					}
				});

			}
		});

//		slider2.setMax(100);
//		slider2.setMajorTickUnit(1);
//		slider2.setValue(0);

		mediaPlayer.setOnEndOfMedia(() -> {
			rt.stop();

//			tt.setFromX(-250);
//			tt.setToX(0);
//			tt.play();
			switch (musicPlayMode) {
			case 1:
				NextMusic();
				break;
			case 2:
				RandomMusic();
				break;

			case 3:
				LoopMusic();
				break;

			default:
				break;
			}

		});

		mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener() {
			@Override
			public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
//				System.out.println(2.4+magnitudes[1]/20);

				// 动态光晕
				logolight.setOpacity(2.4 + magnitudes[1] / 20);
				for (int i = 1; i < waveNum; i++) {
					f = Math.abs(magnitudes[i]);
					wave[i].setHeight(currentVolume - (currentVolume / (60 / f)));

				}
			}
		});

	}

	private Runnable initRunnable() {
		currentPlayBean = playList.get(currentIndex);
		return () -> {
			currentIndex++;
			if (currentIndex >= listView.getItems().size()) {
				currentIndex = 0;
			}
		};
	}

	public void NextMusic() {
		if (playList.size() == 0) {
			return;
		}
		currentIndex++;
		if (currentIndex >= listView.getItems().size()) {
			currentIndex = 0;
		}
		currentPlayBean = playList.get(currentIndex);
		listView.getSelectionModel().clearAndSelect(currentIndex);
		play();
		isplay = 1;
	}

	public void RandomMusic() {
		if (playList.size() == 0) {
			return;
		}
		upper = listView.getItems().size();
		lower = 0;

		int NewIndex = currentIndex;

		while (NewIndex == currentIndex) {
			NewIndex = random.nextInt(upper) % (upper - lower + 1) + lower;
		}
		currentIndex = NewIndex;
		currentPlayBean = playList.get(currentIndex);
		listView.getSelectionModel().clearAndSelect(currentIndex);
		play();
		isplay = 1;
	}

	public void LoopMusic() {
		if (playList.size() == 0) {
			return;
		}
		currentPlayBean = playList.get(currentIndex);
		listView.getSelectionModel().clearAndSelect(currentIndex);
		play();
		isplay = 1;
	}

	// 抽屉音乐列表
	public Node getView(Stage stage) {

		StackPane sp = new StackPane();
		DropShadow ds = new DropShadow();

		ds.setRadius(5);
		ds.setColor(Color.valueOf("#A3A3A355"));
		ds.setOffsetX(1);
		sp.setEffect(ds);
		AnchorPane ap = new AnchorPane();
		iv_blur = new ImageView();

//		iv_blur.setEffect(new GaussianBlur(10));
		iv_blur.setTranslateY(-50);

		// 悬停放大效果
		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
//			int position = 0;

			@Override
			public ListCell<String> call(ListView<String> param) {
				Label label = new Label();
				label.setPrefHeight(15);
				label.setFont(Font.font("Timer New Roman", null, null, 13));
//				label.setFont(font1);
				ListCell<String> cell = new ListCell<String>() {
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty == false && item != null) {
							label.setText(item);
							this.setGraphic(label);
						}
					}

				};
				cell.hoverProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						if (newValue == true && label.getText().equals("") != true) {
//							position = param.getItems().indexOf(label.getText());
							label.setPrefHeight(18);
							label.setFont(Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 16));
//							label.setFont(font2);

						} else {
							label.setPrefHeight(15);
							label.setFont(Font.font("Timer New Roman", null, null, 13));
//							label.setFont(font1);
						}
					}

				});
				return cell;
			}
		});

		listView.setPrefHeight(500);
		listView.setPrefWidth(200);
		ap.getChildren().add(listView);
//		listView.setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0, 0), new CornerRadii(15), null)));
		listView.setOpacity(0.4);

		ImageView plusbutton = new ImageView("cn/res/icon/add.png");
		plusbutton.setPreserveRatio(true);
		plusbutton.setPickOnBounds(true);
		plusbutton.setFitWidth(28);
		plusbutton.setTranslateX(57);
		plusbutton.setTranslateY(167);

		ImageView refreshbutton = new ImageView("cn/res/icon/refresh.png");
		refreshbutton.setPreserveRatio(true);
		refreshbutton.setPickOnBounds(true);
		refreshbutton.setFitWidth(22);
		refreshbutton.setTranslateX(85);
		refreshbutton.setTranslateY(167);

		headview = new ImageView("cn/res/icon/jiqimao.png");
		headview.setPreserveRatio(true);
		headview.setPickOnBounds(true);
		headview.setFitWidth(70);
		headview.setTranslateX(-70);
		headview.setTranslateY(220);

		ImageView information = new ImageView("cn/res/icon/information.png");
		information.setPreserveRatio(true);
		information.setPickOnBounds(true);
		information.setFitWidth(200);
		information.setTranslateX(0);
		information.setTranslateY(216);

		RotateTransition rt1 = new RotateTransition(Duration.seconds(0.4), headview);
		headview.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue() == true) {
					headview.setImage(new Image("cn/res/icon/jiqimao1.png"));

					rt1.setFromAngle(0);
					rt1.setToAngle(360);
					rt1.play();
				} else {
					headview.setImage(new Image("cn/res/icon/jiqimao.png"));

					rt1.setFromAngle(0);
					rt1.setToAngle(-360);
					rt1.play();
				}
			}

		});

		plusbutton.setOnMousePressed(event -> {
			OpenDirectory();
		});

		refreshbutton.setOnMousePressed(event -> {
			readLocalMusic();
		});
		sp.getChildren().addAll(ap, information, plusbutton, refreshbutton, headview);
		sp.setTranslateX(-200);
//		sp.setTranslateY(200);
//		sp.setOpacity();

		// 渐变背景
		ImagePattern imagePattern = new ImagePattern(new Image("cn/res/img/bg2.png"), 1, 1, 1, 1, true);
		sp.setBackground(new Background(new BackgroundFill(imagePattern, null, Insets.EMPTY)));

//		
//		sp.setOnDragDetected(new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent event)
//			{
//				
//			}
//		});

//		listView.setBackground(new Background(new BackgroundFill(Color.valueOf("#696969"), new CornerRadii(25), null)));待改成好看一点的
		sp.setOnDragEntered(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				// 提供周围放光的视觉效果
				sp.setBorder(new Border(new BorderStroke(Paint.valueOf("#FFF0F5"), BorderStrokeStyle.SOLID,
						new CornerRadii(0), new BorderWidths(1))));
				event.consume();
			}
		});

		sp.setOnDragExited(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				sp.setBorder(new Border(new BorderStroke(Paint.valueOf("#FFF0F500"), BorderStrokeStyle.SOLID,
						new CornerRadii(0), new BorderWidths(1))));
				readLocalMusic();
				event.consume();

			}
		});

		sp.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				event.acceptTransferModes(TransferMode.COPY);
				event.consume();
			}
		});

		sp.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				sp.setBorder(new Border(new BorderStroke(Paint.valueOf("#FFF0F500"), BorderStrokeStyle.SOLID,
						new CornerRadii(0), new BorderWidths(1))));

				event.acceptTransferModes(TransferMode.COPY);

				// 拖拽板
				Dragboard db = event.getDragboard();
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				if (db.hasFiles()) {
					List<File> rawFiles = db.getFiles();

					File srcFile = new File(rawFiles.get(0).toString());

					try {

						String srcFileName = srcFile.getName();
						File file2;

						// 判断拖进来是是lrc还是mp3
						if (srcFileName.indexOf("lrc") == -1) {

							file2 = new File(System.getProperty("user.dir") + "/LocalMusic/Music", srcFileName);
						} else {
							file2 = new File(System.getProperty("user.dir") + "/LocalMusic/Lrc", srcFileName);
						}

						// 创建相应节点流
						FileInputStream fis = new FileInputStream(srcFile);
						FileOutputStream fos = new FileOutputStream(file2);

						// 将创建的节点流的对象作为形参传递给缓冲流的构造器中
						bis = new BufferedInputStream(fis);
						bos = new BufferedOutputStream(fos);

						byte[] b = new byte[1024];
						int len;
						while ((len = bis.read(b)) != -1) {
							bos.write(b, 0, len);
							bos.flush();
						}
					} catch (IOException e) {

						e.printStackTrace();
					} finally {

						if (bos != null) {
							try {
								bos.close();
							} catch (IOException e) {

								e.printStackTrace();
							}
						}
						if (bis != null) {
							try {
								bis.close();
							} catch (IOException e) {

								e.printStackTrace();
							}

						}
					}

				}
				event.consume();
			}

		});

		return sp;

	}

	// 最小化和关闭按钮
	public HBox getTopView() {

		ImageView minbutton = new ImageView("cn/res/icon/min.png");
		minbutton.setPickOnBounds(true);
		minbutton.setPreserveRatio(true);
		minbutton.setTranslateX(0);
		minbutton.setTranslateY(52);
		minbutton.setFitWidth(17);

		ImageView closebutton = new ImageView("cn/res/icon/close.png");
		closebutton.setPickOnBounds(true);
		closebutton.setPreserveRatio(true);
		closebutton.setTranslateX(-5);
		closebutton.setTranslateY(50);
		closebutton.setFitWidth(22);

		ImageView topline = new ImageView("cn/res/icon/topline.png");
		topline.setPickOnBounds(true);
		topline.setFitWidth(850);
		topline.setFitHeight(25);
		topline.setOpacity(0);

		topline.setTranslateX(-1050);
		topline.setTranslateY(-10);
		HBox topview = new HBox(20);

		ImageView waveImage = new ImageView("cn/res/icon/wave1.png");
		waveImage.setPreserveRatio(true);
		waveImage.setPickOnBounds(true);
		waveImage.setFitWidth(20);
		waveImage.setTranslateX(-115);
		waveImage.setTranslateY(50);

		topview.getChildren().addAll(minbutton, closebutton, waveImage, topline);
		topview.setOpacity(0.6);
		topview.setTranslateX(974);
		topview.setTranslateY(-1215);

		waveImage.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (wavebox.getOpacity() == 0) {
					wavebox.setOpacity(0.8);
				} else {
					wavebox.setOpacity(0);
				}
			}
		});

		waveImage.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

				if (newValue.booleanValue() == true) {
					waveImage.setImage(new Image("cn/res/icon/wave2.png"));
				} else {
					waveImage.setImage(new Image("cn/res/icon/wave1.png"));

				}

			}

		});

		closebutton.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue() == true) {
					closebutton.setImage(new Image("cn/res/icon/close1.png"));
				} else {
					closebutton.setImage(new Image("cn/res/icon/close.png"));
				}
			}

		});

		closebutton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				primaryStage.close();
			}
		});

		minbutton.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue() == true) {
					minbutton.setImage(new Image("cn/res/icon/min1.png"));
				} else {
					minbutton.setImage(new Image("cn/res/icon/min.png"));
				}
			}

		});

		minbutton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				primaryStage.setIconified(true);

			}
		});

		return topview;
	}

	// 中间部分的歌词信息,专辑照片
	public HBox getMiddleView() {

		// 音乐歌名 作者 专辑 字体加载
		try (FileInputStream in = new FileInputStream(
				new File(System.getProperty("user.dir") + "/src/cn/res/font/1.ttf"))) {
			font1 = Font.loadFont(in, 17);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (FileInputStream in = new FileInputStream(
				new File(System.getProperty("user.dir") + "/src/cn/res/font/1.ttf"))) {
			font2 = Font.loadFont(in, 22);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (FileInputStream in = new FileInputStream(
				new File(System.getProperty("user.dir") + "/src/cn/res/font/1.ttf"))) {
			font3 = Font.loadFont(in, 39);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (FileInputStream in = new FileInputStream(
				new File(System.getProperty("user.dir") + "/src/cn/res/font/1.ttf"))) {
			font4 = Font.loadFont(in, 17);
		} catch (Exception e) {
			e.printStackTrace();
		}

		AnchorPane anchorPane = new AnchorPane();
		labPlayTime = new Label("00:00");
		labPlayTime.setTranslateX(23);
		labPlayTime.setTranslateY(232);
		labPlayTime.setTextFill(Color.WHITE);
		labPlayTime.setFont(new Font(40));
		int width = 300;
		StackPane sp = new StackPane();
		sp.setAlignment(Pos.CENTER);
		panDefaultImage = new Image("cn/res/img/art.png");
		songCoverImageView = new ImageView(panDefaultImage);
		songCoverImageView.setFitWidth(width);
		songCoverImageView.setPreserveRatio(true);
		rt = new RotateTransition(Duration.seconds(50), songCoverImageView);
		rt.setFromAngle(0);
		rt.setToAngle(360);
		// 无限循环
		rt.setCycleCount(Timeline.INDEFINITE);
		// 每次旋转后是否改变旋转方向
		rt.setAutoReverse(false);

		logolight = new ImageView("cn/res/img/Circle.png");

		sp.getChildren().addAll(songCoverImageView, logolight);
		logolight.setPreserveRatio(true);
		logolight.setFitHeight(900);
		logolight.setTranslateX(-200); // 待向左移动
		logolight.setTranslateY(-170);
		logolight.setOpacity(0);

		songCoverImageView.setTranslateX(-202);
		songCoverImageView.setTranslateY(-240);

		Circle c1 = new Circle();
		c1.setCenterX(songCoverImageView.getX() + width / 2);
		c1.setCenterY(songCoverImageView.getY() + width / 2);
		c1.setRadius(width / 2);
		songCoverImageView.setClip(c1);
		sp.setLayoutX(20.0);
		sp.setLayoutY(50.0);

		lrcVBox = new VBox(15);
		lrcVBox.setPadding(new Insets(20, 20, 20, 20));
		lrcVBox.setLayoutX(-20);
		lrcVBox.setLayoutY(100);
		lrcVBox.setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0, 0), new CornerRadii(15), null)));
		lrcVBox.setOpacity(0.9);
		AnchorPane lrcPane = new AnchorPane();

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setPadding(new Insets(0, 0, 0, 0));
		scrollPane.setContent(lrcPane);
		// scrollPane.setPrefHeight(304);
		scrollPane.setMouseTransparent(true);// 不接收鼠标事件,避免对其他控件的点击造成阻碍

		lrcPane.prefWidthProperty().bind(scrollPane.widthProperty());
		lrcPane.prefHeightProperty().bind(scrollPane.heightProperty());
		// lrcPane.setStyle("-fx-background-color: rgba(0,0,0,0);");
		lrcPane.setStyle("-fx-background-color: transparent;");
		lrcPane.getChildren().addAll(lrcVBox);
		scrollPane.setPrefSize(400.0, 310.0);
		scrollPane.setTranslateX(370.0);
		scrollPane.setTranslateY(170.0);

		AnchorPane ap = new AnchorPane();

		singerLabel = new Label("");
		singerLabel.setTextFill(Color.WHITE);
		singerLabel.setFont(font4);
//		singerLabel.setFont(Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 14));
		singerLabel.setTranslateX(440);
		singerLabel.setTranslateY(110);

		songNameLabel = new Label("");
		songNameLabel.setTextFill(Color.WHITE);
//		songNameLabel.setFont(Font.font("楷体", FontWeight.BOLD, FontPosture.ITALIC, 22));
		songNameLabel.setFont(font3);
		songNameLabel.setTranslateX(510);
		songNameLabel.setTranslateY(55);

		albumLabel = new Label("");
		albumLabel.setTextFill(Color.WHITE);
		albumLabel.setTranslateX(580);
		albumLabel.setTranslateY(110);
		albumLabel.setFont(font4);
//		albumLabel.setFont(Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 14));

//		albumLabel = new Label("");
//		albumLabel.setTextFill(Color.WHITE);
//		albumLabel.setFont(Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 12));
//		albumLabel.setLayoutX(350);
//		albumLabel.setLayoutY(100);
//		albumLabel.setPrefWidth(140.0);

		ap.getChildren().addAll(singerLabel, songNameLabel, albumLabel);

		anchorPane.getChildren().addAll(sp, scrollPane, labPlayTime, ap);
		HBox hbox = new HBox();
		hbox.getChildren().addAll(anchorPane);
		// hbox.getChildren().addAll(sp,lrt);
		hbox.setAlignment(Pos.CENTER);
		return hbox;
	}

	// 底部的一系列按钮
	public HBox getBottomView() {
		HBox view = new HBox(92);
		stop = new ImageView("cn/res/icon/start1.png");
		stop.setPreserveRatio(true);
		stop.setPickOnBounds(true);
		stop.setFitWidth(35);

		ImageView left = new ImageView("cn/res/icon/before.png");
		left.setPreserveRatio(true);
		left.setPickOnBounds(true);
		left.setFitWidth(42);

		ImageView right = new ImageView("cn/res/icon/next.png");
		right.setPreserveRatio(true);
		right.setPickOnBounds(true);
		right.setFitWidth(42);

		ImageView mode = new ImageView("cn/res/icon/Repeat.png");
		mode.setPreserveRatio(true);
		mode.setPickOnBounds(true);
		mode.setFitWidth(21);

		volumeImage = new ImageView("cn/res/icon/volume1.png");
		volumeImage.setPreserveRatio(true);
		volumeImage.setPickOnBounds(true);
		volumeImage.setFitWidth(18);

		ImageView list = new ImageView("cn/res/icon/Menu.png");
		list.setPreserveRatio(true);
		list.setPickOnBounds(true);
		list.setFitWidth(25);

		ImageView heart = new ImageView("cn/res/icon/heart11.png");
		heart.setPreserveRatio(true);
		heart.setPickOnBounds(true);
		heart.setFitWidth(23);

		view.getChildren().addAll(heart, mode, left, stop, right, volumeImage, list);
		view.setAlignment(Pos.CENTER);
		view.setTranslateY(-533); // 底部一排按钮的位置
		view.setTranslateX(52);
		list.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue() == true) {
					list.setImage(new Image("cn/res/icon/Menu1.png"));
				} else {
					list.setImage(new Image("cn/res/icon/Menu.png"));

				}
			}

		});
		list.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (listflag == 1) {
					tt.setFromX(-250);
					tt.setToX(0);
				} else {
					tt.setFromX(0);
					tt.setToX(-250);
				}
				listflag = 1 - listflag;

				tt.play();
			}
		});

		// 红心图像
		heart.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				heartFlag = 1 - heartFlag;
				if (heartFlag == 1) {
					heart.setImage(new Image("cn/res/icon/heart1.png"));
				} else {
					heart.setImage(new Image("cn/res/icon/heart2.png"));
				}

			}
		});

		heart.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue() == true) {
					if (heartFlag == 1) {
						heart.setImage(new Image("cn/res/icon/heart1.png"));
					} else {
						heart.setImage(new Image("cn/res/icon/heart2.png"));
					}

				} else {
					if (heartFlag == 1) {
						heart.setImage(new Image("cn/res/icon/heart11.png"));
					} else {
						heart.setImage(new Image("cn/res/icon/heart22.png"));
					}

				}
			}

		});

		stop.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue() == true) {
					if (isplay == 0) {
						stop.setImage(new Image("cn/res/icon/start2.png"));
					} else {
						stop.setImage(new Image("cn/res/icon/stop2.png"));
					}

				} else {
					if (isplay == 0) {
						stop.setImage(new Image("cn/res/icon/start1.png"));
					} else {
						stop.setImage(new Image("cn/res/icon/stop1.png"));
					}

				}
			}

		});

		mode.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue() == true) {
					switch (musicPlayMode) {
					case 1:
						mode.setImage(new Image("cn/res/icon/Repeat1.png"));

						break;
					case 2:
						mode.setImage(new Image("cn/res/icon/ShuffleIcon1.png"));

						break;

					case 3:
						mode.setImage(new Image("cn/res/icon/LoopIcon1.png"));

						break;

					default:
						break;
					}
				}

				else {
					switch (musicPlayMode) {
					case 1:
						mode.setImage(new Image("cn/res/icon/Repeat.png"));

						break;
					case 2:
						mode.setImage(new Image("cn/res/icon/ShuffleIcon.png"));

						break;

					case 3:
						mode.setImage(new Image("cn/res/icon/LoopIcon.png"));

						break;

					default:
						break;
					}

				}
			}
		});

		// 模式选择
		mode.setOnMouseClicked(e -> {
			musicPlayMode++;
			if (musicPlayMode > 3) {
				musicPlayMode = 1;
			}

			switch (musicPlayMode) {
			case 1:

				mode.setImage(new Image("cn/res/icon/Repeat1.png"));

				break;
			case 2:
				mode.setImage(new Image("cn/res/icon/ShuffleIcon1.png"));

				break;

			case 3:
				mode.setImage(new Image("cn/res/icon/LoopIcon1.png"));

				break;

			default:
				break;
			}
		});

		stop.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (mediaPlayer == null) {
					return;
				}
				if (isplay == 0) {
					mediaPlayer.play();
					if (rt != null) {
						rt.play();
					}

					stop.setImage(new Image("cn/res/icon/stop2.png"));
				} else {
					rt.pause();
					mediaPlayer.pause();
					stop.setImage(new Image("cn/res/icon/start2.png"));

				}
				isplay = 1 - isplay;
			}
		});

		heart.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				heartFlag = 1 - heartFlag;
				if (heartFlag == 1) {
					heart.setImage(new Image("cn/res/icon/heart1.png"));
				} else {
					heart.setImage(new Image("cn/res/icon/heart2.png"));
				}

			}
		});

		right.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (mediaPlayer == null) {
					return;
				}
				NextMusic();
			}
		});

		right.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

				if (newValue.booleanValue() == true) {
					right.setImage(new Image("cn/res/icon/next1.png"));
				} else {
					right.setImage(new Image("cn/res/icon/next.png"));

				}

			}

		});

		left.hoverProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue() == true) {
					left.setImage(new Image("cn/res/icon/before1.png"));
				} else {
					left.setImage(new Image("cn/res/icon/before.png"));

				}
			}

		});

		left.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (playList.size() == 0 || mediaPlayer == null) {
					return;
				}
				currentIndex--;
				if (currentIndex < 0) {
					currentIndex = listView.getItems().size() - 1;
				}
				currentPlayBean = playList.get(currentIndex);
				listView.getSelectionModel().clearAndSelect(currentIndex);
				play();
				isplay = 1;
			}
		});

		volumeImage.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (soundImageFlag == 1) {
					sound.setOpacity(0.9);
					slider2.setOpacity(0.3);
				} else {
					sound.setOpacity(0);
					slider2.setOpacity(0.7);
				}
				soundImageFlag = 1 - soundImageFlag;

			}
		});
		return view;
	}

	// 播放时间监听
	private ChangeListener<Duration> initChangeListener() {
		return (observable, oldValue, newValue) -> {

			currentSecond = (int) newValue.toSeconds();

			if (currentSecond == prevSecond + 1) {
				prevSecond++;
				date.setTime((int) slider2.getValue() * 1000);
				labPlayTime.setText(simpleDateFormat.format(date));
			}

			millis = newValue.toMillis();

			min = 0;
			max = 0;

			if (lrcList.size() == 0) {
				return;
			}

			// 判断当前时间是否在正常区间
			if (currentLrcIndex == 0) {
				min = 0;
			} else {
				min = lrcList.get(currentLrcIndex).doubleValue();
			}
			if (currentLrcIndex != lrcList.size() - 1) {
				max = lrcList.get(currentLrcIndex + 1).doubleValue();
			} else {
				max = lrcList.get(currentLrcIndex).doubleValue();

			}

			if (millis >= min && millis < max) {
				return;
			}
			if (currentLrcIndex < lrcList.size() - 1 && millis >= lrcList.get(currentLrcIndex + 1).doubleValue()) {
				currentLrcIndex++;

				// lrcvbox上移

				// 如果是正常播放下去,就展示动画
				if (millis - lrcList.get(currentLrcIndex).doubleValue() < 100) {
					t1.play();
				}
				// 如果是拖动导致的时间推后,直接下一行
				else {
					lrcVBox.setLayoutY(lrcVBox.getLayoutY() - 45);
				}

				Label lab_current = (Label) lrcVBox.getChildren().get(currentLrcIndex);
				lab_current.setFont(boldFont);
				lab_current.getStyleClass().add("shadowLabel");

				Label lab_Pre_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex - 1);
				if (lab_Pre_1 != null) {
					lab_Pre_1.setFont(font);
					lab_Pre_1.getStyleClass().removeAll("shadowLabel");
				}

				if (currentLrcIndex + 1 < lrcList.size()) {
					Label lab_next_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex + 1);
					lab_next_1.setFont(font);
					lab_next_1.getStyleClass().removeAll("shadowLabel");
				}
			} else if (currentLrcIndex > 0 && millis < lrcList.get(currentLrcIndex).doubleValue()) {
				// 进度条回拉

				currentLrcIndex--;

				lrcVBox.setLayoutY(lrcVBox.getLayoutY() + 45);

				Label lab_current = (Label) lrcVBox.getChildren().get(currentLrcIndex);
				lab_current.setFont(boldFont);
				lab_current.getStyleClass().add("shadowLabel");

				if (currentLrcIndex - 1 >= 0) {
					Label lab_Pre_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex - 1);
					lab_Pre_1.setFont(font);
					lab_Pre_1.getStyleClass().removeAll("shadowLabel");
				}

				if (currentLrcIndex + 1 < lrcVBox.getChildren().size()) {
					Label lab_next_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex + 1);
					lab_next_1.setFont(font);
					lab_next_1.getStyleClass().removeAll("shadowLabel");
				}
			}
		};
	}

	private void loadLrc() {
		if (currentPlayBean.getMusicName() == null || currentPlayBean.getMusicName().equals("")) {
			return;
		}

		this.lrcVBox.getChildren().clear();
		this.lrcVBox.setLayoutY(60); // 初始化每次展示的歌词为第三行
		this.lrcList.clear();
		this.currentLrcIndex = 0;
		String[] musicLrcList;
		String lrcString;

		// 歌词文件获取
		if (currentPlayBean.getLrc() == null) {
			String localLrlPath = currentPlayBean.getLocalLrcPath();
			lrcString = LocalMusicUtils.getLrc(localLrlPath);
		} else {
			lrcString = currentPlayBean.getLrc();
		}
		musicLrcList = lrcString.split("\n");
		for (String row : musicLrcList) {
			if (!row.contains("[") || !row.contains("]")) {
				continue;
			}
			if (row.charAt(1) < '0' || row.charAt(1) > '9') {
				continue;
			}
			String strTime = row.substring(1, row.indexOf("]"));
			String strMinute = strTime.substring(0, strTime.indexOf(":"));
			String strSecond = strTime.substring(strTime.indexOf(":") + 1);

			BigDecimal totalMilli = null;
			try {
				int intMinute = Integer.parseInt(strMinute);

				// 换算成毫秒
				totalMilli = new BigDecimal(intMinute * 60).add(new BigDecimal(strSecond))
						.multiply(new BigDecimal("1000"));
			} catch (NumberFormatException e) {
				System.err.println(e);
				totalMilli = new BigDecimal(0);
			}
			this.lrcList.add(totalMilli);
			Label lab = new Label(row.trim().substring(row.indexOf("]") + 1));

			lab.setPrefWidth(380);
			lab.setPrefHeight(30);
			lab.setTextFill(Color.WHITE);
			lab.setAlignment(Pos.CENTER);

			// 如果是第一个歌词就加粗,不是就不加粗
			if (this.lrcVBox.getChildren().size() == 0) {
				lab.setFont(boldFont);
			} else {
				lab.setFont(font);
			}

			this.lrcVBox.getChildren().add(lab);
		}
	}

	public <T> T choose(T obj, T defaultObj) {
		return (obj != null) ? obj : defaultObj;
	}

	// 将频谱转为0-100
	public static float handle_magnitude(float magnitude) {
		float f = Math.abs(magnitude);
		float h = 100 - (100 / (60 / f));
		return h;

	}

	public void OpenDirectory() {
		try {
			java.awt.Desktop.getDesktop().open(new File(System.getProperty("user.dir") + "/LocalMusic/Music"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
