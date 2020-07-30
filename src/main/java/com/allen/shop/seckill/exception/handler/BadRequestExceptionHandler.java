package com.allen.shop.seckill.exception.handler;

import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.allen.shop.common.vo.BaseResult;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@ControllerAdvice
public class BadRequestExceptionHandler{
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	public BaseResult exceptionHandler(MethodArgumentNotValidException ex, WebRequest request) throws Exception{
		
		log.error("自定义exceptionHandler:---->{}",ex.getMessage());
		
		BindingResult bindingResult = ex.getBindingResult();
		// 参数校验, BindingResult bindingResult
		if (bindingResult.hasErrors()) {
			 HashMap<Object, Object> errorMap = bindingResult.getAllErrors().stream()
				.collect( HashMap::new,
						  (map, error) -> {
							  				FieldError fildError = (FieldError)error;
							  				map.put(fildError.getField(), fildError.getDefaultMessage());
						  				  },
						  HashMap::putAll
						);
			return BaseResult.error(errorMap);
		}
		// 
		throw ex;
	}
}
