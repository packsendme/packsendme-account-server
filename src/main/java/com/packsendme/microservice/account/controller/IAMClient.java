package com.packsendme.microservice.account.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name="packsendme-iam-server")
public interface IAMClient {
	
	@PutMapping("/iam/identity/{username}/{password}")
	ResponseEntity<?> allowsFirstUserAccess(@PathVariable("username") String username, @PathVariable("password") String password);

}
