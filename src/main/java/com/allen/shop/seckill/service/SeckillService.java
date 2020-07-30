package com.allen.shop.seckill.service;

import com.allen.shop.common.vo.BaseResult;

public interface SeckillService {

	BaseResult<?> saveSkus2Cache();

	BaseResult<?> getSkuJsonStr();

	BaseResult<?> buy(Long skuId, int randId, Long userId);
}
