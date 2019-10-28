package com.tonytaotao.springboot.dubbo.order.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.tonytaotao.springboot.dubbo.api.ApiAccountService;
import com.tonytaotao.springboot.dubbo.api.ApiCommodityService;
import com.tonytaotao.springboot.dubbo.order.order.entity.UserOrder;
import com.tonytaotao.springboot.dubbo.order.order.mapper.OrderMapper;
import com.tonytaotao.springboot.dubbo.order.order.service.OrderService;
import com.tonytaotao.springboot.dubbo.order.order.vo.PlaceOrderReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务接口实现类
 * </p>
 *
 * @author wujintao
 * @since 2019-10-25
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, UserOrder> implements OrderService {

    /**
     * 不能使用@Autowired自动注入，而要用Dubbo提供的@Reference注解
     */
    @Reference(interfaceClass = ApiAccountService.class, version = "1.0.0", retries = 3)
    private ApiAccountService apiAccountService;

    @Reference(interfaceClass = ApiCommodityService.class, version = "1.0.0", retries = 3)
    private ApiCommodityService apiCommodityService;

    @Override
    @LcnTransaction
    @Transactional
    public Boolean placeOrder(PlaceOrderReq placeOrderReq) {

        double money = 2;

        boolean subBalanceBoo = apiAccountService.subAccountBalance(placeOrderReq.getUserId(), money);
        boolean subQuality = apiCommodityService.subCommodityStock(placeOrderReq.getCommodityId(), placeOrderReq.getQuantity());

        UserOrder userOrder = new UserOrder();
        userOrder.setUserId(placeOrderReq.getUserId())
                .setCommodityId(placeOrderReq.getCommodityId())
                .setQuantity(placeOrderReq.getQuantity())
                .setTotalMoney(money);

        boolean result = this.save(userOrder);

        return result;


    }

}
