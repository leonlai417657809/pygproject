package com.pinyougou.item.activemq.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.File;

/**
 * 接收到消息之后删除指定路径下的商品详情静态html页面
 */
public class ItemDeleteMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    //存放商品静态页面的路径
    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        if(message instanceof ObjectMessage){
            ObjectMessage objectMessage = (ObjectMessage) message;

            //获取商品spu id数组
            Long[] goodsIds = (Long[]) objectMessage.getObject();

            try {
                if (goodsIds != null && goodsIds.length > 0) {
                    for (Long goodsId : goodsIds) {
                        String fileName = ITEM_HTML_PATH + goodsId + ".html";
                        File file = new File(fileName);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
         }
    }
}
