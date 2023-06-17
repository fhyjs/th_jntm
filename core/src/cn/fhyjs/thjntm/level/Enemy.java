package cn.fhyjs.thjntm.level;

import cn.fhyjs.thjntm.ThGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy {
    ThGame game;
    boolean dying=false;
    public float speed, x, y, a,size;
    public int hp;
    public boolean alive;
    public String name,type;
    public Enemy(){alive=true;}
    public void init(float speed, float x, float y, float a, float size, String name,String type,int hp){
        this.speed=speed;
        this.x=x;
        this.y=y;
        this.a=a;
        this.size=size;
        this.type=type;
        this.name=name;
        this.alive=true;
        this.hp=hp;
        game= ((ThGame) Gdx.app.getApplicationListener());
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
        }
    }
    public void draw(SpriteBatch batch) {
        if (!dying)
            batch.draw(game.animationMap.get(type).getKeyFrame(game.elapsed),this.x-45,this.y-45,100,100);
        else
            batch.draw(game.animationMap.get("boom").getKeyFrame(game.elapsed),this.x-45,this.y-45,100,100);
    }
    public void hurt(int d){
        hp-=d;
        if (hp<=0){
            if (dying) return;
            new Die().start();
            game.delEnemy(name);
        }
    }
}
