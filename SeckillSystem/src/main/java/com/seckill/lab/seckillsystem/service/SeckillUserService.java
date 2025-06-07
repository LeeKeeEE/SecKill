package com.seckill.lab.seckillsystem.service;

import com.seckill.lab.seckillsystem.repository.UserRepository;
import com.seckill.lab.seckillsystem.entity.User;
import com.seckill.lab.seckillsystem.redis.RedisService;
import com.seckill.lab.seckillsystem.redis.SeckillUserKey;
import com.seckill.lab.seckillsystem.result.Result;
import com.seckill.lab.seckillsystem.result.ResultCode;
import com.seckill.lab.seckillsystem.util.MD5Util;
import com.seckill.lab.seckillsystem.util.UUIDUtil;
import com.seckill.lab.seckillsystem.vo.LoginResponseVo;
import com.seckill.lab.seckillsystem.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;



@Service
public class SeckillUserService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    public User getByPhone(String phone) {
        // 1.先根据手机尝试查询缓存
        User user = redisService.get(SeckillUserKey.getByPhone, "" + phone, User.class);
        // 缓存中拿到
        if (user != null) {
            return user;
        }
        // 2.缓存中拿不到，那么就去取数据库
        user = userRepository.findByPhone(phone);
        if (user != null) {
            // 3.数据库存在则设置缓存
            redisService.set(SeckillUserKey.getByPhone, "" + phone, user);
        }
        return user;
    }

    public User getByToken(String token, HttpServletResponse responses) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        User user = redisService.get(SeckillUserKey.token, token, User.class);
        // 再次请求的时候，延长有效期
        if (user != null) {
            addCookie(user, token,responses);
        }
        return user;
    }//根据token获取用户信息

    public String loginTest(LoginVo loginVo,HttpServletResponse responses) {
        if (loginVo == null) {
            return ResultCode.SERVER_ERROR.getMsg();
        }
        // 经过一次MD5的密码
        String mobile = loginVo.getPhone();
        String password = loginVo.getPassword();
        // 判断手机号是否存在
        User user = getByPhone(Long.toString(Long.parseLong(mobile)));
        if (user == null) {
            // 查询不到该手机号的用户
            return ResultCode.MOBILE_NOT_EXIST.getMsg();
        }
        // 手机号存在则验证密码，获取数据库里面的密码与salt去验证
        // eg. 111111 -> e5d22cfc746c7da8da84e0a996e0fffa
        String dbPass = user.getPassword();
        String dbSalt = user.getSalt();
        logger.info("dbPass:{}   dbSalt:{}", dbPass, dbSalt);
        // 验证密码，计算二次MD5出来的pass是否与数据库一致
        String tmpPass = MD5Util.formPassToDBPass(password, dbSalt);
        logger.info("formPass:{}   tmpPass:{}", password, tmpPass);
        if (!tmpPass.equals(dbPass)) {
            return ResultCode.PASSWORD_ERROR.getMsg();
        }
        // 生成cookie
        String token = UUIDUtil.uuid();
        addCookie(user, token, responses);
        return token;
    }

    public Result<?> login(LoginVo loginVo, HttpServletResponse responses) { // 1. 修改返回类型为 Result<?>
        if (loginVo == null) {
            return Result.error(ResultCode.SERVER_ERROR); // 直接返回 Result 对象
        }
        // 经过一次MD5的密码
        String mobile = loginVo.getPhone();
        String formPass = loginVo.getPassword();
        // 判断手机号是否存在
        User user = getByPhone(mobile); // 注意：getByPhone需要接收String类型
        // 查询不到该手机号的用户
        if (user == null) {
            return Result.error(ResultCode.MOBILE_NOT_EXIST);
        }
        // 手机号存在的情况，验证密码
        String dbPass = user.getPassword();
        String dbSalt = user.getSalt();
        logger.info("dbPass:{}, dbSalt:{}", dbPass, dbSalt);

        String tmpPass = MD5Util.formPassToDBPass(formPass, dbSalt);
        logger.info("formPass:{}, tmpPass:{}", formPass, tmpPass);
        if (!tmpPass.equals(dbPass)) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        // --- 核心修改：返回包含Token和User的完整数据 ---
        // 2. 生成cookie
        String token = UUIDUtil.uuid();
        addCookie(user, token, responses);

        // 3. 创建响应VO对象
        LoginResponseVo responseVo = new LoginResponseVo(token, user);

        // 4. 将VO作为成功数据返回
        return Result.success(responseVo);
    }

    /**
     * 添加或者更新cookie
     */
    public void addCookie(User user, String token,HttpServletResponse responses) {
        // 可以用旧的token，不用每次都生成cookie
        // 将token写到cookie当中，然后传递给客户端
        // 此token对应的是哪一个用户,将我们的私人信息存放到一个第三方的缓存中
        // prefix:SeckillUserKey.token key:token value:用户的信息 -->以后拿到了token就知道对应的用户信息。
        // SeckillUserKey.token-->
        redisService.set(SeckillUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        // 设置cookie的有效期，与session有效期一致
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        // 设置网站的根目录
        cookie.setPath("/");
        responses.addCookie(cookie);
    }
}
