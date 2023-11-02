package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull
    private String accountFromId;

    @NotNull
    private String accountToId;

    @NotNull
    @Min(value = 0, message = "Transfer amount must be positive.")
    private BigDecimal amount;


    @JsonCreator
    public TransferRequest(@NotNull @JsonProperty("accountFromId") String accountFromId,
                           @NotNull @JsonProperty("accountToId") String accountToId,
                           @NotNull @Min(value = 0, message = "Transfer amount must be positive") @JsonProperty("amount") BigDecimal amount) {
        super();
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
    }

    public TransferRequest() {

    }
}
