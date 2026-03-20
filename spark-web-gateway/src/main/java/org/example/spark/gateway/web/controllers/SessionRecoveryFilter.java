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
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.spark.gateway.web.interactors.SessionDataAccess;
import org.example.spark.gateway.web.models.Session;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SessionRecoveryFilter extends OncePerRequestFilter {

	private final SessionDataAccess sessionDataAccess;

	public SessionRecoveryFilter(@Nonnull SessionDataAccess sessionDataAccess) {
		this.sessionDataAccess = sessionDataAccess;
	}

	@Override
	protected void doFilterInternal(
		@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain
	) throws ServletException, IOException {
		HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
			@Override
			public HttpSession getSession(boolean create) {
				String requestedSessionId = super.getRequestedSessionId();
				if (requestedSessionId != null) {
					Session requestedSession = sessionDataAccess.getSession(requestedSessionId);
					if (requestedSession != null) {
						HttpSession currentHttpSession = super.getSession(true);
						if (!currentHttpSession.getId().equals(requestedSessionId)) {
							String newSessionId = super.changeSessionId();
							Session newSession = new Session(newSessionId, requestedSession.getAccount());
							sessionDataAccess.replaceSession(requestedSession, newSession);
						}
						return currentHttpSession;
					}
				}
				return super.getSession(create);
			}

			@Override
			public HttpSession getSession() {
				return getSession(true);
			}
		};
		filterChain.doFilter(requestWrapper, response);
	}
}
