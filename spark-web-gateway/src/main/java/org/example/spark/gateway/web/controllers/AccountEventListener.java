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
import org.example.spark.gateway.web.converters.AccountEventParser;
import org.example.spark.gateway.web.converters.AccountEventParser.*;
import org.example.spark.gateway.web.interactors.AccountRepositoryReplicaManager;
import org.example.spark.gateway.web.interactors.SessionDataAccess;

public class AccountEventListener {

	private final AccountRepositoryReplicaManager accountRepositoryReplicaManager;

	private final SessionDataAccess sessionDataAccess;

	private final AccountEventParser accountEventParser;

	public AccountEventListener(
		@Nonnull AccountRepositoryReplicaManager accountRepositoryReplicaManager,
		@Nonnull SessionDataAccess sessionDataAccess,
		@Nonnull AccountEventParser accountEventParser
	) {
		this.accountRepositoryReplicaManager = accountRepositoryReplicaManager;
		this.sessionDataAccess = sessionDataAccess;
		this.accountEventParser = accountEventParser;
	}

	void processEvent(
		@Nonnull String eventType,
		@Nonnull String contentType,
		@Nonnull String version,
		@Nonnull String messageId,
		@Nonnull byte[] body
	) throws Exception {
		switch (eventType) {
			case "org.example.spark.account.account-created" -> {
				AccountCreatedEvent event = accountEventParser.parseAccountCreatedEvent(contentType, version, body);
				accountRepositoryReplicaManager
					.addAccount(event.accountId(), event.name(), event.encodedPassword(), event.roles(), messageId);
			}
			case "org.example.spark.account.account-deleted" -> {
				AccountStatusUpdatedEvent event =
					accountEventParser.parseAccountStatusUpdatedEvent(contentType, version, body);
				accountRepositoryReplicaManager.deleteAccount(event.accountId());
				sessionDataAccess.invalidateSessionsForAccount(event.accountId());
			}
			case "org.example.spark.account.account-suspended" -> {
				AccountStatusUpdatedEvent event =
					accountEventParser.parseAccountStatusUpdatedEvent(contentType, version, body);
				accountRepositoryReplicaManager.suspendAccount(event.accountId());
				sessionDataAccess.invalidateSessionsForAccount(event.accountId());
			}
			case "org.example.spark.account.account-restored" -> {
				AccountStatusUpdatedEvent event =
					accountEventParser.parseAccountStatusUpdatedEvent(contentType, version, body);
				accountRepositoryReplicaManager.restoreAccount(event.accountId());
			}
			case "org.example.spark.account.account-roles-updated" -> {
				AccountRolesUpdatedEvent event =
					accountEventParser.parseAccountRolesUpdatedEvent(contentType, version, body);
				accountRepositoryReplicaManager.changeRoles(event.accountId(), event.roles());
			}
		}
	}
}
