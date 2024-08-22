package com.hmw.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmw.account.dto.AccountDto;
import com.hmw.account.dto.CreateAccount;
import com.hmw.account.dto.DeleteAccount;
import com.hmw.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void successCreateAccount() throws Exception{
        //given
        given(accountService.createAccount(anyLong(), anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("12")
                        .balance(12345L)
                        .build());

        //when
        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateAccount.Request(1234L, 100000L)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("12"));
        //then
     }

    @Test
    public void successDeleteAccount() throws Exception {
        //given
        given(accountService.deleteAccount(anyLong(), anyString()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234")
                        .build());

        //when
        mockMvc.perform(delete("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeleteAccount.Request(3333L, "1234567890")
                        )))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234"));
        //then

     }

     @Test
     public void success_getAccountByUserId() throws Exception {
         //given
         List<AccountDto> accountDtos = Arrays.asList(
                 AccountDto.builder()
                         .accountNumber("1234567890")
                         .balance(10000L).build(),

                 AccountDto.builder()
                         .accountNumber("2345678901")
                         .balance(20000L).build(),

                 AccountDto.builder()
                         .accountNumber("3456789012")
                         .balance(30000L).build()
         );

         given(accountService.getAccountByUserId(anyLong()))
                 .willReturn(accountDtos);
         //when


         //then

         mockMvc.perform(get("/account?user_id=1"))
                 .andDo(print())
                 .andExpect(jsonPath("$[0].accountNumber")
                         .value("1234567890"))
                 .andExpect(jsonPath("$[1].accountNumber")
                         .value("2345678901"))
                 .andExpect(jsonPath("$[2].accountNumber")
                         .value("3456789012"))
                 .andExpect(jsonPath("$[0].balance")
                         .value(10000))
                 .andExpect(jsonPath("$[1].balance")
                         .value(20000))
                 .andExpect(jsonPath("$[2].balance")
                         .value(30000));
     }
}