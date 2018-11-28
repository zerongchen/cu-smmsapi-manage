package com.aotain.smmsapi.task;

import com.aotain.common.utils.tools.Random;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Master implements Watcher{

    ZooKeeper zk;
    String port;
    String serverId= Random.getRandomString();
    static boolean isLeader=false;

    Master(String port){
        this.port=port;
    }

    void startZk(){
        try {
            zk=new ZooKeeper(port,15000,this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stopZk(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run4Master(){
        /**
        while (true){
            try {
                zk.create("/master1",serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                isLeader=true;
                break;
            }catch (KeeperException.NodeExistsException e){
                isLeader=false;
                break;
            }catch (KeeperException ex){

            }
            if (checkMaster()) break;
        }*/

        zk.create("/master1", serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
            @Override
            public void processResult( int rc, String path, Object ctx, String name ) {
                switch (KeeperException.Code.get(rc)){
                    case OK:
                        isLeader=true;
                        break;
                    case CONNECTIONLOSS:
                        checkMaster();
                        return;
                    default:
                        isLeader=false;
                }
                System.out.println("is leader "+isLeader);
            }
        },null);

    }

    private void checkMaster() {
        /**
        while (true){
            try {
                Stat state = new Stat();
                byte[] data=zk.getData("/master1",false,state);
                isLeader = new String (data).equals(serverId);
                return isLeader;
            }catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }*/
        zk.getData("/master1", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult( int rc, String path, Object ctx, byte[] data, Stat stat ) {
                switch (KeeperException.Code.get(rc)){
                    case CONNECTIONLOSS:
                        checkMaster();
                        return;
                    case NONODE:
                        run4Master();
                        return;
                }
            }
        },null);
    }

    @Override
    public void process( WatchedEvent event ) {
        System.out.println("================"+event);
    }

    public static void main(String[] args) throws InterruptedException {
//        Master m =new Master("192.168.50.202:2181");
//        m.startZk();
//        m.run4Master();
//
//        if (isLeader){
//            System.out.println("I am leader");
//        }else {
//            System.out.println("I am not leader");
//
//        }
//        Thread.sleep(10000);
//        m.stopZk();

        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(2);
        list.add(1);
        Iterator<Integer> iterator = list.iterator();
        while(iterator.hasNext()){
            Integer integer = iterator.next();
            if(integer==2)
                iterator.remove();   //注意这个地方
        }
        System.out.println(list);
    }


}
