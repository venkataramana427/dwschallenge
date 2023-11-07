package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.utils.ErrorMsgCode;
import jakarta.transaction.SystemException;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AccountsService {
  private Lock lock = new ReentrantLock();//ReentrantLock class implements the Lock interface. It offers the same concurrency and memory semantics.We need to make sure that we are wrapping the lock() and the unlock() calls in the try-finally block to avoid the deadlock situations
  @Getter
  private final AccountsRepository accountsRepository;
  @Autowired
  NotificationService notificationService;
  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  List<String> accountFromList =new ArrayList<String>();
  List<String> accountToList =new ArrayList<String>();

  @Transactional
  public void transferBalance(TransferRequest transfer) throws OverDraftException, AccountNotExistException {
    Account accountFrom = accountsRepository.getAccountByAccountId(transfer.getAccountFromId())
            .orElseThrow(() -> new AccountNotExistException("Account with id:" + transfer.getAccountFromId() + " does not exist.", ErrorMsgCode.ACCOUNT_ERROR));

    Account accountTo = accountsRepository.getAccountByAccountId(transfer.getAccountToId())
            .orElseThrow(() -> new AccountNotExistException("Account with id:" + transfer.getAccountFromId() + " does not exist.", ErrorMsgCode.ACCOUNT_ERROR));

     //Check Balance
     if (accountFrom.getBalance().compareTo(transfer.getAmount()) < 0) {
       throw new OverDraftException("Account with id:" + accountFrom.getAccountId() + " does not have enough balance to transfer.", ErrorMsgCode.ACCOUNT_ERROR);
     }

     if(checkAccountId(accountFrom,accountTo)){
         lock.lock();
         checkTransaction(accountFrom,accountTo,transfer);
         accountFromList.add(accountFrom.getAccountId());
         accountToList.add(accountTo.getAccountId());
         try {
             Thread.sleep(10000);
         }
         catch (InterruptedException e) {
             e.printStackTrace();
         }
         lock.unlock();

     }

     else{
         synchronized (lock){
             lock.lock();
             checkTransaction(accountFrom,accountTo,transfer);
             accountFromList.remove(accountFrom.getAccountId());
             accountToList.remove(accountTo.getAccountId());
             lock.unlock();
         }
     }


  }
//Withdraw Amount
  public  boolean withdraw(BigDecimal amount, Account accountFrom) {
      accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        return true;
    }

  //Deposit Amount
  public boolean deposit(BigDecimal amount,Account accountTo) {
        accountTo.setBalance(accountTo.getBalance().add(amount));
        return true;

    }

    //Check the accountIds in fromList and toList for locking
    public boolean checkAccountId(Account accountFrom,Account accountTo){
            if(accountFromList.contains(accountTo.getAccountId()) || accountToList.contains(accountFrom.getAccountId()))
            {
                return false;
            }
             return true;
    }

    public void checkTransaction(Account accountFrom,Account accountTo,TransferRequest transfer){
        // withdraw amount.
        boolean isWithdraw = withdraw(transfer.getAmount(), accountFrom);
        if (isWithdraw) {
            notificationService.notifyAboutTransfer(accountFrom, "Amount Withdraw from your account Id:" + accountFrom.getAccountId() + "to " + accountTo.getAccountId());
            //Deposit Amount
            boolean isDeposit = deposit(transfer.getAmount(), accountTo);
            if (isDeposit) {
                notificationService.notifyAboutTransfer(accountFrom, "Amount Deposit to your account Id:" + accountTo.getAccountId() + "from " + accountFrom.getAccountId());

            }
        } else {
            // The deposit failed. Refund the money back into the account.
            deposit(transfer.getAmount(), accountFrom);
            notificationService.notifyAboutTransfer(accountFrom, "Amount Refund to your account Id:" + accountTo.getAccountId());

        }


    }

}

