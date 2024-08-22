package com.hmw.account.controller;

import com.hmw.account.aop.AccountLock;
import com.hmw.account.dto.CancelBalance;
import com.hmw.account.dto.QueryTransactionResponse;
import com.hmw.account.dto.TransactionDto;
import com.hmw.account.dto.UseBalance;
import com.hmw.account.exception.AccountException;
import com.hmw.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    @AccountLock
    public UseBalance.Response useBalance(@Valid @RequestBody UseBalance.Request request) {

        try {
            return UseBalance.Response.from(transactionService.useBalance(
                    request.getUserId(),
                    request.getAccountNumber(),
                    request.getAmount()
            ));
        } catch (AccountException e) {
            log.error("Filed to use balance.");

            transactionService.saveFiledUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    @AccountLock
    public CancelBalance.Response cancelBalance(@Valid @RequestBody CancelBalance.Request request) {

        try {
            return CancelBalance.Response.from(
                    transactionService.cancelBalance(
                            request.getTransactionId(),
                            request.getAccountNumber(),
                            request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to cancel balance.");

            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }

    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryResponse(@PathVariable String transactionId) {
        return QueryTransactionResponse.from(transactionService.queryTransaction(transactionId));
    }
}
