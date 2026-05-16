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

import jakarta.servlet.http.HttpSession;
import org.example.spark.authorization.exceptions.AuthorizationException;
import org.example.spark.gateway.web.exceptions.AuthenticationException;
import org.example.spark.gateway.web.models.*;
import org.example.spark.gateway.web.validators.FormValidators;
import org.example.spark.gateway.web.validators.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.*;

@Controller
@RequestMapping("/panel")
public class AccountPanelRequestController {

	@Value("${org.example.spark.rest.timeout}")
	private long timeout;

	@Autowired
	AccountPanelRequestProcessor accountPanelRequestProcessor;

	@Autowired
	AccountRequestProcessor accountRequestProcessor;

	@GetMapping("/accounts")
	public Callable<String> getAccounts(HttpSession httpSession, Model model) {
		return () -> {
			try {
				Account account = accountRequestProcessor.getAccount(httpSession.getId());
				Future<Account[]> gettingAccountsProcess = accountPanelRequestProcessor.getAccounts(httpSession.getId());
				Account[] accounts = gettingAccountsProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("account", Optional.ofNullable(account));
				model.addAttribute("accounts", accounts);
				return "accounts_panel";
			} catch (AuthenticationException e) {
				return "redirect:/login";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return "403";
					} else return "redirect:/login";
				}
				else return "500";
			} catch (TimeoutException e) {
				return "504";
			}
		};
	}

	@GetMapping("/account/{accountId}")
	public Callable<String> getAccount(
		HttpSession httpSession, Model model, @PathVariable(name = "accountId") long accountId
	) {
		return () -> {
			try {
				Account account = accountRequestProcessor.getAccount(httpSession.getId());
				Future<Account> gettingAccountProcess =
					accountPanelRequestProcessor.getAccount(httpSession.getId(), accountId);
				Account retrievedAccount = gettingAccountProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("account", Optional.ofNullable(account));
				model.addAttribute("managedAccount", retrievedAccount);
				model.addAttribute("errorMessage", Optional.empty());
				return "account_management_form";
			} catch (AuthenticationException e) {
				return "redirect:/login";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return "403";
					} else return "redirect:/login";
				}
				else return "500";
			} catch (TimeoutException e) {
				return "504";
			}
		};
	}

	@GetMapping("/newaccount")
	public Callable<String> newAccount(HttpSession httpSession, Model model) {
		return () -> {
			Account account = accountRequestProcessor.getAccount(httpSession.getId());
			model.addAttribute("account", Optional.ofNullable(account));
			model.addAttribute("errorMessage", Optional.empty());
			return "new_account_form";
		};
	}

	@PostMapping("/newaccount")
	@ResponseBody
	public Callable<FormSubmissionResponse> newAccount(
		HttpSession httpSession, @RequestBody @Valid CreatingAccountForm form
	) {
		return () -> {
			try {
				Future<?> creatingAccountProcess;
				if (form.isAdmin()) {
					creatingAccountProcess = accountPanelRequestProcessor
						.createAdministratorAccount(httpSession.getId(), form.getUsername(), form.getPassword());
				} else {
					creatingAccountProcess = accountPanelRequestProcessor
						.createAccount(httpSession.getId(), form.getUsername(), form.getPassword());
				}
				creatingAccountProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/panel/accounts");
			} catch (AuthenticationException e) {
				return new RedirectFormSubmissionResponse("/login");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				}
				else return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			} catch (TimeoutException e) {
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			}
		};
	}

	@PostMapping("/account/{accountId}/save")
	@ResponseBody
	public Callable<FormSubmissionResponse> saveAccount(
		HttpSession httpSession,
		@PathVariable(name = "accountId") long accountId,
		@RequestBody @Valid AccountManagementForm form
	) {
		return () -> {
			try {
				Future<?> savingAccountProcess = accountPanelRequestProcessor.saveAccount(
					httpSession.getId(),
					accountId,
					form.getPreviousStatus(), form.getCurrentStatus(),
					form.getPreviouslyAssignedRoles(), form.getCurrentlyAssignedRoles()
				);
				savingAccountProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/panel/accounts");
			} catch (AuthenticationException e) {
				return new RedirectFormSubmissionResponse("/login");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				}
				else return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			} catch (TimeoutException e) {
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			}
		};
	}
}
