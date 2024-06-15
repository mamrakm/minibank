package cz.ememsoft.minibank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/accounts", produces = "application/json")
public class AccountController {
    @GetMapping("/balance")
    public String getBalance() {
        return "balance";
    }

}
