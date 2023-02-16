package ru.sber.kotlin.school.telegram.bot.service.menu


import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.service.database.DictionaryService
import ru.sber.kotlin.school.telegram.bot.service.database.UserService

//@Service
class DictionaryMenuServiceDRAFT(val userService: UserService, val dictionaryService: DictionaryService) {

    fun getDictMenuAndInfoFavDict(ctx: MessageContext) : SendMessage{
        val chatId = ctx.chatId().toString()
        val userId = ctx.user().id
        var message : SendMessage
        // Create ReplyKeyboardMarkup object
        val keyboardMarkup = ReplyKeyboardMarkup()
        // Create the keyboard (list of keyboard rows)
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        // Create a keyboard row
        var row = KeyboardRow()
        // есть или нет у пользователя словарей на изучении
        when(isFavoritesExist(userId)){
            true -> {
                message = SendMessage(chatId,
                    "Список словарей у Вас на изучении: ${getListFavoriteDictionaries(userId)}")
                createOneButtonInRow("Добавить из готовых",row,keyboard)
                // Create another keyboard row
                row = KeyboardRow();
                createOneButtonInRow("Создать новый словарь",row,keyboard)
                // Create another keyboard row
                row = KeyboardRow()
                createOneButtonInRow("Удалить словарь из списка изучаемых",row,keyboard)
                // Create another keyboard row
                row = KeyboardRow();
                createOneButtonInRow("В главное меню",row,keyboard)
            }
            false -> {
                message = SendMessage(chatId,
                    "У вас нет выбранных словарей для изучения. Выберите из предложенных или создайте свой")
                createOneButtonInRow("Создать новый словарь",row,keyboard)
                // Create another keyboard row
                row = KeyboardRow();
                createOneButtonInRow("Добавить из готовых",row,keyboard)
                // Create another keyboard row
                row = KeyboardRow();
                createOneButtonInRow("В главное меню",row,keyboard)
            }
        }
        // Set the keyboard to the markup
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.oneTimeKeyboard = true
        keyboardMarkup.isPersistent = true
        // Add it to the message
        message.replyMarkup = keyboardMarkup

        return message
    }

    /*fun isUserExist(userId : Long) : Boolean {
        val userId = userService.getUserId(userId)
        requireNotNull(userId) { return false }
        return true
    }*/

    fun isFavoritesExist(userId : Long) : Boolean {
        val userFavorites = userService.getUserFavorites(userId)
        require(userFavorites.isNotEmpty()) { return false }
        return true
    }

    fun getListFavoriteDictionaries(userId : Long) : String {
        var result = ""
        /*var list = arrayListOf<String>()
        list.add("Словарь А")
        list.add("Словарь Б")
        list.add("Словарь С")*/
        userService.getUserFavorites(userId).forEach { favDictionary ->
            //val name = dictionaryService.getNameFavoriteDictionary(favDictionary)
            result += "\n--||--\n${favDictionary.name}"
        }

        return result
    }

    fun createOneButtonInRow(nameButton : String, row : KeyboardRow, keyboard: MutableList<KeyboardRow>) {
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add(nameButton)
        // Add the row to the keyboard
        keyboard.add(row)
    }
}