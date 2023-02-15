package ru.sber.kotlin.school.telegram.bot.service.database

import org.springframework.stereotype.Service
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository


@Service
class UserService(private val userRepository: UserRepository, private val dictionaryRepository: DictionaryRepository) {

    fun getUserFavorites(userId : Long) : List<Dictionary> = userRepository.findUserFavorites(userId)

    fun setFavoriteDictionary(userId: Long, theme: String){
        val user = userRepository.findById(userId).get()
        val dictionary = dictionaryRepository.findDictionaryByTheme(user, theme)
        user.favorites = dictionary
        userRepository.save(user)
    }

    fun deleteFavoriteDictionary(userId: Long, theme: String){
        val user = userRepository.findById(userId).get()
        val dictionary = dictionaryRepository.findDictionaryByTheme(user, theme).first()
        user.favorites = user.favorites.filter { it == dictionary }
        userRepository.save(user)
    }
}