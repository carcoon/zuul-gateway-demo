package com.hhd.gateway.web;

import com.hhd.gateway.event.RefreshRouteService;
import com.hhd.gateway.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ssc on 2020/10/13.
 */
@RestController
public class CustomZuulController {

    @Autowired
    RefreshRouteService refreshRouteService;

    @RequestMapping(value = "/refreshRoute",method={RequestMethod.GET,RequestMethod.POST})
    public String refreshRoute(){
        refreshRouteService.refreshRoute();
        return "refreshRoute";
    }

    @Autowired
    ZuulHandlerMapping zuulHandlerMapping;

    @RequestMapping(value = "/watchNowRoute",method={RequestMethod.GET,RequestMethod.POST})
    public String watchNowRoute(){
        //可以用debug模式看里面具体是什么
        Map<String, Object> handlerMap = zuulHandlerMapping.getHandlerMap();
        List<Object> list = new ArrayList<>(handlerMap.values());
        return "watchNowRoute:";
    }



}
