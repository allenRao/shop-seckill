package com.allen.shop.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 创建 queue exchange binding
@Configuration
public class RabbitmqAdminConfig {
	
	//seckill-exchange 
	@Bean
	public TopicExchange seckillTopicExchange() {
		return ExchangeBuilder.topicExchange("seckill-exchange")
				.durable(true).build();
	}
	
		// orderQueue
	@Bean
	public Queue seckillOrderQueue() {
		return QueueBuilder.durable("seckill-order").build();
	}
	
	@Bean
	public Binding bindingseckillOrderQueue2Exchang() {
		return BindingBuilder.bind(seckillOrderQueue())
				.to(seckillTopicExchange()).with("seckill.order");
	}
}













