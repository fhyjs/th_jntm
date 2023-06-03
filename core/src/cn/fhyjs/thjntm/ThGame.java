package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.enums.Game_Status;
import cn.fhyjs.thjntm.enums.KeyAct;
import cn.fhyjs.thjntm.enums.ResType;
import cn.fhyjs.thjntm.level.Bullet;
import cn.fhyjs.thjntm.resources.I18n;
import cn.fhyjs.thjntm.util.CUncaughtExceptionHandler;
import cn.fhyjs.thjntm.util.GifDecoder;
import cn.fhyjs.thjntm.util.ProgressBar;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.Input.Keys;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.*;

import static com.badlogic.gdx.Gdx.gl;

public class ThGame extends ApplicationAdapter {
	SpriteBatch batch;
	private OrthographicCamera camera;
	public Map<String, Texture> textureMap = new HashMap<>();
	public Map<String, Animation<TextureRegion>> animationMap = new HashMap<>();
	public Map<String, Music> musicMap = new HashMap<>();
	public Map<String, Body> bodyMap = new HashMap<>();
	public List<Body> RMbody = new ArrayList<>();
	public Stack<Game_Status> navigation = new Stack<>();
	public Map<Music, List<Game_Status>> bgmMap = new HashMap<>();
	public Map<String, Sound> soundMap = new HashMap<>();
	World world = new World(new Vector2(0, 0), true);
	public Map<Integer, Boolean> keyMap = new HashMap<>();
	public Game_Status gameStatus, oGS;
	Graphics.Monitor currMonitor;
	Skin skin;
	Graphics.DisplayMode displayMode;
	private GlyphLayout layout;
	public int WindowW, WindowH;
	public ShapeRenderer renderer;
	public BitmapFont font12;
	Slider slider;
	private static final Logger logger = new Logger("Main", Logger.DEBUG);
	private Box2DDebugRenderer debugRenderer;

	public boolean IsDown(int c){
		return keyMap.containsKey(c) && keyMap.get(c);
	}
    public Map<ResType,Map<String,Object>> RegRes(){
        Map<ResType,Map<String,Object>> resm = new HashMap<>();
        Map<String,Object> music = new HashMap<>();
		Map<String,Object> sound = new HashMap<>();
        Map<String,Object> texture = new HashMap<>();
        Map<String,Object> animation = new HashMap<>();

		texture.put("bgimg","jntm/imgs/enterbg.png");
		music.put("ebgm",Gdx.files.internal("jntm/audios/ebgm.mp3"));
		sound.put("ji",Gdx.files.internal("jntm/sounds/ji.ogg"));
		sound.put("niganma",Gdx.files.internal("jntm/sounds/ngm.ogg"));
		texture.put("lanqiu","jntm/imgs/lanqiu.png");
		texture.put("sod3r","jntm/imgs/sod3row.png");
		animation.put("tsk",Gdx.files.internal("jntm/imgs/tieshankao.gif"));

		resm.put(ResType.ANIMATION,animation);
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
		Gdx.graphics.setForegroundFPS(Config.FPS);
		WindowH=Gdx.graphics.getHeight();
		WindowW=Gdx.graphics.getWidth();
		gameStatus=Game_Status.ENTERING;
		debugRenderer = new Box2DDebugRenderer(true,true,true,true,true,true);
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
					if (ctype == ResType.ANIMATION) {
						animationMap.put(name,GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, ((FileHandle) cmap.get(name)).read()));
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
	public Thread thread,thread1;
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
			case TGAME:{
				integerList.clear();
				integerList.add(0);
				integerList.add(300);
				integerList.add(600);
				thread=new G2BgEffect();
				thread.start();
				thread1=new ProcBullet();
				thread1.start();
				playerx=300;
				playery=10;
				createObject();
				bodyMap.get("pdd").setTransform(playerx,playery,1.6f);
				break;
			}
			default:
				try {
					if (thread1!=null)
						thread1.interrupt();
					if (thread!=null)
						thread.interrupt();
				}catch (ThreadDeath ignored){}
				for (Body b : bodyMap.values()) {
					world.destroyBody(b);
				}
				bullets.clear();
				bodyMap.clear();
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
								ChanageGS(Game_Status.TGAME,false);
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
						case 3:{
							if (code == Config.Input_Left){
								Config.FPS=60;
							}
							if (code == Config.Input_Right){
								Config.FPS=30;
							}
							Gdx.graphics.setForegroundFPS(Config.FPS);
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
	public void ProcessInput(){
		switch (gameStatus) {
			case TGAME:{
				if (IsDown(Config.Input_Up)&&playery<600) playery+=5;
				if (IsDown(Config.Input_Down)&&playery>0) playery-=5;
				if (IsDown(Config.Input_Right)&&playerx<600) playerx+=5;
				if (IsDown(Config.Input_Left)&&playerx>0) playerx-=5;
				if (IsDown(Config.Input_Ok)) shoot(playerx,playery,bodyMap.get("pdd").getAngle(),1,10,true);
				break;
			}
		}
	}
	@Override
	public void render () {
		elapsed += Gdx.graphics.getDeltaTime();
		if (gameStatus!=oGS) onSChanaged();
		ProcessInput();
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
				drawText(I18n.get("option.fps"),100,350,Color.WHITE,1);
				slider.setBounds(300, 490, 150, 0);
				slider.setValue(Config.Volume_Bgm);
				slider.draw(batch,1);
				slider.setBounds(300, 430, 150, 20);
				slider.setValue(Config.Volume_Se);
				slider.draw(batch,1);
				batch.draw(textureMap.get("lanqiu"),65,475-(count*50),30,30);
				drawText(I18n.get("option.language.zh_cn"),300,400,(Objects.equals(Config.Language, "zh_cn") ?Color.YELLOW:Color.GRAY),1);
				drawText(I18n.get("option.language.en_us"),450,400,(Objects.equals(Config.Language, "en_us") ?Color.YELLOW:Color.GRAY),1);
				drawText("60",400,350,(Config.FPS==60 ?Color.YELLOW:Color.GRAY),1);
				drawText("30",450,350,(Config.FPS==30 ?Color.YELLOW:Color.GRAY),1);
				break;
			}
			case TGAME:{

				gl.glClearColor(0,0,0,1);
				gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				batch.draw(textureMap.get("sod3r"),-20,integerList.get(0),640,300);
				batch.draw(textureMap.get("sod3r"),-20,integerList.get(1),640,300);
				batch.draw(textureMap.get("sod3r"),-20,integerList.get(2),640,300);

				bodyMap.get("pdd").setTransform(playerx,playery,bodyMap.get("pdd").getAngle()	);
				batch.draw(animationMap.get("tsk").getKeyFrame(elapsed),playerx-45,playery-45,100,100);
				batch.end();
				renderer.begin(ShapeRenderer.ShapeType.Filled);
				renderer.circle(playerx,playery,5);
				renderer.end();
				batch.begin();
				int t = bullets.size();
				for (int i = 0 ;i<t;i++) {
					Bullet bullet = bullets.get(i);
					Body body = bodyMap.get(bullet.name);
					body.setTransform(bullet.x, bullet.y, bullet.a);
				}
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
		int t = RMbody.size();
		for (int i = 0 ;i<t;i++) {
			world.destroyBody(RMbody.get(i));
			RMbody.remove(i);
		}
		checkbgm(gameStatus);
		batch.setColor(Color.WHITE);
		drawText(String.valueOf(Gdx.graphics.getFramesPerSecond()),570,20,Color.WHITE,0.8f);
		batch.end();
		debugRenderer.render(world,camera.combined);
		world.step(1/60f, 6, 2);
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
		world.dispose();
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
	public List<Integer> integerList = new ArrayList<>();
	float elapsed,playerx,playery;
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
			count=0;
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
	private class G2BgEffect extends Thread{
		private boolean r=true;
		public G2BgEffect(){
			super();
			r=true;
		}
		@Override
		public void interrupt(){
			super.interrupt();
			r=false;
		}
		@Override
		public void run() {
			while (r) {
				for (int i = 0; i < integerList.size(); i++) {
					integerList.set(i, integerList.get(i) - 1);
					if (integerList.get(i) < -300) {
						integerList.set(i, integerList.get(i)+900);
					}
				}
				try {
					sleep(50);
				} catch (InterruptedException ignored) {}
			}
		}
	}
	private class ProcBullet extends Thread{
		private boolean r=true;
		public ProcBullet(){
			super();
			r=true;
		}
		@Override
		public void interrupt(){
			super.interrupt();
			r=false;
		}
		@Override
		public void run() {
			while (r) {
				for (int i=0;i<bullets.size();i++){
					Bullet bullet = bullets.get(i);
					Body body = bodyMap.get(bullet.name);
					if (!bullet.name.contains("BULLET-")||body==null)
						continue;
					float angle = body.getAngle(); // Body angle in radians.
					float velX = MathUtils.cos(angle) * bullet.speed; // X-component.
					float velY = MathUtils.sin(angle) * bullet.speed; // Y-component.
					bullet.x=bullet.x+velX;
					bullet.y=bullet.y+velY;
					if (bullet.x>600||bullet.y>600||bullet.x<0||bullet.y<0){
						bodyMap.remove(bullet.name);
						bullets.remove(i);
						RMbody.add(body);
					}
				}
				try {
					sleep(1000/Config.FPS);
				} catch (InterruptedException ignored) {}
			}
		}
	}
	public void shoot(float x,float y,float a,float speed,float size,boolean player){
		bullets.add(new Bullet(speed,x,y,a,size,createCObject(size,"BULLET-"+bullets.size(),x,y)));

	}
	public List<Bullet> bullets = new ArrayList<>();
	private void createObject(){
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;

		Body body = world.createBody(bodyDef);
		CircleShape circle = new CircleShape();
		circle.setRadius(5f);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 0.5f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.6f; // Make it bounce a little bit

		Fixture fixture = body.createFixture(fixtureDef);
		bodyMap.put("pdd",body);
		circle.dispose();
	}
	private String createCObject(float size,String name,float x,float y){
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(new Vector2(x,y));
		Body body = world.createBody(bodyDef);
		CircleShape circle = new CircleShape();
		circle.setRadius(size);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 0.5f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.6f; // Make it bounce a little bit

		Fixture fixture = body.createFixture(fixtureDef);
		bodyMap.put(name,body);
		circle.dispose();
		return name;
	}
}
