package com.rainbowsea.seckill.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rainbowsea.seckill.pojo.Goods;
import com.rainbowsea.seckill.vo.GoodsVo;

import java.util.List;

/**
 * @Entity generator.domain.Goods
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 获取商品列表
     *
     * @return List<GoodsVo>
     */
    List<GoodsVo> findGoodsVo();


    /**
     * 获取商品详细
     * @param goodsId
     * @return GoodsVo
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}




