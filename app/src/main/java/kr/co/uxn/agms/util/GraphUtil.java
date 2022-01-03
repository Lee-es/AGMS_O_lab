package kr.co.uxn.agms.util;


public class GraphUtil {

    private    int graph_Current_MIN = -20;
    private    int graph_Current_MAX = 100;
    private    long graph_TargetTime=300000L;;

    private  static   GraphUtil instance;

    public static   GraphUtil getInstance(){

        if(instance==null)
        {
            synchronized (  GraphUtil.class){

                    instance=new GraphUtil();
            }
        }
        return instance;
    }

    public int getGraph_Current_MIN(){
        return this.graph_Current_MIN;
    }

    public int getGraph_Current_MAX(){
        return this.graph_Current_MAX;
    }
    public long getGraph_TargetTime(){return this.graph_TargetTime;}

    public void setGraph_Current_MIN(int min){
        this.graph_Current_MIN=min;
    }

    public void setGraph_Current_MAX(int max){
        this.graph_Current_MAX=max;
    }
    public void setGraph_TargetTime(int time){this.graph_TargetTime=time*1000L;}

}

