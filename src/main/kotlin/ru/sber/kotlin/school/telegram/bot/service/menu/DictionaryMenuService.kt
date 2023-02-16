package ru.sber.kotlin.school.telegram.bot.service.menu

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlin.school.telegram.bot.service.database.DictionaryService
import ru.sber.kotlin.school.telegram.bot.service.database.UserService

@Service
class DictionaryMenuService(val userService: UserService) {

    fun getDictMenuAndInfoFavDict(ctx: MessageContext) : SendMessage{
        val chatId = ctx.chatId().toString()
        val userId = ctx.user().id
        var message : SendMessage
        // Create InlineKeyboardMarkup object
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        // Create the keyboard (list of InlineKeyboardButton list)
        val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        // Create a list for buttons (the first row)
        var buttons: MutableList<InlineKeyboardButton> = ArrayList()
        var button : InlineKeyboardButton
        // есть или нет у пользователя словарей на изучении
        when(isFavoritesExist(userId)){
            true -> {
                message = SendMessage(chatId,
                    "Список словарей у Вас на изучении: ${getListFavoriteDictionaries(userId)}")
                button = InlineKeyboardButton("Добавить из готовых")
                button.switchInlineQueryCurrentChat = "allDictionaries"
                buttons.add(button)
                keyboard.add(buttons)
                //create another new row
                buttons = ArrayList()
                button = InlineKeyboardButton("Создать новый словарь")
                button.callbackData = "temporary" // пока так, дальше Андрей уточнит
                buttons.add(button)
                keyboard.add(buttons)
                //create another new row
                buttons = ArrayList()
                button = InlineKeyboardButton("Удалить словарь из списка изучаемых")
                button.switchInlineQueryCurrentChat = "allFavDictionaries"
                buttons.add(button)
                keyboard.add(buttons)
                //create another new row
                buttons = ArrayList()
                button = InlineKeyboardButton("В главное меню")
                button.callbackData = "temporary" // пока так, дальше Аня уточнит
                buttons.add(button)
            }
            false -> {
                message = SendMessage(chatId,
                    "У вас нет выбранных словарей для изучения. Выберите из предложенных или создайте свой")
                button = InlineKeyboardButton("Создать новый словарь")
                button.callbackData = "temporary" // пока так, дальше Андрей уточнит
                buttons.add(button)
                keyboard.add(buttons)
                //create another new row
                buttons = ArrayList()
                button = InlineKeyboardButton("Добавить из готовых")
                button.switchInlineQueryCurrentChat = "allDictionaries"
                buttons.add(button)
                keyboard.add(buttons)
                //create another new row
                buttons = ArrayList()
                button = InlineKeyboardButton("В главное меню")
                button.callbackData = "temporary" // пока так, дальше Аня уточнит
                buttons.add(button)
            }
        }
        keyboard.add(buttons)
        inlineKeyboardMarkup.keyboard = keyboard
        // Add it to the message
        message.replyMarkup = inlineKeyboardMarkup

        return message
    }

    fun isFavoritesExist(userId : Long) : Boolean {
        val userFavorites = userService.getUserFavorites(userId)
        require(userFavorites.isNotEmpty()) { return false }
        return true
    }

    fun getListFavoriteDictionaries(userId : Long) : String {
        var result = ""
        userService.getUserFavorites(userId).forEach { favDictionary ->
            result += "\n--||--\n${favDictionary.name}"
        }

        return result
    }
}