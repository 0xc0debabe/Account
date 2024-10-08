package com.hmw.account.service;

import com.hmw.account.exception.AccountException;
import com.hmw.account.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {
    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private LockService lockService;

    @Test
    public void successGetLock() throws InterruptedException {
        //given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);

        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(true);

        //when
        assertDoesNotThrow(() -> lockService.lock("123"));

        //then
     }

     @Test
     public void filedGetLock() throws InterruptedException {
         //given
         given(redissonClient.getLock(anyString()))
                 .willReturn(rLock);

         given(rLock.tryLock(anyLong(), anyLong(), any()))
                 .willReturn(false);

         //when
         AccountException exception = assertThrows(AccountException.class,
                 () -> lockService.lock("123"));
         //then
         assertEquals(ErrorCode.ACCOUNT_TRANSACTION_LOCK, exception.getErrorCode());
      }
}