package cz.ememsoft.minibank.service

/**
 * Service interface for managing accounts in the application.
 *
 * This interface defines the core operations related to accounts, such as retrieving,
 * saving, updating, and deleting account information, as well as handling transactions
 * associated with accounts.
 */
interface AccountService {

    /**
     * Retrieves an account by its identifier.
     *
     * This method is intended to fetch detailed information about a specific account.
     */
    fun getAccount()

    /**
     * Saves a new account to the database.
     *
     * This method is used to create a new account in the system.
     */
    fun saveAccount()

    /**
     * Updates an existing account in the database.
     *
     * This method is used to modify details of an existing account.
     */
    fun updateAccount()

    /**
     * Deletes an account by its identifier.
     *
     * This method removes an account from the system based on its ID.
     */
    fun deleteAccount()

    /**
     * Retrieves transactions associated with an account.
     *
     * This method fetches the transaction history of a specific account.
     */
    fun getAccountTransactions()
}
