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

package org.example.spark.order.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "outbox")
public class EventEntity {

	private String eventId;

	private long seqnum;

	private String eventType;

	private String contentType;

	private String version;

	private String eventBody;

	public EventEntity() { }

	public EventEntity(
		@Nonnull String eventType,
		@Nonnull String contentType,
		@Nonnull String version,
		@Nonnull String eventBody
	) {
		this();
		this.setEventId(UUID.randomUUID().toString());
		this.setEventType(eventType);
		this.setContentType(contentType);
		this.setVersion(version);
		this.setEventBody(eventBody);
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(@Nonnull String eventId) {
		this.eventId = eventId;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getSeqnum() {
		return seqnum;
	}

	public void setSeqnum(long seqnum) {
		this.seqnum = seqnum;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(@Nonnull String eventType) {
		this.eventType = eventType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(@Nonnull String contentType) {
		this.contentType = contentType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(@Nonnull String version) {
		this.version = version;
	}

	public String getEventBody() {
		return eventBody;
	}

	public void setEventBody(@Nonnull String eventBody) {
		this.eventBody = eventBody;
	}
}
