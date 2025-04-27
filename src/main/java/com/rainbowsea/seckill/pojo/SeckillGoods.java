package com.rainbowsea.seckill.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @TableName t_seckill_goods
 */
@TableName(value ="t_seckill_goods")
@Data
public class SeckillGoods implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long goodsId;

    /**
     * 
     */
    private BigDecimal seckillPrice;

    /**
     * 
     */
    private Integer stockCount;

    /**
     * 
     */
    private Date startDate;

    /**
     * 
     */
    private Date endDate;


}