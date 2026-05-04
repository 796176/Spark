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

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "uploads")
public class UploadedFileEntity {

	private String fileName;

	private String contentType;

	private byte[] content;

	public UploadedFileEntity() { }

	public UploadedFileEntity(@Nonnull String fileName, @Nonnull String contentType, @Nonnull byte[] content) {
		this();
		this.setFileName(fileName);
		this.setContentType(contentType);
		this.setContent(content);
	}

	@Id
	public String getFileName() {
		return fileName;
	}

	public void setFileName(@Nonnull String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(@Nonnull String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(@Nonnull byte[] content) {
		this.content = content;
	}
}
