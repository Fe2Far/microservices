package com.far.microservices.currencyconversionservice.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.far.microservices.currencyconversionservice.bean.CurrencyConversionBean;
import com.far.microservices.currencyconversionservice.proxy.CurrencyExchangeServiceProxy;

import ch.qos.logback.classic.Logger;

@RestController
public class CurrencyConversionController {
	
	private Logger logger = (Logger) LoggerFactory.getLogger(CurrencyConversionController.class);
	
	@Autowired
	private CurrencyExchangeServiceProxy currencyExchangeProxy;
	
	@GetMapping("currency-converter/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean retrieveConversion(@PathVariable String from,
			@PathVariable String to,
			@PathVariable BigDecimal quantity) {
		
		//http://localhost:8100/currency-converter/from/USD/to/BRL/quantity/100
		
		Map<String,String> uriVariables = new HashMap<>();
		uriVariables.put("from",from);
		uriVariables.put("to",to);
		
		ResponseEntity<CurrencyConversionBean> responseEntity =  new RestTemplate().getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
				CurrencyConversionBean.class, 
				uriVariables);
		
		CurrencyConversionBean response = responseEntity.getBody();
		
		logger.info("{}",response);
		
		return new CurrencyConversionBean(response.getId(),from,to,response.getConversionMultiple(),
				quantity,quantity.multiply(response.getConversionMultiple()),response.getPort());
	}
	
	@GetMapping("currency-converter-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean retrieveConversionFeign(@PathVariable String from,
			@PathVariable String to,
			@PathVariable BigDecimal quantity) {
		
		//http://localhost:8100/currency-converter/from/USD/to/BRL/quantity/100

		CurrencyConversionBean response = currencyExchangeProxy.retrieveExchangeValue(from, to);
		
		return new CurrencyConversionBean(response.getId(),from,to,response.getConversionMultiple(),
				quantity,quantity.multiply(response.getConversionMultiple()),response.getPort());
	}

}
