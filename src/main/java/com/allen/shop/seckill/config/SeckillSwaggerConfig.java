package com.allen.shop.seckill.config;

import org.springframework.context.annotation.Configuration;

import com.allen.shop.common.config.SwaggerConfig;

@Configuration
public class SeckillSwaggerConfig extends SwaggerConfig{

	@Override
	public String getBasePackage() {
		
		return "com.allen.shop.seckill.controller";
	}

	
}
