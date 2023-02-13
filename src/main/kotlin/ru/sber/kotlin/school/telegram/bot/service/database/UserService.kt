package ru.sber.kotlin.school.telegram.bot.service.database

import org.springframework.stereotype.Service
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository


@Service
class UserService(val userRepository: UserRepository) {

    fun getUserId(userId : Long) : Long = userRepository.findUserId(userId)

    fun getUserFavorites(userId : Long) : List<Dictionary> = userRepository.findUserFavorites(userId)
}