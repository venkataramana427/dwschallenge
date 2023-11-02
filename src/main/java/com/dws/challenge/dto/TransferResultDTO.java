package com.dws.challenge.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferResultDTO {

    private String accountFromId;

    private BigDecimal balanceAfterTransfer;


}
