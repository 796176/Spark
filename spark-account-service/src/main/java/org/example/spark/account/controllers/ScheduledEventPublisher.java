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

package org.example.spark.account.controllers;

import jakarta.annotation.Nonnull;
import org.example.spark.account.intaractors.PublishableEventDataAccess;
import org.example.spark.account.models.PublishableAccountEvent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledEventPublisher implements AutoCloseable {

	private final ScheduledExecutorService scheduledExecutorService;

	private final PublishableEventDataAccess publishableEventDataAccess;

	private final EventPublisher eventPublisher;


	public ScheduledEventPublisher(
		@Nonnull ScheduledExecutorService scheduledExecutorService,
		@Nonnull PublishableEventDataAccess publishableEventDataAccess,
		@Nonnull EventPublisher eventPublisher,
		long publishingInterval
	) {
		if (publishingInterval <= 0) throw new IllegalArgumentException();
		this.scheduledExecutorService = scheduledExecutorService;
		this.publishableEventDataAccess = publishableEventDataAccess;
		this.eventPublisher = eventPublisher;

		scheduledExecutorService.scheduleWithFixedDelay(
			this::publish, 0, publishingInterval, TimeUnit.MILLISECONDS
		);
	}

	private void publish() {
		try {
			PublishableAccountEvent[] events = publishableEventDataAccess.retrieveInChronologicalOrder();
			eventPublisher.publish(events);
			publishableEventDataAccess.delete(events);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws Exception {
		scheduledExecutorService.shutdown();
		scheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}
}
