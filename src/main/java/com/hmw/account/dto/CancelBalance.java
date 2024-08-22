package com.hmw.account.dto;

import com.hmw.account.aop.AccountLockIdInterface;
import com.hmw.account.type.TransactionResultType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class CancelBalance {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request  implements AccountLockIdInterface {
        @NotBlank
        private String transactionId;
        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber;
        private Long amount;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private String accountNumber;
        private TransactionResultType transactionResultType;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactionAt;

        public static Response from(TransactionDto transactionDto) {
            return Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionResultType(transactionDto.getTransactionResultType())
                    .transactionId(transactionDto.getTransactionId())
                    .amount(transactionDto.getAmount())
                    .transactionAt(transactionDto.getTransactedAt())
                    .build();
        }
    }

}
