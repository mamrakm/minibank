package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.dto.client.CreateClientProfileRequestDto
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ClientService {
    // Methods for regular, authenticated users
    fun getClient(id: Long): Mono<ClientDto>
    fun getClientByKeycloakId(keycloakId: String): Mono<ClientDto>
    fun getAllClients(): Flux<ClientDto> // This will now be admin-only at controller level
    fun updateClient(id: Long, updatedClientDto: ClientDto): Mono<ClientDto>
    fun deleteClient(id: Long): Mono<Void> // Soft delete

    // Methods for admin users
    fun createClientProfile(request: CreateClientProfileRequestDto): Mono<ClientDto>
    fun getClientByIdAdmin(id: Long): Mono<ClientDto>
    fun getAllClientsAdmin(): Flux<ClientDto>
    fun getClientsByStatus(status: ClientStatusEnum): Flux<ClientDto>
    fun updateClientStatus(id: Long, newStatus: ClientStatusEnum): Mono<ClientDto>
}