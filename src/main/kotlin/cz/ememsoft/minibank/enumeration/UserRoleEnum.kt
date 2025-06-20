package cz.ememsoft.minibank.enumeration

import cz.ememsoft.minibank.enumeration.UserRoleEnum.ADMIN
import cz.ememsoft.minibank.enumeration.UserRoleEnum.CLIENT

/**
 * Enumeration representing user roles in the banking system.
 *
 * @property CLIENT Regular client who can access only their own banking data
 * @property ADMIN Administrator who can access all client data and admin endpoints
 */
enum class UserRoleEnum {
    /**
     * Regular client role - can only access their own accounts and transactions
     */
    CLIENT,
    
    /**
     * Administrator role - has access to all data and admin endpoints
     */
    ADMIN
}