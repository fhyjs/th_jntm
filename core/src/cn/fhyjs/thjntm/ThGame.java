package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.ctrls.CMain;
import cn.fhyjs.thjntm.enums.Game_Status;
import cn.fhyjs.thjntm.enums.KeyAct;
import cn.fhyjs.thjntm.enums.ResType;
import cn.fhyjs.thjntm.interfaces.ITickable;
import cn.fhyjs.thjntm.level.Bullet;
import cn.fhyjs.thjntm.level.Enemy;
import cn.fhyjs.thjntm.level.Player;
import cn.fhyjs.thjntm.resources.FileManager;
import cn.fhyjs.thjntm.resources.I18n;
import cn.fhyjs.thjntm.util.CUncaughtExceptionHandler;
import cn.fhyjs.thjntm.util.GifDecoder;
import cn.fhyjs.thjntm.util.ProgressBar;
import cn.fhyjs.thjntm.util.Ticker;
import com.badlogic.gdx.*;
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
import com.badlogic.gdx.utils.Pool;

import com.badlogic.gdx.Input.Keys;
import org.apache.commons.cli.*;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.*;

import static com.badlogic.gdx.Gdx.gl;

public class ThGame extends ApplicationAdapter implements ITickable {
	SpriteBatch batch;
	public static boolean IsDevEnv;
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
	// array containing the active bullets.
	public final List<Bullet> activeBullets = new ArrayList<Bullet>();
	public final List<Enemy> activeEnemy = new ArrayList<Enemy>();
	public Screen screen;
	// bullet pool.
	private final Pool<Bullet> bulletPool = new Pool<Bullet>() {

		@Override
		protected Bullet newObject() {
			return new Bullet();
		}
	};
	private final Pool<Enemy> enemyPool = new Pool<Enemy>() {

		@Override
		protected Enemy newObject() {
			return new Enemy();
		}
	};
	private static final Logger logger = new Logger("Main", Logger.DEBUG);
	private Box2DDebugRenderer debugRenderer;
	public ThGame(String[] arg){
		super();
		Options options=new Options();
		options.addOption(Option
				.builder("e")
				.longOpt("enter")
				.argName("enter")
				.hasArg()
				.required(false)
				.desc("set enter status")
				.build());
		options.addOption(Option
				.builder("h")
				.longOpt("help")
				.argName("help")
				.required(false)
				.desc("cmd help")
				.build());
		options.addOption(Option
				.builder("d")
				.longOpt("dev")
				.argName("dev")
				.required(false)
				.desc("dev mode")
				.build());
		CommandLine cmd;
		CommandLineParser parser = new DefaultParser();
		HelpFormatter helper = new HelpFormatter();
		try {
			cmd = parser.parse(options, arg);
			gameStatus=Game_Status.valueOf(cmd.getOptionValue("e","ENTERING"));

			if (cmd.hasOption("h")) {
				throw new ParseException("help");
			}
			IsDevEnv=cmd.hasOption("d");
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			helper.printHelp("Usage:", options);
			System.exit(0);
		}
	}
	public boolean IsDown(int c){
		return keyMap.containsKey(c) && keyMap.get(c);
	}
    @SuppressWarnings("SuspiciousIndentation")
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
		texture.put("ji_icon","jntm/imgs/ji_icon.png");
		texture.put("missing","jntm/imgs/missing.png");
		animation.put("tsk",Gdx.files.internal("jntm/imgs/tieshankao.gif"));
		animation.put("boom",Gdx.files.internal("jntm/imgs/boom.gif"));

		Ticker.AddTickAble(this);

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
						try {
							animationMap.put(name,GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, FileManager.getByteContent(((FileHandle) cmap.get(name)).file().toPath().toString())));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					pb.progress = (j+1) / cmap.size() * 100;
				}
			}
		}//注册
		SetResProp();
        new Ticker();
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
	public Player player;
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
				player=new Player(300,10,90,5);
				c1=0;
				createObject();
				bodyMap.get("pdd").setTransform(player.x,player.y,player.a);
				world.setContactListener(new ThContactListener());
				ctrl=new CMain(Gdx.files.internal("jntm/ectrl/main.ctr").path());
				CTick=0;
				break;
			}
			default:
				world.setContactListener(null);
				activeBullets.clear();
				try {
					if (thread1!=null)
						thread1.interrupt();
					if (thread!=null)
						thread.interrupt();
				}catch (ThreadDeath ignored){}
				for (Body b : bodyMap.values()) {
					world.destroyBody(b);
				}
				bodyMap.clear();
				count=0;
				break;
		}
	}
	public CMain ctrl;
	@SuppressWarnings("SuspiciousIndentation")
	public void ProcessInput(int code, KeyAct act){
		if(IsDown(Keys.SHIFT_LEFT)&&IsDown(Keys.ESCAPE)){ Gdx.app.exit();}
		if(IsDevEnv&&IsDown(Keys.CONTROL_LEFT)&&IsDown(Keys.SHIFT_LEFT)&&IsDown(Keys.E)){ throw new RuntimeException("TEST ERROR");}
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
                            new Ticker();
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
			case TGAME:{
				if(IsDevEnv&&IsDown(Keys.CONTROL_LEFT)&&IsDown(Keys.SHIFT_LEFT)&&IsDown(Keys.T)){ CTick=0;}
				break;
			}
		}
	}
	public void ProcessInput(){
		switch (gameStatus) {
			case TGAME:{
				if (IsDown(Config.Input_Up)&&player.y<600) player.y+=5;
				if (IsDown(Config.Input_Down)&&player.y>0) player.y-=5;
				if (IsDown(Config.Input_Right)&&player.x<600) player.x+=5;
				if (IsDown(Config.Input_Left)&&player.x>0) player.x-=5;
				if (IsDown(Config.Input_Ok)&&c1<=0) {shoot(player.x,player.y,player.a,10,10,true,"lanqiu");c1=4;}
				break;
			}
		}
	}
	int c1,CTick;
	@Override
	public void render () {
		int t1=RMbody.size();
		for (int i = 0 ;i<t1;i++) {
			if (i>RMbody.size()-1) break;
			if (RMbody.get(i)==null) continue;
			bodyMap.remove(getKeyByValue(bodyMap,RMbody.get(i)));
			RMbody.remove(i);
		}
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
				batch.draw(getTex("bgimg"), 0, 0, WindowW, WindowH);

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
				batch.draw(getTex("bgimg"), 0, 0, WindowW, WindowH);
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
				batch.draw(getTex("bgimg"), 0, 0, WindowW, WindowH);
				batch.end();

				renderer.begin(ShapeRenderer.ShapeType.Line);
				gl.glLineWidth(5);
				renderer.setColor(convertArgbToLibGdxColor(0xd8c6ab,1));
				renderer.line(0, 530, 600, 530);
				renderer.end();
				gl.glLineWidth(1);
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
				batch.draw(getTex("lanqiu"),65,475-(count*50),30,30);
				drawText(I18n.get("option.language.zh_cn"),300,400,(Objects.equals(Config.Language, "zh_cn") ?Color.YELLOW:Color.GRAY),1);
				drawText(I18n.get("option.language.en_us"),450,400,(Objects.equals(Config.Language, "en_us") ?Color.YELLOW:Color.GRAY),1);
				drawText("60",400,350,(Config.FPS==60 ?Color.YELLOW:Color.GRAY),1);
				drawText("30",450,350,(Config.FPS==30 ?Color.YELLOW:Color.GRAY),1);
				break;
			}
			case TGAME:{
				player.update();
				gl.glClearColor(0,0,0,1);
				gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				ctrl.exec(CTick);
				batch.draw(getTex("sod3r"),-20,integerList.get(0),640,300);
				batch.draw(getTex("sod3r"),-20,integerList.get(1),640,300);
				batch.draw(getTex("sod3r"),-20,integerList.get(2),640,300);

				bodyMap.get("pdd").setTransform(player.x,player.y,player.a);
				player.draw(batch);
				batch.end();
				renderer.begin(ShapeRenderer.ShapeType.Filled);
				renderer.circle(player.x,player.y,5);
				renderer.end();
				batch.begin();
				if (c1>0) c1--;
				int len = activeBullets.size();
				for (int i = len; --i >= 0;) {
					Bullet item = activeBullets.get(i);
					if (!item.alive) {
						activeBullets.remove(i);
						bulletPool.free(item);
					}
				}
				len = activeEnemy.size();
				for (int i = len; --i >= 0;) {
					Enemy item = activeEnemy.get(i);
					if (!item.alive) {
						activeEnemy.remove(i);
						enemyPool.free(item);
						bodyMap.remove(item.name);
					}
				}
				len = activeEnemy.size();
				for (int i = len; --i >= 0;) {
					Enemy item = activeEnemy.get(i);
					if (!item.alive) {
						activeEnemy.remove(i);
						enemyPool.free(item);
					}
				}
				for (Enemy enemy : activeEnemy){
					String name = enemy.name;
					bodyMap.get(name).setTransform(enemy.x,enemy.y,enemy.a);
					enemy.draw(batch);
				}
				for (Bullet bullet : activeBullets){
					String name = bullet.name;
					bodyMap.get(name).setTransform(bullet.x,bullet.y,bullet.a);
					batch.draw(getTex(bullet.tex),bullet.x-bullet.size,bullet.y-bullet.size,bullet.size*2,bullet.size*2);
				}
				drawText("Players:",10,580,Color.WHITE,0.9f);
				for (int i = 0; i < player.lives; i++) {
					batch.draw(getTex("ji_icon"),100+30*i,560,20,20);
				}
				break;
			}
			case Changing:{
				if (textureMap.containsKey("change")&&getTex("change")!=null) {
					batch.setColor(((float) 70-count) /70, ((float) 70-count) /70, ((float) 70-count) /70, 1);
					Sprite sprite = new Sprite(getTex("change"));
					sprite.flip(false, true);
					batch.draw(sprite, 0, 0, WindowW, WindowH);
					count++;
				}
				break;
			}
		}
		Array<Body> t = new Array<>();
		world.getBodies(t);
		for (int i = 0;i<t.size;i++){
			if (!bodyMap.containsValue(t.get(i))) {
				world.destroyBody(t.get(i));
			}
		}
		checkbgm(gameStatus);
		batch.setColor(Color.WHITE);
		drawText(String.valueOf(Gdx.graphics.getFramesPerSecond()),570,20,Color.WHITE,0.8f);
		batch.end();
		debugRenderer.render(world,camera.combined);
		world.step(1/60f, 6, 2);
		CTick++;
	}
	public void over() {
		System.out.println("over");
	}
	@Override
	public void update() {

	}
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Map.Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
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
		System.exit(0);
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
	public float elapsed;
	private com.badlogic.gdx.graphics.Color convertArgbToLibGdxColor(int rgbColor,float a) {
		com.badlogic.gdx.graphics.Color color = new com.badlogic.gdx.graphics.Color();
		com.badlogic.gdx.graphics.Color.argb8888ToColor(color, rgbColor);
		color.a=a;
		return color;
	}

	public void delEnemy(String name) {
		for (Enemy e:activeEnemy)
			if (Objects.equals(e.name, "ENEMY-"+name))
				e.alive=false;
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
				try {
					for (Bullet bullet : activeBullets) {
						if (!bullet.alive) continue;
						String name = bullet.name;
						float angle = bullet.a; // Body angle in radians.

						float velX = MathUtils.cos(angle) * bullet.speed; // X-component.
						float velY = MathUtils.sin(angle) * bullet.speed; // Y-component.
						bullet.x += velX;
						bullet.y += velY;
						if (bullet.x > 600 || bullet.y > 600 || bullet.x < 0 || bullet.y < 0) {
							bullet.alive = false;
							RMbody.add(bodyMap.get(name));
						}

					}
				}catch (ConcurrentModificationException ignored){}
				try {
					sleep(1000/Config.FPS);
				} catch (InterruptedException ignored) {}
			}
		}
	}
	public void shoot(float x, float y, float a, float speed, float size, boolean player,String tex){
		Bullet item = bulletPool.obtain();
		a= (float) Math.toRadians(a);
		int i=0;
		for (Bullet bullet:activeBullets){
			i=Math.max(i,Integer.parseInt(bullet.name.substring(7)));
		}
		i++;
		item.init(speed,x,y,a,size,player,"BULLET-"+i,tex);
		createCObject(size,item.name,x,y);
		activeBullets.add(item);
	}
	public void addEnemy(float x, float y, float a, float speed, float size, String name, String type, int hp){
		Enemy item = enemyPool.obtain();
		a= (float) Math.toRadians(a);
		item.init(speed,x,y,a,size,"ENEMY-"+name,type,hp);
		createCObject(size,item.name,x,y);
		activeEnemy.add(item);
	}
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
	public Texture getTex(String name){
		if (textureMap.containsKey(name))
			return  textureMap.get(name);
		return textureMap.get("missing");
	}
}
