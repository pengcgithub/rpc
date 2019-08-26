package com.xxx.rpc.registry.zookeeper;

import com.xxx.rpc.registry.ServiceDiscovery;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于 ZooKeeper 的服务发现接口实现<br/>
 *
 * @author pengc
 * @version v3.1.0
 */
public class ZookeeperCuratorServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private final CuratorFramework zkClient;

    public ZookeeperCuratorServiceDiscovery(String zkAddress) {
        zkClient = CuratorFrameworkFactory
                .builder()
                .connectString(zkAddress)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        zkClient.start();
        LOGGER.debug("zookeeper connect success");
    }

    @Override
    public String discover(String serviceName) {
        // 创建 registry 节点（持久）
        String registryPath = Constant.ZK_REGISTRY_PATH;
        try {
            String servicePath = registryPath + "/" + serviceName;
            Stat stat = zkClient.checkExists().forPath(servicePath);
            if (null == stat) {
                throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
            }

            List<String> addressList = zkClient.getChildren().forPath(servicePath);
            if (addressList.isEmpty()) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }

            // 获取address节点
            String address;
            int size = addressList.size();
            if (1 == size) {
                address = addressList.get(0);
            } else {
                // 获取节点的算法，此处可以拓展算法
                address = addressList.get(ThreadLocalRandom.current().nextInt(size));
            }

            String addressPath = servicePath + "/" + address;
            byte[] bytes = zkClient.getData().forPath(addressPath);

            String addressNote = new String(bytes);
            LOGGER.debug("getData address node: {}", addressNote);
            return addressNote;
        } catch (Exception e) {
            LOGGER.error("getData address node error: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
