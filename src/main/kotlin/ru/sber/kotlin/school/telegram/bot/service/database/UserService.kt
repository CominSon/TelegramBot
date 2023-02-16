package ru.sber.kotlin.school.telegram.bot.service.database

import org.springframework.stereotype.Service
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import java.util.*


@Service
class UserService(private val userRepository: UserRepository, private val dictionaryRepository: DictionaryRepository) {

    fun getUserFavorites(userId : Long) : MutableCollection<Dictionary> = userRepository.findById(userId).get().favorites

    fun setFavoriteDictionary(userId: Long, theme: String){
        val user = userRepository.findById(userId).get()
        val dictionary = dictionaryRepository.findDictionaryByTheme(user, theme).first()
        user.favorites.add(dictionary)
        userRepository.save(user)
    }

    fun deleteFavoriteDictionary(userId: Long, theme: String){
        var user = userRepository.findById(userId).get()
        val dictionary = dictionaryRepository.findDictionaryByTheme(user, theme).first()
        val tempMutCol = user.favorites
        user.favorites = Collections.emptyList()
        userRepository.save(user)
        user = userRepository.findById(userId).get()
        tempMutCol.forEach {
            if (it.name != dictionary.name){
                user.favorites.add(it)
            }
        }
        userRepository.save(user)
    }
}