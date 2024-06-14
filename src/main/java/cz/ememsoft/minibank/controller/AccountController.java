package cz.ememsoft.minibank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/account")
public class AccountController {
    @GetMapping("/balance")
    public String getBalance() {
        return "balance";
    }

}
