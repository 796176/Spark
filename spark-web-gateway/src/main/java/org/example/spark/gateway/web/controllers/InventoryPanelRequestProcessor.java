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

package org.example.spark.gateway.web.controllers;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.example.spark.gateway.web.converters.InventoryServiceResponseParser;
import org.example.spark.gateway.web.exceptions.AuthenticationException;
import org.example.spark.gateway.web.exceptions.ServerError;
import org.example.spark.gateway.web.interactors.SessionDataAccess;
import org.example.spark.gateway.web.interactors.UploadedFileDataAccess;
import org.example.spark.gateway.web.models.*;
import org.example.spark.gateway.web.proxies.AdminInventoryServiceProxy;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class InventoryPanelRequestProcessor {

	private final AdminInventoryServiceProxy inventoryService;

	private final SessionDataAccess sessionDataAccess;

	private final InventoryServiceResponseParser inventoryServiceResponseParser;

	private final UploadedFileDataAccess uploadedFileDataAccess;

	public InventoryPanelRequestProcessor(
		@Nonnull AdminInventoryServiceProxy inventoryService,
		@Nonnull SessionDataAccess sessionDataAccess,
		@Nonnull InventoryServiceResponseParser inventoryServiceResponseParser,
		@Nonnull UploadedFileDataAccess uploadedFileDataAccess
	) {
		this.inventoryService = inventoryService;
		this.sessionDataAccess = sessionDataAccess;
		this.inventoryServiceResponseParser = inventoryServiceResponseParser;
		this.uploadedFileDataAccess = uploadedFileDataAccess;
	}

	public Future<Item[]> getItems(@Nonnull String sessionId) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<Item[]> completableFuture = new CompletableFuture<>();
		inventoryService.getItems(session.getAccount(), rcr -> {
			String errorMessage =
				Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
			try {
				if (rcr.isSuccessful()) {
					Item[] items = inventoryServiceResponseParser
						.parseGettingItemsResponse(rcr.getContentType(), rcr.getVersion(), rcr.getResultBody());
					completableFuture.complete(items);
				} else {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				}
			} catch (Exception e) {
				completableFuture.completeExceptionally(new ServerError(errorMessage));
			}
		});
		return completableFuture;
	}

	public Future<Item> getItem(@Nonnull String sessionId, long itemId) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<Item> completableFuture = new CompletableFuture<>();
		inventoryService.getItem(session.getAccount(), itemId, rcr -> {
			String errorMessage =
				Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
			try {
				if (rcr.isSuccessful()) {
					Item item = inventoryServiceResponseParser
						.parseGettingItemResponse(rcr.getContentType(), rcr.getVersion(), rcr.getResultBody());
					completableFuture.complete(item);
				} else {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				}
			} catch (Exception e) {
				completableFuture.completeExceptionally(new ServerError(errorMessage));
			}
		});
		return completableFuture;
	}

	public Future<?> addItem(
		@Nonnull String sessionId,
		@Nonnull String name,
		@Nonnull Money price,
		int amount,
		@Nullable UploadedFile picture
	) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		if (picture != null) {
			String picExt = picture.name().substring(picture.name().lastIndexOf('.') + 1);
			String contentType = "image/" + switch (picExt) {
				case "png" -> "png";
				case "jpg", "jpeg" -> "jpeg";
				default -> throw new IllegalArgumentException();
			};
			uploadedFileDataAccess.addBlob(
				new UploadedFileDataAccess.Blob(picture.name(), contentType, picture.content())
			);
		}
		String picName = picture != null ? picture.name() : null;
		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		inventoryService.addItem(session.getAccount(), name, price, amount, picName, rcr -> {
			if (rcr.isSuccessful()) {
				completableFuture.complete(null);
			} else {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				} catch (Exception e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		});
		return completableFuture;
	}

	public Future<?> deleteItem(@Nonnull String sessionId, long itemId) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		inventoryService.deleteItem(session.getAccount(), itemId, rcr -> {
			if (rcr.isSuccessful()) {
				completableFuture.complete(null);
			} else {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				} catch (Exception e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		});
		return completableFuture;
	}

	public Future<?> saveItem(
		@Nonnull String sessionId, long itemId, @Nonnull String version, int previousItemAmount, int currentItemAmount
	) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<?> updatingItemAmountProcess = CompletableFuture.completedFuture(null);
		if (previousItemAmount != currentItemAmount) {
			updatingItemAmountProcess = updateItemAmount(session.getAccount(), itemId, currentItemAmount, version);
		}

		return CompletableFuture.allOf(updatingItemAmountProcess);
	}

	private CompletableFuture<?> updateItemAmount(
		Account account, long itemId, int amount, String version
	) throws Exception {
		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		inventoryService.updateItemAmount(account, itemId, amount, version, rcr -> {
			if (rcr.isSuccessful()) {
				completableFuture.complete(null);
			} else {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				} catch (Exception e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		});
		return completableFuture;
	}
}
