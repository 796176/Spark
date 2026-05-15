function registerSubmitButton(form) {
	const associatedSubmitButton = form.querySelector(".submit_form_button");
	associatedSubmitButton.addEventListener("click", (e) => {
		e.preventDefault();
		const associatedInputElements = form.querySelectorAll("input,textarea,select");

		let isMultipart = false;
		const json = { };
		let csrfToken;
		for (const inputElement of associatedInputElements) {
			if (inputElement.type === "file") {
				isMultipart = true;
			} else {
				if (inputElement.name === "_csrf") {
					csrfToken = inputElement.value;
				} else if (inputElement.type !== "checkbox" || inputElement.checked == true) {
					json[inputElement.name] = inputElement.value;
				}
			}
		}
	
		let associatedMethod;
		const associatedHiddenMethodField = form.querySelector("input[name=\"_method\"]");
		if (associatedHiddenMethodField != null) {
			associatedMethod = associatedHiddenMethodField.value;
		} else {
			associatedMethod = "POST";
		}
		const fetchOptions = {
			method: associatedMethod,
			headers: {
				"X-CSRF-TOKEN": csrfToken
			}
		};
		if (isMultipart) {
			const formData = new FormData();
			formData.append("form", JSON.stringify(json));
			for (const inputElement of associatedInputElements) {
				if (inputElement.type === "file") {
					if (inputElement.files.length != 0) {
						formData.append(inputElement.name, inputElement.files[0]);
					} else {
						formData.append(inputElement.name, new Blob(), "");
					}
				}
			}
			fetchOptions.body = formData;
		} else {
			fetchOptions.headers["Content-Type"] = "application/json";
			fetchOptions.body = JSON.stringify(json);
		}
		
		let uri = form.action;
		if (uri == null) {
			uri = window.location.pathname;
		}
		fetch(uri, fetchOptions)
			.then((response) => {
				return response.json();
			})
			.then((json) => {
				const associatedErrorField = form.querySelector(".error_field");
				if (json.error != null ) {
					associatedErrorField.textContent = json.error;
				} else {
					window.location = json.redirect;
				}
			});
	});
}


const forms = document.querySelectorAll("form");
for (const form of forms) {
	registerSubmitButton(form);
}
