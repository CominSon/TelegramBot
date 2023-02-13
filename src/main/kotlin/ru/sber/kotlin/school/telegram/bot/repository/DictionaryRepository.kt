package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlin.school.telegram.bot.model.Dictionary

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Long> {

    @Query("SELECT d.name FROM Dictionary d WHERE d = :dictionary")
    fun findDictionaryName(@Param("dictionary") dictionary: Dictionary): String
}
