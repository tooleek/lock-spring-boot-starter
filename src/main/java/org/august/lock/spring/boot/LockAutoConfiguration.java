package org.august.lock.spring.boot;

import java.net.URI;
import java.net.URISyntaxException;

import org.august.lock.spring.boot.config.LockConfig;
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
import org.redisson.config.SingleServerConfig;
import org.redisson.config.SslProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

@Configuration
@EnableConfigurationProperties(LockConfig.class)
@Import({LockInterceptor.class})
public class LockAutoConfiguration {
	
	@Autowired
	private LockConfig lockConfig;
	
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
	private void initClusterConfig(ClusterServersConfig clusterConfig) {
		
	}

}
