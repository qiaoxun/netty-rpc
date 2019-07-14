package com.qiao.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ServiceDiscovery {
    private ZooKeeper zooKeeper;

    @Value("${registry.address}")
    private String registryAddress;

    private int sessionTimeOut = 2000;

    private String rootNode = "/rpc";

    public void connect() throws IOException, KeeperException, InterruptedException {
        zooKeeper = new ZooKeeper(registryAddress, sessionTimeOut, (watchedEvent) -> {
            System.out.println(watchedEvent.getPath());
        });
    }

    public String discoveryRegistedAddress() throws KeeperException, InterruptedException {
        if (zooKeeper != null) {
            List<String> rpcServerNodeList = zooKeeper.getChildren(rootNode, false);
            if (null != rpcServerNodeList || rpcServerNodeList.size() == 0) {
                String nodeName = "";
                if (rpcServerNodeList.size() == 1) {
                    nodeName = rpcServerNodeList.get(0);
                } else {
                    ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
                    int which = threadLocalRandom.nextInt(rpcServerNodeList.size());
                    nodeName = rpcServerNodeList.get(which);
                }
                byte[] bytes = zooKeeper.getData(rootNode + "/" + nodeName, false, null);
                String data = new String(bytes);
                return data;
            } else {
                throw new RuntimeException("No server detected!");
            }
        } else {
            throw new RuntimeException("ZooKeeper is not home!!");
        }
    }

}
