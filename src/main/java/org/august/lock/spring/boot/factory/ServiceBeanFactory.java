package org.august.lock.spring.boot.factory;

import java.util.EnumMap;

import org.august.lock.spring.boot.enumeration.LockType;
import org.august.lock.spring.boot.exception.ServiceNotFoundException;
import org.august.lock.spring.boot.service.LockService;
import org.august.lock.spring.boot.service.impl.ReentrantLockServiceImpl;
import org.august.lock.spring.boot.util.SpringUtil;

public class ServiceBeanFactory {
	
	private static EnumMap<LockType,Class<?>> serviceMap=new EnumMap<>(LockType.class);
	
	static {
		serviceMap.put(LockType.REENTRANT, ReentrantLockServiceImpl.class);
	}

	public LockService getService(LockType lockType) throws ServiceNotFoundException {
		LockService lockService = (LockService) SpringUtil.getBean(serviceMap.get(lockType));
		if(lockService==null) {
			throw new ServiceNotFoundException();
		}
		return lockService;
	}
	
}
