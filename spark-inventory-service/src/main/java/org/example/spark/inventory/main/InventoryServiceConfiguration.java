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

package org.example.spark.inventory.main;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.persistence.EntityManagerFactory;
import org.example.spark.inventory.controllers.*;
import org.example.spark.inventory.converters.JsonItemEventConverter;
import org.example.spark.inventory.interactors.PublishableEventDataAccess;
import org.example.spark.inventory.interactors.SagaDataAccess;
import org.example.spark.inventory.interactors.InventoryService;
import org.example.spark.inventory.interactors.ItemDataAccess;
import org.example.spark.inventory.persistence.JPAItemDataAccess;
import org.example.spark.inventory.persistence.JPAPublishableEventDataAccess;
import org.example.spark.inventory.persistence.JPASagaDataAccess;
import org.example.spark.inventory.sagas.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true, rollbackOn = RollbackOn.ALL_EXCEPTIONS)
@EntityScan({"org.example.spark.inventory.models"})
public class InventoryServiceConfiguration {

	record SagaConnection(Connection con) { }

	@Bean
	ItemEventConverter<String> itemEventConverter() {
		return new JsonItemEventConverter();
	}

	@Bean
	JPAItemDataAccess jpaItemDataAccess(
		EntityManagerFactory entityManagerFactory, ItemEventConverter<String> itemEventConverter
	) {
		return new JPAItemDataAccess(entityManagerFactory, itemEventConverter);
	}

	@Bean
	SagaDataAccess sagaDataAccess(JPAItemDataAccess jpaItemDataAccess, EntityManagerFactory entityManagerFactory) {
		return new JPASagaDataAccess(jpaItemDataAccess, entityManagerFactory);
	}

	@Bean
	TransactionManager jpaTransactionManager() {
		return new JpaTransactionManager();
	}

	@Bean
	InventoryService inventoryService(ItemDataAccess itemDataAccess, SagaManager sagaManager) {
		return new InventoryServiceImpl(itemDataAccess, sagaManager);
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
	SagaMessageHandler sagaMessageHandler(SagaManager sagaController, Executor executor) {
		return new SagaMessageHandler(sagaController, executor);
	}

	@Bean
	RMQSagaMessageConsumer rmqSagaMessageConsumer(
		SagaMessageHandler sagaMessageHandler, Connection connection
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.queueDeclare("spark-inventory-service-saga", true, false, false, null);
		return new RMQSagaMessageConsumer(sagaMessageHandler, ch);
	}

	@Bean
	DefaultSagaManager sagaManager(SagaDataAccess sagaDataAccess, ApplicationContext applicationContext) {
		return new DefaultSagaManager(
			sagaDataAccess,
			(c, l) -> {
				if (c.equals(SagaStateInvalidatingItem.class)) {
					OrderServiceProxy orderServiceProxy = applicationContext.getBean(OrderServiceProxy.class);
					return new SagaStateInvalidatingItem(l, orderServiceProxy, sagaDataAccess);
				} else if (c.equals(SagaStateConfirmingDeletion.class)) {
					InventoryServiceProxy inventoryServiceProxy =
						applicationContext.getBean(InventoryServiceProxy.class);
					return new SagaStateConfirmingDeletion(l, inventoryServiceProxy);
				} else if (c.equals(SagaStateAbortingDeletion.class)) {
					InventoryServiceProxy inventoryServiceProxy =
						applicationContext.getBean(InventoryServiceProxy.class);
					return new SagaStateAbortingDeletion(l, inventoryServiceProxy);
				}
				throw new IllegalArgumentException();
			}
		);
	}

	@Bean
	SagaLoader sagaLoader(
		SagaDataAccess sagaDataAccess, RMQSagaMessageConsumer rmqSagaMessageConsumer, SagaManager sagaManager
	) {
		return new SagaLoader(sagaDataAccess, rmqSagaMessageConsumer, sagaManager);
	}

	@Bean
	SagaConnection sagaConnection(Connection connection) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueDeclare("spark-order-service", true, false, false, null);
		ch.queueBind("spark-order-service", "commands", "spark-order-service");
		return new SagaConnection(connection);
	}

	@Bean
	@Scope("prototype")
	OrderServiceProxy orderServiceProxy(SagaConnection sagaConnection) throws IOException {
		Channel ch = sagaConnection.con().createChannel();
		ch.confirmSelect();
		return new RMQOrderService(ch, "spark-inventory-service-saga");
	}

	@Bean
	InventoryServiceProxy inventoryServiceProxy(ItemDataAccess itemDataAccess, SagaManager sagaManager) {
		return new LocalInventoryService(itemDataAccess, sagaManager);
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
	CommandProcessor commandProcessor(
		Executor executor,
		CommandParser commandParser,
		ResponseEncoder responseEncoder,
		InventoryService inventoryService
	) {
		return new AuthorizedCommandProcessor(executor, commandParser, responseEncoder, inventoryService);
	}

	@Bean
	RMQCommandConsumer rmqCommandConsumer(Connection connection, CommandProcessor commandProcessor) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueDeclare("spark-inventory-service", true, false, false, null);
		ch.queueBind("spark-inventory-service", "commands", "spark-inventory-service");
		RMQCommandConsumer rmqCommandConsumer = new RMQCommandConsumer(commandProcessor, connection, ch);
		ch.basicConsume("spark-inventory-service", rmqCommandConsumer);
		return rmqCommandConsumer;
	}

	@Bean
	PublishableEventDataAccess publishableEventDataAccess(EntityManagerFactory entityManagerFactory) {
		return new JPAPublishableEventDataAccess(entityManagerFactory);
	}

	@Bean
	EventPublisher eventPublisher(Connection connection) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.exchangeDeclare(
			"spark-inventory-service-events", BuiltinExchangeType.FANOUT, true, false, false, null
		);
		return new RMQEventPublisher(ch);
	}

	@Bean
	ScheduledEventPublisher scheduledEventPublisher(
		PublishableEventDataAccess publishableEventDataAccess,
		EventPublisher eventPublisher,
		@Value("${org.example.spark.eventPublishingInterval}") long publishingInterval
	) {
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		return new ScheduledEventPublisher(
			scheduledExecutorService, publishableEventDataAccess, eventPublisher, publishingInterval
		);
	}

	static void main(String[] args) {
		SpringApplication.run(InventoryServiceConfiguration.class, args);
	}
}
