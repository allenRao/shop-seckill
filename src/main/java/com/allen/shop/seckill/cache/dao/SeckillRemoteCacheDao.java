package com.allen.shop.seckill.cache.dao;

import java.util.List;
import java.util.Map;

import com.allen.shop.common.vo.SeckillSkusVo;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface SeckillRemoteCacheDao {

	boolean cacheSeckillSkus(List<SeckillSkusVo> skuList) throws JsonProcessingException;
	Map<Long, SeckillSkusVo> getSkuJson();
	boolean tryAquire(int randId);
	boolean checkRandomId(Long skuId, int randId);
	boolean saveUserId(Long userId);
	boolean checkUserSet(Long userId);
	boolean checkTime(int randId);
}
