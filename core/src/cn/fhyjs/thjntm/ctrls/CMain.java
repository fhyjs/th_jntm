package cn.fhyjs.thjntm.ctrls;

import cn.fhyjs.thjntm.Config;
import cn.fhyjs.thjntm.ThGame;
import cn.fhyjs.thjntm.level.Enemy;
import cn.fhyjs.thjntm.resources.FileManager;
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
                        parm.get("name"),
                        parm.get("enemy"),
                        Integer.parseInt(parm.get("hp"))
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
                    case "square":{
                        for (float i = enemy.x-Float.parseFloat(parm.get("len")); i <= enemy.x+Float.parseFloat(parm.get("len")); i+=Float.parseFloat(parm.get("ivl"))) {
                            game.shoot(i,enemy.y+Float.parseFloat(parm.get("len"))/2,90,Float.parseFloat(parm.get("speed")),Float.parseFloat(parm.get("size")),false,parm.get("tex"));
                        }
                        for (float i = enemy.x-Float.parseFloat(parm.get("len")); i <= enemy.x+Float.parseFloat(parm.get("len")); i+=Float.parseFloat(parm.get("ivl"))) {
                            game.shoot(i,enemy.y-Float.parseFloat(parm.get("len"))/2,-90,Float.parseFloat(parm.get("speed")),Float.parseFloat(parm.get("size")),false,parm.get("tex"));
                        }
                        for (float i = enemy.y-Float.parseFloat(parm.get("len")); i <= enemy.y+Float.parseFloat(parm.get("len")); i+=Float.parseFloat(parm.get("ivl"))) {
                            game.shoot(enemy.x+Float.parseFloat(parm.get("len"))/2,i,0,Float.parseFloat(parm.get("speed")),Float.parseFloat(parm.get("size")),false,parm.get("tex"));
                        }
                        for (float i = enemy.y-Float.parseFloat(parm.get("len")); i <= enemy.y+Float.parseFloat(parm.get("len")); i+=Float.parseFloat(parm.get("ivl"))) {
                            game.shoot(enemy.x-Float.parseFloat(parm.get("len"))/2,i,180,Float.parseFloat(parm.get("speed")),Float.parseFloat(parm.get("size")),false,parm.get("tex"));
                        }
                        break;
                    }
                }
                break;
            }
            case "ME":{
                Enemy enemy=getEnemy(parm.get("name"));
                enemy.a= Float.parseFloat(parm.get("a"));
                new Me(enemy,Float.parseFloat(parm.get("x")),Float.parseFloat(parm.get("y")),Float.parseFloat(parm.get("speed"))).start();
                break;
            }
        }
        return true;
    }
    private class Me extends Thread{
        Enemy enemy;float x;float y;float s;
        boolean xA,yA,xF,yF;
        public Me(Enemy enemy,float x,float y,float s){
            super();
            this.enemy=enemy;this.x=x;this.y=y;this.s=s;
            xA=enemy.x<x;
            yA=enemy.y<y;
        }
        @Override
        public void run() {
            while (!(xF&&yF)){
                if (xA&&!xF){
                    enemy.x+=s;
                    xF=enemy.x>=x;
                }
                if (!xA&&!xF){
                    enemy.x-=s;
                    xF=enemy.x<=x;
                }
                if (yA&&!yF){
                    enemy.y+=s;
                    yF=enemy.y>=y;
                }
                if (!yA&&!yF){
                    enemy.y-=s;
                    yF=enemy.y<=y;
                }
                try {
                    sleep(1000/ Config.FPS);
                } catch (InterruptedException ignored) {}
            }
        }
    }
    private Enemy getEnemy(String name){
        for (Enemy enemy : game.activeEnemy) {
            if (enemy.name.equals("ENEMY-"+name))
                return enemy;
        }
        return null;
    }
}
