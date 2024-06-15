package cz.ememsoft.minibank.dto.request;

import lombok.Data;

@Data
public class WithdrawRequest {
    private String id;
    private double amount;
}
