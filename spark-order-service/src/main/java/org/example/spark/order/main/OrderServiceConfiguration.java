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

package org.example.spark.order.main;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.spark.order.controllers.*;
import org.example.spark.order.converters.*;
import org.example.spark.order.interactors.*;
import org.example.spark.order.persistence.*;
import org.example.spark.order.sagas.DefaultSagaFactory;
import org.example.spark.order.sagas.SagaFactory;
import org.example.spark.order.sagas.SagaManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
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
@EntityScan(basePackages = { "org.example.spark.order.models" })
public class OrderServiceConfiguration {

	@Bean
	TransactionManager transactionManager() {
		return new JpaTransactionManager();
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
	OrderEventConverter<String> orderEventConverter() {
		return new JsonOrderEventConverter();
	}

	@Bean
	OrderDataAccess orderDataAccess(EntityManager entityManager, OrderEventConverter<String> orderEventConverter) {
		return new JPAOrderDataAccess(entityManager, orderEventConverter);
	}

	@Bean
	OrderService orderService(OrderDataAccess orderDataAccess, SagaManager sagaManager) {
		return new OrderServiceImpl(orderDataAccess, sagaManager);
	}

	@Bean
	AuthorizingAccountMessageProcessor authorizingAccountMessageProcessor() {
		return new JsonAuthorizingAccountMessageProcessor();
	}

	@Bean
	SagaFactory sagaFactory(
		ItemDataAccess itemDataAccess,
		OrderDataAccess orderDataAccess,
		Connection connection,
		AuthorizingAccountMessageProcessor authorizingAccountMessageProcessor
	) {
		return new DefaultSagaFactory(
			itemDataAccess,
			orderDataAccess,
			connection,
			"spark-order-service-saga",
			authorizingAccountMessageProcessor
		);
	}

	@Bean
	SagaDataAccess sagaDataAccess(EntityManager entityManager, SagaFactory sagaFactory) {
		return new JPASagaDataAccess(entityManager, sagaFactory);
	}

	@Bean
	SagaManager sagaManager(SagaDataAccess sagaDataAccess, Executor executor) {
		return new DefaultSagaManager(sagaDataAccess, executor);
	}

	@Bean
	ItemDataAccess itemDataAccess(EntityManagerFactory entityManagerFactory) {
		return new JPAItemDataAccess(entityManagerFactory);
	}

	@Bean
	SagaMessageHandler sagaMessageHandler(SagaManager sagaManager, Executor executor) {
		return new SagaMessageHandler(sagaManager, executor);
	}

	@Bean
	RMQSagaMessageConsumer rmqSagaMessageConsumer(
		SagaMessageHandler sagaMessageHandler, Connection connection
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.queueDeclare("spark-order-service-saga", true, false, false, null);
		return new RMQSagaMessageConsumer(sagaMessageHandler, ch);
	}

	@Bean
	SagaLoader sagaLoader(
		SagaDataAccess sagaDataAccess,
		OrderDataAccess orderDataAccess,
		RMQSagaMessageConsumer rmqSagaMessageConsumer,
		SagaManager sagaManager
	) {
		return new SagaLoader(sagaDataAccess, orderDataAccess, rmqSagaMessageConsumer, sagaManager);
	}

	@Bean
	OrderCommandParser orderCommandParser() {
		return new JsonOrderCommandParser();
	}

	@Bean
	ResponseConverter responseConverter() {
		return new JsonResponseConverter();
	}

	@Bean
	CommandProcessor commandProcessor(
		Executor executor,
		OrderCommandParser orderCommandParser,
		ResponseConverter responseConverter,
		OrderService orderService,
		OrderDataAccess orderDataAccess,
		ItemRepositoryReplicaManager itemRepositoryReplicaManager
	) {
		return new AuthorizingCommandProcessing(
			executor, orderCommandParser, responseConverter, orderService, orderDataAccess, itemRepositoryReplicaManager
		);
	}

	@Bean
	PublishableEventDataAccess publishableEventDataAccess(EntityManagerFactory entityManagerFactory) {
		return new JPAPublishableEventDataAccess(entityManagerFactory);
	}

	@Bean
	RMQCommandConsumer rmqCommandConsumer(Connection connection, CommandProcessor commandProcessor) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.exchangeDeclare("commands", BuiltinExchangeType.DIRECT, true, false, false, null);
		ch.queueDeclare("spark-order-service", true, false, false, null);
		ch.queueBind("spark-order-service", "commands", "spark-order-service", null);
		RMQCommandConsumer rmqCommandConsumer = new RMQCommandConsumer(commandProcessor, connection, ch);
		ch.basicConsume("spark-order-service", rmqCommandConsumer);
		return rmqCommandConsumer;
	}

	@Bean
	EventPublisher eventPublisher(Connection connection) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.exchangeDeclare(
			"spark-order-service-events", BuiltinExchangeType.FANOUT, true, false, false, null
		);
		return new RMQEventPublisher(ch);
	}

	@Bean
	ScheduledEventPublisher scheduledEventPublisher(
		PublishableEventDataAccess publishableEventDataAccess,
		EventPublisher eventPublisher,
		@Value("${org.example.spark.eventPublishingInterval}") long interval
	) {
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		return new ScheduledEventPublisher(
			scheduledExecutorService, publishableEventDataAccess, eventPublisher, interval
		);
	}

	@Bean
	ItemRepositoryReplicaManager itemRepositoryReplicaManager(EntityManagerFactory entityManagerFactory) {
		return new JPAItemRepositoryReplicaManager(entityManagerFactory);
	}

	@Bean
	InventoryEventParser inventoryEventParser() {
		return new JsonInventoryEventParser();
	}

	@Bean
	InventoryEventListener inventoryEventListener(
		ItemRepositoryReplicaManager itemRepositoryReplicaManager, InventoryEventParser inventoryEventParser
	) {
		return new InventoryEventListener(itemRepositoryReplicaManager, inventoryEventParser);
	}

	@Bean
	RMQInventoryEventConsumer rmqInventoryEventConsumer(
		Connection connection, InventoryEventListener inventoryEventListener
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare(
			"spark-inventory-service-events", BuiltinExchangeType.FANOUT, true, false, false, null
		);
		ch.queueDeclare("spark-order-service-items-replica", true, false, false, null);
		ch.queueBind("spark-order-service-items-replica", "spark-inventory-service-events", "");
		RMQInventoryEventConsumer rmqInventoryEventConsumer = new RMQInventoryEventConsumer(inventoryEventListener, ch);
		ch.basicConsume("spark-order-service-items-replica", rmqInventoryEventConsumer);
		return rmqInventoryEventConsumer;
	}

	@Bean
	EnrichedEventPublisher enrichedEventPublisher(Connection connection) throws IOException {
		Channel ch = connection.createChannel();
		ch.confirmSelect();
		ch.exchangeDeclare(
			"spark-order-service-enriched-events", BuiltinExchangeType.FANOUT, true, false, false, null
		);
		return new RMQEnrichedEventPublisher(ch);
	}

	@Bean
	EnrichedEventEncoder enrichedEventEncoder() {
		return new JsonEnrichedEventEncoder();
	}

	@Bean
	OrderEventEnricher orderEventEnricher(
		OrderDataAccess orderDataAccess,
		OrderEventConverter<String> orderEventConverter,
		EnrichedEventPublisher enrichedEventPublisher,
		EnrichedEventEncoder enrichedEventEncoder
	) {
		return new OrderEventEnricher(
			orderDataAccess, orderEventConverter, enrichedEventPublisher, enrichedEventEncoder
		);
	}

	@Bean
	RMQOrderEventConsumer rmqOrderEventConsumer(
		Connection connection, OrderEventEnricher orderEventEnricher
	) throws IOException {
		Channel ch = connection.createChannel();
		ch.exchangeDeclare(
			"spark-order-service-events", BuiltinExchangeType.FANOUT, true, false, false, null
		);
		ch.queueDeclare("spark-order-service-event-enricher", true, false, false, null);
		ch.queueBind("spark-order-service-event-enricher", "spark-order-service-events", "");
		RMQOrderEventConsumer rmqOrderEventConsumer = new RMQOrderEventConsumer(orderEventEnricher, ch);
		ch.basicConsume("spark-order-service-event-enricher", rmqOrderEventConsumer);
		return rmqOrderEventConsumer;
	}

	static void main(String[] args) {
		SpringApplication.run(OrderServiceConfiguration.class, args);
	}
}
