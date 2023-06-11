package cn.fhyjs.thjntm.level;

import cn.fhyjs.thjntm.Config;
import cn.fhyjs.thjntm.ThGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player{
    ThGame game;
    boolean dying;
    public float x,y,a;
    public int lives;
    public Player(float x,float y,float a,int lives){
        game=((ThGame) Gdx.app.getApplicationListener());
        this.x=x;
        this.y=y;
        this.a=a;
        this.lives=lives;
        dying=false;
    }
    public void update() {

    }
    public void die() {
        if (dying) return;
        if (lives-1<0){
            game.over();
        }
        new Die().start();
    }

    public void draw(SpriteBatch batch) {
        if (!dying)
            batch.draw(game.animationMap.get("tsk").getKeyFrame(game.elapsed),this.x-45,this.y-45,100,100);
        else
            batch.draw(game.animationMap.get("boom").getKeyFrame(game.elapsed),this.x-45,this.y-45,100,100);
    }
    private class Die extends Thread {
        public Die() {
            super();
            dying=true;
            game.elapsed=0;
        }
        @Override
        public void run() {
            try {
                sleep(1500);
            } catch (InterruptedException ignored) { }
            dying=false;
            lives--;
        }
    }
}
