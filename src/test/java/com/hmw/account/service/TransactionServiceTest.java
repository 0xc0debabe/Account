package com.hmw.account.service;

import com.hmw.account.domain.Account;
import com.hmw.account.domain.AccountUser;
import com.hmw.account.domain.Transaction;
import com.hmw.account.dto.TransactionDto;
import com.hmw.account.exception.AccountException;
import com.hmw.account.repository.AccountRepository;
import com.hmw.account.repository.AccountUserRepository;
import com.hmw.account.repository.TransactionRepository;
import com.hmw.account.type.AccountStatus;
import com.hmw.account.type.ErrorCode;
import com.hmw.account.type.TransactionResultType;
import com.hmw.account.type.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void success_useBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("user")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("1234567890")
                .accountStatus(AccountStatus.IN_USE)
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(1L)
                        .accountUser(user)
                        .balance(1000L)
                        .accountNumber("1234567890")
                        .accountStatus(AccountStatus.IN_USE)
                        .build()));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .account(account)
                        .amount(100L)
                        .balanceSnapshot(account.getBalance())
                        .transactionId("transactionId")
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        TransactionDto transactionDto = transactionService.useBalance(1L, "0987654321", 10L);

        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(990, captor.getValue().getBalanceSnapshot());
        assertEquals(10, captor.getValue().getAmount());
        assertEquals(TransactionType.USE, transactionDto.getTransactionType());
        assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
        assertEquals("transactionId", transactionDto.getTransactionId());
        assertEquals(100, transactionDto.getAmount());
     }

     @Test
     public void Transaction_USER_NOT_FOUND() {
         //given
         given(accountUserRepository.findById(anyLong()))
                 .willReturn(Optional.empty());

         //when
         AccountException exception = assertThrows(AccountException.class,
                 () -> transactionService.useBalance(1L, "1234567890", 100L));

         //then
         assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
      }

      @Test
      public void Transaction_ACCOUNT_NOT_FOUND() {
          //given
          AccountUser user = AccountUser.builder()
                  .id(1L)
                  .name("user")
                  .build();

          given(accountUserRepository.findById(anyLong()))
                  .willReturn(Optional.of(user));

          given(accountRepository.findByAccountNumber(anyString()))
                  .willReturn(Optional.empty());

          //when
          AccountException exception = assertThrows(AccountException.class,
                  () -> transactionService.useBalance(1L, "1234567890", 100L));

          //then
          assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
       }

       @Test
       public void Transaction_USER_ACCOUNT_UN_MATCH() {
           //given
           AccountUser user1 = AccountUser.builder()
                   .id(1L)
                   .name("user1")
                   .build();

           AccountUser user2 = AccountUser.builder()
                   .id(2L)
                   .name("user2")
                   .build();

           given(accountUserRepository.findById(anyLong()))
                   .willReturn(Optional.of(user1));

           given(accountRepository.findByAccountNumber(anyString()))
                   .willReturn(Optional.of(Account.builder()
                           .accountUser(user2)
                           .accountNumber("1234567890")
                           .build()));
           //when
           AccountException exception = assertThrows(AccountException.class,
                   () -> transactionService.useBalance(1L, "1234567890", 1000L));
           //then
           assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
        }

        @Test
        public void Transaction_ACCOUNT_ALREADY_UNREGISTERED() {
            //given
            AccountUser user = AccountUser.builder()
                    .id(1L)
                    .name("user")
                    .build();

            given(accountUserRepository.findById(anyLong()))
                    .willReturn(Optional.of(user));

            given(accountRepository.findByAccountNumber(anyString()))
                    .willReturn(Optional.of(Account.builder()
                            .accountUser(user)
                            .accountStatus(AccountStatus.UNREGISTERED)
                            .accountNumber("1234567890")
                            .build()));

            //when
            AccountException exception = assertThrows(AccountException.class,
                    () -> transactionService.useBalance(1L, "1234567890", 1000L));

            //then
            assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());

         }

         @Test
         public void Transaction_AMOUNT_EXCEED_BALANCE() {
             //given
             AccountUser user = AccountUser.builder()
                     .id(1L)
                     .name("user")
                     .build();

             given(accountUserRepository.findById(anyLong()))
                     .willReturn(Optional.of(user));

             given(accountRepository.findByAccountNumber(anyString()))
                     .willReturn(Optional.of(Account.builder()
                             .accountUser(user)
                             .accountStatus(AccountStatus.IN_USE)
                             .accountNumber("1234567890")
                             .balance(100L)
                             .build()));

             //when
             AccountException exception = assertThrows(AccountException.class,
                     () -> transactionService.useBalance(1L, "1234567890", 1000L));

             //then
             assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode() );
          }

          @Test
          public void success_CancelBalance() {
              //given
              AccountUser user = AccountUser.builder()
                      .id(1L)
                      .name("user")
                      .build();

              Account account = Account.builder()
                      .id(1L)
                      .balance(1000L)
                      .accountUser(user)
                      .accountNumber("1234567890")
                      .accountStatus(AccountStatus.IN_USE)
                      .build();

              given(transactionRepository.findByTransactionId(anyString()))
                      .willReturn(Optional.of(Transaction.builder()
                              .id(1L)
                              .transactionType(TransactionType.CANCEL)
                              .transactionResultType(TransactionResultType.S)
                              .amount(200L)
                              .transactedAt(LocalDateTime.now())
                              .account(account)
                              .build()));

              given(accountRepository.findByAccountNumber(anyString()))
                      .willReturn(Optional.of(account));

              given(transactionRepository.save(any()))
                      .willReturn(Transaction.builder()
                              .id(1L)
                              .transactionType(TransactionType.CANCEL)
                              .transactionResultType(TransactionResultType.S)
                              .amount(200L)
                              .transactedAt(LocalDateTime.now())
                              .account(account)
                              .build());

              ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

              //when
              TransactionDto transactionDto = transactionService.cancelBalance(
                      "transactionId", "0123456789", 200L);

              //then
              verify(transactionRepository, times(1)).save(captor.capture());
              assertEquals(TransactionType.CANCEL, transactionDto.getTransactionType());
              assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
              assertEquals(1200, captor.getValue().getBalanceSnapshot());
           }

           @Test
           public void cancelBalance_AMOUNT_EXCEED_BALANCE() {
               //given
               given(transactionRepository.findByTransactionId(anyString()))
                       .willReturn(Optional.empty());

               //when
               AccountException exception = assertThrows(AccountException.class,
                       () -> transactionService.cancelBalance(
                               "transactionId",
                               "0987654321",
                               1234L
                       ));

               //then
               assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
            }

            @Test
            public void cancelBalance_ACCOUNT_NOT_FOUND() {
                //given
                AccountUser user = AccountUser.builder()
                        .id(1L)
                        .name("user")
                        .build();

                Account account = Account.builder()
                        .id(1L)
                        .balance(1000L)
                        .accountUser(user)
                        .accountNumber("1234567890")
                        .accountStatus(AccountStatus.IN_USE)
                        .build();

                given(transactionRepository.findByTransactionId(anyString()))
                        .willReturn(Optional.of(
                                Transaction.builder()
                                        .id(1L)
                                        .transactionType(TransactionType.CANCEL)
                                        .transactionResultType(TransactionResultType.S)
                                        .amount(200L)
                                        .transactedAt(LocalDateTime.now())
                                        .account(account)
                                        .build()));

                given(accountRepository.findByAccountNumber(anyString()))
                        .willReturn(Optional.empty());

                //when
                AccountException exception = assertThrows(AccountException.class,
                        () -> transactionService.cancelBalance(
                                "transactionId",
                                "0987654321",
                                1234L
                        ));

                //then
                assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
             }

             @Test
             public void cancelBalance_CANCEL_MUST_FULLY() {
                 //given
                 AccountUser user = AccountUser.builder()
                         .id(1L)
                         .name("user")
                         .build();

                 Account account = Account.builder()
                         .id(1L)
                         .balance(200L)
                         .accountUser(user)
                         .accountNumber("1234567890")
                         .accountStatus(AccountStatus.IN_USE)
                         .build();

                 given(transactionRepository.findByTransactionId(anyString()))
                         .willReturn(Optional.of(
                                 Transaction.builder()
                                         .id(1L)
                                         .transactionType(TransactionType.CANCEL)
                                         .transactionResultType(TransactionResultType.S)
                                         .amount(200L)
                                         .transactedAt(LocalDateTime.now())
                                         .account(account)
                                         .build()));

                 given(accountRepository.findByAccountNumber(anyString()))
                         .willReturn(Optional.of(account));

                 //when
                 AccountException exception = assertThrows(AccountException.class,
                         () -> transactionService.cancelBalance(
                                 "transactionId",
                                 "0987654321",
                                 1234L
                         ));

                 //then
                 assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
              }

              @Test
              public void cancelBalance_TRANSACTION_ACCOUNT_UN_MATCH() {
                  //given
                  AccountUser user = AccountUser.builder()
                          .id(1L)
                          .name("user")
                          .build();

                  Account account = Account.builder()
                          .id(1L)
                          .balance(1000L)
                          .accountUser(user)
                          .accountNumber("1234567890")
                          .accountStatus(AccountStatus.IN_USE)
                          .build();

                  Account account2 = Account.builder()
                          .id(2L)
                          .balance(1000L)
                          .accountUser(user)
                          .accountNumber("1234567890")
                          .accountStatus(AccountStatus.IN_USE)
                          .build();

                  given(transactionRepository.findByTransactionId(anyString()))
                          .willReturn(Optional.of(
                                  Transaction.builder()
                                          .id(1L)
                                          .transactionType(TransactionType.CANCEL)
                                          .transactionResultType(TransactionResultType.S)
                                          .amount(1000L)
                                          .transactedAt(LocalDateTime.now())
                                          .account(account)
                                          .build()));

                  given(accountRepository.findByAccountNumber(anyString()))
                          .willReturn(Optional.of(account2));

                  //when
                  AccountException exception = assertThrows(AccountException.class,
                          () -> transactionService.cancelBalance(
                                  "transactionId",
                                  "0987654321",
                                  1000L
                          ));

                  //then
                  assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
               }

               @Test
               public void success_queryTransaction() {
                   //given
                   AccountUser user = AccountUser.builder()
                           .id(1L)
                           .name("user")
                           .build();

                   Account account = Account.builder()
                           .id(1L)
                           .balance(1000L)
                           .accountUser(user)
                           .accountNumber("1234567890")
                           .accountStatus(AccountStatus.IN_USE)
                           .build();

                   given(transactionRepository.findByTransactionId(anyString()))
                           .willReturn(Optional.of(Transaction.builder()
                                   .id(1L)
                                   .transactionId("transactionId")
                                   .transactionType(TransactionType.USE)
                                   .transactionResultType(TransactionResultType.S)
                                   .amount(1000L)
                                   .transactedAt(LocalDateTime.now())
                                   .account(account)
                                   .build()));

                   //when
                   TransactionDto transactionDto = transactionService.queryTransaction("test");

                   //then
                   assertEquals("transactionId", transactionDto.getTransactionId());
                   assertEquals(1000, transactionDto.getAmount());
                   assertEquals(TransactionType.USE, transactionDto.getTransactionType());
                   assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
                   assertEquals("1234567890", transactionDto.getAccountNumber());
                }

                @Test
                public void queryTransaction_TRANSACTION_NOT_FOUND() {
                    //given
                    given(transactionRepository.findByTransactionId(anyString()))
                            .willReturn(Optional.empty());

                    //when
                    AccountException exception = assertThrows(AccountException.class,
                            () -> transactionService.queryTransaction("test"));

                    //then
                    assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
                 }


}
