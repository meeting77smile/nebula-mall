package com.hmall.api.client;
import com.hmall.api.client.fallback.ItemClientFallback;
import com.hmall.api.config.DefaultFeignConfig;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service",configuration = DefaultFeignConfig.class,
            fallbackFactory = ItemClientFallback.class)    //声明服务名称
public interface ItemClient {

    //根据id批量查询商品
    //@RequestParam("ids")Collection<Long> ids：声明请求参数
    //List<ItemDTO>：返回值类型

    @GetMapping("/items")   //声明请求方式+声明请求路径
    List<ItemDTO> queryItemByIds(@RequestParam("ids")Collection<Long> ids);

    //批量扣减库存
    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);
}
