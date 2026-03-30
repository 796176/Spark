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

package org.example.spark.gateway.web.messaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import jakarta.annotation.Nonnull;
import org.example.spark.gateway.web.converters.ErrorMessageParser;
import org.example.spark.gateway.web.models.RemoteCallResult;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RMQConsumer implements com.rabbitmq.client.Consumer {

	private final Channel acknowledgementChannel;

	private final Map<String, Consumer<RemoteCallResult>> map = new ConcurrentHashMap<>();

	private final ErrorMessageParser errorMessageParser;

	public RMQConsumer(@Nonnull Channel acknowledgementChannel, @Nonnull ErrorMessageParser errorMessageParser) {
		this.acknowledgementChannel = acknowledgementChannel;
		this.errorMessageParser = errorMessageParser;
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
	public void handleRecoverOk(String s) {
	}

	@Override
	public void handleDelivery(
		String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes
	) throws IOException {
		try {
			boolean success = Integer.parseInt(basicProperties.getHeaders().get("Status-Code").toString()) == 0;
			String version = basicProperties.getHeaders().get("Version").toString();
			ErrorMessageParser.ParsedError parsedError =
				errorMessageParser.parse(basicProperties.getContentType(), version, bytes);
			RemoteCallResult remoteCallResult = new RemoteCallResult(
				success,
				bytes,
				version,
				basicProperties.getContentType(),
				parsedError.errorType(),
				parsedError.errorMessage()
			);
			Consumer<RemoteCallResult> callResultConsumer = map.get(basicProperties.getCorrelationId());
			if (callResultConsumer == null) {
				return;
			}
			callResultConsumer.accept(remoteCallResult);
		} finally {
			acknowledgementChannel.basicAck(envelope.getDeliveryTag(), false);
		}
	}

	public void register(@Nonnull String correlationId, @Nonnull Consumer<RemoteCallResult> callResultConsumer) {
		map.put(correlationId, callResultConsumer);
	}
}
