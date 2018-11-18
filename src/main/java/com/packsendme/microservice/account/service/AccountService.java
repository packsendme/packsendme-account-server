package com.packsendme.microservice.account.service;

import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClientException;
import com.packsendme.lib.common.constants.HttpExceptionPackSend;
import com.packsendme.lib.common.response.Response;
import com.packsendme.lib.utility.ConvertFormat;
import com.packsendme.microservice.account.controller.IAMClient;
import com.packsendme.microservice.account.dao.AccountDAO;
import com.packsendme.microservice.account.dto.AccountDto;
import com.packsendme.microservice.account.repository.AccountModel;

@Service
@ComponentScan("com.packsendme.lib.utility")
public class AccountService {
	
	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private IAMClient iamClient; 

	@Autowired
	private ConvertFormat convertObj;
	//private ConvertFormat convertObj = new ConvertFormat();
	
	public ResponseEntity<?> registerAccount(AccountDto accountDto) throws Exception {
		AccountModel accountSave = null;
		Response<AccountModel> responseObj = new Response<AccountModel>(HttpExceptionPackSend.CREATED_ACCOUNT.getAction(), accountSave);
		AccountModel account = new AccountModel();
		account = convertToEntity(accountDto);
		Date dtCreation = convertObj.convertStringToDate(accountDto.getDtAction());
		account.setDateCreation(dtCreation);
		try {
			accountSave = accountDAO.add(account); 
			if(accountSave != null) {
				// Call IAMService - To allows User Access 
				ResponseEntity<?> userAccessEnable = iamClient.allowsFirstUserAccess(account.getUsername(),
						account.getPassword(), accountDto.getDtAction());
				if(userAccessEnable.getStatusCode() == HttpStatus.OK) {
					return new ResponseEntity<>(responseObj, HttpStatus.ACCEPTED);
				}
				// Call IAMService - Error update that delete account
				else {
					accountDAO.remove(accountSave);
					return new ResponseEntity<>(responseObj, HttpStatus.BAD_GATEWAY);
				}
			}
			else {
				return new ResponseEntity<>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			accountDAO.remove(accountSave);
			return new ResponseEntity<>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public ResponseEntity<?> updateAccountByUsername(String username, String usernamenew, String dtAction) throws Exception {
		AccountModel entity = new AccountModel();
		Response<AccountModel> responseObj = new Response<AccountModel>(HttpExceptionPackSend.UPDATE_USERNAME.getAction(), entity);
		Date dtUpdate = convertObj.convertStringToDate(dtAction);
		try {
			entity.setUsername(username);
			AccountModel account = accountDAO.find(entity);
			if(account != null) {
				account.setDateUpdate(dtUpdate);
				account.setUsername(usernamenew);
				account = accountDAO.update(account);
				return new ResponseEntity<>(responseObj, HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(responseObj, HttpStatus.NOT_FOUND);
			}
		}
		catch (MongoClientException e ) {
			return new ResponseEntity<>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	
	private AccountModel convertToEntity(AccountDto accountDto) {
		//AccountModel accountModel = modelMapper.map(accountDto, AccountDto.class);
		AccountModel accountModel = new AccountModel();
		BeanUtils.copyProperties(accountDto, accountModel);
		return accountModel;

	}
	
	public ResponseEntity<?> findAccountToLoad(String username) {
		AccountModel entity = new AccountModel();
		try {
			entity.setUsername(username);
			entity = accountDAO.find(entity);
			if(entity != null){
				Response<AccountModel> responseObj = new Response<AccountModel>(HttpExceptionPackSend.FOUND_ACCOUNT.getAction(), entity);
				return new ResponseEntity<>(responseObj, HttpStatus.OK);
			}
			else {
				Response<AccountModel> responseObj = new Response<AccountModel>(HttpExceptionPackSend.FOUND_ACCOUNT.getAction(), entity);
				return new ResponseEntity<>(responseObj, HttpStatus.NOT_FOUND);
			}
		}
		catch (MongoClientException e ) {
			e.printStackTrace();
			Response<AccountModel> responseErrorObj = new Response<AccountModel>(HttpExceptionPackSend.FOUND_ACCOUNT.getAction(), entity);
			return new ResponseEntity<>(responseErrorObj, HttpStatus.NOT_FOUND);
		}
	}


	public ResponseEntity<?> updateAccountByAll(AccountDto accountDto) throws Exception {
		AccountModel entity = new AccountModel();
		entity = convertToEntity(accountDto);
		Date dtUpdate = convertObj.convertStringToDate(accountDto.getDtAction());
		Response<AccountModel> responseObj = new Response<AccountModel>(HttpExceptionPackSend.UPDATE_ACCOUNT.getAction(), null);
		try {
			entity.setDateUpdate(dtUpdate);
			AccountModel accountFind = accountDAO.find(entity);
			if(accountFind != null) {
				entity.setId(accountFind.getId());
				entity = accountDAO.update(entity);
				return new ResponseEntity<>(responseObj, HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(responseObj, HttpStatus.NOT_FOUND);
			}
		}
		catch (MongoClientException e ) {
			return new ResponseEntity<>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	public ResponseEntity<?> findAccountByEmail(String email) {
		AccountModel entity = new AccountModel();
		Response<AccountModel> responseObj = new Response<AccountModel>(HttpExceptionPackSend.FOUND_EMAIL.getAction(), null);
		try {
			entity.setEmail(email);
			entity = accountDAO.find(entity);
			if(entity != null) {
				return new ResponseEntity<>(responseObj, HttpStatus.FOUND);
			}
			else {
				return new ResponseEntity<>(responseObj, HttpStatus.NOT_FOUND);
			}
		}
		catch (MongoClientException e ) {
			e.printStackTrace();
			return new ResponseEntity<>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
