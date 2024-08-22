package com.hmw.account.controller;

import com.hmw.account.dto.AccountDto;
import com.hmw.account.dto.AccountInfo;
import com.hmw.account.dto.CreateAccount;
import com.hmw.account.dto.DeleteAccount;
import com.hmw.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class AccountController{
    private final AccountService accountService;

    //    사용자 아이디, 초기 잔액
    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @Valid @RequestBody CreateAccount.Request request) {
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(),
                        request.getInitialBalance()
                )
        );
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(
            @Valid @RequestBody DeleteAccount.Request request) {
        AccountDto accountDto = accountService.deleteAccount(
                request.getUserId(),
                request.getAccountNumber());

        return DeleteAccount.Response.from(accountDto);
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountByUserId(@RequestParam("user_id") Long userId) {
        return accountService.getAccountByUserId(userId)
                .stream()
                .map(accountDto -> AccountInfo.builder()
                        .accountNumber(accountDto.getAccountNumber())
                        .balance(accountDto.getBalance())
                        .build())
                .collect(Collectors.toList());
    }
}