package org.august.lock.spring.boot;

import org.august.lock.spring.boot.config.LockConfig;
import org.august.lock.spring.boot.config.LockConfig.ClusterConfig;
import org.august.lock.spring.boot.config.LockConfig.SentinelConfig;
import org.august.lock.spring.boot.config.LockConfig.SingleConfig;
import org.august.lock.spring.boot.constant.LoadBalancerTypeConstant;
import org.august.lock.spring.boot.constant.LockCommonConstant;
import org.august.lock.spring.boot.constant.SubReadModeTypeConstant;
import org.august.lock.spring.boot.core.LockInterceptor;
import org.august.lock.spring.boot.enumeration.ServerPattern;
import org.august.lock.spring.boot.exception.UnknownLoadBalancerException;
import org.august.lock.spring.boot.exception.UnknownReadModeException;
import org.august.lock.spring.boot.exception.UnknownSubscriptionModeException;
import org.august.lock.spring.boot.factory.ServiceBeanFactory;
import org.august.lock.spring.boot.service.impl.*;
import org.august.lock.spring.boot.store.MapStore;
import org.august.lock.spring.boot.util.SpringUtil;
import org.august.lock.spring.boot.util.ValidateUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;
import org.redisson.connection.balancer.LoadBalancer;
import org.redisson.connection.balancer.RandomLoadBalancer;
import org.redisson.connection.balancer.RoundRobinLoadBalancer;
import org.redisson.connection.balancer.WeightedRoundRobinBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动装配
 *
 * @author TanRq
 */
@Configuration
@EnableConfigurationProperties(LockConfig.class)
@Import({LockInterceptor.class})
public class LockAutoConfiguration {

    @Autowired
    private LockConfig lockConfig;

    /**
     * 创建redisSonClient
     *
     * @return RedissonClient
     * @throws URISyntaxException URISyntaxException
     */
    @Bean(name = "lockRedissonClient", destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient() throws URISyntaxException {
        Config config = new Config();
        ServerPattern serverPattern = MapStore.getServerPattern(lockConfig.getPattern());

        if (serverPattern == ServerPattern.SINGLE) {
            SingleServerConfig singleConfig = config.useSingleServer();
            initSingleConfig(singleConfig);
        }
        if (serverPattern == ServerPattern.CLUSTER) {
            ClusterServersConfig clusterConfig = config.useClusterServers();
            initClusterConfig(clusterConfig);
        }
        if (serverPattern == ServerPattern.MASTER_SLAVE) {
            MasterSlaveServersConfig masterSlaveConfig = config.useMasterSlaveServers();
            initMasterSlaveConfig(masterSlaveConfig);
        }
        if (serverPattern == ServerPattern.REPLICATED) {
            ReplicatedServersConfig replicatedServersConfig = config.useReplicatedServers();
            initReplicatedServersConfig(replicatedServersConfig);
        }
        if (serverPattern == ServerPattern.SENTINEL) {
            SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
            initSentinelServersConfig(sentinelServersConfig);
        }
        return Redisson.create(config);
    }

    @Bean
    public ServiceBeanFactory serviceBeanFactory() {
        return new ServiceBeanFactory();
    }

    @Bean
    public SpringUtil springUtil() {
        return new SpringUtil();
    }

    @Bean
    @Scope("prototype")
    public ReentrantLockServiceImpl reentrantLockServiceImpl() {
        return new ReentrantLockServiceImpl();
    }

    @Bean
    @Scope("prototype")
    public FairLockServiceImpl fairLockServiceImpl() {
        return new FairLockServiceImpl();
    }

    @Bean
    @Scope("prototype")
    public MultiLockServiceImpl multiLockServiceImpl() {
        return new MultiLockServiceImpl();
    }

    @Bean
    @Scope("prototype")
    public RedLockServiceImpl redLockServiceImpl() {
        return new RedLockServiceImpl();
    }

    @Bean
    @Scope("prototype")
    public ReadLockServiceImpl readLockServiceImpl() {
        return new ReadLockServiceImpl();
    }

    @Bean
    @Scope("prototype")
    public WriteLockServiceImpl writeLockServiceImpl() {
        return new WriteLockServiceImpl();
    }

    /**
     * 初始化单机模式参数
     *
     * @param singleConfig 单机模式配置
     * @throws URISyntaxException URISyntaxException
     */
    private void initSingleConfig(SingleServerConfig singleConfig) throws URISyntaxException {
        SingleConfig singleServerConfig = lockConfig.getSingleServer();
        singleConfig.setAddress(String.format("%s%s%s%s", LockCommonConstant.REDIS_URL_PREFIX, singleServerConfig.getAddress(), LockCommonConstant.COLON, singleServerConfig.getPort()));
        singleConfig.setClientName(lockConfig.getClientName());
        singleConfig.setConnectionMinimumIdleSize(singleServerConfig.getConnMinIdleSize());
        singleConfig.setConnectionPoolSize(singleServerConfig.getConnPoolSize());
        singleConfig.setConnectTimeout(singleServerConfig.getConnTimeout());
        singleConfig.setDatabase(singleServerConfig.getDatabase());
        singleConfig.setDnsMonitoringInterval(singleServerConfig.getDnsMonitoringInterval());
        singleConfig.setIdleConnectionTimeout(singleServerConfig.getIdleConnTimeout());
        singleConfig.setKeepAlive(singleServerConfig.isKeepAlive());
        singleConfig.setPassword(singleServerConfig.getPassword());
        singleConfig.setRetryAttempts(singleServerConfig.getRetryAttempts());
        singleConfig.setRetryInterval(singleServerConfig.getRetryInterval());
        singleConfig.setSslEnableEndpointIdentification(lockConfig.isSslEnableEndpointIdentification());
        if (lockConfig.getSslKeystore() != null) {
            singleConfig.setSslKeystore(new URI(lockConfig.getSslKeystore()));
        }
        if (lockConfig.getSslKeystorePassword() != null) {
            singleConfig.setSslKeystorePassword(lockConfig.getSslKeystorePassword());
        }
        singleConfig.setSslProvider(LockCommonConstant.JDK.equalsIgnoreCase(lockConfig.getSslProvider()) ? SslProvider.JDK : SslProvider.OPENSSL);
    }

    /**
     * 初始化集群模式参数
     *
     * @param clusterServerConfig 集群模式配置
     */
    private void initClusterConfig(ClusterServersConfig clusterServerConfig) {
        ClusterConfig clusterConfig = lockConfig.getClusterServer();
        String[] addressArr = clusterConfig.getNodeAddresses().split(LockCommonConstant.COMMA);
        Arrays.asList(addressArr).forEach(address ->
                clusterServerConfig.addNodeAddress(String.format("%s%s", LockCommonConstant.REDIS_URL_PREFIX, address))
        );
        clusterServerConfig.setScanInterval(clusterConfig.getScanInterval());

        ReadMode readMode = getReadMode(clusterConfig.getReadMode());
        ValidateUtil.notNull(readMode, UnknownReadModeException.class, "未知读取操作的负载均衡模式类型");
        clusterServerConfig.setReadMode(readMode);

        SubscriptionMode subscriptionMode = getSubscriptionMode(clusterConfig.getSubMode());
        ValidateUtil.notNull(subscriptionMode, UnknownSubscriptionModeException.class, "未知订阅操作的负载均衡模式类型");
        clusterServerConfig.setSubscriptionMode(subscriptionMode);

        LoadBalancer loadBalancer = getLoadBalancer(clusterConfig.getLoadBalancer(), clusterConfig.getWeightMaps(), clusterConfig.getDefaultWeight());
        ValidateUtil.notNull(loadBalancer, UnknownLoadBalancerException.class, "未知负载均衡算法类型");
        clusterServerConfig.setLoadBalancer(loadBalancer);

        clusterServerConfig.setSubscriptionConnectionMinimumIdleSize(clusterConfig.getSubConnMinIdleSize());
        clusterServerConfig.setSubscriptionConnectionPoolSize(clusterConfig.getSubConnPoolSize());
        clusterServerConfig.setSlaveConnectionMinimumIdleSize(clusterConfig.getSlaveConnMinIdleSize());
        clusterServerConfig.setSlaveConnectionPoolSize(clusterConfig.getSlaveConnPoolSize());
        clusterServerConfig.setMasterConnectionMinimumIdleSize(clusterConfig.getMasterConnMinIdleSize());
        clusterServerConfig.setMasterConnectionPoolSize(clusterConfig.getMasterConnPoolSize());
        clusterServerConfig.setIdleConnectionTimeout(clusterConfig.getIdleConnTimeout());
        clusterServerConfig.setConnectTimeout(clusterConfig.getConnTimeout());
        clusterServerConfig.setTimeout(clusterConfig.getTimeout());
        clusterServerConfig.setRetryAttempts(clusterConfig.getRetryAttempts());
        clusterServerConfig.setRetryInterval(clusterConfig.getRetryInterval());
        clusterServerConfig.setPassword(clusterConfig.getPassword());
        clusterServerConfig.setSubscriptionsPerConnection(clusterConfig.getSubPerConn());
        clusterServerConfig.setClientName(lockConfig.getClientName());
    }

    /**
     * 初始化哨兵模式参数
     *
     * @param sentinelServersConfig 哨兵模式配置
     * @throws URISyntaxException URISyntaxException
     */
    private void initSentinelServersConfig(SentinelServersConfig sentinelServersConfig) throws URISyntaxException {
        SentinelConfig sentinelConfig = lockConfig.getSentinelServer();
        String[] addressArr = sentinelConfig.getSentinelAddresses().split(LockCommonConstant.COMMA);
        Arrays.asList(addressArr).forEach(address ->
                sentinelServersConfig.addSentinelAddress(String.format("%s%s", LockCommonConstant.REDIS_URL_PREFIX, address))
        );

        ReadMode readMode = getReadMode(sentinelConfig.getReadMode());
        ValidateUtil.notNull(readMode, UnknownReadModeException.class, "未知读取操作的负载均衡模式类型");
        sentinelServersConfig.setReadMode(readMode);

        SubscriptionMode subscriptionMode = getSubscriptionMode(sentinelConfig.getSubMode());
        ValidateUtil.notNull(subscriptionMode, UnknownSubscriptionModeException.class, "未知订阅操作的负载均衡模式类型");
        sentinelServersConfig.setSubscriptionMode(subscriptionMode);

        LoadBalancer loadBalancer = getLoadBalancer(sentinelConfig.getLoadBalancer(), sentinelConfig.getWeightMaps(), sentinelConfig.getDefaultWeight());
        ValidateUtil.notNull(loadBalancer, UnknownLoadBalancerException.class, "未知负载均衡算法类型");
        sentinelServersConfig.setLoadBalancer(loadBalancer);

        sentinelServersConfig.setMasterName(sentinelConfig.getMasterName());
        sentinelServersConfig.setDatabase(sentinelConfig.getDatabase());
        sentinelServersConfig.setSlaveConnectionPoolSize(sentinelConfig.getSlaveConnectionPoolSize());
        sentinelServersConfig.setMasterConnectionPoolSize(sentinelConfig.getMasterConnectionPoolSize());
        sentinelServersConfig.setSubscriptionConnectionPoolSize(sentinelConfig.getSubscriptionConnectionPoolSize());
        sentinelServersConfig.setSlaveConnectionMinimumIdleSize(sentinelConfig.getSlaveConnectionMinimumIdleSize());
        sentinelServersConfig.setMasterConnectionMinimumIdleSize(sentinelConfig.getMasterConnectionMinimumIdleSize());
        sentinelServersConfig.setSubscriptionConnectionMinimumIdleSize(sentinelConfig.getSubscriptionConnectionMinimumIdleSize());
        sentinelServersConfig.setDnsMonitoringInterval(sentinelConfig.getDnsMonitoringInterval());
        sentinelServersConfig.setSubscriptionsPerConnection(sentinelConfig.getSubscriptionsPerConnection());
        sentinelServersConfig.setPassword(sentinelConfig.getPassword());
        sentinelServersConfig.setRetryAttempts(sentinelConfig.getRetryAttempts());
        sentinelServersConfig.setRetryInterval(sentinelConfig.getRetryInterval());
        sentinelServersConfig.setTimeout(sentinelConfig.getTimeout());
        sentinelServersConfig.setConnectTimeout(sentinelConfig.getConnectTimeout());
        sentinelServersConfig.setIdleConnectionTimeout(sentinelConfig.getIdleConnectionTimeout());
        sentinelServersConfig.setSslEnableEndpointIdentification(lockConfig.isSslEnableEndpointIdentification());
        sentinelServersConfig.setClientName(lockConfig.getClientName());
        if (lockConfig.getSslKeystore() != null) {
            sentinelServersConfig.setSslKeystore(new URI(lockConfig.getSslKeystore()));
        }
        if (lockConfig.getSslKeystorePassword() != null) {
            sentinelServersConfig.setSslKeystorePassword(lockConfig.getSslKeystorePassword());
        }
        if (lockConfig.getSslTruststore() != null) {
            sentinelServersConfig.setSslTruststore(new URI(lockConfig.getSslTruststore()));
        }
        if (lockConfig.getSslTruststorePassword() != null) {
            sentinelServersConfig.setSslTruststorePassword(lockConfig.getSslTruststorePassword());
        }
        sentinelServersConfig.setSslProvider(LockCommonConstant.JDK.equalsIgnoreCase(lockConfig.getSslProvider()) ? SslProvider.JDK : SslProvider.OPENSSL);
    }

    private void initReplicatedServersConfig(ReplicatedServersConfig replicatedServersConfig) {
        // TODO Auto-generated method stub

    }

    private void initMasterSlaveConfig(MasterSlaveServersConfig masterSlaveConfig) {
        //masterSlaveConfig.setmas
    }

    /**
     * 根据用户的配置类型设置对应的LoadBalancer
     *
     * @param loadBalancerType   负载均衡算法类名
     * @param customerWeightMaps 权重值设置，当负载均衡算法是权重轮询调度算法时该属性有效
     * @param defaultWeight      默认权重值，当负载均衡算法是权重轮询调度算法时该属性有效
     * @return LoadBalancer OR NULL
     */
    private LoadBalancer getLoadBalancer(String loadBalancerType, String customerWeightMaps, int defaultWeight) {
        if (LoadBalancerTypeConstant.RANDOM_LOAD_BALANCER.equals(loadBalancerType)) {
            return new RandomLoadBalancer();
        }
        if (LoadBalancerTypeConstant.ROUND_ROBIN_LOAD_BALANCER.equals(loadBalancerType)) {
            return new RoundRobinLoadBalancer();
        }
        if (LoadBalancerTypeConstant.WEIGHTED_ROUND_ROBIN_BALANCER.equals(loadBalancerType)) {
            Map<String, Integer> weights = new HashMap<>(16);
            String[] weightMaps = customerWeightMaps.split(LockCommonConstant.SEMICOLON);
            Arrays.asList(weightMaps).forEach(weightMap ->
                    weights.put(LockCommonConstant.REDIS_URL_PREFIX + weightMap.split(LockCommonConstant.COMMA)[0], Integer.parseInt(weightMap.split(LockCommonConstant.COMMA)[1]))
            );
            return new WeightedRoundRobinBalancer(weights, defaultWeight);
        }
        return null;
    }

    /**
     * 根据readModeType返回ReadMode
     *
     * @param readModeType 读取操作的负载均衡模式类型
     * @return ReadMode OR NULL
     */
    private ReadMode getReadMode(String readModeType) {
        if (SubReadModeTypeConstant.SLAVE.equals(readModeType)) {
            return ReadMode.SLAVE;
        }
        if (SubReadModeTypeConstant.MASTER.equals(readModeType)) {
            return ReadMode.MASTER;
        }
        if (SubReadModeTypeConstant.MASTER_SLAVE.equals(readModeType)) {
            return ReadMode.MASTER_SLAVE;
        }
        return null;
    }

    /**
     * 根据subscriptionModeType返回SubscriptionMode
     *
     * @param subscriptionModeType 订阅操作的负载均衡模式类型
     * @return SubscriptionMode OR NULL
     */
    private SubscriptionMode getSubscriptionMode(String subscriptionModeType) {
        if (SubReadModeTypeConstant.SLAVE.equals(subscriptionModeType)) {
            return SubscriptionMode.SLAVE;
        }
        if (SubReadModeTypeConstant.MASTER.equals(subscriptionModeType)) {
            return SubscriptionMode.MASTER;
        }
        return null;
    }

}
