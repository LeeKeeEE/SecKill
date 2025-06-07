package com.seckill.lab.seckillsystem.vo;

import com.seckill.lab.seckillsystem.entity.Product;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@ToString
public class SeckillActivityVo {

    private Long id;

    private String name;

    public void setProductVo(Product product) {
        if (product == null) {
            this.productVo = null;
            return;
        }
        ProductVo tempVo = new ProductVo();
        BeanUtils.copyProperties(product, tempVo);
        this.productVo = tempVo;
    }

    private ProductVo productVo;

    private BigDecimal seckillPrice;

    private Integer stockCount;

    private Date startTime;

    private Date endTime;

    private Integer status;


}
