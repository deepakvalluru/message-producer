package com.deepak.messageproducer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig
{
   public static final String QUEUE_ORDERS_PRIORITY           = "orders-queue-priority";
   public static final String QUEUE_ORDERS_NONPRIORITY        = "orders-queue-nonpriority";
   public static final String EXCHANGE_ORDERS                 = "orders-exchange";
   public static final String EXCHANGE_FAN_OUT_ORDERS         = "orders-fan-out-exchange";
   public static final String PRIORITY_ORDERS_BINDING_KEY     = "orders.priority";
   public static final String NOT_PRIORITY_ORDERS_BINDING_KEY = "orders.nonpriority";
   public static final String ALL_ORDERS_BINDING_KEY          = "orders.*";
   public static final String QUEUE_ORDERS_DLQ                = "orders.dlq";
   public static final String EXCHANGE_ORDERS_DLE             = "orders.exchange.dle";

   @Bean
   Queue ordersPriorityQueue()
   {
      return QueueBuilder.durable( QUEUE_ORDERS_PRIORITY )
                         .withArgument( "x-dead-letter-exchange", ordersDLE().getName() )
                         .withArgument( "x-dead-letter-routing-key", PRIORITY_ORDERS_BINDING_KEY )
                         .build();
   }

   @Bean
   Queue ordersNonPriorityQueue()
   {
      return QueueBuilder.durable( QUEUE_ORDERS_NONPRIORITY ).build();
   }

   @Bean
   Exchange ordersExchange()
   {
      return ExchangeBuilder.topicExchange( EXCHANGE_ORDERS ).build();
   }

   @Bean
   Queue ordersDLQ()
   {
      return QueueBuilder.durable( QUEUE_ORDERS_DLQ )
//                         .withArgument( "x-message-ttl", 20000 )
//                         .withArgument( "x-dead-letter-exchange", ordersExchange().getName() )
                         .build();
   }

   @Bean
   Exchange ordersDLE()
   {
      return ExchangeBuilder.topicExchange( EXCHANGE_ORDERS_DLE ).build();
   }

   @Bean
   Binding ordersDLBinding()
   {
      return BindingBuilder.bind( ordersDLQ() ).to( ordersDLE() ).with( "#" ).noargs();
   }

   @Bean
   Exchange ordersAllExchange()
   {
      return ExchangeBuilder.fanoutExchange( EXCHANGE_FAN_OUT_ORDERS ).build();
   }

   @Bean
   Binding bindingPriorityOrders( Queue ordersPriorityQueue, TopicExchange ordersExchange )
   {
      return BindingBuilder.bind( ordersPriorityQueue ).to( ordersExchange ).with( PRIORITY_ORDERS_BINDING_KEY );
   }

   @Bean
   Binding bindingNonPriorityOrders( Queue ordersNonPriorityQueue, TopicExchange ordersExchange )
   {
      return BindingBuilder.bind( ordersNonPriorityQueue ).to( ordersExchange ).with( NOT_PRIORITY_ORDERS_BINDING_KEY );
   }

   @Bean
   Binding bindingAllOrdersToPriorityQueue( Queue ordersPriorityQueue, FanoutExchange ordersAllExchange )
   {
      return BindingBuilder.bind( ordersPriorityQueue ).to( ordersAllExchange );
   }

   @Bean
   Binding bindingAllOrdersToNonPriorityQueue( Queue ordersNonPriorityQueue, FanoutExchange ordersAllExchange )
   {
      return BindingBuilder.bind( ordersNonPriorityQueue ).to( ordersAllExchange );
   }

   @Bean
   public RabbitTemplate rabbitTemplate( final ConnectionFactory connectionFactory )
   {
      final RabbitTemplate rabbitTemplate = new RabbitTemplate( connectionFactory );
      rabbitTemplate.setMessageConverter( producerJackson2MessageConverter() );
      return rabbitTemplate;
   }

   @Bean
   public Jackson2JsonMessageConverter producerJackson2MessageConverter()
   {
      return new Jackson2JsonMessageConverter();
   }
   
   @Bean
   public ListenerRetryAdviceCustomizer retryCustomizer(SimpleRabbitListenerContainerFactory containerFactory,
           RabbitProperties rabbitPropeties) {
       return new ListenerRetryAdviceCustomizer(containerFactory, rabbitPropeties);
   }

}
