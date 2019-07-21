package cn.itcast.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    //字符串
    @Test
    public void testString(){
        redisTemplate.boundValueOps("string_key").set("hello heima");
        Object obj = redisTemplate.boundValueOps("string_key").get();
        System.out.println(obj);
    }

    //散列hash
    @Test
    public void testHash(){
        redisTemplate.boundHashOps("h_key").put("f1", "v1");
        redisTemplate.boundHashOps("h_key").put("f2", "v2");
        List list = redisTemplate.boundHashOps("h_key").values();
        System.out.println(list);
    }

    //列表
    @Test
    public void testList(){
        redisTemplate.boundListOps("l_key").rightPush("c");
        redisTemplate.boundListOps("l_key").leftPush("b");
        redisTemplate.boundListOps("l_key").leftPush("a");
        //-1表示最后一个元素
        List list = redisTemplate.boundListOps("l_key").range(0, -1);
        System.out.println(list);
    }

    //集合
    @Test
    public void testSet(){
        redisTemplate.boundSetOps("s_key").add("a", "b", "c");
        Set set = redisTemplate.boundSetOps("s_key").members();
        System.out.println(set);
    }

    //有序集合；根据分值升序
    @Test
    public void testSortedSet(){
        redisTemplate.boundZSetOps("z_key").add("a", 20);
        redisTemplate.boundZSetOps("z_key").add("b", 10);
        redisTemplate.boundZSetOps("z_key").add("c", 5);
        Set set = redisTemplate.boundZSetOps("z_key").range(0, -1);
        System.out.println(set);
    }
}
