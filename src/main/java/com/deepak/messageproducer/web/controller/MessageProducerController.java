package com.deepak.messageproducer.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.deepak.messageproducer.model.Order;
import com.deepak.messageproducer.service.OrderMessageSender;

import java.util.stream.IntStream;

@RequestMapping("/message")
@Controller
public class MessageProducerController
{
    @Autowired
    private OrderMessageSender sender;

    @Bean
    public TaskExecutor threadPoolTaskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("default_task_executor_thread");
        executor.initialize();
        return executor;
    }

    @PostMapping("/send")
    @ResponseBody
    public String sendMessage(@RequestBody Order order)
    {
        TaskExecutor taskExecutor = threadPoolTaskExecutor();
        try
        {
            IntStream.range(0, 10).forEach( i -> taskExecutor.execute( getTask( i, order ) ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally {
//            taskExecutor.
        }

        return "OK";
    }

    private Runnable getTask (int i, Order order) {
        return () -> {
            System.out.printf("running task %d. Thread: %s%n",
                    i,
                    Thread.currentThread().getName());
            sender.sendOrder( order );
        };
    }
}
