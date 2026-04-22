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

public class ItemManagementForm {

	private final Integer previousItemAmount;

	private final Integer currentItemAmount;

	private final String version;

	public ItemManagementForm(
		@Nullable Integer previousItemAmount,
		@Nullable Integer currentItemAmount,
		@Nullable String version
	) {
		this.previousItemAmount = previousItemAmount;
		this.currentItemAmount = currentItemAmount;
		this.version = version;
	}

	@Nullable
	public Integer getPreviousItemAmount() {
		return previousItemAmount;
	}

	@Nullable
	public Integer getCurrentItemAmount() {
		return currentItemAmount;
	}

	@Nullable
	public String getVersion() {
		return version;
	}
}
