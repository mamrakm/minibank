package cz.ememsoft.minibank.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidInputException(message: String) : RuntimeException(message)
