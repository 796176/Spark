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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.spark.authorization.Role;
import org.example.spark.authorization.exceptions.AuthorizationException;
import org.example.spark.gateway.web.exceptions.AuthenticationException;
import org.example.spark.gateway.web.models.*;
import org.example.spark.gateway.web.validators.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

@Controller
@RequestMapping
public class AccountRequestController {

	@Value("${org.example.spark.rest.timeout}")
	private long timeout;

	@Autowired
	private AccountRequestProcessor accountRequestProcessor;

	@GetMapping("/login")
	public Callable<String> logIn(HttpSession httpSession) {
		return () -> {
			if (accountRequestProcessor.isLoggedIn(httpSession.getId())) return "redirect:/index";
			return "log_in_form";
		};
	}

	@PostMapping("/login")
	public Callable<String> logIn(
		@RequestBody @Valid LogInForm logInForm, Model model, HttpServletRequest httpServletRequest
	) {
		return () -> {
			try {
				accountRequestProcessor.logIn(
					httpServletRequest.getRequestedSessionId(), logInForm.getUsername(), logInForm.getPassword()
				);
			} catch (AuthenticationException e) {
				ErrorMessage errorMessage = new ErrorMessage("Incorrect name or password");
				model.addAttribute("errorMessage", errorMessage);
				return "log_in_form_with_error";
			}
			return "redirect:/index";
		};
	}

	@GetMapping("/signin")
	public String signIn(HttpSession httpSession) {
		if (accountRequestProcessor.isLoggedIn(httpSession.getId())) return "redirect:/index";
		return "sign_in_form";
	}

	@PostMapping("/signin")
	public Callable<String> signIn(
		@RequestBody @Valid SignInForm signInForm, Model model, HttpServletRequest httpServletRequest
	) {
		return () -> {
			Future<?> signInProcess = accountRequestProcessor.signIn(
				httpServletRequest.changeSessionId(),
				signInForm.getUsername(),
				signInForm.getPassword(),
				new Role[] {},
				-1
			);

			try {
				signInProcess.get(timeout, TimeUnit.MILLISECONDS);
				return "redirect:/index";
			} catch (TimeoutException timeoutException) {
				signInProcess.cancel(true);
				return "504";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpServletRequest.getSession(true).getId())) {
						return "403";
					} else return "redirect:/login";
				} else return "500";
			} catch (Exception e) {
				model.addAttribute("errorMessage", new ErrorMessage(e.getMessage()));
				return "sign_in_form_with_error";
			}
		};
	}

	@GetMapping("/myaccount")
	public Callable<String> myAccount(HttpSession httpSession, Model model) {
		return () -> {
			Account account = accountRequestProcessor.getAccount(httpSession.getId());
			if (account == null) return "redirect:/login";
			model.addAttribute("account", account);
			return "my_account_form";
		};
	}

	@PostMapping("/logout")
	public Callable<String> logOut(HttpSession httpSession) {
		return () -> {
			accountRequestProcessor.logOut(httpSession.getId());
			return "redirect:/index";
		};
	}

	@GetMapping("/logout")
	public String logout() {
		return "redirect:/index";
	}

	@DeleteMapping("/myaccount")
	public Callable<String> deleteAccount(HttpSession httpSession) {
		return () -> {
			Future<?> deletingAccountProcess = accountRequestProcessor.deleteAccount(httpSession.getId());
			try {
				deletingAccountProcess.get(timeout, TimeUnit.MILLISECONDS);
				return "redirect:/logout";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return "redirect:/403";
					} else return "redirect:/login";
				} else return "redirect:/500";
			} catch (TimeoutException e) {
				return "redirect:/504";
			}
		};
	}
}
