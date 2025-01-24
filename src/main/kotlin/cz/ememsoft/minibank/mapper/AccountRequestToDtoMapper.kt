/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Author: EMeMSoft spol. s r.o.
 * 1/24/25, 2:44 PM
 */

package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.dto.request.ClientSaveRequest
import cz.ememsoft.minibank.api.dto.request.CreateAccountRequest
import cz.ememsoft.minibank.api.dto.response.AccountCreatedResponse
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.dto.ClientDto
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

/**
 * Mapper for converting between [ClientSaveRequest] and [ClientDto].
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface AccountRequestToDtoMapper {

    /**
     * Maps a [ClientSaveRequest] to a [ClientDto].
     *
     * @param request The request object containing client details.
     * @return The corresponding [ClientDto].
     */
    fun toDto(request: CreateAccountRequest): AccountDto
}
