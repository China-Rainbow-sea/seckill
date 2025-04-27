package com.rainbowsea.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainbowsea.seckill.mapper.GoodsMapper;
import com.rainbowsea.seckill.pojo.Goods;
import com.rainbowsea.seckill.service.GoodsService;
import com.rainbowsea.seckill.vo.GoodsVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods>
        implements GoodsService {


    @Resource
    private GoodsMapper goodsMapper;


    /**
     * 获取商品列表
     *
     * @return List<GoodsVo>
     */
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    /**
     * 获取商品详情
     *
     * @param goodsId
     * @return GoodsVo
     */
    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }


}