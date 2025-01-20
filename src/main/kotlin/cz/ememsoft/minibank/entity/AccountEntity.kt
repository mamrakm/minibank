package cz.ememsoft.minibank.entity

import jakarta.persistence.*

@Entity
@Table(name = "account", schema = "bank")
class AccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    val id: Long,

    @ManyToOne(cascade = [CascadeType.REFRESH], optional = false)
    @JoinColumn(name = "client_entity_client_id", nullable = false)
    var clientEntity: ClientEntity
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountEntity) return false

        if (id != other.id) return false
        if (clientEntity != other.clientEntity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + clientEntity.hashCode()
        return result
    }
}