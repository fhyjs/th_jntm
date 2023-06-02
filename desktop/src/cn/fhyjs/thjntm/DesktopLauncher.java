package cn.fhyjs.thjntm;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import cn.fhyjs.thjntm.ThGame;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("libgdx_th_jtnm");
		config.setForegroundFPS(60);
		config.setWindowedMode(600,600);
		new Lwjgl3Application(new ThGame(), config);
	}
}
