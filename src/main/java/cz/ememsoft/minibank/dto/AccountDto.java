package cz.ememsoft.minibank.dto;

import cz.ememsoft.minibank.entity.Account;
import cz.ememsoft.minibank.entity.Customer;

import java.io.Serializable;

/**
 * DTO for {@link Account}
 */
public record AccountDto(Long ac, Long balance, Customer customerId) implements Serializable {
}