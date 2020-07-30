package com.allen.shop.seckill.rpc.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.allen.shop.common.vo.BaseResult;
import com.allen.shop.common.vo.SeckillSkusVo;

import io.swagger.annotations.ApiOperation;

@FeignClient(url = "http://localhost:8001", name = "orderFeignSevice")
@RequestMapping(path = "/api/sku/seckill",
produces = MediaType.APPLICATION_JSON_VALUE)
public interface OrderRpcService {
	
	@ApiOperation(value = "获得List<SeckillSkusVo>",response = BaseResult.class)
	@GetMapping
	public BaseResult<List<SeckillSkusVo>> getSkus();

}
