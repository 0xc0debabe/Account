package com.hmw.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmw.account.dto.CancelBalance;
import com.hmw.account.dto.TransactionDto;
import com.hmw.account.dto.UseBalance;
import com.hmw.account.service.TransactionService;
import com.hmw.account.type.TransactionResultType;
import com.hmw.account.type.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    public void successUseBalance() throws Exception{
        //given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1234567890")
                        .transactedAt(LocalDateTime.now())
                        .amount(12345L)
                        .transactionId("transactionId")
                        .transactionResultType(TransactionResultType.S)
                        .build());

        mockMvc.perform(post("/transaction/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UseBalance.Request(1L, "1234567890", 4000L)
                        ))
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionResultType").value("S"))
                .andExpect(jsonPath("$.amount").value(12345L))
                .andExpect(jsonPath("$.transactionId").value("transactionId"));
        //when

        //then

     }

     @Test
     public void success_cancelBalance() throws Exception{
         //given
         given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                 .willReturn(TransactionDto.builder()
                         .transactionId("transactionId")
                         .accountNumber("1234567890")
                         .transactionResultType(TransactionResultType.S)
                         .amount(1000L)
                         .build());

         //when
         mockMvc.perform(post("/transaction/cancel")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(
                         new CancelBalance.Request(
                                 "transactionId",
                                 "3210987654",
                                 10000L
                         ))))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.transactionId").value("transactionId"))
                 .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                 .andExpect(jsonPath("$.transactionResultType").value("S"))
                 .andExpect(jsonPath("$.amount").value(1000L));
         //then
      }

      @Test
      public void success_queryResponse() throws Exception{
          //given
          given(transactionService.queryTransaction(anyString()))
                  .willReturn(TransactionDto.builder()
                          .transactionId("transactionId")
                          .transactionType(TransactionType.USE)
                          .amount(1000L)
                          .transactionResultType(TransactionResultType.S)
                          .accountNumber("1234567890")
                          .build());

          //when
          mockMvc.perform(get("/transaction/12345"))
                  .andDo(print())
                  .andExpect(status().isOk())
                  .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                  .andExpect(jsonPath("$.transactionType").value("USE"))
                  .andExpect(jsonPath("$.transactionResultType").value("S"))
                  .andExpect(jsonPath("$.transactionId").value("transactionId"))
                  .andExpect(jsonPath("$.amount").value(1000));
          //then

       }
}