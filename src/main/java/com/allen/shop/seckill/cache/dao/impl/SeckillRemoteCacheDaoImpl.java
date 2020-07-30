package com.allen.shop.seckill.cache.dao.impl;

import java.awt.Checkbox;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.redisson.api.LocalCachedMapOptions;

import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RSemaphore;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.allen.shop.common.vo.SeckillSkusVo;
import com.allen.shop.seckill.cache.dao.SeckillRemoteCacheDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SeckillRemoteCacheDaoImpl implements SeckillRemoteCacheDao {
	public final String SECKILL_PRIFIX = "seckill:";
	public final String SEMAPHORE_KEY_PRIFIX = SECKILL_PRIFIX + "semaphore:";
	public final String SKUJSON_KEY = SECKILL_PRIFIX + "skus";
	public final String lOCK_KEY = SECKILL_PRIFIX + "lock";
	public final String USERID_SET_KEYS = SECKILL_PRIFIX + "idSet";
	public final int EXPIRE_TIME_MINUTES = 5;

	private volatile RLocalCachedMap<Long, SeckillSkusVo> localCachedMap;

	@Autowired
	private RedissonClient redissonClient;

	@Override
	public boolean cacheSeckillSkus(List<SeckillSkusVo> skuList) {
		RLocalCachedMap<Long, SeckillSkusVo> localCached = this.getLocalCachedMap();
		RLock lock = redissonClient.getLock(lOCK_KEY);
		// 防止重复刷缓存
		lock.lock();
		try {
			// 如果 skus map 不存在 则开始cacheSeckillSkus
			RMap<Object, Object> map = redissonClient.getMap(SKUJSON_KEY);
			boolean exists = map.isExists();
			// 如果存在 则别人已经设置好缓存, 就可以不再设置
			if (!exists) {
				// 如果不存在 则设置缓存
				doCacheSeckillSkus(skuList);
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			lock.unlock();
		}

		localCached.clearLocalCache();
		log.info("clearLocalCache");
		return true;
	}

	// 缓存cacheSeckillSkus

	private void doCacheSeckillSkus(List<SeckillSkusVo> skuList) {
		Random random = new Random();
		HashMap<Long, SeckillSkusVo> skuHashMap = new HashMap<>();
		for (SeckillSkusVo skuVo : skuList) {
			// 生成randomid 保存到list中
			int randId = random.nextInt();
			skuVo.setRandId(randId);
			Long skuId = skuVo.getSkuId();
			// 保存信号量 seckill:semaphore:randid--> num
			RSemaphore semaphore = redissonClient.getSemaphore(SEMAPHORE_KEY_PRIFIX + randId);
			semaphore.trySetPermits(skuVo.getNum());
			semaphore.expire(EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);
			// 保存skuList field key = randomId
			skuHashMap.put(Long.valueOf(randId), skuVo);

		}
		// 将skuHashMap 保存到localcache getLocalCachedMap

		RLocalCachedMap<Long, SeckillSkusVo> cachedMap = this.getLocalCachedMap();
		cachedMap.putAll(skuHashMap);
		cachedMap.expire(EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);
		log.info("加载缓存完成");
		// 清空userId set
		// 如果set 初始为空就会无法设置上 expire
		RSet<Long> set = redissonClient.getSet(USERID_SET_KEYS);
		set.clear();
		set.add(1L); // 加一个值 占位set expire time
		set.expire(EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);
		
	}

	@Override
	public Map<Long, SeckillSkusVo> getSkuJson() {
		RLocalCachedMap<Long, SeckillSkusVo> cachedMap = this.getLocalCachedMap();
		// 取得本地缓存, 如果本地缓存为空, 则加载缓存
		Map<Long, SeckillSkusVo> map = cachedMap.getCachedMap();
		if (map.isEmpty()) {
			cachedMap.preloadCache();
			log.info("preloadCache");
			map = cachedMap.getCachedMap();

		}

		return map;

	}

	// check start and end time
	@Override
	public boolean checkTime(int randId){
		RLocalCachedMap<Long, SeckillSkusVo> cachedMap = this.getLocalCachedMap();
		SeckillSkusVo skusVo = cachedMap.get(Long.valueOf(randId));
		LocalDateTime now = LocalDateTime.now();
		
		return now.isAfter(skusVo.getStartTime()) && now.isBefore(skusVo.getEndTime()); 
	}
	
	
	
	// check element is exist
	@Override
	public boolean checkUserSet(Long userId) {
		RSet<Long> set = redissonClient.getSet(USERID_SET_KEYS);
		boolean contains = set.contains(userId);
		return contains;
	}

	// save userId set
	@Override
	public boolean saveUserId(Long userId) {
		RSet<Long> set = redissonClient.getSet(USERID_SET_KEYS);
		boolean add = set.add(userId);
		return add;
	}

	// check randid
	@Override
	public boolean checkRandomId(Long skuId, int randId) {

		RLocalCachedMap<Long, SeckillSkusVo> cachedMap = this.getLocalCachedMap();
		SeckillSkusVo skusVo = cachedMap.get(Long.valueOf(randId));

		return (skusVo != null) && (skusVo.getRandId() == randId);
	}

	// acquire semaphore
	@Override
	public boolean tryAquire(int randId) {
		RSemaphore semaphore = redissonClient.getSemaphore(SEMAPHORE_KEY_PRIFIX + randId);
		boolean tryAcquire = semaphore.tryAcquire(1);

		return tryAcquire;
	}

	// singleton localCacheMap 单例
	private RLocalCachedMap<Long, SeckillSkusVo> getLocalCachedMap() {
		if (this.localCachedMap != null) {
			return this.localCachedMap;
		} else {
			synchronized (this) {
				if (this.localCachedMap == null) {
					localCachedMap = redissonClient.getLocalCachedMap(SKUJSON_KEY, LocalCachedMapOptions.defaults());
					return localCachedMap;
				}
			}
		}
		return this.localCachedMap;
	}

	@PreDestroy
	public void destory() {
		if (this.localCachedMap != null) {
			localCachedMap.destroy();
		}
	}

}
