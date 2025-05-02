package com.rainbowsea.seckill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rainbowsea.seckill.pojo.Order;
import com.rainbowsea.seckill.pojo.SeckillOrder;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.GoodsService;
import com.rainbowsea.seckill.service.OrderService;
import com.rainbowsea.seckill.service.SeckillOrderService;
import com.rainbowsea.seckill.vo.GoodsVo;
import com.rainbowsea.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

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

    // 定义 map- 记录秒杀商品
    private HashMap<Long, Boolean> entryStockMap = new HashMap<>();

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
    @RequestMapping(value = "/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId) {
        System.out.println("秒杀 V 4.0 ");

        // 定义 map - 记录秒杀商品是否还有库存

        if (null == user) { //用户没有登录
            return "login";
        }

        // 登录了，则返回用户信息给下一个模板内容
        model.addAttribute("user", user);

        // 获取到 GoodsVo
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 判断库存
        if (goodsVo.getStockCount() < 1) {  // 没有库存，不可以购买
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
            return "secKillFail"; // 返回一个错误页面
        }

        // 判断用户是否复购-直接到 Redis 当中获取(因为我们抢购成功直接
        // 将表单信息存储到了Redis 当中了。 key表示:order:userId:goodsId  Value表示订单 seckillOrder)，
        // 获取对应的秒杀订单，如果有，则说明该
        // 用户已经桥抢购了，每人限购一个
        SeckillOrder o = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" +
                goodsVo.getId()); // 因为我们在 Redis 当中的 value值就是 SeckillOrder 订单对象，所以这里可以直接强制类型转换
        if (null != o) { // 不为null，说明 Redis 存在该用户订单信息，说明该用户已经抢购了该商品
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
            return "secKillFail"; // 返回一个错误页面
        }


        // 对map进行判断[内存标记]，如果商品在  map 已经标记为没有库存,则直接返回,无需进行 Redis 预减
        if (entryStockMap.get(goodsId)) {
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
            return "secKillFail"; // 返回一个错误页面
        }

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
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage()); // 将错误信息返回给下一页的模板当中
            return "secKillFail"; // 返回一个错误页面
        }

        // 抢购
        Order order = orderService.seckill(user, goodsVo);
        if (order == null) { // 说明抢购失败了，由于什么原因
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
            return "secKillFail"; // 返回一个错误页面
        }

        // 走到这里，说明抢购成功了，将信息，通过 model 返回给页面
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);

        System.out.println("秒杀 V 4.0 ");

        return "orderDetail";  // 进入到订单详情页

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
}
