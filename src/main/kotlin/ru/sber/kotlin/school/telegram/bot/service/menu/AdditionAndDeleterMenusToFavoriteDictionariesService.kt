package ru.sber.kotlin.school.telegram.bot.service.menu

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.service.database.DictionaryService
import ru.sber.kotlin.school.telegram.bot.service.database.UserService

@Service
class AdditionAndDeleterMenusToFavoriteDictionariesService(private val dictionaryService: DictionaryService, private val userService: UserService) {

    fun getMenuAllDictionaries(upd: Update, partOfMessageText: String) : AnswerInlineQuery {
        val query = upd.inlineQuery
        val userId = getUser(upd).id
        val queryId = query.id
        val results = mutableListOf<InlineQueryResult>()
        val forCircle = when (partOfMessageText){
            "добавлен в словари" -> getAllDictionariesByUser(userId)
            "удален из словарей" -> getAllFavDictionariesByUser(userId)
            else -> { listOf<Dictionary>() }
        }
        forCircle.forEach { dictionaryName ->
            val queryResult = InlineQueryResultArticle.builder().id(dictionaryName.id.toString())
                .title(dictionaryName.name)
                .inputMessageContent(
                    InputTextMessageContent.builder()
                        .messageText("Словарь с темой ${dictionaryName.name}* $partOfMessageText для изучения")
                        .build()
                )
                .build()
            results.add(queryResult)
        }

        return AnswerInlineQuery.builder()
            .inlineQueryId(queryId)
            .cacheTime(0)
            //.switchPmText("Go to bot")
            //.switchPmParameter("dict")
            //ставим ограничение на вывод, потому что всю базу он не выведет через всплывающий список (у tg лимит 50)
            .results(results.take(50))
            .build()
    }

    fun getAllDictionariesByUser(userId: Long) : List<Dictionary> {
        return dictionaryService.getAllDictionariesByUser(userId)
    }

    fun addDictionaryToFavorites(upd: Update){
        val userId = getUser(upd).id
        val message = upd.message.text
        //16 символов включая пробелы до темы в - Словарь с темой
        val theme = message.substring(16,message.lastIndexOf('*'))
        userService.setFavoriteDictionary(userId, theme)
    }

    fun getAllFavDictionariesByUser(userId: Long) : MutableCollection<Dictionary> {
        return userService.getUserFavorites(userId)
    }

    fun deleteDictionaryFromFavorites(upd: Update){
        val userId = getUser(upd).id
        val message = upd.message.text
        //16 символов включая пробелы до темы в - Словарь с темой
        val theme = message.substring(16,message.lastIndexOf('*'))
        userService.deleteFavoriteDictionary(userId, theme)
    }
}