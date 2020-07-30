package com.allen.shop.seckill.config;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitmqConfig {

	@Bean
	@Primary
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	// producer rabbitTemplate 配置
	@Bean
	public Boolean configRabbitTemplate(RabbitTemplate rabbitTemplate) {
		//rabbitTemplate.setUsePublisherConnection(true);
		
		rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				System.out.println(
						"ack:" + ack + "--correlation:" + correlationData.getId() + Thread.currentThread().getName());

			}
		});

		rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
			@Override
			public void returnedMessage(Message message, int replyCode, String replyText, String exchange,
					String routingKey) {
				System.out.println("returnCallback:" + replyText + Thread.currentThread().getName());

			}
		});

		return true;
	}

	@Configuration(proxyBeanMethods = false)
	@Primary
	protected static class RabbitConnectionFactoryCreator {

		@Bean("oder-rabbitConnectionFactory")
		@Primary
		public CachingConnectionFactory rabbitConnectionFactory(RabbitProperties properties,
				ObjectProvider<ConnectionNameStrategy> connectionNameStrategy) throws Exception {
			PropertyMapper map = PropertyMapper.get();
			CachingConnectionFactory factory = new CachingConnectionFactory(
					getRabbitConnectionFactoryBean(properties).getObject());
			map.from(properties::determineAddresses).to(factory::setAddresses);
			map.from(properties::isPublisherReturns).to(factory::setPublisherReturns);
			map.from(properties::getPublisherConfirmType).whenNonNull().to(factory::setPublisherConfirmType);
			RabbitProperties.Cache.Channel channel = properties.getCache().getChannel();
			map.from(channel::getSize).whenNonNull().to(factory::setChannelCacheSize);
			map.from(channel::getCheckoutTimeout).whenNonNull().as(Duration::toMillis)
					.to(factory::setChannelCheckoutTimeout);
			RabbitProperties.Cache.Connection connection = properties.getCache().getConnection();
			map.from(connection::getMode).whenNonNull().to(factory::setCacheMode);
			map.from(connection::getSize).whenNonNull().to(factory::setConnectionCacheSize);
			map.from(connectionNameStrategy::getIfUnique).whenNonNull().to(factory::setConnectionNameStrategy);
			factory.setConnectionThreadFactory(new ThreadFactory() {
				private final AtomicInteger atomicInteger = new AtomicInteger();
				@Override
				public Thread newThread(Runnable r) {
					
					return new Thread(r, "seckill-heartbeat-"+atomicInteger.getAndIncrement());
				}
			});
			
//			默认为newFixedThreadPool 
			
//			 ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		        executor.setCorePoolSize(4);
//		        executor.setMaxPoolSize(10);
//		        executor.setQueueCapacity(10);
//		        executor.setThreadNamePrefix("order-clients-");
//		        executor.initialize();
//			
//			factory.setExecutor(executor);
			
//------------------自定义一个类似tomcat的线程池  参考StandardThreadExcutor 和 TaskQueue
			//public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
			TaskQueue taskQueue = new TaskQueue();
			ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 60000, TimeUnit.MILLISECONDS,taskQueue); 
			
			 taskQueue.setParent(executor);
			 
			 factory.setExecutor(executor);
//-----------------------
			return factory;
		}

		private RabbitConnectionFactoryBean getRabbitConnectionFactoryBean(RabbitProperties properties)
				throws Exception {
			PropertyMapper map = PropertyMapper.get();
			RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
			map.from(properties::determineHost).whenNonNull().to(factory::setHost);
			map.from(properties::determinePort).to(factory::setPort);
			map.from(properties::determineUsername).whenNonNull().to(factory::setUsername);
			map.from(properties::determinePassword).whenNonNull().to(factory::setPassword);
			map.from(properties::determineVirtualHost).whenNonNull().to(factory::setVirtualHost);
			map.from(properties::getRequestedHeartbeat).whenNonNull().asInt(Duration::getSeconds)
					.to(factory::setRequestedHeartbeat);
			map.from(properties::getRequestedChannelMax).to(factory::setRequestedChannelMax);
			RabbitProperties.Ssl ssl = properties.getSsl();
			if (ssl.determineEnabled()) {
				factory.setUseSSL(true);
				map.from(ssl::getAlgorithm).whenNonNull().to(factory::setSslAlgorithm);
				map.from(ssl::getKeyStoreType).to(factory::setKeyStoreType);
				map.from(ssl::getKeyStore).to(factory::setKeyStore);
				map.from(ssl::getKeyStorePassword).to(factory::setKeyStorePassphrase);
				map.from(ssl::getTrustStoreType).to(factory::setTrustStoreType);
				map.from(ssl::getTrustStore).to(factory::setTrustStore);
				map.from(ssl::getTrustStorePassword).to(factory::setTrustStorePassphrase);
				map.from(ssl::isValidateServerCertificate)
						.to((validate) -> factory.setSkipServerCertificateValidation(!validate));
				map.from(ssl::getVerifyHostname).to(factory::setEnableHostnameVerification);
			}
			map.from(properties::getConnectionTimeout).whenNonNull().asInt(Duration::toMillis)
					.to(factory::setConnectionTimeout);
			factory.afterPropertiesSet();
			return factory;
		}

	}

}
