package cz.ememsoft.minibank.enumeration

/**
 * Client status enumeration for soft delete functionality.
 * 
 * Banking systems require audit trails and cannot delete client records.
 * Instead, clients are marked with different status values.
 */
enum class ClientStatusEnum {
    /**
     * Active client - can perform all operations
     */
    ACTIVE,
    
    /**
     * Inactive client - marked for soft delete, cannot perform operations
     */
    INACTIVE,
    
    /**
     * Suspended client - temporarily disabled, can be reactivated
     */
    SUSPENDED
}