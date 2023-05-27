package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.enums.Game_Status;
import cn.fhyjs.thjntm.resources.FileManager;
import cn.fhyjs.thjntm.resources.I18n;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.badlogic.gdx.Gdx.*;

public class ThGame extends ApplicationAdapter {
	SpriteBatch batch;
	public Map<String,Texture> textureMap = new HashMap<>();
	public Map<String, Music> musicMap = new HashMap<>();
	public Game_Status gameStatus;
	Graphics.Monitor currMonitor;
	Graphics.DisplayMode displayMode;
	private GlyphLayout layout;
	public int WindowW,WindowH;
	private ShapeRenderer renderer;
	public BitmapFont font12;
	private static final Logger logger=new Logger("Main",Logger.DEBUG);
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

		textureMap.put("bgimg",new Texture("jntm/imgs/enterbg.png"));
		musicMap.put("ebgm",Gdx.app.getAudio().newMusic(Gdx.files.internal("jntm/audios/ebgm.mp3")));
		musicMap.get("ebgm").setLooping(true);
	}
	public void drawText(String txt,float x,float y,int c,int size){
		renderer.begin(ShapeRenderer.ShapeType.Line);
		batch.setColor(new Color(c));
		font12.getData().setScale(size,size);
		font12.draw(batch, txt, x, y);
		renderer.end();
	}
	public int count;
	public boolean b1;
	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		switch (gameStatus){
			case ENTERING: {
				if (!musicMap.get("ebgm").isPlaying()) {
					musicMap.get("ebgm").play();
				}
				batch.setColor(0.5f, 0.5f, 0.5f, 1);
				batch.draw(textureMap.get("bgimg"), 0, 0, WindowW, WindowH);

				drawText(I18n.get("enter.msg"), (float) (125 - 5 + count * .03), (float) (100 - 5 + count * .03), 0xfcfcfc, 2);
				if (b1) {
					count++;
					if (count>500)
						b1=false;
				} else {
					count--;
					if (count<0)
						b1=true;
				}
			}
				break;
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
}
