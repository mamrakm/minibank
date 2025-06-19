package cz.ememsoft.minibank.enumeration

/**
 * Account status enumeration for soft delete functionality.
 * 
 * Banking systems require audit trails and cannot delete account records.
 * Instead, accounts are marked with different status values.
 */
enum class AccountStatusEnum {
    /**
     * Active account - can perform all operations
     */
    ACTIVE,
    
    /**
     * Closed account - marked for soft delete, cannot perform operations
     * Transaction history is preserved for audit purposes
     */
    CLOSED,
    
    /**
     * Frozen account - temporarily disabled, cannot perform transactions
     */
    FROZEN,
    
    /**
     * Suspended account - temporarily disabled by admin, can be reactivated
     */
    SUSPENDED
}