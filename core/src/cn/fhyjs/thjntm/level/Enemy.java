package cn.fhyjs.thjntm.level;

public class Enemy {
    public float speed, x, y, a,size;
    public boolean alive;
    public String name;
    public Enemy(){alive=true;}
    public void init(float speed, float x, float y, float a, float size, String name){
        this.speed=speed;
        this.x=x;
        this.y=y;
        this.a=a;
        this.size=size;
        this.name=name;
        this.alive=true;
    }
}
