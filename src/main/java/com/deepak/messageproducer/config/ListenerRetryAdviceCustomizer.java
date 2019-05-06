package com.deepak.messageproducer.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties.ListenerRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.policy.SimpleRetryPolicy;

public class ListenerRetryAdviceCustomizer implements InitializingBean
{

   private final SimpleRabbitListenerContainerFactory containerFactory;

   private final RabbitProperties                     rabbitPropeties;

   public ListenerRetryAdviceCustomizer( SimpleRabbitListenerContainerFactory containerFactory,
                                         RabbitProperties rabbitPropeties )
   {
      this.containerFactory = containerFactory;
      this.rabbitPropeties = rabbitPropeties;
   }

   @Override
   public void afterPropertiesSet() throws Exception
   {
      ListenerRetry retryConfig = this.rabbitPropeties.getListener().getSimple().getRetry();
      if( retryConfig.isEnabled() )
      {
         RetryInterceptorBuilder< ? > builder = ( retryConfig.isStateless() ? RetryInterceptorBuilder.stateless()
                                                                            : RetryInterceptorBuilder.stateful() );
         Map< Class< ? extends Throwable >, Boolean > retryableExceptions = new HashMap<>();
         retryableExceptions.put( AmqpRejectAndDontRequeueException.class, false );
         retryableExceptions.put( IllegalStateException.class, true );
         SimpleRetryPolicy policy = new SimpleRetryPolicy( retryConfig.getMaxAttempts(), retryableExceptions, true );
         ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
         backOff.setInitialInterval( retryConfig.getInitialInterval().toMillis() );
         backOff.setMultiplier( retryConfig.getMultiplier() );
         backOff.setMaxInterval( retryConfig.getMaxInterval().toMillis() );
         builder.retryPolicy( policy ).backOffPolicy( backOff );
         this.containerFactory.setAdviceChain( builder.build() );
      }
   }

}
