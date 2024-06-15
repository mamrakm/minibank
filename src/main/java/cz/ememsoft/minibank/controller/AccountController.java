package cz.ememsoft.minibank.controller;

import cz.ememsoft.minibank.dto.request.BalanceRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/accounts", produces = "application/json")
public class AccountController {
    @GetMapping("/balance")
    public String getBalance(final BalanceRequest balanceRequest) {
        balanceRequest.getAccountNumber();
        balanceRequest.getCustomer();
        return "balance";
    }

}
