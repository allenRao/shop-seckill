package com.allen.shop.seckill;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.allen.shop.common.vo.BaseResult;
import com.allen.shop.common.vo.SeckillSkusVo;
import com.allen.shop.seckill.rpc.client.OrderRpcService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBootTest(classes = ShopSeckillApplication.class)
class ShopSeckillApplicationTests {

	@Autowired
	private OrderRpcService orderRpcService;
	@Test
	void contextLoads() {
		
		BaseResult<List<SeckillSkusVo>> skus = orderRpcService.getSkus();
		log.info(skus.toString());
		
	}

}
