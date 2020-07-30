package com.allen.shop.seckill.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.allen.shop.common.constant.ResultStatus;
import com.allen.shop.common.vo.BaseResult;
import com.allen.shop.common.vo.SeckillSkusVo;
import com.allen.shop.seckill.cache.dao.SeckillRemoteCacheDao;
import com.allen.shop.seckill.cache.dao.impl.SeckillRemoteCacheDaoImpl;
import com.allen.shop.seckill.rpc.client.OrderRpcService;
import com.allen.shop.seckill.service.SeckillService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

	@Autowired
	private OrderRpcService orderRpcService;
	@Autowired
	private SeckillRemoteCacheDao sckillRemoteCacheDaoImpl;

	@Override
	public BaseResult<?> saveSkus2Cache() {
		// rpc 调用sku服务 得到数据
		BaseResult<List<SeckillSkusVo>> result = orderRpcService.getSkus();
		log.info(result.toString());

		if (result.getStatus() == ResultStatus.ERROR.getCode() || result.getData() == null
				|| result.getData().size() == 0) {

			return BaseResult.error();
		}
		List<SeckillSkusVo> skus = result.getData();
		// 数据保存至cache
		try {
			boolean cacheSeckillSkus = sckillRemoteCacheDaoImpl.cacheSeckillSkus(skus);

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return BaseResult.error();
		}
		return BaseResult.success();
	}

	@Override
	public BaseResult<?> getSkuJsonStr(){
		Map<Long, SeckillSkusVo> skuMap = sckillRemoteCacheDaoImpl.getSkuJson();
		if (skuMap == null) {
			return BaseResult.error();
		}else {
			return BaseResult.success(skuMap);
		}
		
	}
	
	@Override
	public BaseResult<?> buy(Long skuId, int randId, Long userId){
		boolean result = false;
		// 验证token登录 todo 登录拦截器做的
		
		// 当前时间是否落在start 和end 时间之间
		if (!sckillRemoteCacheDaoImpl.checkTime(randId)) {
			log.info("抢购时间不匹配");
			return BaseResult.error("抢购时间不匹配");
		}
		
			// 验证randid 是否存在 防刷
		if (!sckillRemoteCacheDaoImpl.checkRandomId(skuId, randId)) {
			log.info("randId 错误");
			return BaseResult.error("randId 错误");
		}
		
		//userId 向mock 一个userId 重复购买
		if (sckillRemoteCacheDaoImpl.checkUserSet(userId)) {
			return BaseResult.error("重复购买");
		}
	
		// 抢信号量 
		if (!sckillRemoteCacheDaoImpl.tryAquire(randId)) {
			return BaseResult.error("已抢购空了");
		}
		// 生成订单号 
			//todo mock order id出来
		
		long orderId = Math.abs(new Random().nextLong());
		// 发送消息队列
			
		// 保存userid 到防重set
		sckillRemoteCacheDaoImpl.saveUserId(userId);
		// 返回订单号
		return BaseResult.success(orderId);
	}
}
