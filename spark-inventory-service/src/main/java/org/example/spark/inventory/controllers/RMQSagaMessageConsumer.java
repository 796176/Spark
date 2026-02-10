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

import java.io.IOException;

public class RMQSagaMessageConsumer implements Consumer {

	private final SagaMessageHandler sagaMessageHandler;

	private final Channel acknowledgementChannel;

	public RMQSagaMessageConsumer(
		@Nonnull SagaMessageHandler sagaMessageHandler,
		@Nonnull Channel acknowledgementChannel
	) {
		this.sagaMessageHandler = sagaMessageHandler;
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
	public void handleDelivery(
		String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] body
	) {
		String correlationId = basicProperties.getCorrelationId();
		String messageType = basicProperties.getType();
		String contentType = basicProperties.getContentType();
		int statusCode = Integer.parseInt(basicProperties.getHeaders().get("Status-Code").toString());
		String version = basicProperties.getHeaders().get("Version").toString();
		Runnable acknowledgementRunnable = () -> {
			synchronized (acknowledgementChannel) {
				try {
					acknowledgementChannel.basicAck(envelope.getDeliveryTag(), false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		sagaMessageHandler.handleMessage(
			correlationId, messageType, contentType, statusCode, version, body, acknowledgementRunnable
		);
	}

	public Channel getChannel() {
		return acknowledgementChannel;
	}
}
