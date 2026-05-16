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
@RequestMapping("/panel/account/{accountId}")
public class OrderPanelRequestController {

	@Value("${org.example.spark.rest.timeout}")
	private long timeout;

	@Autowired
	private OrderPanelRequestProcessor orderPanelRequestProcessor;

	@Autowired
	private AccountRequestProcessor accountRequestProcessor;

	@GetMapping("/orders")
	public Callable<String> getOrders(
		HttpSession httpSession, Model model, @PathVariable(name = "accountId") long accountId
	) {
		return () -> {
			try {
				Account account = accountRequestProcessor.getAccount(httpSession.getId());
				Future<Order[]> gettingOrdersProcess =
					orderPanelRequestProcessor.getOrders(httpSession.getId(), accountId);
				Order[] orders = gettingOrdersProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("account", Optional.ofNullable(account));
				model.addAttribute("accountId", accountId);
				model.addAttribute("orders", orders);
				return "account_orders_panel";
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

	@GetMapping("/order/{orderId}")
	public Callable<String> getOrder(
		HttpSession httpSession,
		Model model,
		@PathVariable(name = "accountId") long accountId,
		@PathVariable(name = "orderId") long orderId
	) {
		return () -> {
			try {
				Account account = accountRequestProcessor.getAccount(httpSession.getId());
				Future<DetailedOrder> gettingOrderProcessing =
					orderPanelRequestProcessor.getOrder(httpSession.getId(), orderId);
				DetailedOrder detailedOrder = gettingOrderProcessing.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("account", Optional.ofNullable(account));
				model.addAttribute("errorMessage", Optional.empty());
				model.addAttribute("accountId", accountId);
				model.addAttribute("managedOrder", detailedOrder);
				return "order_management_form";
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

	@PostMapping("/order/{orderId}/save")
	@ResponseBody
	public Callable<FormSubmissionResponse> saveOrder(
		HttpSession httpSession,
		@PathVariable(name = "accountId") long accountId,
		@PathVariable(name = "orderId") long orderId,
		@RequestBody @Valid OrderManagementForm form
	) {
		return () -> {
			try {
				Future<?> savingOrderProcess = orderPanelRequestProcessor.saveOrder(
					httpSession.getId(), orderId, form.getVersion(), form.getPreviousStatus(), form.getCurrentStatus()
				);
				savingOrderProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/panel/account/" + accountId + "/orders");
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
