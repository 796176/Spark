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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.spark.gateway.web.models.ErrorFormSubmissionResponse;
import org.example.spark.gateway.web.models.FormSubmissionResponse;
import org.example.spark.gateway.web.validators.Valid;
import org.example.spark.gateway.web.validators.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.concurrent.Callable;

@Aspect
public class ValidatingControllerAdvising {

	@Autowired
	private List<Validator> validators;

	@Pointcut("@annotation(org.springframework.web.bind.annotation.ResponseBody)")
	public void restEndpoint() { }

	@Pointcut("within(@org.springframework.stereotype.Controller org.example.spark.gateway.web.controllers.*)")
	public void controllerBean() { }

	@Pointcut("execution(public java.util.concurrent.Callable *(..))")
	public void asynchronousMethod() { }

	@Around("controllerBean() && restEndpoint() && asynchronousMethod()")
	public Object validationAdvise(ProceedingJoinPoint pjp) throws Throwable {
		Object[] args = pjp.getArgs();
		Method advisedMethod = ((MethodSignature) pjp.getSignature()).getMethod();
		Parameter[] parameters = advisedMethod.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].getDeclaredAnnotation(Valid.class) != null) {
				Validator.ValidationResult validationResult = validate(args[i]);
				if (validationResult != null) {
					return (Callable<FormSubmissionResponse>) () -> {
						return new ErrorFormSubmissionResponse(validationResult.errorMessage());
					};
				}
			}
		}
		return pjp.proceed();
	}

	private Validator.ValidationResult validate(Object o) {
		for (Validator validator: validators) {
			if (validator.supports(o.getClass())) {
				return validator.validate(o);
			}
		}
		return null;
	}
}
