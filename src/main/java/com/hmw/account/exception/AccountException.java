package com.hmw.account.exception;

import com.hmw.account.type.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;

    public AccountException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getDescription();
    }
}
