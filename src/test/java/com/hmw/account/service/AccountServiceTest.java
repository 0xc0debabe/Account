package com.hmw.account.service;

import com.hmw.account.domain.Account;
import com.hmw.account.domain.AccountUser;
import com.hmw.account.dto.AccountDto;
import com.hmw.account.exception.AccountException;
import com.hmw.account.repository.AccountRepository;
import com.hmw.account.repository.AccountUserRepository;
import com.hmw.account.type.AccountStatus;
import com.hmw.account.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Documented;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void success_createAccount() {
        //given

        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1234567890")
                        .build()));


        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(pobi)
                        .accountNumber("1234567891")
                        .build());


        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1234567891", accountDto.getAccountNumber());
     }

     @Test
     public void createAccount_USER_NOT_FOUND() {
         //given

         //when
         AccountException accountException = assertThrows(AccountException.class,
                 () -> accountService.createAccount(1L, 10000L));

         //then
         assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
      }

      @Test
      public void createAccount_MAX_ACCOUNT_PER_USER_10() {
          //given
          AccountUser user = AccountUser.builder()
                  .id(1L)
                  .name("user1")
                  .build();

          given(accountUserRepository.findById(anyLong()))
                  .willReturn(Optional.of(user));

          given(accountRepository.countByAccountUser(any()))
                  .willReturn(10);

          //when
          AccountException exception = assertThrows(AccountException.class,
                  () -> accountService.createAccount(10L, 1000L));

          //then
          assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
       }

       @Test
       public void createFirstAccount() {
           //given
           AccountUser user = AccountUser.builder()
                   .id(12L)
                   .name("user1")
                   .build();

           given(accountUserRepository.findById(anyLong()))
                   .willReturn(Optional.of(user));

           given(accountRepository.findFirstByOrderByIdDesc())
                   .willReturn(Optional.empty());

           given(accountRepository.save(any())).willReturn(Account.builder()
                   .accountUser(user)
                   .accountNumber("1234567890")
                   .build());


           ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

           //when
           AccountDto account = accountService.createAccount(1L, 100L);

           //then
           verify(accountRepository, times(1)).save(captor.capture());

           assertEquals(12L, account.getUserId());
           assertEquals("1000000000", captor.getValue().getAccountNumber());
           assertEquals("1234567890", account.getAccountNumber());
        }

        @Test
        public void success_deleteAccount() {
            //given
            AccountUser user = AccountUser.builder()
                    .id(12L)
                    .name("user1")
                    .build();

            Account account = Account.builder()
                    .accountNumber("1234567890")
                    .accountUser(user)
                    .balance(0L)
                    .accountStatus(AccountStatus.IN_USE)
                    .build();

            given(accountUserRepository.findById(anyLong()))
                    .willReturn(Optional.of(user));

            given(accountRepository.findByAccountNumber(anyString()))
                    .willReturn(Optional.of(account));

            ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            //when
            AccountDto accountDto = accountService.deleteAccount(1L, "0987654321");

            //then
            verify(accountRepository, times(1)).save(captor.capture());
            assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
            assertEquals("1234567890", captor.getValue().getAccountNumber());
            assertEquals(12L, captor.getValue().getAccountUser().getId());
            assertEquals(12L, accountDto.getUserId());
         }

         @Test
         public void deleteAccount_USER_NOT_FOUND() {
             //given
             given(accountUserRepository.findById(anyLong()))
                     .willReturn(Optional.empty());

             //when
             AccountException exception = assertThrows(AccountException.class,
                     () -> accountService.deleteAccount(1L, "1234567890"));

             //then
             assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
          }

          @Test
          public void deleteAccount_ACCOUNT_NOT_FOUND() {
              //given
              AccountUser user = AccountUser.builder()
                      .id(12L)
                      .name("user")
                      .build();

              given(accountUserRepository.findById(anyLong()))
                      .willReturn(Optional.of(user));

              given(accountRepository.findByAccountNumber(anyString()))
                      .willReturn(Optional.empty());

              //when
              AccountException exception = assertThrows(AccountException.class,
                      () -> accountService.deleteAccount(12L, "1234567890"));

              //then
              assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
           }

           @Test
           public void deleteAccount_USER_ACCOUNT_UN_MATCH() {
               //given
               AccountUser accountUser = AccountUser.builder()
                       .id(12L)
                       .name("user")
                       .build();

               AccountUser accountUser1 = AccountUser.builder()
                       .id(13L)
                       .name("user1")
                       .build();

               given(accountUserRepository.findById(anyLong()))
                       .willReturn(Optional.of(accountUser));

               given(accountRepository.findByAccountNumber(anyString()))
                       .willReturn(Optional.of(Account.builder()
                               .accountUser(accountUser1)
                               .build()));

               //when
               AccountException exception = assertThrows(AccountException.class,
                       () -> accountService.deleteAccount(1L, "1234567889"));

               //then
               assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
            }

            @Test
            public void deleteAccount_ACCOUNT_ALREADY_UNREGISTERED() {
                //given
                AccountUser user = AccountUser.builder()
                        .id(12L)
                        .name("user")
                        .build();

                given(accountUserRepository.findById(anyLong()))
                        .willReturn(Optional.of(user));

                given(accountRepository.findByAccountNumber(anyString()))
                        .willReturn(Optional.of(
                                Account.builder()
                                        .accountUser(user)
                                        .accountStatus(AccountStatus.UNREGISTERED)
                                        .accountNumber("1234567890")
                                        .build()
                        ));

                //when
                AccountException exception = assertThrows(AccountException.class,
                        () -> accountService.deleteAccount(1L, "1234567890"));

                //then
                assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
             }

             @Test
             public void deleteAccount_BALANCE_NOT_EMPTY() {
                 //given
                 AccountUser user = AccountUser.builder()
                         .id(12L)
                         .name("user")
                         .build();

                 given(accountUserRepository.findById(anyLong()))
                         .willReturn(Optional.of(user));

                 given(accountRepository.findByAccountNumber(anyString()))
                         .willReturn(Optional.of(
                                 Account.builder()
                                         .accountUser(user)
                                         .balance(1000L)
                                         .accountNumber("1234567890")
                                         .build()
                         ));

                 //when
                 AccountException exception = assertThrows(AccountException.class,
                         () -> accountService.deleteAccount(12L, "0987654321"));

                 //then
                 assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
              }

              @Test
              public void success_getAccountByUserId() {
                  //given
                  AccountUser user = AccountUser.builder()
                          .id(12L)
                          .name("user")
                          .build();

                  List<Account> accounts = Arrays.asList(
                          Account.builder()
                                  .id(1L)
                                  .accountUser(user)
                                  .accountNumber("1234567890")
                                  .balance(10000L).build(),

                          Account.builder()
                                  .id(2L)
                                  .accountUser(user)
                                  .accountNumber("2345678901")
                                  .balance(20000L).build(),

                          Account.builder()
                                  .id(3L)
                                  .accountUser(user)
                                  .accountNumber("3456789012")
                                  .balance(30000L).build()
                  );

                  given(accountUserRepository.findById(anyLong()))
                          .willReturn(Optional.of(user));

                  given(accountRepository.findByAccountUser(any()))
                          .willReturn(accounts);

                  //when
                  accountService.getAccountByUserId(1L);

                  //then
                  assertEquals(1L, accounts.get(0).getId());
                  assertEquals(2L, accounts.get(1).getId());
                  assertEquals(3L, accounts.get(2).getId());

                  assertEquals("1234567890", accounts.get(0).getAccountNumber());
                  assertEquals(12, accounts.get(2).getAccountUser().getId());
                  assertEquals(20000, accounts.get(1).getBalance());

               }
}