package cz.ememsoft.minibank.api.dto.response

import cz.ememsoft.minibank.dto.ClientDto

data class ClientGetAllResponse(val clients: List<ClientDto>)
