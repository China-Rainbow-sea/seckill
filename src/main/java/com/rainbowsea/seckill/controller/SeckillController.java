package com.rainbowsea.seckill.controller;


import cn.hutool.json.JSONUtil;
import com.rainbowsea.seckill.config.AccessLimit;
import com.rainbowsea.seckill.pojo.SeckillMessage;
import com.rainbowsea.seckill.pojo.SeckillOrder;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.rabbitmq.MQSenderMessage;
import com.rainbowsea.seckill.service.GoodsService;
import com.rainbowsea.seckill.service.OrderService;
import com.rainbowsea.seckill.service.SeckillOrderService;
import com.rainbowsea.seckill.vo.GoodsVo;
import com.rainbowsea.seckill.vo.RespBean;
import com.rainbowsea.seckill.vo.RespBeanEnum;
import com.ramostear.captcha.HappyCaptcha;
import com.ramostear.captcha.common.Fonts;
import com.ramostear.captcha.support.CaptchaStyle;
import com.ramostear.captcha.support.CaptchaType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
// InitializingBean 当中的 afterPropertiesSet 表示项目启动就自动给执行该方法当中的内容
public class SeckillController implements InitializingBean {


    // 装配需要的组件/对象
    @Resource
    private GoodsService goodsService;

    @Resource
    private SeckillOrderService seckillOrderService;


    @Resource
    private OrderService orderService;


    // 如果某个商品库存已经为空, 则标记到 entryStockMap
    @Resource
    private RedisTemplate redisTemplate;

    // 定义 map- 记录秒杀商品
    private HashMap<Long, Boolean> entryStockMap = new HashMap<>();


    // 装配消息的生产者/发送者
    @Resource
    private MQSenderMessage mqSenderMessage;


    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V1.0版本，没有进行复购处理，后面在高并发的情况下，还会继续优化
     *
     * @param model   返回给模块的 model 信息
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @return 返回到映射在 resources 下的 templates 下的页面
     */
    //@RequestMapping(value = "/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
    //    System.out.println("秒杀 V 1.0 ");
    //
    //    if (null == user) { //用户没有登录
    //        return "login";
    //    }
    //
    //    // 登录了，则返回用户信息给下一个模板内容
    //    model.addAttribute("user", user);
    //
    //    // 获取到 GoodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 判断库存
    //    if (goodsVo.getStockCount() < 1) {  // 没有库存，不可以购买
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 判断用户是否复购-判断当前购买用户的ID和购买商品id是否已经在商品秒杀表当中存在了。
    //    SeckillOrder seckillOrder = seckillOrderService.getOne(
    //            new QueryWrapper<SeckillOrder>()
    //                    .eq("user_id", user.getId())
    //                    .eq("goods_id", goodsId)
    //    );  // 这里的 column 不可以随便写，要对应上数据表当中的对应的“字段名”
    //
    //    if (seckillOrder != null) {  // 不为 null ，说明用户购买过了，进行了复购
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 抢购
    //    Order order = orderService.seckill(user, goodsVo);
    //    if (order == null) { // 说明抢购失败了，由于什么原因
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 走到这里，说明抢购成功了，将信息，通过 model 返回给页面
    //    model.addAttribute("order", order);
    //    model.addAttribute("goods", goodsVo);
    //
    //    System.out.println("秒杀 V 1.0 ");
    //
    //    return "orderDetail";  // 进入到订单详情页
    //
    //
    //}


    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V2.0版本，利用 MySQL默认的事务隔离级别【REPEATABLE-READ】
     *
     * @param model   返回给模块的 model 信息
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @return 返回到映射在 resources 下的 templates 下的页面
     */
    //@RequestMapping(value = "/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
    //    System.out.println("秒杀 V 1.0 ");
    //
    //    if (null == user) { //用户没有登录
    //        return "login";
    //    }
    //
    //    // 登录了，则返回用户信息给下一个模板内容
    //    model.addAttribute("user", user);
    //
    //    // 获取到 GoodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 判断库存
    //    if (goodsVo.getStockCount() < 1) {  // 没有库存，不可以购买
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 判断用户是否复购-直接到 Redis 当中获取(因为我们抢购成功直接
    //    // 将表单信息存储到了Redis 当中了。 key表示:order:userId:goodsId  Value表示订单 seckillOrder)，
    //    // 获取对应的秒杀订单，如果有，则说明该
    //    // 用户已经桥抢购了，每人限购一个
    //    SeckillOrder o = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" +
    //            goodsVo.getId()); // 因为我们在 Redis 当中的 value值就是 SeckillOrder 订单对象，所以这里可以直接强制类型转换
    //    if(null != o) { // 不为null，说明 Redis 存在该用户订单信息，说明该用户已经抢购了该商品
    //        model.addAttribute("errmsg",RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 抢购
    //    Order order = orderService.seckill(user, goodsVo);
    //    if (order == null) { // 说明抢购失败了，由于什么原因
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 走到这里，说明抢购成功了，将信息，通过 model 返回给页面
    //    model.addAttribute("order", order);
    //    model.addAttribute("goods", goodsVo);
    //
    //    System.out.println("秒杀 V 2.0 ");
    //
    //    return "orderDetail";  // 进入到订单详情页
    //
    //
    //}


    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V3.0版本，
     * - 利用 MySQL默认的事务隔离级别【REPEATABLE-READ】
     * - 使用 优化秒杀： Redis 预减库存+Decrement
     *
     * @param model   返回给模块的 model 信息
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @return 返回到映射在 resources 下的 templates 下的页面
     */
    //@RequestMapping(value = "/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
    //    System.out.println("秒杀 V 2.0 ");
    //
    //    if (null == user) { //用户没有登录
    //        return "login";
    //    }
    //
    //    // 登录了，则返回用户信息给下一个模板内容
    //    model.addAttribute("user", user);
    //
    //    // 获取到 GoodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 判断库存
    //    if (goodsVo.getStockCount() < 1) {  // 没有库存，不可以购买
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 判断用户是否复购-直接到 Redis 当中获取(因为我们抢购成功直接
    //    // 将表单信息存储到了Redis 当中了。 key表示:order:userId:goodsId  Value表示订单 seckillOrder)，
    //    // 获取对应的秒杀订单，如果有，则说明该
    //    // 用户已经桥抢购了，每人限购一个
    //    SeckillOrder o = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" +
    //            goodsVo.getId()); // 因为我们在 Redis 当中的 value值就是 SeckillOrder 订单对象，所以这里可以直接强制类型转换
    //    if (null != o) { // 不为null，说明 Redis 存在该用户订单信息，说明该用户已经抢购了该商品
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // Redis库存预减，如果在 Redis 中预减库存,发现秒杀商品已经没有了，就直接返回
    //    // 从面减少去执行 orderService.seckill()请求，防止线程堆积，优化秒杀/高并发
    //    // 提示: Redis 的 decrement是具有原子性的，已经存在了原子性，就是一条一条执行的，不会存在，复购，多购的可能性。
    //    // 注意:这里我们要操作的 key 的是:seckillGoods:商品Id,value:该商品的库存量
    //    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
    //    if (decrement < 0) {  // 说明这个商品已经没有库存了,返回
    //        // 这里我们可以恢复库存为 0 ，因为后面可能会一直减下去，恢复为 0 让数据更好看一些
    //        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 抢购
    //    Order order = orderService.seckill(user, goodsVo);
    //    if (order == null) { // 说明抢购失败了，由于什么原因
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 走到这里，说明抢购成功了，将信息，通过 model 返回给页面
    //    model.addAttribute("order", order);
    //    model.addAttribute("goods", goodsVo);
    //
    //    System.out.println("秒杀 V 2.0 ");
    //
    //    return "orderDetail";  // 进入到订单详情页
    //
    //
    //}


    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V 4.0版本，
     * - 利用 MySQL默认的事务隔离级别【REPEATABLE-READ】
     * - 使用 优化秒杀： Redis 预减库存+Decrement
     * - 优化秒杀: 加入内存标记，避免总到 Redis 查询库存
     *
     * @param model   返回给模块的 model 信息
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @return 返回到映射在 resources 下的 templates 下的页面
     */
    //@RequestMapping(value = "/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
    //    System.out.println("秒杀 V 4.0 ");
    //
    //    // 定义 map - 记录秒杀商品是否还有库存
    //
    //    if (null == user) { //用户没有登录
    //        return "login";
    //    }
    //
    //    // 登录了，则返回用户信息给下一个模板内容
    //    model.addAttribute("user", user);
    //
    //    // 获取到 GoodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 判断库存
    //    if (goodsVo.getStockCount() < 1) {  // 没有库存，不可以购买
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 判断用户是否复购-直接到 Redis 当中获取(因为我们抢购成功直接
    //    // 将表单信息存储到了Redis 当中了。 key表示:order:userId:goodsId  Value表示订单 seckillOrder)，
    //    // 获取对应的秒杀订单，如果有，则说明该
    //    // 用户已经桥抢购了，每人限购一个
    //    SeckillOrder o = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" +
    //            goodsVo.getId()); // 因为我们在 Redis 当中的 value值就是 SeckillOrder 订单对象，所以这里可以直接强制类型转换
    //    if (null != o) { // 不为null，说明 Redis 存在该用户订单信息，说明该用户已经抢购了该商品
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //
    //    // 对map进行判断[内存标记]，如果商品在  map 已经标记为没有库存,则直接返回,无需进行 Redis 预减
    //    if (entryStockMap.get(goodsId)) {
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // Redis库存预减，如果在 Redis 中预减库存,发现秒杀商品已经没有了，就直接返回
    //    // 从面减少去执行 orderService.seckill()请求，防止线程堆积，优化秒杀/高并发
    //    // 提示: Redis 的 decrement是具有原子性的，已经存在了原子性，就是一条一条执行的，不会存在，复购，多购的可能性。
    //    // 注意:这里我们要操作的 key 的是:seckillGoods:商品Id,value:该商品的库存量
    //    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
    //    if (decrement < 0) {  // 说明这个商品已经没有库存了,返回
    //
    //        // 说明当前秒杀的商品，已经没有库存
    //        entryStockMap.put(goodsId, true);
    //
    //        // 这里我们可以恢复库存为 0 ，因为后面可能会一直减下去，恢复为 0 让数据更好看一些
    //        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 抢购
    //    Order order = orderService.seckill(user, goodsVo);
    //    if (order == null) { // 说明抢购失败了，由于什么原因
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 走到这里，说明抢购成功了，将信息，通过 model 返回给页面
    //    model.addAttribute("order", order);
    //    model.addAttribute("goods", goodsVo);
    //
    //    System.out.println("秒杀 V 4.0 ");
    //
    //    return "orderDetail";  // 进入到订单详情页
    //
    //}


    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V 5.0版本，
     * - 利用 MySQL默认的事务隔离级别【REPEATABLE-READ】
     * - 使用 优化秒杀： Redis 预减库存+Decrement
     * - 优化秒杀: 加入内存标记，避免总到 Redis 查询库存
     * - 优化秒杀: 加入消息队列，实现秒杀的异步请求
     *
     * @param model   返回给模块的 model 信息
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @return 返回到映射在 resources 下的 templates 下的页面
     */
    //@RequestMapping(value = "/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
    //    System.out.println("秒杀 V 5.0 ");
    //
    //    // 定义 map - 记录秒杀商品是否还有库存
    //
    //    if (null == user) { //用户没有登录
    //        return "login";
    //    }
    //
    //    // 登录了，则返回用户信息给下一个模板内容
    //    model.addAttribute("user", user);
    //
    //    // 获取到 GoodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 判断库存
    //    if (goodsVo.getStockCount() < 1) {  // 没有库存，不可以购买
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // 判断用户是否复购-直接到 Redis 当中获取(因为我们抢购成功直接
    //    // 将表单信息存储到了Redis 当中了。 key表示:order:userId:goodsId  Value表示订单 seckillOrder)，
    //    // 获取对应的秒杀订单，如果有，则说明该
    //    // 用户已经桥抢购了，每人限购一个
    //    SeckillOrder o = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" +
    //            goodsVo.getId()); // 因为我们在 Redis 当中的 value值就是 SeckillOrder 订单对象，所以这里可以直接强制类型转换
    //    if (null != o) { // 不为null，说明 Redis 存在该用户订单信息，说明该用户已经抢购了该商品
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //
    //    // 对map进行判断[内存标记]，如果商品在  map 已经标记为没有库存,则直接返回,无需进行 Redis 预减
    //    if (entryStockMap.get(goodsId)) {
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    // Redis库存预减，如果在 Redis 中预减库存,发现秒杀商品已经没有了，就直接返回
    //    // 从面减少去执行 orderService.seckill()请求，防止线程堆积，优化秒杀/高并发
    //    // 提示: Redis 的 decrement是具有原子性的，已经存在了原子性，就是一条一条执行的，不会存在，复购，多购的可能性。
    //    // 注意:这里我们要操作的 key 的是:seckillGoods:商品Id,value:该商品的库存量
    //    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
    //    if (decrement < 0) {  // 说明这个商品已经没有库存了,返回
    //
    //        // 说明当前秒杀的商品，已经没有库存
    //        entryStockMap.put(goodsId, true);
    //
    //        // 这里我们可以恢复库存为 0 ，因为后面可能会一直减下去，恢复为 0 让数据更好看一些
    //        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
    //        return "secKillFail"; // 返回一个错误页面
    //    }
    //
    //    /*
    //    抢购，向消息队列发送秒杀请求，实现了秒杀异步请求
    //    这里我们发送秒杀消息后，立即快速返回结果【临时结果】- “比如排队中...”
    //    客户端可以通过轮询，获取到最终结果
    //    创建 SeckillMessage
    //     */
    //
    //    SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
    //    // 将 seckillMessage 对象封装为 JSON 格式的 String 让RabbitMQ 生产者发送出去
    //    // 被消费者接受消费
    //    mqSenderMessage.sendSeckillMessage(JSONUtil.toJsonStr(seckillMessage));
    //    model.addAttribute("errmsg", "排队中...");
    //
    //    System.out.println("秒杀 V 5.0 ");
    //
    //    return "secKillFail";
    //
    //    //return "orderDetail";  // 进入到订单详情页
    //
    //}


    /**
     * 获取秒杀路径
     *
     * @param user    用户信息
     * @param goodsId 秒杀商品ID
     * @return RespBean 返回信息，携带秒杀路径 path
     */
    //@RequestMapping("/path")
    //@ResponseBody
    //public RespBean getPath(User user, Long goodsId) {
    //    // 我们的设计的商品 gooodsId 是一定大于 0 的
    //    if (user == null || goodsId < 0) {
    //        return RespBean.error(RespBeanEnum.SESSION_ERROR);
    //    }
    //
    //
    //    String path = orderService.createPath(user, goodsId);
    //
    //    return RespBean.success(path);
    //}


    /**
     * 获取秒杀路径
     *
     * @param user    用户信息
     * @param goodsId 秒杀商品ID
     * @return RespBean 返回信息，携带秒杀路径 path
     * -v 2.0 增加了 happyCaptcha 验证码
     */
    //@RequestMapping("/path")
    //@ResponseBody
    //public RespBean getPath(User user, Long goodsId, String captcha) {
    //    // 我们的设计的商品 gooodsId 是一定大于 0 的
    //    if (user == null || goodsId < 0 || !StringUtils.hasText(captcha)) {
    //        return RespBean.error(RespBeanEnum.SESSION_ERROR);
    //    }
    //
    //    // 增加一个业务逻辑-校验用户输入的验证码是否正确
    //    boolean check = orderService.checkCaptcha(user, goodsId, captcha);
    //    if (!check) {
    //        return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
    //    }
    //
    //    String path = orderService.createPath(user, goodsId);
    //
    //    return RespBean.success(path);
    //}

    /**
     * 获取秒杀路径
     *
     * @param user    用户信息
     * @param goodsId 秒杀商品ID
     * @return RespBean 返回信息，携带秒杀路径 path
     * -v 3.0 增加了 happyCaptcha 验证码
     * - 增加 Redis 计数器，完成对用户的限流防刷
     */
    //@RequestMapping("/path")
    //@ResponseBody
    //public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
    //    // 我们的设计的商品 gooodsId 是一定大于 0 的
    //    if (user == null || goodsId < 0 || !StringUtils.hasText(captcha)) {
    //        return RespBean.error(RespBeanEnum.SESSION_ERROR);
    //    }
    //
    //    // 增加业务逻辑: 加入 Redis 计数器，完成对用户的限流防刷
    //    // 比如: 5 秒内访问次数超过 5 次，我们就认为是刷接口
    //    // 这里老师先把代码写在方法中，后面我们使用注解提高使用的通用性
    //    // uri 就是 localhost:8080/seckill/path 当中的 /seckill/path
    //    String uri = request.getRequestURI();
    //    ValueOperations valueOperations = redisTemplate.opsForValue();
    //    // 存储到 Redis 当中，key uri + ":" + user.getId()
    //    String key = uri + ":" + user.getId();
    //    Integer count = (Integer) valueOperations.get(key);
    //
    //    if (count == null) {  // 说明还没有 key,就初始化: 值为 1，过期时间为 5 秒
    //        valueOperations.set(key, 1, 5, TimeUnit.SECONDS);
    //    } else if (count < 5) { // 说明正常访问
    //        valueOperations.increment(key); // -1
    //    } else { // > 5 说明用户在刷接口
    //        return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
    //    }
    //
    //
    //    // 增加一个业务逻辑-校验用户输入的验证码是否正确
    //    boolean check = orderService.checkCaptcha(user, goodsId, captcha);
    //    if (!check) {
    //        return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
    //    }
    //
    //    String path = orderService.createPath(user, goodsId);
    //
    //    return RespBean.success(path);
    //}

    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V 6.0版本，
     * - 利用 MySQL默认的事务隔离级别【REPEATABLE-READ】
     * - 使用 优化秒杀： Redis 预减库存+Decrement
     * - 优化秒杀: 加入内存标记，避免总到 Redis 查询库存
     * - 优化秒杀: 加入消息队列，实现秒杀的异步请求
     * - 秒杀接口地址隐藏
     * - happyCaptcha 验证码
     * 这里就不需要 model 返回给下一个模板信息了。
     *
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @param path    秒杀路径
     * @return 返回到映射在 resources 下的 templates 下的页面
     */
    //@RequestMapping(value = "/{path}/doSeckill")
    //@ResponseBody
    //public RespBean doSeckill(@PathVariable("path") String path, User user, Long goodsId) {
    //    System.out.println("秒杀 V 6.0 ");
    //
    //
    //    System.out.println("从客户端发来的 path = " + path);
    //    //检查秒杀生成的路径是否和服务器一致,校验用户携带的 path 是否正确
    //    boolean b = orderService.checkPath(user, goodsId, path);
    //    if (!b) {//如果生成的路径不对，就返回错误页面.
    //        return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
    //    }
    //
    //
    //    if (null == user) { //用户没有登录
    //        return RespBean.error(RespBeanEnum.SESSION_ERROR);
    //    }
    //
    //    // 不需要使用 model 返回给下一个模板信息了。
    //    // 获取到 GoodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 判断库存
    //    if (goodsVo.getStockCount() < 1) {  // 没有库存，不可以购买
    //        return RespBean.error(RespBeanEnum.ENTRY_STOCK);
    //    }
    //
    //    // 判断用户是否复购-直接到 Redis 当中获取(因为我们抢购成功直接
    //    // 将表单信息存储到了Redis 当中了。 key表示:order:userId:goodsId  Value表示订单 seckillOrder)，
    //    // 获取对应的秒杀订单，如果有，则说明该
    //    // 用户已经桥抢购了，每人限购一个
    //    SeckillOrder o = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" +
    //            goodsVo.getId()); // 因为我们在 Redis 当中的 value值就是 SeckillOrder 订单对象，所以这里可以直接强制类型转换
    //    if (null != o) { // 不为null，说明 Redis 存在该用户订单信息，说明该用户已经抢购了该商品
    //        return RespBean.error(RespBeanEnum.REPEAT_ERROR);
    //    }
    //
    //
    //    // 对map进行判断[内存标记]，如果商品在  map 已经标记为没有库存,则直接返回,无需进行 Redis 预减
    //    if (entryStockMap.get(goodsId)) {
    //        return RespBean.error(RespBeanEnum.ENTRY_STOCK);
    //    }
    //
    //    // Redis库存预减，如果在 Redis 中预减库存,发现秒杀商品已经没有了，就直接返回
    //    // 从面减少去执行 orderService.seckill()请求，防止线程堆积，优化秒杀/高并发
    //    // 提示: Redis 的 decrement是具有原子性的，已经存在了原子性，就是一条一条执行的，不会存在，复购，多购的可能性。
    //    // 注意:这里我们要操作的 key 的是:seckillGoods:商品Id,value:该商品的库存量
    //    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
    //    if (decrement < 0) {  // 说明这个商品已经没有库存了,返回
    //
    //        // 说明当前秒杀的商品，已经没有库存
    //        entryStockMap.put(goodsId, true);
    //
    //        // 这里我们可以恢复库存为 0 ，因为后面可能会一直减下去，恢复为 0 让数据更好看一些
    //        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
    //        return RespBean.error(RespBeanEnum.ENTRY_STOCK);
    //    }
    //
    //    /*
    //    抢购，向消息队列发送秒杀请求，实现了秒杀异步请求
    //    这里我们发送秒杀消息后，立即快速返回结果【临时结果】- “比如排队中...”
    //    客户端可以通过轮询，获取到最终结果
    //    创建 SeckillMessage
    //     */
    //    SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
    //    // 将 seckillMessage 对象封装为 JSON 格式的 String 让RabbitMQ 生产者发送出去
    //    // 被消费者接受消费
    //    mqSenderMessage.sendSeckillMessage(JSONUtil.toJsonStr(seckillMessage));
    //
    //    System.out.println("秒杀 V 6.0 ");
    //
    //    return RespBean.error(RespBeanEnum.SKL_KILL_WATT);
    //
    //}


    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V 7.0版本，
     * - 利用 MySQL默认的事务隔离级别【REPEATABLE-READ】
     * - 使用 优化秒杀： Redis 预减库存+Decrement
     * - 优化秒杀: 加入内存标记，避免总到 Redis 查询库存
     * - 优化秒杀: 加入消息队列，实现秒杀的异步请求
     * - 优化: 扩展: 采用 Redis 分布式锁，控制事务
     *
     * @param model   返回给模块的 model 信息
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @return 返回到映射在 resources 下的 templates 下的页面
     */
    //@RequestMapping(value = "/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
    //    System.out.println("秒杀 V 7.0 ");
    //
    //    if (user == null) {//用户没有登录
    //        return "login";
    //    }
    //    //将user放入到model, 下一个模板可以使用
    //    model.addAttribute("user", user);
    //
    //    //获取到goodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    //判断库存
    //    if (goodsVo.getStockCount() < 1) {//没有库存
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";//错误页面
    //    }
    //
    //
    //    //判断用户是否复购-直接到Redis中,获取对应的秒杀订单,如果有,则说明已经抢购了
    //    SeckillOrder o = (SeckillOrder) redisTemplate.opsForValue()
    //            .get("order:" + user.getId() + ":" + goodsVo.getId());
    //    if (null != o) { //说明该用户已经抢购了该商品
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
    //        return "secKillFail";//错误页面
    //    }
    //
    //    //对map进行判断[内存标记],如果商品在map已经标记为没有库存，则直接返回，无需进行Redis预减
    //    if (entryStockMap.get(goodsId)) {
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";//错误页面
    //    }
    //    // 1. 获取锁,setnx
    //    // 得到一个 uuid 值，作为锁的值
    //    String uuid = UUID.randomUUID().toString();
    //
    //    // 锁放入到 Redis 当中，过期时间 3 秒
    //    Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
    //
    //    // 1.1 定义 lua脚本
    //    String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
    //            "then return redis.call('del', KEYS[1]) else return 0 end";
    //
    //    // 使用 redis 执行 lua 执行
    //    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
    //    redisScript.setScriptText(script);
    //    redisScript.setResultType(Long.class);
    //
    //
    //    // 2. 获取锁成功,查询 num 的值
    //    if (lock) {
    //        // 执行你自己的业务-这里就可以有多个操作了，都具有了原子性
    //
    //
    //        // Redis库存预减，如果在 Redis 中预减库存,发现秒杀商品已经没有了，就直接返回
    //        // 从面减少去执行 orderService.seckill()请求，防止线程堆积，优化秒杀/高并发
    //        // 提示: Redis 的 decrement是具有原子性的，已经存在了原子性，就是一条一条执行的，不会存在，复购，多购的可能性。
    //        // 注意:这里我们要操作的 key 的是:seckillGoods:商品Id,value:该商品的库存量
    //        Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
    //        if (decrement < 0) {  // 说明这个商品已经没有库存了,返回
    //
    //            // 说明当前秒杀的商品，已经没有库存
    //            entryStockMap.put(goodsId, true);
    //
    //            // 这里我们可以恢复库存为 0 ，因为后面可能会一直减下去，恢复为 0 让数据更好看一些
    //            redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
    //
    //            // 释放分布式锁,lua 为什么使用 redis+lua脚本释放锁，前面说过在 Redis 内容当中
    //            redisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);
    //
    //            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
    //            return "secKillFail"; // 返回一个错误页面
    //        }
    //
    //        // 释放分布式锁,lua 为什么使用 redis+lua脚本释放锁，前面说过在 Redis 内容当中
    //        redisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);
    //
    //        System.out.println("秒杀 V 7.0 ");
    //
    //    } else {
    //        // 3. 获取锁失败
    //        model.addAttribute("errmsg", RespBeanEnum.SET_KILL_RETRY.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    //抢购,向消息队列发送秒杀请求,实现了秒杀异步请求
    //    //这里我们发送秒杀消息后，立即快速返回结果[临时结果] - "比如排队中.."
    //    //客户端可以通过轮询，获取到最终结果
    //    //创建SeckillMessage
    //    //SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
    //    SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
    //    mqSenderMessage.sendSeckillMessage(JSONUtil.toJsonStr(seckillMessage));
    //    model.addAttribute("errmsg", "排队中...");
    //    return "secKillFail";
    //
    //}

    /**
     * 获取秒杀路径
     *
     * @param user    用户信息
     * @param goodsId 秒杀商品ID
     * @return RespBean 返回信息，携带秒杀路径 path
     * -v 4.0 增加了 happyCaptcha 验证码
     * - 增加 Redis 计数器，完成对用户的限流防刷
     * - 通用接口限流-防刷-封装为-一个注解搞定
     * second = 5, maxCount = 5 说明是在 5 秒内可以访问的最大次数是 5 次
     * needLogin = true 表示用户是否需要登录，true 表示用户需要登录
     */
    @RequestMapping("/path")
    @ResponseBody
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
        // 我们的设计的商品 gooodsId 是一定大于 0 的
        if (user == null || goodsId < 0 || !StringUtils.hasText(captcha)) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }


        // 增加一个业务逻辑-校验用户输入的验证码是否正确
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
        }

        String path = orderService.createPath(user, goodsId);

        return RespBean.success(path);
    }


    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V 8.0版本，
     * - 利用 MySQL默认的事务隔离级别【REPEATABLE-READ】
     * - 使用 优化秒杀： Redis 预减库存+Decrement
     * - 优化秒杀: 加入内存标记，避免总到 Redis 查询库存
     * - 优化秒杀: 加入消息队列，实现秒杀的异步请求
     * - 优化: 扩展: 采用 Redis 分布式锁，控制事务
     * - 优化：使用增加配置执行脚本，执行 lua 脚本
     *
     * @param model   返回给模块的 model 信息
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @return 返回到映射在 resources 下的 templates 下的页面
     */


    @Resource
    private RedisScript<Long> script;

    @RequestMapping(value = "/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId) {
        System.out.println("秒杀 V 8.0 ");

        if (user == null) {//用户没有登录
            return "login";
        }
        //将user放入到model, 下一个模板可以使用
        model.addAttribute("user", user);

        //获取到goodsVo
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        //判断库存
        if (goodsVo.getStockCount() < 1) {//没有库存
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
            return "secKillFail";//错误页面
        }


        //判断用户是否复购-直接到Redis中,获取对应的秒杀订单,如果有,则说明已经抢购了
        SeckillOrder o = (SeckillOrder) redisTemplate.opsForValue()
                .get("order:" + user.getId() + ":" + goodsVo.getId());
        if (null != o) { //说明该用户已经抢购了该商品
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";//错误页面
        }

        //对map进行判断[内存标记],如果商品在map已经标记为没有库存，则直接返回，无需进行Redis预减
        if (entryStockMap.get(goodsId)) {
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
            return "secKillFail";//错误页面
        }
        // 1. 获取锁,setnx
        // 得到一个 uuid 值，作为锁的值
        String uuid = UUID.randomUUID().toString();

        // 锁放入到 Redis 当中，过期时间 3 秒
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);

        // lua 脚本，从Spring IOC 容器当中获取了。为  script,如下代码获取到了
        /*
            @Resource
            private RedisScript<Long> script;
         */

        // 2. 获取锁成功,查询 num 的值
        if (lock) {
            // 执行你自己的业务-这里就可以有多个操作了，都具有了原子性


            // Redis库存预减，如果在 Redis 中预减库存,发现秒杀商品已经没有了，就直接返回
            // 从面减少去执行 orderService.seckill()请求，防止线程堆积，优化秒杀/高并发
            // 提示: Redis 的 decrement是具有原子性的，已经存在了原子性，就是一条一条执行的，不会存在，复购，多购的可能性。
            // 注意:这里我们要操作的 key 的是:seckillGoods:商品Id,value:该商品的库存量
            Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
            if (decrement < 0) {  // 说明这个商品已经没有库存了,返回

                // 说明当前秒杀的商品，已经没有库存
                entryStockMap.put(goodsId, true);

                // 这里我们可以恢复库存为 0 ，因为后面可能会一直减下去，恢复为 0 让数据更好看一些
                redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);

                // 释放分布式锁,lua 为什么使用 redis+lua脚本释放锁，前面说过在 Redis 内容当中
                redisTemplate.execute(script, Arrays.asList("lock"), uuid);

                model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
                return "secKillFail"; // 返回一个错误页面
            }

            // 释放分布式锁,lua 为什么使用 redis+lua脚本释放锁，前面说过在 Redis 内容当中
            redisTemplate.execute(script, Arrays.asList("lock"), uuid);

            System.out.println("秒杀 V 8.0 ");

        } else {
            // 3. 获取锁失败
            model.addAttribute("errmsg", RespBeanEnum.SET_KILL_RETRY.getMessage());
            return "secKillFail";
        }

        //抢购,向消息队列发送秒杀请求,实现了秒杀异步请求
        //这里我们发送秒杀消息后，立即快速返回结果[临时结果] - "比如排队中.."
        //客户端可以通过轮询，获取到最终结果
        //创建SeckillMessage
        //SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSenderMessage.sendSeckillMessage(JSONUtil.toJsonStr(seckillMessage));
        model.addAttribute("errmsg", "排队中...");
        return "secKillFail";

    }

    /**
     * InitializingBean 接口当中的 afterPropertiesSet 表示项目启动就自动给执行该方法当中的内容
     * 该方法是在类的所有属性，都是初始化后，自动执行的
     * 这里我们就可以将所有秒杀商品的库存量，加载到 Redis 当中
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 获取所有可以秒杀的商品信息
        List<GoodsVo> list = goodsService.findGoodsVo();
        // 先判断是否为空
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // 遍历 List,然后将秒杀商品的库存量,放入到 Redis
        // key:秒杀商品库存量对应 key:seckillGoods:商品Id,value:该商品的库存量
        list.forEach(
                goodsVo -> {
                    redisTemplate.opsForValue()
                            .set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
                    // 初始化 map
                    // 如果 goodsId: false 表示有库存
                    // 如果 goodsId: true 表示没有库存
                    entryStockMap.put(goodsVo.getId(), false);
                });

    }


    /**
     * 生成验证码
     * 注意：HappyCaptcha 执行该方法后，会自动默认将验证码放入到 Session 当中。对应 HappyCaptcha验证码的Key为“happy-captcha”。
     * 手动清理Session中存放的验证码，HappyCaptcha验证码的Key为“happy-captcha”。
     * 这里我们考虑到项目的分布式，如果将验证码存入到 Session 当中，如果采用分布式，不同机器可能
     * 登录访问的该验证码就不存在，不同的机器当中，就像我们上面设置的共享 Session 的问题是一样的
     * 所以这里我们同时也将 HappyCaptcha验证码的存储到 Redis 当中。Redis 当中验证码的key设计为:captcha:userId:goodsId
     * 同时设置超时时间 100s，过后没登录就，该验证码失效
     *
     * @param request
     * @param response
     */
    @GetMapping("/captcha")
    public void captcha(User user,
                        Long goodsId
            , HttpServletRequest request,
                        HttpServletResponse response) {
        HappyCaptcha.require(request, response)
                .style(CaptchaStyle.IMG)            //设置展现样式为图片
                .type(CaptchaType.NUMBER)            //设置验证码内容为数字
                .length(5)                            //设置字符长度为5
                .width(220)                            //设置动画宽度为220
                .height(80)                            //设置动画高度为80
                .font(Fonts.getInstance().zhFont())    //设置汉字的字体
                .build().finish();                //生成并输出验证码

        // 从 Session 当中把验证码的值，保存 Redis当中【考虑项目分布式】，同时设计验证码 100s 失效
        // Redis 当中验证码的key设计为:captcha:userId:goodsId
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId,
                (String) request.getSession().getAttribute("happy-captcha"),
                100, TimeUnit.SECONDS);
    }


}
