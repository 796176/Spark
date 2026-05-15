/*
 * Spark - The inventory management application
 * Copyright (C) 2026 Yegore Vlussove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.example.spark.gateway.web.main;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManagerFactory;
import org.example.spark.gateway.web.controllers.*;
import org.example.spark.gateway.web.converters.*;
import org.example.spark.gateway.web.interactors.AccountDataAccess;
import org.example.spark.gateway.web.interactors.AccountRepositoryReplicaManager;
import org.example.spark.gateway.web.interactors.SessionDataAccess;
import org.example.spark.gateway.web.interactors.UploadedFileDataAccess;
import org.example.spark.gateway.web.messaging.*;
import org.example.spark.gateway.web.persistence.JPAAccountDataAccess;
import org.example.spark.gateway.web.persistence.JPAAccountRepositoryReplicaManager;
import org.example.spark.gateway.web.persistence.JPASessionDataAccess;
import org.example.spark.gateway.web.persistence.JPAUploadedFileDataAccess;
import org.example.spark.gateway.web.proxies.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true, rollbackOn = RollbackOn.ALL_EXCEPTIONS)
@ComponentScan(basePackages = {"org.example.spark.gateway.web.controllers", "org.example.spark.gateway.web.validators"})
@EntityScan(basePackages = {"org.example.spark.gateway.web.models"})
public class WebGatewayConfiguration extends SpringBootServletInitializer implements WebMvcConfigurer, WebApplicationInitializer {

	@Bean
	public AccountRequestController accountRequestController() {
		return new AccountRequestController();
	}

	@Bean
	public ValidatingControllerAdvising validatorAspect() {
		return new ValidatingControllerAdvising();
	}

	@Override
	public void configureViewResolvers(@Nonnull ViewResolverRegistry viewResolverRegistry) {
		viewResolverRegistry.jsp("/WEB-INF/jsp/", ".jsp");
	}

	@Nonnull
	@Override
	protected SpringApplicationBuilder configure(@Nonnull SpringApplicationBuilder application) {
		return application.sources(WebGatewayConfiguration.class);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) {
		return http
			.logout(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().anonymous())
			.csrf(Customizer.withDefaults())
			.headers(Customizer.withDefaults())
			.addFilterAfter(new LogOutFilter(), CsrfFilter.class)
			.build();
	}

	@Order(50)
	@Bean
	HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}

	@Order(100)
	@Bean
	SessionRecoveryFilter sessionRecoveryFilter(SessionDataAccess sessionDataAccess) {
		return new SessionRecoveryFilter(sessionDataAccess);
	}

	@Override
	public void addResourceHandlers(@Nonnull ResourceHandlerRegistry resourceHandlerRegistry) {
		resourceHandlerRegistry
			.addResourceHandler("/static/**")
			.addResourceLocations("/static");
	}

	@Override
	public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
		builder
			.addCustomConverter(new SignInFormConverter())
			.addCustomConverter(new LogInFormConverter())
			.addCustomConverter(new NewOrderFormConverter())
			.addCustomConverter(new PlacedOrderFormConverter())
			.addCustomConverter(new AccountManagementFormConverter())
			.addCustomConverter(new ItemManagementFormConverter())
			.addCustomConverter(new OrderManagementFormConverter())
			.addCustomConverter(new CreatingAccountFormConverter())
			.addCustomConverter(new CreatingItemFormConverter())
			.addCustomConverter(new ErrorMessageResponseObjectConverter())
			.addCustomConverter(new RedirectResponseObjectConverter());
	}

	@Bean
	ConnectionFactory connectionFactory(
		@Value("${org.example.spark.rmq.address}") String address,
		@Value("${org.example.spark.rmq.port}") int port,
		@Value("${org.example.spark.rmq.username}") String username,
		@Value("${org.example.spark.rmq.password}") String password
	) {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(address);
		connectionFactory.setPort(port);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		return connectionFactory;
	}

	@Bean
	@Scope("prototype")
	Connection connection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
		return connectionFactory.newConnection();
	}

	@Bean
	TransactionManager transactionManager() {
		return new JpaTransactionManager();
	}

	@Bean
	AccountRepositoryReplicaManager accountRepositoryReplicaManager(EntityManagerFactory entityManagerFactory) {
		return new JPAAccountRepositoryReplicaManager(entityManagerFactory);
	}

	@Bean
	SessionDataAccess sessionDataAccess(EntityManagerFactory entityManagerFactory) {
		return new JPASessionDataAccess(entityManagerFactory);
	}

	@Bean
	AccountDataAccess accountDataAccess(EntityManagerFactory entityManagerFactory) {
		return new JPAAccountDataAccess(entityManagerFactory);
	}

	@Bean
	AccountServiceCommandEncoder accountServiceCommandEncoder() {
		return new JsonAccountServiceCommandEncoder();
	}

	@Bean
	AccountServiceResponseParser accountServiceResponseParser() {
		return new JsonAccountServiceResponseParser();
	}

	@Bean
	ErrorMessageParser errorMessageParser() {
		return new JsonErrorMessageParser();
	}

	@Bean
	RMQConsumer rmqConsumer(Connection connection, ErrorMessageParser errorMessageParser) throws IOException {
		Channel ch = connection.createChannel();
		ch.queueDeclare("spark-web-gateway", true, false, false, null);
		RMQConsumer rmqConsumer = new RMQConsumer(ch, errorMessageParser);
		ch.basicConsume("spark-web-gateway", rmqConsumer);
		return rmqConsumer;
	}

	@Bean
	UserAccountServiceProxy userAccountServiceProxy(
		Connection connection, RMQConsumer rmqConsumer, AccountServiceCommandEncoder accountServiceCommandEncoder
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.queueDeclare("spark-account-service", true, false, false, null);
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueBind("spark-account-service", "commands", "spark-account-service");
		return new RMQUserAccountServiceProxy(ch, rmqConsumer, accountServiceCommandEncoder);
	}

	@Bean
	AccountEventParser accountEventParser() {
		return new JsonAccountEventParser();
	}

	@Bean
	AccountEventListener accountEventListener(
		AccountRepositoryReplicaManager accountRepositoryReplicaManager,
		SessionDataAccess sessionDataAccess,
		AccountEventParser accountEventParser
	) {
		return new AccountEventListener(accountRepositoryReplicaManager, sessionDataAccess, accountEventParser);
	}

	@Bean
	AccountEventConsumer accountEventConsumer(
		AccountEventListener accountEventListener, Connection connection
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare("spark-account-service-events", BuiltinExchangeType.FANOUT, true, false, false, null);
		ch.queueDeclare("spark-web-gateway-accounts-replica", true, false, false, null);
		ch.queueBind("spark-web-gateway-accounts-replica", "spark-account-service-events", "");
		AccountEventConsumer accountEventConsumer = new AccountEventConsumer(accountEventListener, ch);
		ch.basicConsume("spark-web-gateway-accounts-replica", accountEventConsumer);
		return accountEventConsumer;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new SpringPasswordEncoder();
	}

	@Bean
	AccountRequestProcessor accountRequestProcessor(
		SessionDataAccess sessionDataAccess,
		AccountDataAccess accountDataAccess,
		PasswordEncoder passwordEncoder,
		UserAccountServiceProxy userAccountService,
		Executor executor
	) {
		return new AccountRequestProcessor(
			sessionDataAccess, accountDataAccess, passwordEncoder, userAccountService, executor
		);
	}

	@Bean
	InventoryServiceCommandEncoder inventoryServiceCommandEncoder() {
		return new JsonInventoryServiceCommandEncoder();
	}

	@Bean
	InventoryServiceResponseParser inventoryServiceResponseParser() {
		return new JsonInventoryServiceResponseParser();
	}

	@Bean
	UserInventoryServiceProxy userInventoryServiceProxy(
		Connection connection, RMQConsumer rmqConsumer, InventoryServiceCommandEncoder inventoryServiceCommandEncoder
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueDeclare("spark-inventory-service", true, false, false, null);
		ch.queueBind("spark-inventory-service", "commands", "spark-inventory-service");
		ch.confirmSelect();
		return new RMQUserInventoryServiceProxy(ch, rmqConsumer, inventoryServiceCommandEncoder);
	}

	@Bean
	InventoryRequestProcessor inventoryRequestProcessor(
		SessionDataAccess sessionDataAccess,
		UserInventoryServiceProxy inventoryServiceProxy,
		InventoryServiceResponseParser inventoryServiceResponseParser
	) {
		return new InventoryRequestProcessor(sessionDataAccess, inventoryServiceProxy, inventoryServiceResponseParser);
	}

	@Bean
	OrderServiceCommandEncoder orderServiceCommandEncoder() {
		return new JsonOrderServiceCommandEncoder();
	}

	@Bean
	OrderServiceResponseParser orderServiceResponseParser() {
		return new JsonOrderServiceResponseParser();
	}

	@Bean
	UserOrderServiceProxy userOrderServiceProxy(
		Connection connection, RMQConsumer rmqConsumer, OrderServiceCommandEncoder orderServiceCommandEncoder
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueDeclare("spark-order-service", true, false, false, null);
		ch.queueBind("spark-order-service", "commands", "spark-order-service");
		ch.confirmSelect();
		return new RMQUserOrderServiceProxy(ch, rmqConsumer, orderServiceCommandEncoder);
	}

	@Bean
	OrderRequestProcessor orderRequestProcessor(
		SessionDataAccess sessionDataAccess,
		UserOrderServiceProxy userOrderServiceProxy,
		UserInventoryServiceProxy userInventoryServiceProxy,
		OrderServiceResponseParser orderServiceResponseParser,
		InventoryServiceResponseParser inventoryServiceResponseParser,
		Executor executor
	) {
		return new OrderRequestProcessor(
			sessionDataAccess,
			userOrderServiceProxy,
			userInventoryServiceProxy,
			orderServiceResponseParser,
			inventoryServiceResponseParser,
			executor
		);
	}

	@Bean
	AdminAccountServiceProxy adminAccountServiceProxy(
		Connection connection, RMQConsumer rmqConsumer, AccountServiceCommandEncoder accountServiceCommandEncoder
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.queueDeclare("spark-account-service", true, false, false, null);
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueBind("spark-account-service", "commands", "spark-account-service");
		return new RMQAdminAccountServiceProxy(ch, rmqConsumer, accountServiceCommandEncoder);
	}

	@Bean
	AccountPanelRequestProcessor accountPanelRequestProcessor(
		AdminAccountServiceProxy adminAccountServiceProxy,
		SessionDataAccess sessionDataAccess,
		AccountServiceResponseParser accountServiceResponseParser
	) {
		return new AccountPanelRequestProcessor(
			adminAccountServiceProxy, sessionDataAccess, accountServiceResponseParser
		);
	}

	@Bean
	AdminInventoryServiceProxy adminInventoryServiceProxy(
		Connection connection, RMQConsumer rmqConsumer, InventoryServiceCommandEncoder inventoryServiceCommandEncoder
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueDeclare("spark-inventory-service", true, false, false, null);
		ch.queueBind("spark-inventory-service", "commands", "spark-inventory-service");
		ch.confirmSelect();
		return new RMQAdminInventoryServiceProxy(ch, rmqConsumer, inventoryServiceCommandEncoder);
	}

	@Bean
	UploadedFileDataAccess uploadedFileDataAccess(EntityManagerFactory entityManagerFactory) {
		return new JPAUploadedFileDataAccess(entityManagerFactory);
	}

	@Bean
	InventoryPanelRequestProcessor inventoryPanelRequestProcessor(
		AdminInventoryServiceProxy adminInventoryServiceProxy,
		SessionDataAccess sessionDataAccess,
		InventoryServiceResponseParser inventoryServiceResponseParser,
		UploadedFileDataAccess uploadedFileDataAccess
	) {
		return new InventoryPanelRequestProcessor(
			adminInventoryServiceProxy, sessionDataAccess, inventoryServiceResponseParser, uploadedFileDataAccess
		);
	}

	@Bean
	AdminOrderServiceProxy adminOrderServiceProxy(
		Connection connection, RMQConsumer rmqConsumer, OrderServiceCommandEncoder orderServiceCommandEncoder
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueDeclare("spark-order-service", true, false, false, null);
		ch.queueBind("spark-order-service", "commands", "spark-order-service");
		ch.confirmSelect();
		return new RMQAdminOrderServiceProxy(ch, rmqConsumer, orderServiceCommandEncoder);
	}

	@Bean
	OrderPanelRequestProcessor orderPanelRequestProcessor(
		AdminOrderServiceProxy adminOrderServiceProxy,
		AdminInventoryServiceProxy adminInventoryServiceProxy,
		SessionDataAccess sessionDataAccess,
		OrderServiceResponseParser orderServiceResponseParser,
		InventoryServiceResponseParser inventoryServiceResponseParser,
		Executor executor
	) {
		return new OrderPanelRequestProcessor(
			adminOrderServiceProxy,
			adminInventoryServiceProxy,
			sessionDataAccess,
			orderServiceResponseParser,
			inventoryServiceResponseParser,
			executor
		);
	}

	@Bean
	GeneralRequestProcessor generalRequestProcessor(UploadedFileDataAccess uploadedFileDataAccess) {
		return new GeneralRequestProcessor(uploadedFileDataAccess);
	}

	static void main(String[] args) {
		SpringApplication.run(WebGatewayConfiguration.class, args);
	}
}
