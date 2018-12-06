package com.longxian.watermark.service;

import com.alibaba.fastjson.JSON;
import com.longxian.watermark.SpringWatermarkApplication;
import com.longxian.watermark.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringWatermarkApplication.class})
public class ApplicationTests {

    //在测试类中不能使用@Autowired注解
    @Resource
    private RedisTemplate<String, User> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisService redisService;

	@Test
	public void test001() throws Exception {
        User longxian =  redisTemplate.opsForValue().get("test001");
       System.out.println("输出参数："+JSON.toJSONString(longxian));
    }

    @Test
    public void test002() throws Exception {
        User longxian =  (User)redisService.get("test001");
        System.out.println("输出参数："+JSON.toJSONString(longxian));
    }

    @Test
    public void test003() throws Exception {
        redisService.remove("test001");
        System.out.println("输出参数：redisService.remove success");
    }

	@Test
	public void test005() throws Exception {

		// 保存对象
		User user = new User("test001", 20);
		redisTemplate.opsForValue().set(user.getUsername(), user);

		/*user = new User("test002", 30);
		redisTemplate.opsForValue().set(user.getUsername(), user);

		user = new User("test003", 40);
		redisTemplate.opsForValue().set(user.getUsername(), user);*/

        Assert.assertEquals(20, redisTemplate.opsForValue().get("test001").getAge().longValue());
	}

}