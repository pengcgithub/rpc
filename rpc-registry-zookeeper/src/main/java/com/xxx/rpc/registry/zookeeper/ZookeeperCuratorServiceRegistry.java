package com.xxx.rpc.registry.zookeeper;

import com.xxx.rpc.registry.ServiceRegistry;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 ZooKeeper Curator 的服务注册接口实现<br/>
 *
 * @author pengc
 * @version v3.1.0
 */
public class ZookeeperCuratorServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private final CuratorFramework zkClient;

    public ZookeeperCuratorServiceRegistry(String zkAddress) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        zkClient = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
        zkClient.start();
        LOGGER.debug("connect zookeeper");

    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        // 创建 registry 节点（持久）
        String registryPath = Constant.ZK_REGISTRY_PATH;

        if (null == zkClient) {
            throw new RuntimeException("zookeeper connect fail");
        }

        try {
            // 创建持久节点
            String servicePath = registryPath + "/" + serviceName;
            Stat stat = zkClient.checkExists().forPath(servicePath);
            if (null == stat) {
                String serviceNode = zkClient.create().forPath(registryPath+"/"+serviceName);
                LOGGER.debug("create address node: {}", serviceNode);
            }

            // 创建临时节点
            String addressPath = servicePath + "/address-";
            String addressNode = zkClient.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(addressPath, serviceAddress.getBytes());
            LOGGER.debug("create address node: {}", addressNode);
        } catch (Exception e) {
            LOGGER.error("create address node error: {}", e.getMessage());
            e.printStackTrace();
        }

    }
}
