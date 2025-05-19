package com.hmall.gateway.router;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 动态路由监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouterLoader {
    private final RouteDefinitionWriter writer;//用于更新路由表
    private final NacosConfigManager nacosConfigManager;//利用Nacos官方写好的方法来获取ConfigService

    //路由配置文件的id和分组（即指定操作的是哪个配置文件）
    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    //保存先前更新过的路由id，便于再次更新路由表之前先清空原来的路由表
    private final Set<String> routeIds = new HashSet<String>();

    @PostConstruct //使得当前类一初始化就会调用该方法
    public void initRouteConfigListener() throws NacosException{
        //1.注册监听器并首次拉取配置
        //configInfo为获取到的配置信息，未防止路由表一直未发生变化（路由表中无更新，即无内容），则一开始就要获取配置信息并更新
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {//5000表示超时时间
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {//configInfo：ConfigService返回的最新配置信息
                        //当监听器监听到路由表变化，则更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        //2.首次启动时，先更新一次配置
        updateConfigInfo(configInfo);
    }
    private void updateConfigInfo(String configInfo){
        log.debug("监听到路由配置变更,{}",configInfo);
        //1.反序列化，将路由配置信息从String类型转为JOSN格式，便于转换成RouteDefinition从而获取路由表信息
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo,RouteDefinition.class);
        //2.更新之前先清空原来的路由表，即将新内容覆盖旧内容（因为更新不一定是新增路由表信息，也可能是删除，因此采取覆盖的方式能考虑到所有情况）
        //2.1清除旧路由
        for(String routeId : routeIds){
            //删除routeId对应的路由
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();//清空原路由id集合
        //2.2判断是否有新的路由要更新
        if(CollUtil.isEmpty(routeDefinitions)){
            //无新路由配置，直接结束
            return;
        }
        //3.更新路由
        routeDefinitions.forEach(routeDefinition -> {
            //3.1更新路由
            writer.save(Mono.just(routeDefinition)).subscribe();
            //3.2记录；路由id，方便将来删除
            routeIds.add(routeDefinition.getId());
        });
    }
}
