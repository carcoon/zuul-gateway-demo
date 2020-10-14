//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.hhd.gateway.config;

import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.http.ZuulServlet;
import com.netflix.zuul.monitoring.CounterFactory;
import com.netflix.zuul.monitoring.TracerFactory;
import java.util.Collection;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.ZuulFilterInitializer;
import org.springframework.cloud.netflix.zuul.ZuulRouteApplicationContextInitializer;
import org.springframework.cloud.netflix.zuul.filters.CompositeRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter;
import org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.DebugFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.FormBodyWrapperFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.Servlet30WrapperFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.ServletDetectionFilter;
import org.springframework.cloud.netflix.zuul.filters.route.SendForwardFilter;
import org.springframework.cloud.netflix.zuul.metrics.DefaultCounterFactory;
import org.springframework.cloud.netflix.zuul.metrics.EmptyCounterFactory;
import org.springframework.cloud.netflix.zuul.metrics.EmptyTracerFactory;
import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
@EnableConfigurationProperties({ZuulProperties.class})
@ConditionalOnClass({ZuulServlet.class})
@Import({ServerPropertiesAutoConfiguration.class})
public class ZuulCustomServerAutoConfiguration {
	@Autowired
	protected ZuulProperties zuulProperties;
	@Autowired
	protected ServerProperties server;
	@Autowired(
			required = false
	)
	private ErrorController errorController;

	public ZuulCustomServerAutoConfiguration() {
	}

	@Bean
	public HasFeatures zuulFeature() {
		return HasFeatures.namedFeature("Zuul (Simple)", ZuulCustomServerAutoConfiguration.class);
	}

	@Bean
	@Primary
	public CompositeRouteLocator primaryRouteLocator(Collection<RouteLocator> routeLocators) {
		return new CompositeRouteLocator(routeLocators);
	}

	@Bean
	@ConditionalOnMissingBean({SimpleRouteLocator.class})
	public SimpleRouteLocator simpleRouteLocator() {
		return new SimpleRouteLocator(this.server.getServletPrefix(), this.zuulProperties);
	}

	@Bean
	public ZuulController zuulController() {
		return new ZuulController();
	}

	@Bean
	public ZuulHandlerMapping zuulHandlerMapping(RouteLocator routes) {
		ZuulHandlerMapping mapping = new ZuulHandlerMapping(routes, this.zuulController());
		mapping.setErrorController(this.errorController);
		return mapping;
	}

	@Bean
	public ApplicationListener<ApplicationEvent> zuulRefreshRoutesListener() {
		return new ZuulCustomServerAutoConfiguration.ZuulRefreshListener();
	}

	@Bean
	@ConditionalOnMissingBean(
			name = {"zuulServlet"}
	)
	public ServletRegistrationBean zuulServlet() {
		ServletRegistrationBean servlet = new ServletRegistrationBean(new ZuulServlet(), new String[]{this.zuulProperties.getServletPattern()});
		servlet.addInitParameter("buffer-requests", "false");
		return servlet;
	}

	@Bean
	public ServletDetectionFilter servletDetectionFilter() {
		return new ServletDetectionFilter();
	}

	@Bean
	public FormBodyWrapperFilter formBodyWrapperFilter() {
		return new FormBodyWrapperFilter();
	}

	@Bean
	public DebugFilter debugFilter() {
		return new DebugFilter();
	}

	@Bean
	public Servlet30WrapperFilter servlet30WrapperFilter() {
		return new Servlet30WrapperFilter();
	}

	@Bean
	public SendResponseFilter sendResponseFilter(ZuulProperties properties) {
		return new SendResponseFilter(this.zuulProperties);
	}

	@Bean
	public SendErrorFilter sendErrorFilter() {
		return new SendErrorFilter();
	}

	@Bean
	public SendForwardFilter sendForwardFilter() {
		return new SendForwardFilter();
	}

	@Bean
	@ConditionalOnProperty(
			value = {"zuul.ribbon.eager-load.enabled"},
			matchIfMissing = false
	)
	public ZuulRouteApplicationContextInitializer zuulRoutesApplicationContextInitiazer(SpringClientFactory springClientFactory) {
		return new ZuulRouteApplicationContextInitializer(springClientFactory, this.zuulProperties);
	}

	private static class ZuulRefreshListener implements ApplicationListener<ApplicationEvent> {
		@Autowired
		private ZuulHandlerMapping zuulHandlerMapping;
		private HeartbeatMonitor heartbeatMonitor;

		private ZuulRefreshListener() {
			this.heartbeatMonitor = new HeartbeatMonitor();
		}

		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			if (!(event instanceof ContextRefreshedEvent) && !(event instanceof RefreshScopeRefreshedEvent) && !(event instanceof RoutesRefreshedEvent)) {
//				if (event instanceof HeartbeatEvent && this.heartbeatMonitor.update(((HeartbeatEvent)event).getValue())) {
//					this.zuulHandlerMapping.setDirty(true);
//				}
			} else {
				this.zuulHandlerMapping.setDirty(true);
			}

		}
	}

	@Configuration
	protected static class ZuulMetricsConfiguration {
		protected ZuulMetricsConfiguration() {
		}

		@Bean
		@ConditionalOnMissingBean({CounterFactory.class})
		public CounterFactory counterFactory() {
			return new EmptyCounterFactory();
		}

		@ConditionalOnMissingBean({TracerFactory.class})
		@Bean
		public TracerFactory tracerFactory() {
			return new EmptyTracerFactory();
		}
	}

	@Configuration
	@ConditionalOnClass({CounterService.class})
	protected static class ZuulCounterFactoryConfiguration {
		protected ZuulCounterFactoryConfiguration() {
		}

		@Bean
		@ConditionalOnBean({CounterService.class})
		public CounterFactory counterFactory(CounterService counterService) {
			return new DefaultCounterFactory(counterService);
		}
	}

	@Configuration
	protected static class ZuulFilterConfiguration {
		@Autowired
		private Map<String, ZuulFilter> filters;

		protected ZuulFilterConfiguration() {
		}

		@Bean
		public ZuulFilterInitializer zuulFilterInitializer(CounterFactory counterFactory, TracerFactory tracerFactory) {
			FilterLoader filterLoader = FilterLoader.getInstance();
			FilterRegistry filterRegistry = FilterRegistry.instance();
			return new ZuulFilterInitializer(this.filters, counterFactory, tracerFactory, filterLoader, filterRegistry);
		}
	}
}
