package bgu.spl.mics;

public class MicroPair<E,B> {
    private E first;
    private B second;
    public MicroPair(E f,B s){
        first=f;
        second=s;
    }
    public E first(){return first;}
    public B second(){return second;}
    public void setFirst(E change){first=change;}
    public void setSecond(B change){second=change;}
}
