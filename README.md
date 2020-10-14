# zuul-gateway-demo

# Zuul 一般用法
一般的，我们如果使用Spring Cloud Zuul 进行路由配置，类似于下面的样子:
~~~
server.contextPath=/
zuul.routes.books.serviceId=server-book
zuul.routes.books.path=/book/**
zuul.routes.books.stripPrefix=true
zuul.routes.vehicles.serviceId=server-vehicle
zuul.routes.vehicles.path=/vehicle/**
zuul.routes.vehicles.stripPrefix=true
zuul.routes.baidu.url=https://www.baidu.com
zuul.routes.baidu.path=/baidu/**
~~~
当我们要新增或者改变一个网关路由时，我们不得不停止网关服务，修改配置文件，保存再重新启动网关服务，这样才能让我们新的设置生效。在生产环境，为了一个小小的路由变更，就这样的停止再重启。实在是让人受不了。我已经忍了很长时间了。接下来，看看我们怎么能做到动态配置网关路由，让网关路由配置在服务不需要重启的情况生效。
# 数据库建表
~~~
create table `gateway_api_define` (
  `id` varchar(50) not null,
  `path` varchar(255) not null,
  `service_id` varchar(50) default null,
  `url` varchar(255) default null,
  `retryable` tinyint(1) default null,
  `enabled` tinyint(1) not null,
  `strip_prefix` int(11) default null,
  `api_name` varchar(255) default null,
  primary key (`id`)
) engine=innodb default charset=utf8;
~~~

# 定义CustomRouteLocator类 和ZuulRouteVO 实体类
主要的是locateRoutes和locateRoutesFromDB这两个函数，locateRoutes是从SimpleRouteLocator Override过来的，先装载配置文件里面的路由信息，在从数据库里面获取路由信息，最后都是保存在SimpleRoteLocator 的AtomicReference<Map<String, ZuulRoute>> routes属性中，注意routes是类型，它是可以保证线程安全的。
~~~
package com.hhd.gateway.zuul;

import com.hhd.gateway.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ssc on 2020/10/13.
 */
public class CustomRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator{

    public final static Logger logger = LoggerFactory.getLogger(CustomRouteLocator.class);

    private JdbcTemplate jdbcTemplate;

    private ZuulProperties properties;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public CustomRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
        this.properties = properties;
        logger.info("servletPath:{}",servletPath);
    }


    @Override
    public void refresh() {
        doRefresh();
    }

    @Override
    protected Map<String, ZuulRoute> locateRoutes() {
        LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
        //从application.properties中加载路由信息
        routesMap.putAll(super.locateRoutes());
        //从db中加载路由信息
        routesMap.putAll(locateRoutesFromDB());
        //优化一下配置
        LinkedHashMap<String, ZuulRoute> values = new LinkedHashMap<>();
        for (Map.Entry<String, ZuulRoute> entry : routesMap.entrySet()) {
            String path = entry.getKey();
            // Prepend with slash if not already present.
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (StringUtils.hasText(this.properties.getPrefix())) {
                path = this.properties.getPrefix() + path;
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
            }
            values.put(path, entry.getValue());
        }
        logger.warn(GsonUtil.toJson(values));
        return values;
    }

    private Map<String, ZuulRoute> locateRoutesFromDB(){
        Map<String, ZuulRoute> routes = new LinkedHashMap<>();
        List<ZuulRouteVO> results = jdbcTemplate.query("select * from gateway_api_define where enabled = true ",new BeanPropertyRowMapper<>(ZuulRouteVO.class));
        for (ZuulRouteVO result : results) {
            if(org.apache.commons.lang3.StringUtils.isBlank(result.getPath()) || org.apache.commons.lang3.StringUtils.isBlank(result.getUrl()) ){
                continue;
            }
            ZuulRoute zuulRoute = new ZuulRoute();
            try {
                org.springframework.beans.BeanUtils.copyProperties(result,zuulRoute);
            } catch (Exception e) {
                logger.error("=============load zuul route info from db with error==============",e);
            }
            routes.put(zuulRoute.getPath(),zuulRoute);
        }
        return routes;
    }

    public static class ZuulRouteVO {

        /**
         * The ID of the route (the same as its map key by default).
         */
        private String id;

        /**
         * The path (pattern) for the route, e.g. /foo/**.
         */
        private String path;

        /**
         * The service ID (if any) to map to this route. You can specify a physical URL or
         * a service, but not both.
         */
        private String serviceId;

        /**
         * A full physical URL to map to the route. An alternative is to use a service ID
         * and service discovery to find the physical address.
         */
        private String url;

        /**
         * Flag to determine whether the prefix for this route (the path, minus pattern
         * patcher) should be stripped before forwarding.
         */
        private boolean stripPrefix = true;

        /**
         * Flag to indicate that this route should be retryable (if supported). Generally
         * retry requires a service ID and ribbon.
         */
        private Boolean retryable;

        private String apiName;

        private Boolean enabled;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isStripPrefix() {
            return stripPrefix;
        }

        public void setStripPrefix(boolean stripPrefix) {
            this.stripPrefix = stripPrefix;
        }

        public Boolean getRetryable() {
            return retryable;
        }

        public void setRetryable(Boolean retryable) {
            this.retryable = retryable;
        }

        public String getApiName() {
            return apiName;
        }

        public void setApiName(String apiName) {
            this.apiName = apiName;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
}

~~~

# 增加CustomZuulConfig类
主要是为了配置CustomRouteLocator

CustomerRouteLocator 去数据库获取路由配置信息，需要一个JdbcTemplate Bean。this.zuulProperties 就是配置文件里面的路由配置，应该是网关服务启动时自动就获取过来的。
~~~
@Configuration
public class CustomZuulConfig {

    @Autowired
    ZuulProperties zuulProperties;
    @Autowired
    ServerProperties server;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Bean
    public CustomRouteLocator routeLocator() {
        CustomRouteLocator routeLocator = new CustomRouteLocator(this.server.getServletPath(), this.zuulProperties);
        routeLocator.setJdbcTemplate(jdbcTemplate);
        return routeLocator;
    }

}

~~~
# 配置JDBC 
~~~
spring.datasource.url=jdbc:log4jdbc:mysql://127.0.0.1:3306/h_cloud?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=111111
spring.datasource.driver-class-name=net.sf.log4jdbc.DriverSpy
~~~

# 增加RefreshRouteService类，用于实现数据库路由信息的刷新

~~~
@Service
public class RefreshRouteService {
    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    RouteLocator routeLocator;

    public void refreshRoute() {
        RoutesRefreshedEvent routesRefreshedEvent = new RoutesRefreshedEvent(routeLocator);
        publisher.publishEvent(routesRefreshedEvent);

    }
}
~~~
# 增加CustomZuulController
提供从浏览器访问的刷新功能

~~~

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
~~~
上面两个实现的功能是，在数据库里面新增或者修改路由信息，通过上面的功能进行刷新。
# 与Eureka 结合
所有微服务和zuul 的Pom 中增加eureka-client依赖
~~~
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
~~~
所有微服务和zuul 的配置文件中增加
~~~
eureka.client.serviceUrl.defaultZone=http://127.0.0.1:8501/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.instanceId=${spring.application.name}:${spring.cloud.client.ipAddress}:${server.port}

#租期到期时间，默认90秒
eureka.instance.lease-expiration-duration-in-seconds=30
#租赁更新时间间隔，默认30，即30秒发送一次心跳
eureka.instance.lease-renewal-interval-in-seconds=10
~~~
# 配置application
使用@EnableZuulProxy 注解开启route
~~~
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.ZuulProxyAutoConfiguration;
import org.springframework.cloud.netflix.zuul.ZuulServerAutoConfiguration;

@EnableZuulProxy
@SpringBootApplication(exclude = {ZuulProxyAutoConfiguration.class, ZuulServerAutoConfiguration.class})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
~~~
# 在配置数据库
在数据库里插入数据
~~~
INSERT INTO `gateway_api_define`(`id`, `path`, `service_id`, `url`, `retryable`, `enabled`, `strip_prefix`, `api_name`) VALUES ('book', '/book/**', 'server-book', NULL, 0, 1, 1, NULL);
INSERT INTO `gateway_api_define`(`id`, `path`, `service_id`, `url`, `retryable`, `enabled`, `strip_prefix`, `api_name`) VALUES ('pppp', '/pppp/**', NULL, 'http://localhost:8503', 0, 1, 1, NULL);
INSERT INTO `gateway_api_define`(`id`, `path`, `service_id`, `url`, `retryable`, `enabled`, `strip_prefix`, `api_name`) VALUES ('vehicle', '/vehicle/**', 'server-vehicle', NULL, 0, 1, 1, NULL);
INSERT INTO `gateway_api_define`(`id`, `path`, `service_id`, `url`, `retryable`, `enabled`, `strip_prefix`, `api_name`) VALUES ('xxxx', '/xxxx/**', NULL, 'http://localhost:8504', 0, 1, 1, NULL);
~~~
# 在controller中执行刷新
在地址栏执行
~~~
http://localhost:8502/refreshRoute
~~~
配置立刻生效。

注意：zuul 服务默认是60秒执行一次刷新。所以就算不在controller中执行刷新，1分钟后也对自动刷新。因为ZuulRefreshListener 中有个心跳会定时刷新。
~~~
private static class ZuulRefreshListener implements ApplicationListener<ApplicationEvent> {
        @Autowired
        private ZuulHandlerMapping zuulHandlerMapping;
        private HeartbeatMonitor heartbeatMonitor;

        private ZuulRefreshListener() {
            this.heartbeatMonitor = new HeartbeatMonitor();
        }

        public void onApplicationEvent(ApplicationEvent event) {
            if (!(event instanceof ContextRefreshedEvent) && !(event instanceof RefreshScopeRefreshedEvent) && !(event instanceof RoutesRefreshedEvent)) {
                if (event instanceof HeartbeatEvent && this.heartbeatMonitor.update(((HeartbeatEvent)event).getValue())) {
                    this.zuulHandlerMapping.setDirty(true);
                }
            } else {
                this.zuulHandlerMapping.setDirty(true);
            }

        }
    }
~~~
# 问题：
使用过程中发现路由是每分钟同时刷新两次
## 问题分析及源码跟踪
原因是@EnableZuulProxy 是通过ZuulProxyAutoConfiguration 注入的。ZuulProxyAutoConfiguration 是继承ZuulServerAutoConfiguration 的。可是

ZuulServerAutoConfiguration 声明了ZuulRefreshListener，而ZuulServerAutoConfiguration 声明了ZuulDiscoveryRefreshListener 。两个监听都有对心跳的处理。所以重复执行。
## 解决办法
重写这两个类。只保留一个心跳处理或者两个都不要。

## 代码实现
1.RibbonCommandFactoryConfiguration 复制代码到config目录，代码不做修改。主要目的是为了自动配置类引用。


2. config 目录创建ZuulCustomServerAutoConfiguration 类



在这个类里注释了onApplicationEvent 事件里的心跳处理部分。
3. config目录下创建ZuulCustomProxyAutoConfiguration类

	
ZuulCustomProxyAutoConfiguration 继承了ZuulCustomServerAutoConfiguration
在这个类里注释了zuulDiscoveryRefreshRoutesListener 和discoveryRouteLocator
如果保留每分钟心跳刷新路由的功能，可以继承ZuulServerAutoConfiguration类。

4. 修改application注解
~~~
@SpringBootApplication(exclude = {ZuulProxyAutoConfiguration.class, ZuulServerAutoConfiguration.class})
~~~

