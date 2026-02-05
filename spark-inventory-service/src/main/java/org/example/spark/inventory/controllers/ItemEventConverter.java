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

import org.example.spark.inventory.events.ItemEvent;

public abstract class ItemEventConverter<T> {

	public abstract class EncodedEventProperties {

		public abstract String getContentType();

		public abstract String getVersion();

		public abstract T getBody();
	}

	public abstract <S extends ItemEvent> S convert(String contentType, String version, String s, Class<S> c);

	public abstract EncodedEventProperties convert(ItemEvent itemEvent);
}
