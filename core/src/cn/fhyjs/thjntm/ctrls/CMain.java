package cn.fhyjs.thjntm.ctrls;

import cn.fhyjs.thjntm.ThGame;
import cn.fhyjs.thjntm.level.Enemy;
import cn.fhyjs.thjntm.resources.FileManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class CMain {
    Map<Integer,Map<String,String>> cmd=new HashMap<>();
    ThGame game;
    public CMain(String fn){
        game=((ThGame) Gdx.app.getApplicationListener());
        try {
            String[] con = new String(Objects.requireNonNull(FileManager.getByteContent(fn)), StandardCharsets.UTF_8).replaceAll("\r","").split("\n");
            for (String s : con) {
                int tick = -1;
                Map<String,String> parm=new HashMap<>();
                String[] t = s.split(";");
                for (String s1 : t) {
                    String[] t1 = s1.split("=");
                    if (t1[0].equals("tick")){
                        tick= Integer.parseInt(t1[1]);
                    }
                    parm.put(t1[0],t1[1]);
                }
                if (tick==-1){
                    throw new RuntimeException("parm doesn't include \"tick\"");
                }
                cmd.put(tick,parm);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public boolean exec(int tick){
        if (!cmd.containsKey(tick)) return false;
        Map<String,String> parm=cmd.get(tick);
        switch (parm.get("cmd")){
            case "CE":{
                game.addEnemy(
                        Float.parseFloat(parm.get("x")),
                        Float.parseFloat(parm.get("y")),
                        Float.parseFloat(parm.get("a")),
                        Float.parseFloat(parm.get("speed")),
                        Float.parseFloat(parm.get("size")),
                        parm.get("name")
                );
                break;
            }
            case "DE":{
                game.delEnemy(parm.get("name"));
                break;
            }
            case "SD":{
                Enemy enemy=getEnemy(parm.get("shooter"));
                switch (parm.get("type")){
                    case "circle":{
                        for (float i = 0; i < 361; i+=Float.parseFloat(parm.get("angle"))) {
                            game.shoot(enemy.x,enemy.y,i,Float.parseFloat(parm.get("speed")),Float.parseFloat(parm.get("size")),false,parm.get("tex"));
                        }
                        break;
                    }
                }
                break;
            }
        }
        return true;
    }
    private Enemy getEnemy(String name){
        for (Enemy enemy : game.activeEnemy) {
            if (enemy.name.equals("ENEMY-"+name))
                return enemy;
        }
        return null;
    }
}
