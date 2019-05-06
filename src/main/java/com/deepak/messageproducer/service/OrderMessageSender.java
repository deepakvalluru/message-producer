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
   static final Logger          logger = LoggerFactory.getLogger( OrderMessageSender.class );

   private final RabbitTemplate rabbitTemplate;
   private final Exchange       ordersExchange;
   private final Exchange       ordersAllExchange;

   @Autowired
   public OrderMessageSender( RabbitTemplate rabbitTemplate, Exchange ordersExchange, Exchange ordersAllExchange )
   {
      this.rabbitTemplate = rabbitTemplate;
      this.ordersExchange = ordersExchange;
      this.ordersAllExchange = ordersAllExchange;
   }

   public void sendOrder( Order order )
   {
      logger.debug( "Order sent : {}", order );
      if( order.getPriority().equalsIgnoreCase( "High" ) )
      {
         this.rabbitTemplate.convertAndSend( ordersExchange.getName(),
                                             RabbitConfig.PRIORITY_ORDERS_BINDING_KEY,
                                             order );
      }
      else if( order.getPriority().equalsIgnoreCase( "Low" ) )
      {
         this.rabbitTemplate.convertAndSend( ordersExchange.getName(),
                                             RabbitConfig.NOT_PRIORITY_ORDERS_BINDING_KEY,
                                             order );
      }
      else
      {
         // this.rabbitTemplate.convertAndSend( ordersAllExchange.getName(), RabbitConfig.ALL_ORDERS_BINDING_KEY, order );
         this.rabbitTemplate.convertAndSend( ordersAllExchange.getName(), "", order );
      }

   }
}
