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

package org.example.spark.inventory.models;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.util.UUID;

@StaticMetamodel(EventEntity.class)
public class EventEntity_ {
	public static volatile EntityType<EventEntity> class_;

	public static volatile String EVENT_ID = "eventId";
	public static volatile String SEQNUM = "seqnum";
	public static volatile String EVENT_TYPE = "eventType";
	public static volatile String CONTENT_TYPE = "contentType";
	public static volatile String VERSION = "version";
	public static volatile String EVENT_BODY = "eventBody";

	public static volatile SingularAttribute<EventEntity, String> eventId;
	public static volatile SingularAttribute<EventEntity, Long> seqnum;
	public static volatile SingularAttribute<EventEntity, String> eventType;
	public static volatile SingularAttribute<EventEntity, String> contentType;
	public static volatile SingularAttribute<EventEntity, String> version;
	public static volatile SingularAttribute<EventEntity, String> eventBody;
}
