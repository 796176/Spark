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

package org.example.spark.account.main;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.persistence.EntityManagerFactory;
import org.example.spark.account.controllers.*;
import org.example.spark.account.converters.AccountServiceCommandEncoder;
import org.example.spark.account.converters.JsonAccountServiceCommandEncoder;
import org.example.spark.account.events.AccountEventConverter;
import org.example.spark.account.events.JsonAccountEventConverter;
import org.example.spark.account.intaractors.AccountDataAccess;
import org.example.spark.account.intaractors.PublishableEventDataAccess;
import org.example.spark.account.persistence.JPAAccountDataAccess;
import org.example.spark.account.persistence.JPAPublishableEventDataAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;


@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true, rollbackOn = RollbackOn.ALL_EXCEPTIONS)
@EntityScan({"org.example.spark.account.models"})
public class AccountServiceConfiguration {

	@Bean
	AccountEventConverter<String> accountEventConverter() {
		return new JsonAccountEventConverter();
	}

	@Bean
	TransactionManager transactionManager() {
		return new JpaTransactionManager();
	}

	@Bean
	AccountDataAccess accountDataAccess(
		EntityManagerFactory entityManagerFactory, AccountEventConverter<String> accountEventConverter
	) {
		return new JPAAccountDataAccess(entityManagerFactory, accountEventConverter);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new SpringPasswordEncoder(new BCryptPasswordEncoder());
	}

	@Bean
	AccountService accountService(AccountDataAccess accountDataAccess) {
		return new AccountServiceImpl(accountDataAccess);
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
	CommandParser commandParser() {
		return new JsonCommandParser();
	}

	@Bean
	ResponseEncoder responseEncoder() {
		return new JsonResponseEncoder();
	}

	@Bean
	AuthorizedCommandProcessor authorizedCommandProcessor(
		Executor executor, CommandParser commandParser, ResponseEncoder responseEncoder, AccountService accountService
	) {
		return new AuthorizedCommandProcessor(executor, commandParser, responseEncoder, accountService);
	}

	@Bean
	AccountServiceCommandEncoder accountServiceCommandEncoder() {
		return new JsonAccountServiceCommandEncoder();
	}

	@Bean
	CreatingAdminAccountCommandFilter creatingAdminAccountCommandFilter(
		AccountServiceCommandEncoder accountServiceCommandEncoder,
		CommandParser commandParser,
		PasswordEncoder passwordEncoder
	) {
		return new CreatingAdminAccountCommandFilter(accountServiceCommandEncoder, commandParser, passwordEncoder);
	}

	@Bean
	CreatingAccountCommandFilter creatingAccountCommandFilter(
		AccountServiceCommandEncoder accountServiceCommandEncoder,
		CommandParser commandParser,
		PasswordEncoder passwordEncoder
	) {
		return new CreatingAccountCommandFilter(accountServiceCommandEncoder, commandParser, passwordEncoder);
	}

	@Bean
	DecoratingCommandProcessor decoratingCommandProcessor(
		AuthorizedCommandProcessor authorizedCommandProcessor,
		CreatingAccountCommandFilter creatingAccountCommandFilter,
		CreatingAdminAccountCommandFilter creatingAdminAccountCommandFilter
	) {
		return new DecoratingCommandProcessor(
			authorizedCommandProcessor, creatingAccountCommandFilter, creatingAdminAccountCommandFilter
		);
	}

	@Bean
	CommandController commandController(
		DecoratingCommandProcessor decoratingCommandProcessor, Connection connection
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true);
		ch.queueDeclare("spark-account-service", true, false, false, Map.of());
		ch.queueBind("spark-account-service", "commands", "spark-account-service");
		CommandController commandController = new CommandController(decoratingCommandProcessor, connection, ch);
		ch.basicConsume("spark-account-service", false, commandController);
		return commandController;
	}

	@Bean
	@Scope("prototype")
	Connection connection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
		return connectionFactory.newConnection();
	}

	@Bean
	PublishableEventDataAccess publishableEventDataAccess(EntityManagerFactory entityManagerFactory) {
		return new JPAPublishableEventDataAccess(entityManagerFactory);
	}

	@Bean
	EventPublisher eventPublisher(Connection connection) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare("spark-account-service-events", BuiltinExchangeType.FANOUT, true, false, null);
		ch.confirmSelect();
		return new RMQEventPublisher(ch);
	}

	@Bean
	ScheduledEventPublisher scheduledEventPublisher(
		PublishableEventDataAccess publishableEventDataAccess,
		EventPublisher eventPublisher,
		@Value("${org.example.spark.eventPublishingInterval}") long eventPublishingInterval
	) {
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		return new ScheduledEventPublisher(
			scheduledExecutorService, publishableEventDataAccess, eventPublisher, eventPublishingInterval
		);
	}

	static void main(String[] args) {
		SpringApplication.run(AccountServiceConfiguration.class, args);
	}
}
