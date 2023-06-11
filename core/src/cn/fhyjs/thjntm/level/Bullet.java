package cn.fhyjs.thjntm.level;

public class Bullet {
    public float speed, x, y, a,size;
    public boolean alive;
    public String name,tex;
    public boolean player;

    public Bullet(){alive=true;}
    public void init(float speed, float x, float y, float a, float size,boolean player, String name,String tex){
        this.speed=speed;
        this.x=x;
        this.y=y;
        this.a=a;
        this.size=size;
        this.name=name;
        this.alive=true;
        this.player=player;
        this.tex=tex;
    }
}
