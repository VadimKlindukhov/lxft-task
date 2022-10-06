package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransferControllerTest {

  private static final String FROM_ACCOUNT_ID = "TransferServiceTest_from_accountId";
  private static final String TO_ACCOUNT_ID = "TransferServiceTest_to_accountId";
  private static final String TRANSFER_URL_FORMAT = "/v1/transfer?accountFrom=%s&accountTo=%s&amount=%d";

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private TransferService transferService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private void createAccountsWithBalances(int fromAccountBalance, int toAccountBalance) throws Exception {
    Account fromAccount = new Account(FROM_ACCOUNT_ID);
    fromAccount.setBalance(new BigDecimal(fromAccountBalance));
    accountsService.createAccount(fromAccount);

    Account toAccount = new Account(TO_ACCOUNT_ID);
    toAccount.setBalance(new BigDecimal(toAccountBalance));
    accountsService.createAccount(toAccount);
  }

  @Before
  public void prepareMockMvc() {
    mockMvc = webAppContextSetup(webApplicationContext).build();

    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void doTransfer() throws Exception {
    createAccountsWithBalances(1000, 0);
    String url = String.format(TRANSFER_URL_FORMAT, FROM_ACCOUNT_ID, TO_ACCOUNT_ID, 500);
    mockMvc.perform(post(url)).andExpect(status().isOk());

    assertThat(accountsService.getAccount(FROM_ACCOUNT_ID).getBalance().intValue()).isEqualTo(500);
    assertThat(accountsService.getAccount(TO_ACCOUNT_ID).getBalance().intValue()).isEqualTo(500);
  }

  @Test
  public void doTransferWithWrongAccountId() throws Exception {
    String url = String.format(TRANSFER_URL_FORMAT, FROM_ACCOUNT_ID, TO_ACCOUNT_ID, 500);
    mockMvc.perform(post(url))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string("Account with id " + FROM_ACCOUNT_ID + " not found"));
  }

  @Test
  public void doTransferWithNegativeBalance() throws Exception {
    createAccountsWithBalances(1000, 0);
    String url = String.format(TRANSFER_URL_FORMAT, FROM_ACCOUNT_ID, TO_ACCOUNT_ID, 1500);
    mockMvc.perform(post(url))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string("Not enough money to perform transfer, account id " + FROM_ACCOUNT_ID));
  }

  @Test
  public void doTransferWithWrongAmount() throws Exception {
    String url = String.format(TRANSFER_URL_FORMAT, FROM_ACCOUNT_ID, TO_ACCOUNT_ID, -1000);
    mockMvc.perform(post(url))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Amount must be greater than 0"));
  }
}
