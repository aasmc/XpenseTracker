package ru.aasmc.xpensemanager.domain.model

interface Mapper<Domain, Dto> {
    fun toDomain(dto: Dto): Domain

    fun toDto(domain: Domain): Dto
}