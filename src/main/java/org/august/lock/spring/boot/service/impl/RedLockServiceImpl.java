package org.august.lock.spring.boot.service.impl;

import org.august.lock.spring.boot.core.LockKey;
import org.august.lock.spring.boot.service.LockService;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class RedLockServiceImpl implements LockService {

	@Qualifier("lockRedissonClient")
	@Autowired
	private RedissonClient lockRedissonClient;
	
	private LockKey lockKey;
	
	private RedissonRedLock lock;
	
	@Override
	public void setLockKey(LockKey lockKey) {
		this.lockKey=lockKey;
	}

	@Override
	public void lock() throws Exception {
		RLock[] lockList = new RLock[lockKey.getKeyList().size()];
		for(int i=0;i<lockKey.getKeyList().size();i++) {
			lockList[i]=lockRedissonClient.getLock(lockKey.getKeyList().get(i));
		}
		
		lock=new RedissonRedLock(lockList);
		
		if(lockKey.getLeaseTime()==-1&&lockKey.getWaitTime()==-1) {
			lock.lock();
			return;
		}
		if(lockKey.getLeaseTime()!=-1&&lockKey.getWaitTime()==-1) {
			lock.lock(lockKey.getLeaseTime(),lockKey.getTimeUnit());
			return;
		}
		if(lockKey.getLeaseTime()!=-1&&lockKey.getWaitTime()!=-1) {
			lock.tryLock(lockKey.getWaitTime(),lockKey.getLeaseTime(),lockKey.getTimeUnit());
			return;
		}
		
		lock.lock();
	}

	@Override
	public void release() {
		lock.unlock();
	}

}
