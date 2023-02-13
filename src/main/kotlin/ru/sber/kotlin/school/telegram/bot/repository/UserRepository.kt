package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User


@Repository
interface UserRepository : JpaRepository<User, Long> {

    @Query("SELECT u.id FROM User u WHERE u.id = :userId")
    fun findUserId(@Param("userId") userId: Long): Long

    @Query("SELECT u.favorites FROM User u WHERE u.id = :userId")
    fun findUserFavorites(@Param("userId") userId: Long): List<Dictionary>
}
