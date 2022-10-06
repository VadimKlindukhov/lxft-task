package com.db.awmd.challenge.web;

import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.exception.WrongAmountException;
import com.db.awmd.challenge.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/v1/transfer")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping
    public ResponseEntity<Object> transfer(@RequestParam String accountFrom, @RequestParam String accountTo, @RequestParam BigDecimal amount) {
        transferService.transfer(accountFrom,accountTo, amount);

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({WrongAmountException.class})
    public ResponseEntity<Object> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler({AccountNotFoundException.class, NegativeBalanceException.class})
    public ResponseEntity<Object> handleBadData(Exception exception) {
        return ResponseEntity.unprocessableEntity().body(exception.getMessage());
    }
}
