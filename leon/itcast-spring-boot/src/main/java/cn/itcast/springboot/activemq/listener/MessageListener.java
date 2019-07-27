package cn.itcast.springboot.activemq.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageListener {
    @JmsListener(destination = "spring.boot.mq.queue")
    public void receiveMsg(Map<String,Object> map){
        System.out.println("从spring.boot.mq.queue接收到的消息为："+map);
    }
}
