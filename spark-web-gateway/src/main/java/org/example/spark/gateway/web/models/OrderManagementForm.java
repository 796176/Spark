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

package org.example.spark.gateway.web.models;

import jakarta.annotation.Nullable;

public class OrderManagementForm {

	private final Order.Status previousStatus;

	private final Order.Status currentStatus;

	private final String version;

	public OrderManagementForm(
		@Nullable Order.Status previousStatus,
		@Nullable Order.Status currentStatus,
		@Nullable String version
	) {
		this.previousStatus = previousStatus;
		this.currentStatus = currentStatus;
		this.version = version;
	}

	@Nullable
	public Order.Status getPreviousStatus() {
		return previousStatus;
	}

	@Nullable
	public Order.Status getCurrentStatus() {
		return currentStatus;
	}

	@Nullable
	public String getVersion() {
		return version;
	}
}
