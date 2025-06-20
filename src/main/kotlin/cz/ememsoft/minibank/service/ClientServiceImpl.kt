package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.dto.client.CreateClientProfileRequestDto
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.DuplicateClientException
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.repository.ClientRepository
import cz.ememsoft.minibank.repository.findByStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class ClientServiceImpl(
    private val clientRepository: ClientRepository,
    private val clientMapper: ClientMapper,
) : ClientService {

    override fun getClient(id: Long): Mono<ClientDto> {
        logger.info { "Fetching client with ID: $id" }
        return clientRepository.findActiveById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .map { clientMapper.entityToDto(it) }
    }

    override fun getClientByKeycloakId(keycloakId: String): Mono<ClientDto> {
        logger.info { "Fetching client with Keycloak ID: $keycloakId" }
        return clientRepository.findByKeycloakUserId(keycloakId)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with Keycloak ID $keycloakId not found")))
            .map { clientMapper.entityToDto(it) }
    }

    override fun getAllClients(): Flux<ClientDto> {
        logger.info { "Fetching all active clients" }
        return clientRepository.findAllActive()
            .map { clientMapper.entityToDto(it) }
    }

    override fun updateClient(id: Long, updatedClientDto: ClientDto): Mono<ClientDto> {
        logger.info { "Updating client with ID: $id" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { existingClient ->
                val updatedEntity = clientMapper.mergeForUpdate(updatedClientDto, existingClient)
                clientRepository.save(updatedEntity)
            }
            .map { clientMapper.entityToDto(it) }
    }

    override fun deleteClient(id: Long): Mono<Void> {
        logger.info { "Soft deleting client with ID: $id" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { client ->
                val updatedClient = client.copy(status = ClientStatusEnum.INACTIVE)
                clientRepository.save(updatedClient)
            }
            .then()
    }

    // --- Administrative Methods ---

    @Transactional
    override fun createClientProfile(request: CreateClientProfileRequestDto): Mono<ClientDto> {
        logger.info { "Admin: Creating client profile for Keycloak ID: ${request.keycloakUserId}" }
        return clientRepository.findByKeycloakUserId(request.keycloakUserId)
            .hasElement()
            .flatMap { exists ->
                if (exists) {
                    Mono.error(DuplicateClientException("Client profile for Keycloak user ID ${request.keycloakUserId} already exists."))
                } else {
                    val clientEntity = clientMapper.createProfileRequestToEntity(request)
                    clientRepository.save(clientEntity)
                        .map { clientMapper.entityToDto(it) }
                }
            }
    }

    override fun getClientByIdAdmin(id: Long): Mono<ClientDto> {
        logger.info { "Admin: Fetching client with ID: $id (any status)" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .map { clientMapper.entityToDto(it) }
    }

    override fun getAllClientsAdmin(): Flux<ClientDto> {
        logger.info { "Admin: Fetching all clients (any status)" }
        return clientRepository.findAll()
            .map { clientMapper.entityToDto(it) }
    }

    override fun getClientsByStatus(status: ClientStatusEnum): Flux<ClientDto> {
        logger.info { "Admin: Fetching clients with status: $status" }
        return clientRepository.findByStatus(status)
            .map { clientMapper.entityToDto(it) }
    }

    override fun updateClientStatus(id: Long, newStatus: ClientStatusEnum): Mono<ClientDto> {
        logger.info { "Admin: Updating client status for ID: $id to $newStatus" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { existingClient ->
                val updatedClient = existingClient.copy(status = newStatus)
                clientRepository.save(updatedClient)
            }
            .map { clientMapper.entityToDto(it) }
    }
}