package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.enums.Game_Status;
import cn.fhyjs.thjntm.enums.KeyAct;
import cn.fhyjs.thjntm.enums.ResType;
import cn.fhyjs.thjntm.resources.I18n;
import cn.fhyjs.thjntm.util.CUncaughtExceptionHandler;
import cn.fhyjs.thjntm.util.ProgressBar;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.Input.Keys;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.*;

import static com.badlogic.gdx.Gdx.gl;

public class ThGame extends ApplicationAdapter {
	SpriteBatch batch;
	private OrthographicCamera camera;
	public Map<String, Texture> textureMap = new HashMap<>();
	public Map<String, Music> musicMap = new HashMap<>();
	public Stack<Game_Status> navigation = new Stack<>();
	public Map<Music, List<Game_Status>> bgmMap = new HashMap<>();
	public Map<String, Sound> soundMap = new HashMap<>();
	public Map<Integer, Boolean> keyMap = new HashMap<>();
	public Game_Status gameStatus, oGS;
	Graphics.Monitor currMonitor;
	Skin skin;
	Graphics.DisplayMode displayMode;
	private GlyphLayout layout;
	public int WindowW, WindowH;
	private ShapeRenderer renderer;
	public BitmapFont font12;
	Slider slider;
	private static final Logger logger = new Logger("Main", Logger.DEBUG);
	public boolean IsDown(int c){
		return keyMap.containsKey(c) && keyMap.get(c);
	}

    public Map<ResType,Map<String,Object>> RegRes(){
        Map<ResType,Map<String,Object>> resm = new HashMap<>();
        Map<String,Object> music = new HashMap<>();
		Map<String,Object> sound = new HashMap<>();
        Map<String,Object> texture = new HashMap<>();

        texture.put("bgimg","jntm/imgs/enterbg.png");
        music.put("ebgm",Gdx.files.internal("jntm/audios/ebgm.mp3"));
		sound.put("ji",Gdx.files.internal("jntm/sounds/ji.ogg"));
		sound.put("niganma",Gdx.files.internal("jntm/sounds/ngm.ogg"));
		texture.put("lanqiu","jntm/imgs/lanqiu.png");

        resm.put(ResType.MUSIC,music);
		resm.put(ResType.SOUND,sound);
        resm.put(ResType.TEXTURE,texture);
        return resm;
    }
	private void SetResProp(){
		regbgm("ebgm",Game_Status.ENTERING,Game_Status.MENU,Game_Status.OPTION);
	}
	@Override
	public void create () {
		logger.info("Starting...");
		Thread.setDefaultUncaughtExceptionHandler(new CUncaughtExceptionHandler());
		try {
			Config.Sync();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		I18n.init();
		currMonitor = Gdx.graphics.getMonitor();
		displayMode = Gdx.graphics.getDisplayMode(currMonitor);
		if(Gdx.graphics.supportsDisplayModeChange()) {
			Gdx.graphics.setTitle(I18n.get("th_jntm.name"));
			Gdx.graphics.setResizable(false);
		}
		WindowH=Gdx.graphics.getHeight();
		WindowW=Gdx.graphics.getWidth();
		gameStatus=Game_Status.ENTERING;
		batch = new SpriteBatch();
		font12 = new BitmapFont(Gdx.files.internal("jntm/fonts/msyh/msyh.fnt"),false);
		font12.getData().markupEnabled=true;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(batch.getProjectionMatrix());
		layout = new GlyphLayout();
		skin=new Skin(Gdx.files.internal("jntm/skin/uiskin.json"));
		count=0;
		slider = new Slider(0,100,5,false,skin);
		ThInputProcessor inputProcessor = new ThInputProcessor();
		Gdx.input.setInputProcessor(inputProcessor);
		{
			Map<ResType, Map<String, Object>> resm = RegRes();
			for (int i = 0; i < resm.size(); i++) {
				ProgressBar pb = new ProgressBar("pb", "running");
				pb.start();
				pb.jf.setTitle(I18n.get("proc.res.name"));
				pb.txt1.setText(I18n.get("proc." + resm.keySet().toArray()[i].toString() + ".name"));
				Map<String, Object> cmap = resm.get(resm.keySet().toArray()[i]);
				ResType ctype = (ResType) resm.keySet().toArray()[i];
				for (int j = 0; j < cmap.size(); j++) {
					String name = (String) cmap.keySet().toArray()[j];
					if (ctype == ResType.MUSIC) {
						FileHandle file = (FileHandle) cmap.get(name);
						musicMap.put(name, Gdx.app.getAudio().newMusic(file));
					}
					if (ctype == ResType.TEXTURE) {
						String file = (String) cmap.get(name);
						textureMap.put(name, new Texture(file));
					}
					if (ctype == ResType.SOUND) {
						soundMap.put(name, Gdx.audio.newSound((FileHandle) cmap.get(name)));
					}
					pb.progress = (j+1) / cmap.size() * 100;
				}
			}
		}//注册
		SetResProp();

	}
	public void drawText(String txt,float x,float y,Color c,float size){
		renderer.begin(ShapeRenderer.ShapeType.Line);
		font12.setColor(c);
		font12.getData().setScale(size,size);
		font12.draw(batch, txt, x, y);
		renderer.end();
	}
	public int count;
	public boolean b1;
	public void ChanageGS(Game_Status to,boolean noHistory) {
		textureMap.put("change", new Texture(takeScreen()));
		gameStatus=Game_Status.Changing;
		if (!noHistory) navigation.add(to);
		new Changer(to).start();
	}
	public void onSChanaged(){
		oGS=gameStatus;

		switch (gameStatus) {
			default:
				count=0;
				break;
		}
	}
	public void ProcessInput(int code, KeyAct act){
		if(IsDown(Keys.SHIFT_LEFT)&&IsDown(Keys.ESCAPE)){ Gdx.app.exit();}
		if(IsDown(Keys.CONTROL_LEFT)&&IsDown(Keys.SHIFT_LEFT)&&IsDown(Keys.E)){ throw new RuntimeException("TEST ERROR");}
		if (code== Keys.ESCAPE&&act==KeyAct.Down&&gameStatus!=Game_Status.ENTERING&&gameStatus!=Game_Status.Changing){
			PlaySound("ji",1.5f,1);
			if(gameStatus==Game_Status.MENU&&count==4) {Gdx.app.exit();}
			if (gameStatus==Game_Status.MENU) {count=4;return;}
			ChanageGS(navigation.get(navigation.size()-2),true);
			navigation.pop();
		}
		switch (gameStatus) {
			case ENTERING: {
				if(act==KeyAct.UP) {
					ChanageGS(Game_Status.MENU,false);
				}
				break;
			}
			case MENU:{
				if (act==KeyAct.Down) {
					if (code==Config.Input_Up && count > 0) {
						PlaySound("ji");
						count--;
					}
					if (code==Config.Input_Down && count < 4) {
						PlaySound("ji");
						count++;
					}
					if (code==Config.Input_Ok){
						PlaySound("niganma");
						switch (count){
							case 0:{

								break;
							}
							case 1:{
								break;
							}
							case 2:{
								ChanageGS(Game_Status.ASSETS,false);
								break;
							}
							case 3:{
								ChanageGS(Game_Status.OPTION,false);
								break;
							}
							case 4:{
								Gdx.app.exit();
								break;
							}
						}
					}
				}
				break;
			}
			case OPTION:{
				if (act==KeyAct.Down) {
					if (code == Config.Input_Up && count > 0) {
						PlaySound("ji");
						count--;
					}
					if (code == Config.Input_Down && count < 3) {
						PlaySound("ji");
						count++;
					}
					switch (count){
						case 0:{
							if (code == Config.Input_Left&&Config.Volume_Bgm>0){
								Config.Volume_Bgm-=5;
							}
							if (code == Config.Input_Right&&Config.Volume_Bgm<100){
								Config.Volume_Bgm+=5;
							}
							break;
						}
						case 1:{
							if (code == Config.Input_Left&&Config.Volume_Se>0){
								Config.Volume_Se-=5;
							}
							if (code == Config.Input_Right&&Config.Volume_Se<100){
								Config.Volume_Se+=5;
							}
							PlaySound("ji");
							break;
						}
						case 2:{
							if (code == Config.Input_Left){
								Config.Language="zh_cn";
							}
							if (code == Config.Input_Right){
								Config.Language="en_us";
							}
							PlaySound("ji");
							break;
						}
					}

				}
				try {
					Config.write();
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
				break;
			}
		}
	}


	@Override
	public void render () {
		if (gameStatus!=oGS) onSChanaged();
		gl.glClearColor(1, 0, 0, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		renderer.setProjectionMatrix(camera.combined);
		batch.begin();
		switch (gameStatus){
			case ENTERING: {
				batch.setColor(0.6f, 0.6f, 0.6f, 1);
				batch.draw(textureMap.get("bgimg"), 0, 0, WindowW, WindowH);

				drawText(I18n.get("enter.msg"), (float) (125 - 5 + count * .03), (float) (100 - 5 + count * .03), convertArgbToLibGdxColor(0xfcfcfc,1), 1.5f);
				drawText(I18n.get("game.name"), (float) (100 - 5 + count * .03), (float) (450 - 5 + count * .03), convertArgbToLibGdxColor(0xfcfcfc,1), 4f);
				if (b1) {
					count++;
					if (count>500)
						b1=false;
				} else {
					count--;
					if (count<0)
						b1=true;

				}
				break;
			}
			case MENU:{
				batch.setColor(0.6f, 0.6f, 0.6f, 1);
				batch.draw(textureMap.get("bgimg"), 0, 0, WindowW, WindowH);
				drawText(I18n.get("game.name"), 85, 500, convertArgbToLibGdxColor(0xfcfcfc,1), 4f);

				drawText(I18n.get("menu.start1"),210,320,convertArgbToLibGdxColor(0xfcfcfc,(count==0?1:0.5f)),(count==0?1.5f:1));
				drawText(I18n.get("menu.start2"),230,280,convertArgbToLibGdxColor(0xfcfcfc,(count==1?1:0.5f)),(count==1?1.5f:1));
				drawText(I18n.get("menu.assets"),240,240,convertArgbToLibGdxColor(0xfcfcfc,(count==2?1:0.5f)),(count==2?1.5f:1));
				drawText(I18n.get("menu.option"),225,200,convertArgbToLibGdxColor(0xfcfcfc,(count==3?1:0.5f)),(count==3?1.5f:1));
				drawText(I18n.get("menu.exit"),200,160,convertArgbToLibGdxColor(0xfcfcfc,(count==4?1:0.5f)),(count==4?1.5f:1));
				break;
			}
			case OPTION:{
				batch.setColor(0.6f, 0.6f, 0.6f, 1);
				batch.draw(textureMap.get("bgimg"), 0, 0, WindowW, WindowH);
				batch.end();

				renderer.begin(ShapeRenderer.ShapeType.Line);
				gl.glLineWidth(5);
				renderer.setColor(convertArgbToLibGdxColor(0xd8c6ab,1));
				renderer.line(0, 530, 600, 530);
				renderer.end();
				batch.begin();
				drawText(I18n.get("menu.option"),30,580,Color.WHITE,2);
				drawText(I18n.get("option.bgmvol"),100,500,Color.WHITE,1);
				drawText(I18n.get("option.sevol"),100,450,Color.WHITE,1);
				drawText(I18n.get("option.language"),100,400,Color.WHITE,1);
				slider.setBounds(300, 490, 150, 0);
				slider.setValue(Config.Volume_Bgm);
				slider.draw(batch,1);
				slider.setBounds(300, 430, 150, 20);
				slider.setValue(Config.Volume_Se);
				slider.draw(batch,1);
				batch.draw(textureMap.get("lanqiu"),65,475-(count*50),30,30);
				drawText(I18n.get("option.language.zh_cn"),300,400,(Objects.equals(Config.Language, "zh_cn") ?Color.YELLOW:Color.GRAY),1);
				drawText(I18n.get("option.language.en_us"),450,400,(Objects.equals(Config.Language, "en_us") ?Color.YELLOW:Color.GRAY),1);
				break;
			}
			case Changing:{
				if (textureMap.containsKey("change")&&textureMap.get("change")!=null) {
					batch.setColor(((float) 70-count) /70, ((float) 70-count) /70, ((float) 70-count) /70, 1);
					Sprite sprite = new Sprite(textureMap.get("change"));
					sprite.flip(false, true);
					batch.draw(sprite, 0, 0, WindowW, WindowH);
					count++;
				}
				break;
			}
		}
		checkbgm(gameStatus);
		batch.setColor(Color.WHITE);
		batch.end();
	}
	public void checkbgm(Game_Status Cgs){
		for (Music m:bgmMap.keySet()){
			List<Game_Status> gameStatuses=bgmMap.get(m);
			if(gameStatuses.contains(Cgs)){
				if (!m.isPlaying())
					m.play();
				m.setVolume((float) Config.Volume_Bgm /100);
				return;
			}
		}
		for (Music m:bgmMap.keySet()){
			m.stop();
		}
	}
	public void regbgm(String id,Game_Status... where){
		musicMap.get(id).setLooping(true);
		List<Game_Status> t = new ArrayList<>(Arrays.asList(where));
		bgmMap.put(musicMap.get(id),t);
	}
	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
	}
	@Override
	public void dispose () {
		logger.info("Quiting...");
		batch.dispose();
		renderer.dispose();
		for (Texture t:textureMap.values()){
			t.dispose();
		}
		for (Music t:musicMap.values()){
			t.dispose();
		}
	}
	private Pixmap takeScreen() {
		Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		ByteBuffer pixels = pixmap.getPixels();

		int size = Gdx.graphics.getBackBufferWidth() * Gdx.graphics.getBackBufferHeight() * 4;
		for (int i = 3; i < size; i += 4) {
			pixels.put(i, (byte) 255);
		}

		return pixmap;
	}
	public long PlaySound(String id){
		return soundMap.get(id).play((float) Config.Volume_Se /100);
	}
	public long PlaySound(String id ,float p,float pan){
		return soundMap.get(id).play((float) Config.Volume_Se /100,p,pan);
	}
	public Pixmap FlipPixmap(Pixmap src) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		Pixmap flipped = new Pixmap(width, height, src.getFormat());

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				flipped.drawPixel(x, y, src.getPixel(width - x - 1, y));
			}
		}
		return flipped;
	}
	private com.badlogic.gdx.graphics.Color convertArgbToLibGdxColor(int rgbColor,float a) {
		com.badlogic.gdx.graphics.Color color = new com.badlogic.gdx.graphics.Color();
		com.badlogic.gdx.graphics.Color.argb8888ToColor(color, rgbColor);
		color.a=a;
		return color;
	}
	private class Changer extends Thread{
		Game_Status to;
		public Changer(Game_Status to){
			super();
			this.to=to;
		}
		@Override
		public void run(){
			while (true){
				if (count>70){
					gameStatus=to;
					break;
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
