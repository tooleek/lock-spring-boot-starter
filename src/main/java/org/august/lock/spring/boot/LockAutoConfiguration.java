package org.august.lock.spring.boot;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.august.lock.spring.boot.config.LockConfig;
import org.august.lock.spring.boot.config.LockConfig.ClusterConfig;
import org.august.lock.spring.boot.config.LockConfig.SingleConfig;
import org.august.lock.spring.boot.core.LockInterceptor;
import org.august.lock.spring.boot.enumeration.ServerPattern;
import org.august.lock.spring.boot.factory.ServiceBeanFactory;
import org.august.lock.spring.boot.service.impl.FairLockServiceImpl;
import org.august.lock.spring.boot.service.impl.MultiLockServiceImpl;
import org.august.lock.spring.boot.service.impl.ReadLockServiceImpl;
import org.august.lock.spring.boot.service.impl.RedLockServiceImpl;
import org.august.lock.spring.boot.service.impl.ReentrantLockServiceImpl;
import org.august.lock.spring.boot.service.impl.WriteLockServiceImpl;
import org.august.lock.spring.boot.store.MapStore;
import org.august.lock.spring.boot.util.SpringUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.ReadMode;
import org.redisson.config.ReplicatedServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.SslProvider;
import org.redisson.config.SubscriptionMode;
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

/**
 * 自动装配
 * @author TanRq
 *
 */
@Configuration
@EnableConfigurationProperties(LockConfig.class)
@Import({LockInterceptor.class})
public class LockAutoConfiguration {
	
	@Autowired
	private LockConfig lockConfig;
	
	/**
	 * 创建redissonClient
	 * @return
	 * @throws URISyntaxException
	 */
	@Bean(name="lockRedissonClient",destroyMethod = "shutdown")
	@ConditionalOnMissingBean
	public RedissonClient redissionClient() throws URISyntaxException {
		Config config=new Config();
		ServerPattern serverPattern=MapStore.getServerPattern(lockConfig.getPattern());
		
		if(serverPattern==ServerPattern.SINGLE) {
			SingleServerConfig singleConfig=config.useSingleServer();
			initSingleConfig(singleConfig);
		}
		if(serverPattern==ServerPattern.CLUSTER) {
			ClusterServersConfig clusterConfig=config.useClusterServers();
			initClusterConfig(clusterConfig);
		}
		if(serverPattern==ServerPattern.MASTER_SLAVE) {
			MasterSlaveServersConfig masterSlaveConfig=config.useMasterSlaveServers();
			initMasterSlaveConfig(masterSlaveConfig);
		}
		if(serverPattern==ServerPattern.REPLICATED) {
			ReplicatedServersConfig replicatedServersConfig = config.useReplicatedServers();
			initReplicatedServersConfig(replicatedServersConfig);
		}
		if(serverPattern==ServerPattern.SENTINEL) {
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
	 * @param singleConfig
	 * @throws URISyntaxException 
	 */
	private void initSingleConfig(SingleServerConfig singleConfig) throws URISyntaxException {
		SingleConfig singleServerConfig = lockConfig.getSingleServer();
		singleConfig.setAddress("redis://"+singleServerConfig.getAddress()+":"+singleServerConfig.getPort());
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
		if(lockConfig.getSslKeystore()!=null) {
			singleConfig.setSslKeystore(new URI(lockConfig.getSslKeystore()));
		}
		if(lockConfig.getSslKeystorePassword()!=null) {
			singleConfig.setSslKeystorePassword(lockConfig.getSslKeystorePassword());
		}
		singleConfig.setSslProvider("JDK".equalsIgnoreCase(lockConfig.getSslProvider())?SslProvider.JDK:SslProvider.OPENSSL);
	}
	
	/**
	 * 初始化集群模式参数
	 * @param clusterConfig
	 */
	private void initClusterConfig(ClusterServersConfig clusterServerConfig) {
		ClusterConfig clusterConfig = lockConfig.getClusterServer();
		String[] addressArr = clusterConfig.getNodeAddresses().split(",");
		Arrays.asList(addressArr).stream().forEach(address -> 
			clusterServerConfig.addNodeAddress("redis://"+address)
		);
		clusterServerConfig.setScanInterval(clusterConfig.getScanInterval());
		if("SLAVE".equals(clusterConfig.getReadMode())) {
			clusterServerConfig.setReadMode(ReadMode.SLAVE);
		}
		if("MASTER".equals(clusterConfig.getReadMode())) {
			clusterServerConfig.setReadMode(ReadMode.MASTER);
		}
		if("MASTER_SLAVE".equals(clusterConfig.getReadMode())) {
			clusterServerConfig.setReadMode(ReadMode.MASTER_SLAVE);
		}
		if("SLAVE".equals(clusterConfig.getSubMode())) {
			clusterServerConfig.setSubscriptionMode(SubscriptionMode.SLAVE);
		}
		if("MASTER".equals(clusterConfig.getSubMode())) {
			clusterServerConfig.setSubscriptionMode(SubscriptionMode.MASTER);
		}
		if("RoundRobinLoadBalancer".equals(clusterConfig.getLoadBalancer())) {
			clusterServerConfig.setLoadBalancer(new RoundRobinLoadBalancer());
		}
		if("WeightedRoundRobinBalancer".equals(clusterConfig.getLoadBalancer())) {
			Map<String, Integer> weights=new HashMap<String, Integer>();
			String[] weightMaps = clusterConfig.getWeightMaps().split(";");
			Arrays.asList(weightMaps).stream().forEach(weightMap -> 
				weights.put("redis://"+weightMap.split(",")[0], Integer.parseInt(weightMap.split(",")[1]))
			);
			clusterServerConfig.setLoadBalancer(new WeightedRoundRobinBalancer(weights, clusterConfig.getDefaultWeight()));
		}
		if("RandomLoadBalancer".equals(clusterConfig.getLoadBalancer())) {
			clusterServerConfig.setLoadBalancer(new RandomLoadBalancer());
		}
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
	
	private void initSentinelServersConfig(SentinelServersConfig sentinelServersConfig) {
		// TODO Auto-generated method stub
		
	}

	private void initReplicatedServersConfig(ReplicatedServersConfig replicatedServersConfig) {
		// TODO Auto-generated method stub
		
	}

	private void initMasterSlaveConfig(MasterSlaveServersConfig masterSlaveConfig) {
		//masterSlaveConfig.setmas
	}

}
