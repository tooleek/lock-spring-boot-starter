package org.august.lock.spring.boot.config;

import org.august.lock.spring.boot.enumeration.ServerPattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * 锁配置文件
 * @author TanRq
 *
 */
@Component
@ConfigurationProperties(prefix="lock-config")
public class LockConfig {
	
	/**
	 * Redis的运行模式,默认使用单机模式
	 */
	private String pattern=ServerPattern.SINGLE.getPattern();
	
	private SingleConfig singleServer;
	
	private ClusterConfig clusterServer;
	
	private ReplicatedConfig replicatedServer;
	
	private SentinelConfig sentinelServer;
	
	private MasterSlaveConfig masterSlaveServer;
	/**
	 * 客户端名称
	 */
	private String clientName="Lock";
	/**
	 * 启用SSL终端识别
	 */
	private boolean sslEnableEndpointIdentification=true;
	/**
	 * SSL实现方式，确定采用哪种方式（JDK或OPENSSL）来实现SSL连接
	 */
	private String sslProvider="JDK";
	/**
	 * SSL信任证书库路径
	 */
	private String sslTruststore;
	/**
	 * SSL信任证书库密码
	 */
	private String sslTruststorePassword;
	/**
	 * SSL钥匙库路径
	 */
	private String sslKeystore;
	/**
	 * SSL钥匙库密码
	 */
	private String sslKeystorePassword;

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public boolean isSslEnableEndpointIdentification() {
		return sslEnableEndpointIdentification;
	}

	public void setSslEnableEndpointIdentification(boolean sslEnableEndpointIdentification) {
		this.sslEnableEndpointIdentification = sslEnableEndpointIdentification;
	}

	public String getSslProvider() {
		return sslProvider;
	}

	public void setSslProvider(String sslProvider) {
		this.sslProvider = sslProvider;
	}

	public String getSslTruststore() {
		return sslTruststore;
	}

	public void setSslTruststore(String sslTruststore) {
		this.sslTruststore = sslTruststore;
	}

	public String getSslTruststorePassword() {
		return sslTruststorePassword;
	}

	public void setSslTruststorePassword(String sslTruststorePassword) {
		this.sslTruststorePassword = sslTruststorePassword;
	}

	public String getSslKeystore() {
		return sslKeystore;
	}

	public void setSslKeystore(String sslKeystore) {
		this.sslKeystore = sslKeystore;
	}

	public String getSslKeystorePassword() {
		return sslKeystorePassword;
	}

	public void setSslKeystorePassword(String sslKeystorePassword) {
		this.sslKeystorePassword = sslKeystorePassword;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public SingleConfig getSingleServer() {
		return singleServer;
	}

	public void setSingleServer(SingleConfig singleServer) {
		this.singleServer = singleServer;
	}
	
	public ClusterConfig getClusterServer() {
		return clusterServer;
	}

	public void setClusterServer(ClusterConfig clusterServer) {
		this.clusterServer = clusterServer;
	}

	public ReplicatedConfig getReplicatedServer() {
		return replicatedServer;
	}

	public void setReplicatedServer(ReplicatedConfig replicatedServer) {
		this.replicatedServer = replicatedServer;
	}

	public SentinelConfig getSentinelServer() {
		return sentinelServer;
	}

	public void setSentinelServer(SentinelConfig sentinelServer) {
		this.sentinelServer = sentinelServer;
	}

	public MasterSlaveConfig getMasterSlaveServer() {
		return masterSlaveServer;
	}

	public void setMasterSlaveServer(MasterSlaveConfig masterSlaveServer) {
		this.masterSlaveServer = masterSlaveServer;
	}

	public static class MasterSlaveConfig {
		
	}
	
	public static class SentinelConfig {
		
	}
	
	public static class ReplicatedConfig {
		
	}
	
	public static class ClusterConfig {
		
		private String nodeAddresses;
		private int scanInterval=1000;
		private int slots=231;
		private String readMode="SLAVE";
		private String subMode="订阅操作的负载均衡模式";
		private String loadBalancer="org.redisson.connection.balancer.RoundRobinLoadBalancer";
		private int subConnMinIdleSize=1;
		private int subConnPoolSize=50;
		private int slaveConnMinIdleSize=32;
		private int slaveConnPoolSize=64;
		private int masterConnMinIdleSize=32;
		private int masterConnPoolSize=64;
		private int idleConnTimeout=10000;
		private int connTimeout=10000;
		private int timeout=3000;
		private int retryAttempts=3;
		private int retryInterval=1500;
		private int reconnTimeout=3000;
		private int failedAttempts=3;
		private String password;
		private int subPerConn=5;
	}
	
	public static class SingleConfig {
		/**
		 * 节点地址
		 */
		private String address;
		/**
		 * 节点端口
		 */
		private int port;
		/**
		 * 发布和订阅连接的最小空闲连接数
		 */
		private int subConnMinIdleSize=1;
		/**
		 * 发布和订阅连接池大小
		 */
		private int subConnPoolSize=50;
		/**
		 * 最小空闲连接数
		 */
		private int connMinIdleSize=32;
		/**
		 * 连接池大小
		 */
		private int connPoolSize=64;
		/**
		 * 是否启用DNS监测
		 */
		private boolean dnsMonitoring=false;
		/**
		 * DNS监测时间间隔，单位：毫秒，该配置需要dnsMonitoring设为true
		 */
		private int dnsMonitoringInterval=5000;
		/**
		 * 连接空闲超时，单位：毫秒
		 */
		private int idleConnTimeout=10000;
		/**
		 * 
		 */
		private boolean keepAlive=false;
		/**
		 * 连接超时，单位：毫秒
		 */
		private int connTimeout=10000;
		/**
		 * 命令等待超时，单位：毫秒
		 */
		private int timeout=3000;
		/**
		 * 命令失败重试次数
		 * 如果尝试达到 retryAttempts（命令失败重试次数） 仍然不能将命令发送至某个指定的节点时，将抛出错误。如果尝试在此限制之内发送成功，则开始启用 timeout（命令等待超时） 计时。
		 */
		private int retryAttempts=3;
		/**
		 * 命令重试发送时间间隔，单位：毫秒
		 */
		private int retryInterval=1500;
		/**
		 * 数据库编号
		 */
		private int database=0;
		/**
		 * 密码
		 */
		private String password;
		/**
		 * 单个连接最大订阅数量
		 */
		private int subPerConn=5;
		
		
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public int getSubConnMinIdleSize() {
			return subConnMinIdleSize;
		}
		public void setSubConnMinIdleSize(int subConnMinIdleSize) {
			this.subConnMinIdleSize = subConnMinIdleSize;
		}
		public int getSubConnPoolSize() {
			return subConnPoolSize;
		}
		public void setSubConnPoolSize(int subConnPoolSize) {
			this.subConnPoolSize = subConnPoolSize;
		}
		public int getConnMinIdleSize() {
			return connMinIdleSize;
		}
		public void setConnMinIdleSize(int connMinIdleSize) {
			this.connMinIdleSize = connMinIdleSize;
		}
		public int getConnPoolSize() {
			return connPoolSize;
		}
		public void setConnPoolSize(int connPoolSize) {
			this.connPoolSize = connPoolSize;
		}
		public boolean isDnsMonitoring() {
			return dnsMonitoring;
		}
		public void setDnsMonitoring(boolean dnsMonitoring) {
			this.dnsMonitoring = dnsMonitoring;
		}
		public int getDnsMonitoringInterval() {
			return dnsMonitoringInterval;
		}
		public void setDnsMonitoringInterval(int dnsMonitoringInterval) {
			this.dnsMonitoringInterval = dnsMonitoringInterval;
		}
		public int getIdleConnTimeout() {
			return idleConnTimeout;
		}
		public void setIdleConnTimeout(int idleConnTimeout) {
			this.idleConnTimeout = idleConnTimeout;
		}
		public int getConnTimeout() {
			return connTimeout;
		}
		public void setConnTimeout(int connTimeout) {
			this.connTimeout = connTimeout;
		}
		public int getTimeout() {
			return timeout;
		}
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}
		public int getRetryAttempts() {
			return retryAttempts;
		}
		public void setRetryAttempts(int retryAttempts) {
			this.retryAttempts = retryAttempts;
		}
		public int getRetryInterval() {
			return retryInterval;
		}
		public void setRetryInterval(int retryInterval) {
			this.retryInterval = retryInterval;
		}
		public int getDatabase() {
			return database;
		}
		public void setDatabase(int database) {
			this.database = database;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public int getSubPerConn() {
			return subPerConn;
		}
		public void setSubPerConn(int subPerConn) {
			this.subPerConn = subPerConn;
		}
		public boolean isKeepAlive() {
			return keepAlive;
		}
		public void setKeepAlive(boolean keepAlive) {
			this.keepAlive = keepAlive;
		}
	}

}
