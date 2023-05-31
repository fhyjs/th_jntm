package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.enums.Game_Status;
import cn.fhyjs.thjntm.enums.KeyAct;
import cn.fhyjs.thjntm.enums.ResType;
import cn.fhyjs.thjntm.resources.I18n;
import cn.fhyjs.thjntm.util.ProgressBar;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.Input.Keys;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.*;

public class ThGame extends ApplicationAdapter {
	SpriteBatch batch;
	public Map<String, Texture> textureMap = new HashMap<>();
	public Map<String, Music> musicMap = new HashMap<>();
	public Stack<Game_Status> navigation = new Stack<>();
	public Map<Music, List<Game_Status>> bgmMap = new HashMap<>();
	public Map<Integer, Boolean> keyMap = new HashMap<>();
	public Game_Status gameStatus, oGS;
	Graphics.Monitor currMonitor;
	Graphics.DisplayMode displayMode;
	private GlyphLayout layout;
	public int WindowW, WindowH;
	private ShapeRenderer renderer;
	public BitmapFont font12;
	private static final Logger logger = new Logger("Main", Logger.DEBUG);
	public boolean IsDown(int c){
		return keyMap.containsKey(c) && keyMap.get(c);
	}

    public Map<ResType,Map<String,Object>> RegRes(){
        Map<ResType,Map<String,Object>> resm = new HashMap<>();
        Map<String,Object> music = new HashMap<>();
        Map<String,Object> texture = new HashMap<>();

        texture.put("bgimg","jntm/imgs/enterbg.png");
        music.put("ebgm",Gdx.files.internal("jntm/audios/ebgm.mp3"));

        resm.put(ResType.MUSIC,music);
        resm.put(ResType.TEXTURE,texture);
        return resm;
    }
	private void SetResProp(){
		regbgm("ebgm",Game_Status.ENTERING,Game_Status.MENU);
	}
	@Override
	public void create () {
		logger.info("Starting...");
		I18n.init("zh_cn");
		try {
			Config.Sync();
		} catch (URISyntaxException e) {
			logger.error("Config Core Error!");
		}
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
		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(batch.getProjectionMatrix());
		layout = new GlyphLayout();
		count=0;
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
		if (code== Keys.ESCAPE&&act==KeyAct.Down&&gameStatus!=Game_Status.ENTERING){if(gameStatus==Game_Status.MENU&&count==4){Gdx.app.exit();}if (gameStatus==Game_Status.MENU){count=4;return;}ChanageGS(navigation.get(navigation.size()-2),true);navigation.pop();}
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
						count--;
					}
					if (code==Config.Input_Down && count < 4) {
						count++;
					}
					if (code==Config.Input_Ok){
						switch (count){
							case 0:{

								break;
							}
							case 1:{
								break;
							}
							case 2:{
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
			}
		}
	}
	@Override
	public void render () {
		if (gameStatus!=oGS) onSChanaged();
		ScreenUtils.clear(1, 0, 0, 1);
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
	public void dispose () {
		logger.info("Quiting...");
		batch.dispose();
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
