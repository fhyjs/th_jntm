package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.enums.KeyAct;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

public class ThInputProcessor implements InputProcessor {
    ApplicationListener applicationListener = Gdx.app.getApplicationListener();
    public boolean keyDown (int keycode) {
        ((ThGame)applicationListener).keyMap.put(keycode,true);
        ((ThGame)applicationListener).ProcessInput(keycode,KeyAct.Down);
        return false;
    }

    public boolean keyUp (int keycode) {
        ((ThGame)applicationListener).keyMap.put(keycode,false);
        ((ThGame)applicationListener).ProcessInput(keycode,KeyAct.UP);
        return false;
    }

    public boolean keyTyped (char character) {
        return false;
    }

    public boolean touchDown (int x, int y, int pointer, int button) {
        return false;
    }

    public boolean touchUp (int x, int y, int pointer, int button) {
        return false;
    }

    public boolean touchDragged (int x, int y, int pointer) {
        return false;
    }

    public boolean mouseMoved (int x, int y) {
        return false;
    }

    public boolean scrolled (float amountX, float amountY) {
        return false;
    }
}

