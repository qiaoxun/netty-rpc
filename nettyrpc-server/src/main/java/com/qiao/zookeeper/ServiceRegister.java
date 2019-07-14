package com.qiao.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ServiceRegister {
    private ZooKeeper zooKeeper;

    @Value("${registry.address}")
    private String registryAddress;

    private int sessionTimeOut = 2000;

    private String rootNode = "/rpc";

    public void connect() throws IOException, KeeperException, InterruptedException {
        zooKeeper = new ZooKeeper(registryAddress, sessionTimeOut, (watchedEvent) -> {
            System.out.println(watchedEvent.getPath());
        });

        List<String> nodeList = zooKeeper.getChildren("/", false);
        if (!nodeList.contains(rootNode.replace("/", ""))) {
            registerRootNode();
        }

    }

    private void registerRootNode() throws KeeperException, InterruptedException {
        if (zooKeeper != null) {
            zooKeeper.create(rootNode, "rpc".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    public void registerNode(String data) throws KeeperException, InterruptedException, IOException {
        if (zooKeeper != null) {
            zooKeeper.create(rootNode + "/provider" , data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } else {
            connect();
            registerNode(data);
        }
    }

}
