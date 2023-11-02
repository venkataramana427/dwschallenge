# dwschallenge
 Spring boot application which provide RESTful API
 Prerequisite
    Maven
    JDK 1.8+
	
 Database
    H2
 Features
    Creating account
	Retrieving account
	Transfer Amount

 Basic API Information
 Method	  Path	                     Usage
  POST	 /v1/accounts/	             Create Account
  GET	/v1/accounts/{accountId}	 Retrieving account	
  POST   /v1/accounts/amounttransfer Transfer Amount from one account to another account
  
  
 Sample Playloads:
 Creating account:
 http://localhost:18080/v1/accounts
 {
"accountId":"SBI123457",
"balance":10000

} 
Retrieving account:
http://localhost:18080/v1/accounts/SBI123457

Transfer:
http://localhost:18080/v1/accounts/amounttransfer
{
    "accountFromId":"SBI123456",
    "accountToId":"SBI123457",
    "amount":2000
}
  