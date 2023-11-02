package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
import java.util.Optional;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;
  @Autowired
  private WebApplicationContext webApplicationContext;
  @Mock
  private AccountsRepository accountsRepository;
  private MockMvc mockMvc;
  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }
  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  public void testTransferBalance() throws Exception, Exception, Exception {

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":10}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":10}")).andExpect(status().isCreated());

    Account accFrom = accountsService.getAccount("Id-123");
    Account accTo = accountsService.getAccount("Id-124");
    String accountFromId = "Id-123";
    String accountFromTo = "Id-124";
    BigDecimal amount = new BigDecimal(10);

    TransferRequest request = new TransferRequest();
    request.setAccountFromId(accountFromId);
    request.setAccountToId(accountFromTo);
    request.setAmount(amount);

    when(accountsRepository.getAccountByAccountId(accountFromId)).thenReturn(Optional.of(accFrom));
    when(accountsRepository.getAccountByAccountId(accountFromTo)).thenReturn(Optional.of(accTo));

    this.accountsService.transferBalance(request);

    assertEquals(BigDecimal.ZERO, accFrom.getBalance());
    assertEquals(BigDecimal.TEN.add(BigDecimal.TEN), accTo.getBalance());
  }

  @Test
  public void testOverdraftBalance() throws OverDraftException, AccountNotExistException,Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":10}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":10}")).andExpect(status().isCreated());

    Account accFrom = accountsService.getAccount("Id-123");
    Account accTo = accountsService.getAccount("Id-124");
    String accountFromId = "Id-123";
    String accountFromTo = "Id-124";
    BigDecimal amount = new BigDecimal(20);

    TransferRequest request = new TransferRequest();
    request.setAccountFromId(accountFromId);
    request.setAccountToId(accountFromTo);
    request.setAmount(amount);

    when(accountsRepository.getAccountByAccountId(accountFromId)).thenReturn(Optional.of(accFrom));
    when(accountsRepository.getAccountByAccountId(accountFromTo)).thenReturn(Optional.of(accTo));
    Exception exception = assertThrows(OverDraftException.class, () -> {
      this.accountsService.transferBalance(request);
    });
    String expectedMessage = "does not have enough balance to transfer";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }
}
