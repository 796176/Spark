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
	@ResponseBody
	public Callable<FormSubmissionResponse> logIn(
		@RequestBody @Valid LogInForm logInForm, HttpServletRequest httpServletRequest
	) {
		return () -> {
			try {
				accountRequestProcessor.logIn(
					httpServletRequest.getRequestedSessionId(), logInForm.getUsername(), logInForm.getPassword()
				);
			} catch (AuthenticationException e) {
				return new ErrorFormSubmissionResponse("Incorrect name or password");
			}
			return new RedirectFormSubmissionResponse("/index");
		};
	}

	@GetMapping("/signin")
	public String signIn(HttpSession httpSession) {
		if (accountRequestProcessor.isLoggedIn(httpSession.getId())) return "redirect:/index";
		return "sign_in_form";
	}

	@PostMapping("/signin")
	@ResponseBody
	public Callable<FormSubmissionResponse> signIn(
		@RequestBody @Valid SignInForm signInForm, HttpServletRequest httpServletRequest
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
				return new RedirectFormSubmissionResponse("/index");
			} catch (TimeoutException timeoutException) {
				signInProcess.cancel(true);
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpServletRequest.getSession(true).getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				} else return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
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
	@ResponseBody
	public Callable<FormSubmissionResponse> logOut(HttpSession httpSession) {
		return () -> {
			accountRequestProcessor.logOut(httpSession.getId());
			return new RedirectFormSubmissionResponse("/login");
		};
	}

	@GetMapping("/logout")
	public String logout() {
		return "redirect:/index";
	}

	@DeleteMapping("/myaccount")
	@ResponseBody
	public Callable<FormSubmissionResponse> deleteAccount(HttpSession httpSession) {
		return () -> {
			Future<?> deletingAccountProcess = accountRequestProcessor.deleteAccount(httpSession.getId());
			try {
				deletingAccountProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/logout");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				} else return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			} catch (TimeoutException e) {
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			}
		};
	}
}
