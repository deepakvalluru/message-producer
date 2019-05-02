package com.deepak.messageproducer.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.deepak.messageproducer.model.Order;
import com.deepak.messageproducer.service.OrderMessageSender;

@RequestMapping("/message")
@Controller
public class MessageProducerController
{
   @Autowired
   private OrderMessageSender sender;
   
   @PostMapping("/send")
   @ResponseBody
   public String sendMessage( @RequestBody Order order)
   {
     sender.sendOrder( order );
     return "OK";
   }
}
