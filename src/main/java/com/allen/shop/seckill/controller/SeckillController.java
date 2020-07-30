package com.allen.shop.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.allen.shop.common.vo.BaseResult;
import com.allen.shop.seckill.service.SeckillService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Api(tags = "seckill api")
@Slf4j
@RestController
@RequestMapping(path = "/api/seckill",
				produces = MediaType.APPLICATION_JSON_VALUE)
public class SeckillController {

	@Autowired
	private SeckillService seckillServiceImpl;
	
	@ApiOperation(value = "触发保存skucache",response = BaseResult.class)
	@GetMapping
	public BaseResult<?> saveSkusToCache(){
		
		return seckillServiceImpl.saveSkus2Cache();
	}
	
	@ApiOperation(value = "getSkuJsonStr",response = BaseResult.class)
	@GetMapping( path = "/skus")
	public BaseResult<?> getSkuJsonStr(){
		
		return seckillServiceImpl.getSkuJsonStr();
	}
	@ApiOperation(value = "抢购",response = BaseResult.class)
	@PostMapping( path = "/skus/{skuId}")
	public BaseResult<?> buy(@PathVariable("skuId")Long skuId,
			@RequestParam(value = "randId", required = true)int randId,
			@RequestParam(value = "userId", required = true)Long userId){
		
		BaseResult<?> buy = seckillServiceImpl.buy(skuId, randId, userId);
		
		return buy;
	}
}


