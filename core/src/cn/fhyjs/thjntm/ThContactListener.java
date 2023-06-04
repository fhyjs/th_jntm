package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.level.Bullet;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;

import java.lang.reflect.Array;
import java.util.*;

public class ThContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Map<String, Body> nameM = new HashMap<>();
        List<Bullet> activeBullets = ((ThGame) (Gdx.app.getApplicationListener())).activeBullets;
        nameM.put(ThGame.getKeyByValue(((ThGame) (Gdx.app.getApplicationListener())).bodyMap, contact.getFixtureA().getBody()),contact.getFixtureA().getBody());
        nameM.put(ThGame.getKeyByValue(((ThGame) (Gdx.app.getApplicationListener())).bodyMap, contact.getFixtureB().getBody()),contact.getFixtureA().getBody());
        boolean player = nameM.containsKey("pdd");
        if (player){
            short playerpos= (short) ((nameM.keySet().toArray()[0]).equals("pdd")?0:1);
            for (String n:nameM.keySet()){
                if (n.contains("BULLET-")){
                    Body b= (Body) nameM.values().toArray()[1-playerpos];
                    Bullet a = null;
                    for (Bullet d:activeBullets) if (d.name.equals(n)) a=d;
                    if (!a.player) ((ThGame) Gdx.app.getApplicationListener()).player.die();
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
