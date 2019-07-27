package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import tk.mybatis.mapper.entity.Example;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends BaseServiceImpl<TbUser> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ActiveMQQueue itcastSmsQueue;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${signName}")
    private String signName;

    @Value("${templateCode}")
    private String templateCode;

    @Override
    public PageInfo<TbUser> search(Integer pageNum, Integer pageSize, TbUser user) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbUser.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(user.getProperty())) {
            criteria.andLike("property", "%" + user.getProperty() + "%");
        }*/

        List<TbUser> list = userMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void sendSmsCode(String phone) {
        //- 生成6位数字随机数作为验证码 0.0112312123可能存在不是6位的情况 TODO 请求修复该问题
        String code =(int)(1000000*Math.random()) + "";
        System.out.println("---------------------------------------------------验证码为："+ code);

        //- 将验证码保存到redis中并设置过期时间5分钟过期
        redisTemplate.boundValueOps(phone).set(code);
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);

        //- 发送验证码及其信息（手机号、签名、模板编号、模板参数）发送到mq的队列itcast_sms_queue
        jmsTemplate.send(itcastSmsQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                //手机号
                mapMessage.setString("mobile",phone);
                //签名
                mapMessage.setString("signName",signName);
                //模板编号
                mapMessage.setString("templateCode",templateCode);
                //模板参数
                mapMessage.setString("templateParam", "{\"code\": "+code+"}");
                return mapMessage;
            }
        });
    }

    @Override
    public boolean checkSmsCode(String phone, String smsCode) {
        //获取redis中的验证码
        String code = (String) redisTemplate.boundValueOps(phone).get();
        if(smsCode.equals(code)){
            //删除redis中的数据
            redisTemplate.delete(phone);
            return true;
        }
        return false;
    }

}
