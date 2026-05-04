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

package org.example.spark.gateway.web.persistence;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import org.example.spark.gateway.web.interactors.UploadedFileDataAccess;
import org.example.spark.gateway.web.models.UploadedFileEntity;
import org.example.spark.gateway.web.models.UploadedFileEntity_;

import java.util.concurrent.atomic.AtomicReference;

public class JPAUploadedFileDataAccess implements UploadedFileDataAccess {

	private final EntityManagerFactory entityManagerFactory;

	public JPAUploadedFileDataAccess(@Nonnull EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void addBlob(@Nonnull Blob blob) {
		entityManagerFactory.runInTransaction(entityManager -> {
			UploadedFileEntity uploadedFileEntity = new UploadedFileEntity(blob.fileName(), blob.contentType(), blob.content());
			entityManager.persist(uploadedFileEntity);
		});
	}

	@Override
	public Blob getBlob(@Nonnull String fileName) {
		AtomicReference<Blob> blob = new AtomicReference<>();
		entityManagerFactory.runInTransaction(entityManager -> {
			UploadedFileEntity uploadedFileEntity = entityManager.find(UploadedFileEntity.class, fileName);
			blob.set(new Blob(uploadedFileEntity.getFileName(), uploadedFileEntity.getContentType(), uploadedFileEntity.getContent()));
		});
		return blob.get();
	}

	@Override
	public void deleteBlob(@Nonnull String fileName) {
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaDelete<UploadedFileEntity> q = cb.createCriteriaDelete(UploadedFileEntity.class);
		Root<UploadedFileEntity> uploadedFile = q.from(UploadedFileEntity.class);
		q.where(cb.equal(uploadedFile.get(UploadedFileEntity_.FILE_NAME), fileName));

		entityManagerFactory.runInTransaction(entityManager -> entityManager.createQuery(q).executeUpdate());
	}
}
