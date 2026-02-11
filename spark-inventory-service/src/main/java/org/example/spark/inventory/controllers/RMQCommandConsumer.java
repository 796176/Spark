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

package org.example.spark.inventory.controllers;

import com.rabbitmq.client.*;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Map;

public class RMQCommandConsumer implements Consumer {

	private final CommandProcessor commandProcessor;

	private final Connection connection;

	private final Channel acknowledgementChannel;

	public RMQCommandConsumer(
		@Nonnull CommandProcessor commandProcessor,
		@Nonnull Connection connection,
		@Nonnull Channel acknowledgementChannel
	) {
		this.commandProcessor = commandProcessor;
		this.connection = connection;
		this.acknowledgementChannel = acknowledgementChannel;
	}


	@Override
	public void handleConsumeOk(String s) { }

	@Override
	public void handleCancelOk(String s) { }

	@Override
	public void handleCancel(String s) { }

	@Override
	public void handleShutdownSignal(String s, ShutdownSignalException e) { }

	@Override
	public void handleRecoverOk(String s) { }

	@Override
	public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) {
		commandProcessor.processCommand(
			basicProperties.getType(),
			Long.parseLong(basicProperties.getHeaders().get("Caller-Id").toString()),
			Arrays
				.stream(basicProperties.getHeaders().get("Caller-Roles").toString().split(","))
				.mapToLong(Long::parseLong)
				.toArray(),
			basicProperties.getMessageId(),
			basicProperties.getContentType(),
			basicProperties.getHeaders().get("Version").toString(),
			bytes,
			(statusCode, contentType, version, body) -> {
				AMQP.BasicProperties replyProperties = new AMQP.BasicProperties.Builder()
					.deliveryMode(2)
					.contentType(contentType)
					.correlationId(basicProperties.getCorrelationId())
					.headers(Map.of("Status-Code", Integer.toString(statusCode), "Version", version))
					.build();
				//TODO get rid of short living channels
				try (Channel channel = connection.createChannel()) {
					channel.confirmSelect();
					do {
						channel.basicPublish("", basicProperties.getReplyTo(), replyProperties, body);
					} while (!channel.waitForConfirms());
					synchronized (acknowledgementChannel) {
						acknowledgementChannel.basicAck(envelope.getDeliveryTag(), false);
					}
				}
			}
		);
	}
}
