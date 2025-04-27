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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
@RequestMapping("/seckill")
public class SeckillController {


    // 装配需要的组件/对象
    @Resource
    private GoodsService goodsService;

    @Resource
    private SeckillOrderService seckillOrderService;


    @Resource
    private OrderService orderService;


    /**
     * 方法: 处理用户抢购请求/秒杀
     * 说明: 我们先完成一个 V1.0版本，后面在高并发的情况下，还会继续优化
     *
     * @param model   返回给模块的 model 信息
     * @param user    User 通过用户使用了，自定义参数解析器获取 User 对象，
     * @param goodsId 秒杀商品的 ID 信息
     * @return 返回到映射在 resources 下的 templates 下的页面
     */
    @RequestMapping(value = "/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId) {
        System.out.println("秒杀 V 1.0 ");

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

        // 判断用户是否复购-判断当前购买用户的ID和购买商品id是否已经在商品秒杀表当中存在了。
        SeckillOrder seckillOrder = seckillOrderService.getOne(
                new QueryWrapper<SeckillOrder>()
                        .eq("user_id", user.getId())
                        .eq("goods_id", goodsId)
        );  // 这里的 column 不可以随便写，要对应上数据表当中的对应的“字段名”

        if (seckillOrder != null) {  // 不为 null ，说明用户购买过了，进行了复购
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
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

        System.out.println("秒杀 V 1.0 ");

        return "orderDetail";  // 进入到订单详情页


    }
}
