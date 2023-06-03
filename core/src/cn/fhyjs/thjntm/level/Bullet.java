package cn.fhyjs.thjntm.level;

public class Bullet {
    public float speed, x, y, a,size;
    public String name;
    public Bullet(float speed, float x, float y, float a, float size, String name){
        this.speed=speed;
        this.x=x;
        this.y=y;
        this.a=a;
        this.size=size;
        this.name=name;
    }
}
