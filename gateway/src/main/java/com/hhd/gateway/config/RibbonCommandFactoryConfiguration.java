//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.hhd.gateway.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.RestClientRibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
import org.springframework.cloud.netflix.zuul.filters.route.apache.HttpClientRibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.okhttp.OkHttpRibbonCommandFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

public class RibbonCommandFactoryConfiguration {
	public RibbonCommandFactoryConfiguration() {
	}

	private static class OnRibbonRestClientCondition extends AnyNestedCondition {
		public OnRibbonRestClientCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty({"ribbon.restclient.enabled"})
		static class RibbonProperty {
			RibbonProperty() {
			}
		}
	}

	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Conditional({RibbonCommandFactoryConfiguration.OnRibbonRestClientCondition.class})
	@interface ConditionalOnRibbonRestClient {
	}

	private static class OnRibbonOkHttpClientCondition extends AnyNestedCondition {
		public OnRibbonOkHttpClientCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty({"ribbon.okhttp.enabled"})
		static class RibbonProperty {
			RibbonProperty() {
			}
		}
	}

	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Conditional({RibbonCommandFactoryConfiguration.OnRibbonOkHttpClientCondition.class})
	@interface ConditionalOnRibbonOkHttpClient {
	}

	private static class OnRibbonHttpClientCondition extends AnyNestedCondition {
		public OnRibbonHttpClientCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty(
				name = {"ribbon.httpclient.enabled"},
				matchIfMissing = true
		)
		static class RibbonProperty {
			RibbonProperty() {
			}
		}
	}

	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Conditional({RibbonCommandFactoryConfiguration.OnRibbonHttpClientCondition.class})
	@interface ConditionalOnRibbonHttpClient {
	}

	@Configuration
	@RibbonCommandFactoryConfiguration.ConditionalOnRibbonHttpClient
	protected static class HttpClientRibbonConfiguration {
		@Autowired(
				required = false
		)
		private Set<ZuulFallbackProvider> zuulFallbackProviders = Collections.emptySet();

		protected HttpClientRibbonConfiguration() {
		}

		@Bean
		@ConditionalOnMissingBean
		public RibbonCommandFactory<?> ribbonCommandFactory(SpringClientFactory clientFactory, ZuulProperties zuulProperties) {
			return new HttpClientRibbonCommandFactory(clientFactory, zuulProperties, this.zuulFallbackProviders);
		}
	}

	@Configuration
	@RibbonCommandFactoryConfiguration.ConditionalOnRibbonOkHttpClient
	@ConditionalOnClass(
			name = {"okhttp3.OkHttpClient"}
	)
	protected static class OkHttpRibbonConfiguration {
		@Autowired(
				required = false
		)
		private Set<ZuulFallbackProvider> zuulFallbackProviders = Collections.emptySet();

		protected OkHttpRibbonConfiguration() {
		}

		@Bean
		@ConditionalOnMissingBean
		public RibbonCommandFactory<?> ribbonCommandFactory(SpringClientFactory clientFactory, ZuulProperties zuulProperties) {
			return new OkHttpRibbonCommandFactory(clientFactory, zuulProperties, this.zuulFallbackProviders);
		}
	}

	@Configuration
	@RibbonCommandFactoryConfiguration.ConditionalOnRibbonRestClient
	protected static class RestClientRibbonConfiguration {
		@Autowired(
				required = false
		)
		private Set<ZuulFallbackProvider> zuulFallbackProviders = Collections.emptySet();

		protected RestClientRibbonConfiguration() {
		}

		@Bean
		@ConditionalOnMissingBean
		public RibbonCommandFactory<?> ribbonCommandFactory(SpringClientFactory clientFactory, ZuulProperties zuulProperties) {
			return new RestClientRibbonCommandFactory(clientFactory, zuulProperties, this.zuulFallbackProviders);
		}
	}
}
