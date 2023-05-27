package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.enums.Game_Status;
import cn.fhyjs.thjntm.enums.KeyAct;
import cn.fhyjs.thjntm.resources.FileManager;
import cn.fhyjs.thjntm.resources.I18n;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.Input.Keys;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.badlogic.gdx.Gdx.*;

public class ThGame extends ApplicationAdapter {
	SpriteBatch batch;
	public Map<String,Texture> textureMap = new HashMap<>();
	public Map<String, Music> musicMap = new HashMap<>();
	public Map<Integer,Boolean> keyMap = new HashMap<>();
	public Game_Status gameStatus,oGS;
	Graphics.Monitor currMonitor;
	Graphics.DisplayMode displayMode;
	private GlyphLayout layout;
	public int WindowW,WindowH;
	private ShapeRenderer renderer;
	public BitmapFont font12;
	private static final Logger logger=new Logger("Main",Logger.DEBUG);
	public boolean IsDown(int c){
		return keyMap.containsKey(c) && keyMap.get(c);
	}
	@Override
	public void create () {
		logger.info("Starting...");
		I18n.init("zh_cn");
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
		font12 = new BitmapFont(Gdx.files.internal("jntm/fonts/Hanazono-standard.fnt"),Gdx.files.internal("jntm/fonts/Hanazono-standard.png"),false);
		font12.getData().markupEnabled=true;
		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(batch.getProjectionMatrix());
		layout = new GlyphLayout();
		count=0;
		ThInputProcessor inputProcessor = new ThInputProcessor();
		Gdx.input.setInputProcessor(inputProcessor);

		textureMap.put("bgimg",new Texture("jntm/imgs/enterbg.png"));
		musicMap.put("ebgm",Gdx.app.getAudio().newMusic(Gdx.files.internal("jntm/audios/ebgm.mp3")));
		musicMap.get("ebgm").setLooping(true);

	}
	public void drawText(String txt,float x,float y,int c,float size){
		renderer.begin(ShapeRenderer.ShapeType.Line);
		batch.setColor(new Color(c));
		font12.getData().setScale(size,size);
		font12.draw(batch, txt, x, y);
		renderer.end();
	}
	public int count;
	public boolean b1;
	public void ChanageGS(Game_Status to) {
		textureMap.put("change", new Texture(takeScreen()));
		gameStatus=Game_Status.Changing;
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
		switch (gameStatus) {
			case ENTERING: {
				if(act==KeyAct.UP) {
					ChanageGS(Game_Status.MENU);
				}
				break;
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
				if (!musicMap.get("ebgm").isPlaying()) {
					musicMap.get("ebgm").play();
				}
				batch.setColor(0.6f, 0.6f, 0.6f, 1);
				batch.draw(textureMap.get("bgimg"), 0, 0, WindowW, WindowH);

				drawText(I18n.get("enter.msg"), (float) (125 - 5 + count * .03), (float) (100 - 5 + count * .03), 0xfcfcfc, 2);
				drawText(I18n.get("game.name"), (float) (100 - 5 + count * .03), (float) (450 - 5 + count * .03), 0xfcfcfc, 5f);
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
				drawText(I18n.get("game.name"), (float) (100 - 5 + count * .03), (float) (450 - 5 + count * .03), 0xfcfcfc, 5f);
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
		if (gameStatus!=Game_Status.ENTERING){
			musicMap.get("ebgm").stop();
		}
		batch.setColor(Color.WHITE);
		batch.end();
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
