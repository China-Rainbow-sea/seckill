package com.rainbowsea.seckill.exception;


import com.rainbowsea.seckill.vo.RespBean;
import com.rainbowsea.seckill.vo.RespBeanEnum;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;

/**
 * 全局异常处理定义
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有的异常
     *
     * @param e 异常对象
     * @return RespBean
     */
    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e) {
        //如果是全局异常，正常处理
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            return RespBean.error(ex.getRespBeanEnum());
        } else if (e instanceof BindException) {  // BindException 绑定异常
            // 如果是绑定异常 ：由于我们自定义的注解只会在控制台打印错误信息，想让改信息传给前端。
            // 需要获取改异常 BindException，进行打印
            BindException ex = (BindException) e;
            RespBean respBean = RespBean.error(RespBeanEnum.BING_ERROR);
            respBean.setMessage(" 参 数 校 验 异 常 ~ ： " +
                    ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        return RespBean.error(RespBeanEnum.ERROR);
    }
}
