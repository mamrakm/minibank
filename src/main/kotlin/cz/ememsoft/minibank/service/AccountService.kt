package cz.ememsoft.minibank.service

interface AccountService {
    fun getAccount()
    fun saveAccount()
    fun updateAccount()
    fun deleteAccount()
    fun getAccountTransactions()
}
