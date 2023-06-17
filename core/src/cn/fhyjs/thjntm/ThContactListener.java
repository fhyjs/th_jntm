package cn.fhyjs.thjntm;

import cn.fhyjs.thjntm.level.Bullet;
import cn.fhyjs.thjntm.level.Enemy;
import cn.fhyjs.thjntm.util.StrU;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;

import java.lang.reflect.Array;
import java.util.*;

public class ThContactListener implements ContactListener {
    ThGame game;
    public ThContactListener(){
        super();
        game=((ThGame) Gdx.app.getApplicationListener());
    }
    @Override
    public void beginContact(Contact contact) {
        Map<String, Body> nameM = new HashMap<>();
        List<Bullet> activeBullets = ((ThGame) (Gdx.app.getApplicationListener())).activeBullets;
        nameM.put(ThGame.getKeyByValue(((ThGame) (Gdx.app.getApplicationListener())).bodyMap, contact.getFixtureA().getBody()), contact.getFixtureA().getBody());
        nameM.put(ThGame.getKeyByValue(((ThGame) (Gdx.app.getApplicationListener())).bodyMap, contact.getFixtureB().getBody()), contact.getFixtureA().getBody());
        boolean player = nameM.containsKey("pdd");
        if (player) {
            short playerpos = (short) ((nameM.keySet().toArray()[0]).equals("pdd") ? 0 : 1);
            for (String n : nameM.keySet()) {
                if (n.contains("BULLET-")) {
                    Bullet a = null;
                    for (Bullet d : activeBullets) if (d.name.equals(n)) a = d;
                    if (a == null) continue;
                    Body b = ((ThGame) Gdx.app.getApplicationListener()).bodyMap.get(a.name);
                    if (!a.player) {
                        ((ThGame) Gdx.app.getApplicationListener()).player.die();
                        a.alive = false;
                        ((ThGame) Gdx.app.getApplicationListener()).RMbody.add(b);
                    }
                }
            }
        }else{
            if (StrU.IsMapKeyHas("ENEMY-",nameM)&&StrU.IsMapKeyHas("BULLET-",nameM)) {
                Bullet a = null;
                Enemy e = null;
                if (((String) nameM.keySet().toArray()[0]).contains("BULLET-")) {
                    String t1 = (String) nameM.keySet().toArray()[0];
                    String t2 = (String) nameM.keySet().toArray()[1];
                    for (Bullet d : activeBullets) if (d.name.equals(t1)) a = d;
                    for (Enemy d : game.activeEnemy) if (d.name.equals(t2)) e = d;
                } else {
                    String t1 = (String) nameM.keySet().toArray()[1];
                    String t2 = (String) nameM.keySet().toArray()[0];
                    for (Bullet d : activeBullets) if (d.name.equals(t1)) a = d;
                    for (Enemy d : game.activeEnemy) if (d.name.equals(t2)) e = d;
                }
                Body b = ((ThGame) Gdx.app.getApplicationListener()).bodyMap.get(a.name);
                if (a.player) {
                    e.hurt(2);
                    a.alive = false;
                    ((ThGame) Gdx.app.getApplicationListener()).RMbody.add(b);
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
