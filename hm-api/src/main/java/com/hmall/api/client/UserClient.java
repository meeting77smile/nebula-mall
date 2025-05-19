package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 扣减用户余额
 */
@FeignClient("user-service")
public interface UserClient {
    @PutMapping("/users/money/deduct")
    //@RequestParam("pw"):在UserController中也指定了
    void deductMoney(@RequestParam("pw") String pw,@RequestParam("amount") Integer amount);
}
