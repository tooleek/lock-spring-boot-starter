package org.august.lock.spring.boot.service.impl;

import org.august.lock.spring.boot.core.LockKey;
import org.august.lock.spring.boot.service.LockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class WriteLockServiceImpl implements LockService {

	@Qualifier("lockRedissonClient")
	@Autowired
	private RedissonClient lockRedissonClient;
	
	private LockKey lockKey;
	
	private RLock lock;
	
	@Override
	public void setLockKey(LockKey lockKey) {
		this.lockKey=lockKey;
	}

	@Override
	public void lock() throws Exception {
		
		this.lock = lockRedissonClient.getReadWriteLock(lockKey.getKeyList().get(0)).writeLock();
		
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
		
		this.lock.lock();
	}

	@Override
	public void release() {
		this.lock.unlock();
	}

}
