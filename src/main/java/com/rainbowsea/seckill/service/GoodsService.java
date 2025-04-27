package com.rainbowsea.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rainbowsea.seckill.pojo.Goods;
import com.rainbowsea.seckill.vo.GoodsVo;

import java.util.List;

/**
 *
 */
public interface GoodsService extends IService<Goods> {


    /**
     * 商品列表
     *
     * @return List<GoodsVo>
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详细
     *
     * @param goodsId
     * @return GoodsVo
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);

}
