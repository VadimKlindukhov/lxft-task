package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.exception.WrongAmountException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransferServiceTest {

    private static final String FROM_ACCOUNT_ID = "TransferServiceTest_from_accountId";
    private static final String TO_ACCOUNT_ID = "TransferServiceTest_to_accountId";

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private TransferService transferService;

    @Before
    public void prepare() throws Exception {
        accountsService.getAccountsRepository().clearAccounts();
    }

    private void createAccountsWithBalances(int fromAccountBalance, int toAccountBalance) throws Exception {
        Account fromAccount = new Account(FROM_ACCOUNT_ID);
        fromAccount.setBalance(new BigDecimal(fromAccountBalance));
        accountsService.createAccount(fromAccount);

        Account toAccount = new Account(TO_ACCOUNT_ID);
        toAccount.setBalance(new BigDecimal(toAccountBalance));
        accountsService.createAccount(toAccount);
    }

    @Test
    public void doTransfer() throws Exception {
        createAccountsWithBalances(1000, 0);

        transferService.transfer(FROM_ACCOUNT_ID, TO_ACCOUNT_ID, BigDecimal.valueOf(1000));

        assertThat(accountsService.getAccount(FROM_ACCOUNT_ID).getBalance().intValue()).isEqualTo(0);
        assertThat(accountsService.getAccount(TO_ACCOUNT_ID).getBalance().intValue()).isEqualTo(1000);
    }

    @Test
    public void doTransferWithWrongAccountId() throws Exception {
        try {
            transferService.transfer(FROM_ACCOUNT_ID, TO_ACCOUNT_ID, BigDecimal.valueOf(1000));
            fail("Should have failed when doing transfer with wrong account id");
        } catch (AccountNotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account with id " + FROM_ACCOUNT_ID + " not found");
        }
    }

    @Test
    public void doTransferWithWrongAmount() throws Exception {
        createAccountsWithBalances(1000, 0);

        try {
            transferService.transfer(FROM_ACCOUNT_ID, TO_ACCOUNT_ID, BigDecimal.valueOf(-1000));
            fail("Should have failed when doing transfer with wrong amount");
        } catch (WrongAmountException ex) {
            assertThat(ex.getMessage()).isEqualTo("Amount must be greater than 0");
        }
    }

    @Test
    public void doTransferWithNegativeBalance() throws Exception {
        createAccountsWithBalances(1000, 0);

        try {
            transferService.transfer(FROM_ACCOUNT_ID, TO_ACCOUNT_ID, BigDecimal.valueOf(2000));
            fail("Should have failed when doing transfer with wrong amount");
        } catch (NegativeBalanceException ex) {
            assertThat(ex.getMessage()).isEqualTo("Not enough money to perform transfer, account id " + FROM_ACCOUNT_ID);
        }
    }
}
