package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<AccountEntity, Long> {
}
