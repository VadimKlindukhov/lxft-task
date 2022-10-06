package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.exception.WrongAmountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountsService accountsService;

    private final NotificationService notificationService;

    @Autowired
    public TransferService(AccountsService accountsService, NotificationService notificationService) {
        this.accountsService = accountsService;
        this.notificationService = notificationService;
    }

    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WrongAmountException("Amount must be greater than 0");
        }

        if (fromAccountId.equals(toAccountId)) {
            return;
        }

        Account fromAccount = loadAccount(fromAccountId);
        Account toAccount = loadAccount(toAccountId);

        //trying to avoid deadlock
        boolean directOrder = fromAccountId.compareTo(toAccountId) > 0;
        Account outer = directOrder ? fromAccount : toAccount;
        Account inner = directOrder ? toAccount : fromAccount;

        synchronized (outer) {
            synchronized (inner) {
                if (fromAccount.getBalance().compareTo(amount) < 0) {
                    throw new NegativeBalanceException("Not enough money to perform transfer, account id " + fromAccountId);
                }

                fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                toAccount.setBalance(toAccount.getBalance().add(amount));
            }
        }

        notificationService.notifyAboutTransfer(fromAccount, "Funds transferred to the account " + toAccountId + ", amount = " + amount);
        notificationService.notifyAboutTransfer(toAccount, "Funds received from the account " + fromAccountId + ", amount = " + amount);
    }

    private Account loadAccount(String accountId) {
        Account account = accountsService.getAccount(accountId);

        if (account == null) {
            throw new AccountNotFoundException("Account with id " + accountId + " not found");
        }

        return account;
    }
}
