package com.itheima.reggie.controller;

import com.aliyuncs.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();


        //生成随机的4位验证码
        String code = null;
        if (!StringUtils.isEmpty(phone)) {
            code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}",code);
            //调用阿里云提供的短信服务
            // SMSUtils.sendMessage("瑞吉外卖", "", phone, code);

            //将生成的验证码保存起来
            //session.setAttribute(phone,code);


            //将生成的验证码缓存到redis中，并且设置有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("手机验证码短信发送成功");
        }else {
            return R.error("短信发送失败");
        }

    }


    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
      log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();


        //获取验证码
        String code = map.get("code").toString();



        //从session中获取保存的验证码

       // Object codeInSession = session.getAttribute(phone);


        //从redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对(页面提交的验证码和Session中保存的验证码比对)
         if (codeInSession != null && codeInSession.equals(code)){
             //如果比对成功 说明登录成功
             LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

             queryWrapper.eq(User::getPhone,phone);

             User one = userService.getOne(queryWrapper);

             if (one == null){
                 //判断当前手机号对应的用户是否为新用户，如果是新用户 自动完成注册
                 one = new User();
                 one.setPhone(phone);
                 one.setStatus(1);
                 userService.save(one);
             }
             session.setAttribute("user",one.getId());

             //如果用户登录成功，则删除redis中缓存的验证码
             redisTemplate.delete(phone);

            return R.success(one);
         }

        //判断当前手机号对应的用户是否为新用户，如果是新用户 自动完成注册

        return R.error("短信发送失败");


    }
}
