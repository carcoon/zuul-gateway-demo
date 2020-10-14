//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.hhd.gateway.config;

import com.netflix.zuul.filters.FilterRegistry;
import java.util.Collections;
import java.util.List;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer;
import org.springframework.cloud.netflix.zuul.FiltersEndpoint;
import com.hhd.gateway.config.RibbonCommandFactoryConfiguration.RestClientRibbonConfiguration;
import com.hhd.gateway.config.RibbonCommandFactoryConfiguration.HttpClientRibbonConfiguration;
import com.hhd.gateway.config.RibbonCommandFactoryConfiguration.OkHttpRibbonConfiguration;
import org.springframework.cloud.netflix.zuul.RoutesEndpoint;
import org.springframework.cloud.netflix.zuul.RoutesMvcEndpoint;
import org.springframework.cloud.netflix.zuul.ZuulServerAutoConfiguration;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.TraceProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.ServiceRouteMapper;
import org.springframework.cloud.netflix.zuul.filters.discovery.SimpleServiceRouteMapper;
import org.springframework.cloud.netflix.zuul.filters.pre.PreDecorationFilter;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RestClientRibbonConfiguration.class, OkHttpRibbonConfiguration.class, HttpClientRibbonConfiguration.class, HttpClientConfiguration.class})

public class ZuulCustomProxyAutoConfiguration extends ZuulCustomServerAutoConfiguration {
	@Autowired(
			required = false
	)
	private List<RibbonRequestCustomizer> requestCustomizers = Collections.emptyList();
	@Autowired(
			required = false
	)
	private Registration registration;
	@Autowired
	private DiscoveryClient discovery;
	@Autowired
	private ServiceRouteMapper serviceRouteMapper;

	public ZuulCustomProxyAutoConfiguration() {
	}

	public HasFeatures zuulFeature() {
		return HasFeatures.namedFeature("Zuul (Discovery)", ZuulCustomProxyAutoConfiguration.class);
	}

//	@Bean
//	@ConditionalOnMissingBean({DiscoveryClientRouteLocator.class})
//	public DiscoveryClientRouteLocator discoveryRouteLocator() {
//		return new DiscoveryClientRouteLocator(this.server.getServletPrefix(), this.discovery, this.zuulProperties, this.serviceRouteMapper, this.registration);
//	}

	@Bean
	public PreDecorationFilter preDecorationFilter(RouteLocator routeLocator, ProxyRequestHelper proxyRequestHelper) {
		return new PreDecorationFilter(routeLocator, this.server.getServletPrefix(), this.zuulProperties, proxyRequestHelper);
	}

	@Bean
	public RibbonRoutingFilter ribbonRoutingFilter(ProxyRequestHelper helper, RibbonCommandFactory<?> ribbonCommandFactory) {
		RibbonRoutingFilter filter = new RibbonRoutingFilter(helper, ribbonCommandFactory, this.requestCustomizers);
		return filter;
	}

	@Bean
	@ConditionalOnMissingBean({SimpleHostRoutingFilter.class, CloseableHttpClient.class})
	public SimpleHostRoutingFilter simpleHostRoutingFilter(ProxyRequestHelper helper, ZuulProperties zuulProperties, ApacheHttpClientConnectionManagerFactory connectionManagerFactory, ApacheHttpClientFactory httpClientFactory) {
		return new SimpleHostRoutingFilter(helper, zuulProperties, connectionManagerFactory, httpClientFactory);
	}

	@Bean
	@ConditionalOnMissingBean({SimpleHostRoutingFilter.class})
	public SimpleHostRoutingFilter simpleHostRoutingFilter2(ProxyRequestHelper helper, ZuulProperties zuulProperties, CloseableHttpClient httpClient) {
		return new SimpleHostRoutingFilter(helper, zuulProperties, httpClient);
	}

//	@Bean
//	public ApplicationListener<ApplicationEvent> zuulDiscoveryRefreshRoutesListener() {
//		return new ZuulCustomProxyAutoConfiguration.ZuulDiscoveryRefreshListener();
//	}

	@Bean
	@ConditionalOnMissingBean({ServiceRouteMapper.class})
	public ServiceRouteMapper serviceRouteMapper() {
		return new SimpleServiceRouteMapper();
	}

	private static class ZuulDiscoveryRefreshListener implements ApplicationListener<ApplicationEvent> {
		private HeartbeatMonitor monitor;
		@Autowired
		private ZuulHandlerMapping zuulHandlerMapping;

		private ZuulDiscoveryRefreshListener() {
			this.monitor = new HeartbeatMonitor();
		}

		public void onApplicationEvent(ApplicationEvent event) {
			if (event instanceof InstanceRegisteredEvent) {
				this.reset();
			} else if (event instanceof ParentHeartbeatEvent) {
				ParentHeartbeatEvent e = (ParentHeartbeatEvent)event;
				this.resetIfNeeded(e.getValue());
			} else if (event instanceof HeartbeatEvent) {
				HeartbeatEvent e = (HeartbeatEvent)event;
				this.resetIfNeeded(e.getValue());
			}

		}

		private void resetIfNeeded(Object value) {
			if (this.monitor.update(value)) {
				this.reset();
			}

		}

		private void reset() {
			this.zuulHandlerMapping.setDirty(true);
		}
	}

	@Configuration
	@ConditionalOnClass({Endpoint.class})
	protected static class EndpointConfiguration {
		@Autowired(
				required = false
		)
		private TraceRepository traces;

		protected EndpointConfiguration() {
		}

		@ConditionalOnEnabledEndpoint("routes")
		@Bean
		public RoutesEndpoint routesEndpoint(RouteLocator routeLocator) {
			return new RoutesEndpoint(routeLocator);
		}

		@ConditionalOnEnabledEndpoint("routes")
		@Bean
		public RoutesMvcEndpoint routesMvcEndpoint(RouteLocator routeLocator, RoutesEndpoint endpoint) {
			return new RoutesMvcEndpoint(endpoint, routeLocator);
		}

		@ConditionalOnEnabledEndpoint("filters")
		@Bean
		public FiltersEndpoint filtersEndpoint() {
			FilterRegistry filterRegistry = FilterRegistry.instance();
			return new FiltersEndpoint(filterRegistry);
		}

		@Bean
		public ProxyRequestHelper proxyRequestHelper(ZuulProperties zuulProperties) {
			TraceProxyRequestHelper helper = new TraceProxyRequestHelper();
			if (this.traces != null) {
				helper.setTraces(this.traces);
			}

			helper.setIgnoredHeaders(zuulProperties.getIgnoredHeaders());
			helper.setTraceRequestBody(zuulProperties.isTraceRequestBody());
			return helper;
		}
	}

	@Configuration
	@ConditionalOnMissingClass({"org.springframework.boot.actuate.endpoint.Endpoint"})
	protected static class NoActuatorConfiguration {
		protected NoActuatorConfiguration() {
		}

		@Bean
		public ProxyRequestHelper proxyRequestHelper(ZuulProperties zuulProperties) {
			ProxyRequestHelper helper = new ProxyRequestHelper();
			helper.setIgnoredHeaders(zuulProperties.getIgnoredHeaders());
			helper.setTraceRequestBody(zuulProperties.isTraceRequestBody());
			return helper;
		}
	}
}
