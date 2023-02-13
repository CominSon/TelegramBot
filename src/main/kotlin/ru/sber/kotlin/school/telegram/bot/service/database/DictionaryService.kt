package ru.sber.kotlin.school.telegram.bot.service.database


import org.springframework.stereotype.Service
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository

@Service
class DictionaryService(val dictionaryRepository: DictionaryRepository) {

    fun getNameFavoriteDictionary(dictionary: Dictionary) : String = dictionaryRepository.findDictionaryName(dictionary)
}