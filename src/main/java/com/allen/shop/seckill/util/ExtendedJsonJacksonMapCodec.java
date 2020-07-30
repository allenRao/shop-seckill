package com.allen.shop.seckill.util;

import org.redisson.codec.JsonJacksonCodec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ExtendedJsonJacksonMapCodec extends JsonJacksonCodec {

	@Override
	protected void init(ObjectMapper objectMapper) {

		JavaTimeModule javaTimeModule = new JavaTimeModule();
		objectMapper.registerModule(javaTimeModule);
		super.init(objectMapper);
	}
}
