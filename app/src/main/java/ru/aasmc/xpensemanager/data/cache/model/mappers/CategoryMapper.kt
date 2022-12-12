package ru.aasmc.xpensemanager.data.cache.model.mappers

import ru.aasmc.xpensemanager.data.cache.model.DBCategory
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Mapper

class CategoryMapper: Mapper<Category, DBCategory> {

    override fun toDomain(dto: DBCategory): Category = Category(
        id = dto.id,
        name = dto.name
    )

    override fun toDto(domain: Category): DBCategory = DBCategory(
        id = domain.id,
        name = domain.name
    )

}