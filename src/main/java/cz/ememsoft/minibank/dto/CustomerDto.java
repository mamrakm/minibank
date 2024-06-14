package cz.ememsoft.minibank.dto;

import cz.ememsoft.minibank.entity.Customer;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link Customer}
 */
public record CustomerDto(Long id, String first_name, String middle_name, String last_name,
                          @NotNull List<AccountDto> account) implements Serializable {
}