package cz.ememsoft.minibank.dto.request;


import cz.ememsoft.minibank.entity.Customer;
import lombok.Data;

@Data
public class BalanceRequest {
    private Long accountNumber;
    private Customer customer;
}
