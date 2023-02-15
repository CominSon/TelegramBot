package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Long> {

    @Query("SELECT d.name FROM Dictionary d WHERE d = :dictionary")
    fun findDictionaryName(@Param("dictionary") dictionary: Dictionary): String

    @Query("SELECT d FROM Dictionary d WHERE d.owner = :user Or d.owner = 1")
    fun findAllDictionariesByUser(@Param("user") user: User): List<Dictionary>

    @Query("SELECT d FROM Dictionary d WHERE d.name = :theme AND (d.owner = :user OR d.owner = 1)")
    fun findDictionaryByTheme(@Param("user") user: User, @Param("theme") theme: String): List<Dictionary>

}
