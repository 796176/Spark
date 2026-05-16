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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.concurrent.*;

@Controller
public class OrderRequestController {

	@Value("${org.example.spark.rest.timeout}")
	private long timeout;

	@Autowired
	private AccountRequestProcessor accountRequestProcessor;

	@Autowired
	private OrderRequestProcessor orderRequestProcessor;

	@Autowired
	private InventoryRequestProcessor inventoryRequestProcessor;

	@GetMapping("/orders")
	public Callable<String> getOrders(HttpSession httpSession, Model model) {
		return () -> {
			Account account = accountRequestProcessor.getAccount(httpSession.getId());
			if (account == null) return "redirect:/login";
			model.addAttribute("account", Optional.of(account));

			Future<Order[]> gettingOrdersProcess = orderRequestProcessor.getOrders(httpSession.getId());
			try {
				Order[] orders = gettingOrdersProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("orders", orders);
				return "orders";
			} catch (TimeoutException e) {
				gettingOrdersProcess.cancel(true);
				return "504";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthenticationException)
					return "redirect:/login";
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return "403";
					} else return "redirect:/login";
				}
				return "500";
			}
		};
	}

	@GetMapping("/order/{orderId}")
	public Callable<String> getOrder(
		HttpSession httpSession, @PathVariable(name = "orderId") long orderId, Model model
	) {
		return () -> {
			Account account = accountRequestProcessor.getAccount(httpSession.getId());
			if (account == null) return "redirect:/login";
			model.addAttribute("account", Optional.of(account));

			Future<DetailedOrder> gettingOrderProcess = orderRequestProcessor.getOrder(httpSession.getId(), orderId);
			try {
				DetailedOrder order = gettingOrderProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("detailedOrder", order);
				return "order";
			} catch (TimeoutException e) {
				gettingOrderProcess.cancel(true);
				return "504";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthenticationException)
					return "redirect:/login";
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return "403";
					} else return "redirect:/login";
				}
				return "500";
			}
		};
	}

	@GetMapping("/placeorder")
	public Callable<String> placeOrder(
		HttpSession httpSession,
		Model model
	) {
		return () -> {
			Account account = accountRequestProcessor.getAccount(httpSession.getId());
			if (account == null) return "redirect:/login";
			model.addAttribute("account", Optional.of(account));

			Future<Item[]> gettingInventoryProcess = inventoryRequestProcessor.getInventory(httpSession.getId());
			try {
				Item[] items = gettingInventoryProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("items", items);
				return "place_order_form";
			} catch (TimeoutException e) {
				gettingInventoryProcess.cancel(true);
				return "504";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthenticationException)
					return "redirect:/login";
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return "403";
					} else return "redirect:/login";
				}
				return "500";
			}
		};
	}

	@PostMapping("/placeorder")
	@ResponseBody
	public Callable<FormSubmissionResponse> placeOrder(
		HttpSession httpSession, @RequestBody @Valid NewOrderForm newOrderForm
	) {
		return () -> {
			Future<?> placingOrderProcess = orderRequestProcessor.placeOrder(
				httpSession.getId(), newOrderForm.getTimestamp(), newOrderForm.getLineItems()
			);
			try {
				placingOrderProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/orders");
			} catch (TimeoutException e) {
				placingOrderProcess.cancel(true);
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthenticationException)
					return new RedirectFormSubmissionResponse("/login");
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				}
				return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			}
		};
	}

	@PostMapping("/cancelorder")
	@ResponseBody
	public Callable<FormSubmissionResponse> cancelOrder(
		HttpSession httpSession, @RequestBody @Valid PlacedOrderForm placedOrderForm
	) {
		return () -> {
			Future<?> cancellingOrderProcess = orderRequestProcessor.cancelOrder(
				httpSession.getId(), placedOrderForm.getOrderId(), placedOrderForm.getVersion()
			);
			try {
				cancellingOrderProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/orders");
			} catch (TimeoutException e) {
				cancellingOrderProcess.cancel(true);
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthenticationException)
					return new RedirectFormSubmissionResponse("/login");
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				}
				return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			}
		};
	}

	@PostMapping("/restoreorder")
	@ResponseBody
	public Callable<FormSubmissionResponse> restoreOrder(
		HttpSession httpSession, @RequestBody @Valid PlacedOrderForm placedOrderForm
	) {
		return () -> {
			Future<?> cancellingOrderProcess = orderRequestProcessor.restoreOrder(
				httpSession.getId(), placedOrderForm.getOrderId(), placedOrderForm.getVersion()
			);
			try {
				cancellingOrderProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/orders");
			} catch (TimeoutException e) {
				cancellingOrderProcess.cancel(true);
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthenticationException)
					return new RedirectFormSubmissionResponse("/login");
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				}
				return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			}
		};
	}
}
