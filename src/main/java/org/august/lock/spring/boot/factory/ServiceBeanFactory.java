package org.august.lock.spring.boot.factory;

import java.util.EnumMap;

import org.august.lock.spring.boot.enumeration.LockType;
import org.august.lock.spring.boot.exception.ServiceNotFoundException;
import org.august.lock.spring.boot.service.LockService;
import org.august.lock.spring.boot.service.impl.FairLockServiceImpl;
import org.august.lock.spring.boot.service.impl.MultiLockServiceImpl;
import org.august.lock.spring.boot.service.impl.ReadLockServiceImpl;
import org.august.lock.spring.boot.service.impl.RedLockServiceImpl;
import org.august.lock.spring.boot.service.impl.ReentrantLockServiceImpl;
import org.august.lock.spring.boot.service.impl.WriteLockServiceImpl;
import org.august.lock.spring.boot.util.SpringUtil;

public class ServiceBeanFactory {
	
	private static EnumMap<LockType,Class<?>> serviceMap=new EnumMap<>(LockType.class);
	
	static {
		serviceMap.put(LockType.REENTRANT, ReentrantLockServiceImpl.class);
		serviceMap.put(LockType.FAIR, FairLockServiceImpl.class);
		serviceMap.put(LockType.MULTI, MultiLockServiceImpl.class);
		serviceMap.put(LockType.READ, ReadLockServiceImpl.class);
		serviceMap.put(LockType.RED, RedLockServiceImpl.class);
		serviceMap.put(LockType.WRITE, WriteLockServiceImpl.class);
	}

	public LockService getService(LockType lockType) throws ServiceNotFoundException {
		LockService lockService = (LockService) SpringUtil.getBean(serviceMap.get(lockType));
		if(lockService==null) {
			throw new ServiceNotFoundException();
		}
		return lockService;
	}
	
}
