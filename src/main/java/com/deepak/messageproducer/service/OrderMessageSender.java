package com.deepak.messageproducer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deepak.messageproducer.config.RabbitConfig;
import com.deepak.messageproducer.model.Order;

@Service
public class OrderMessageSender
{
   static final Logger logger = LoggerFactory.getLogger( OrderMessageSender.class );
   
   private final RabbitTemplate rabbitTemplate;
   
   private final Exchange exchange;

   @Autowired
   public OrderMessageSender( RabbitTemplate rabbitTemplate, Exchange exchange )
   {
      this.rabbitTemplate = rabbitTemplate;
      this.exchange = exchange;
   }

   public void sendOrder( Order order )
   {
      logger.debug( "Order sent : " + order );
      this.rabbitTemplate.convertAndSend( exchange.getName(), RabbitConfig.PRIORITY_ORDERS_BINDING_KEY , order );
   }
}
