package ru.sber.kotlin.school.telegram.bot.service.database


import org.springframework.stereotype.Service
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository

@Service
class DictionaryService(val dictionaryRepository: DictionaryRepository, val userRepository: UserRepository) {

    fun getAllDictionariesByUser(userId: Long) : List<Dictionary> {
        val user = userRepository.findById(userId).get()
        return dictionaryRepository.findAllDictionariesByUser(user)
    }
}