package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.service.menu.DictionaryMenuService

//@Component
class AbilityBotExampleDRAFT(
    @Value("\${telegram.bot.token}")
    private val token: String,
    @Value("\${telegram.bot.name}")
    private val name: String,
    val dictionaryMenuService: DictionaryMenuService
) : AbilityBot(token, name) {
    override fun creatorId(): Long = 1234

    /**
     * Создание списков в режиме inline, для перехода в режим нужно:
     * 1. Включить опцию в боте через @BotFather
     * 2. Набрать в поисковой строке @BotName
     * 3. С клавиатурных кнопок в режим не перейти, значит нужно через inlineButtons
     * пример в sendInlineKeyboard() кнопка toInline
     */
    fun echoInlineReply(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val answer = processInline(upd)
            sender.execute(answer)
        }
        return Reply.of(action, isInlineQuery())
    }

    /**
     * Так можно выполнять фильтрацию по инлайн командам
     */
    private fun isInlineQuery(): (Update) -> Boolean = { upd: Update ->
        upd.hasInlineQuery() && upd.inlineQuery.query == "toInline"
    }

    fun processInline(upd: Update): AnswerInlineQuery {
        val query = upd.inlineQuery
        val results = mutableListOf<InlineQueryResult>()

        for (i: Int in 0..5) {
            val queryResult = InlineQueryResultArticle.builder().id("$i")
                .title("Query$i")
                .inputMessageContent(
                    InputTextMessageContent.builder()
                        .messageText("This is query $i")
                        .build()
                )
                .build()
            results.add(queryResult)
        }

        return AnswerInlineQuery.builder()
            .inlineQueryId(query.id)
            .cacheTime(0)
            .switchPmText("Go to bot")
            .switchPmParameter("dict")
            .results(results)
            .build()

    }

    /**
     * Создание команды /hello, которая отправляет пользователю текстовое
     * сообщение с данными его Telegram аккаунт
     */
    fun echoMessage(): Ability {
        return Ability.builder()
            .name("hello")
            .info("says hello world!")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val user = ctx.user()
                val data = prepareUserData(user)
                silent.send(data, ctx.chatId())
            }
            .build()
    }

    private fun prepareUserData(user: User): String = """Hi!
                    ${user.userName} - username
                    ${user.id} - id
                    ${user.isBot} - isBot
                    ${user.canJoinGroups} - canJoin
                    ${user.firstName} - firstName
                    ${user.lastName} - lastName
                    ${user.addedToAttachmentMenu} - AttachmentMenu
                    ${user.canReadAllGroupMessages} - canRead
                    ${user.languageCode} - language
                    ${user.supportInlineQueries} - queries
                    ${user.isPremium} - premium
                """.trimMargin()


    /**
     * Обработка текстовых сообщений от пользователя,
     * предикат isNotCommandUpdate() проверяет, что текст не является командой
     */
    /*fun echoReplyMessage(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val msg = SendMessage(
                upd.message.chatId.toString(),
                "${upd.message.from.firstName} said ${upd.message.text.uppercase()}"
            )
            //Удалить кастомную клавиатуру
            msg.replyMarkup = ReplyKeyboardRemove(true)

            sender.execute(msg)
        }

        return Reply.of(action, isNotCommandUpdate())
    }

    private fun isNotCommandUpdate(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && !upd.message.text.startsWith("/")
    }*/

    /**
     * Команда /keys отправляет пользователю кнопки в районе клавиатуры
     */
    fun drawKeyboardButtons(): Ability {
        return Ability.builder()
            .name("keys")
            .info("Get keyboardButtons")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val msg = sendCustomKeyboard(ctx.chatId().toString())
                sender.execute(msg)
            }
            .build()
    }

    fun sendCustomKeyboard(chatId: String): SendMessage {
        val message = SendMessage(chatId, "Message with keyboard buttons")

        // Create ReplyKeyboardMarkup object
        val keyboardMarkup = ReplyKeyboardMarkup()
        // Create the keyboard (list of keyboard rows)
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        // Create a keyboard row
        var row = KeyboardRow()
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add("Button1");
        row.add("Row 1 Button 2");
        row.add("Row 1 Button 3");
        // Add the first row to the keyboard
        keyboard.add(row)
        // Create another keyboard row
        row = KeyboardRow();
        // Set each button for the second line
        row.add("Row 2 Button 1");
        row.add("Row 2 Button 2");
        row.add("Row 2 Button 3");
        // Add the second row to the keyboard
        keyboard.add(row);
        // Set the keyboard to the markup
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.oneTimeKeyboard = true
        keyboardMarkup.isPersistent = true
        // Add it to the message
        message.replyMarkup = keyboardMarkup

        return message
    }

    /**
     * Команда /inlBtn отправляет пользователю кнопки под сообщением
     */
    fun drawInlineButtons(): Ability = Ability.builder()
        .name("inl")
        .info("Get keyboardButtons")
        .locality(Locality.USER)
        .privacy(Privacy.PUBLIC)
        .action { ctx ->
            val msg = sendInlineKeyboard(ctx.chatId().toString())
            sender.execute(msg)
        }
        .build()

    fun sendInlineKeyboard(chatId: String): SendMessage {
        val message = SendMessage(chatId, "Message with inline buttons")

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        // Create the keyboard (list of InlineKeyboardButton list)
        val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        // Create a list for buttons
        val buttons: MutableList<InlineKeyboardButton> = ArrayList()
        // Initialize each button, the text must be written
        val youtube: InlineKeyboardButton = InlineKeyboardButton("youtube")
        // Also must use exactly one of the optional fields,it can edit  by set method
        youtube.url = "https://www.youtube.com"

        // Add button to the list
        buttons.add(youtube)

        val toInline = InlineKeyboardButton("toInlineSuc")
        toInline.switchInlineQueryCurrentChat = "toInline"
        buttons.add(toInline)
        //This fails by predicate isInlineQuery()
        val toInlineFail = InlineKeyboardButton("toInlineFail")
        toInlineFail.switchInlineQueryCurrentChat = "toInlineFail"
        buttons.add(toInlineFail)

        keyboard.add(buttons)
        inlineKeyboardMarkup.keyboard = keyboard
        // Add it to the message
        message.replyMarkup = inlineKeyboardMarkup

        return message
    }

    /**
     * Команда /scr возвращает список меню
     * (пока для отображения меню требуется перезагрузить страницу) - DRAFT
     */
    fun menuButtons() : Ability = Ability.builder()
        .name("scr")
        .info("Get scrollMenuButtons")
        .locality(Locality.USER)
        .privacy(Privacy.PUBLIC)
        .action { ctx ->
            //sendMenuButton(ctx.chatId().toString())
            val msg = sendMenuButton(ctx.chatId().toString())
            sender.execute(msg)
        }
        .build()

    fun sendMenuButton(chatId: String) : SetMyCommands {
        val message = SendMessage(chatId, "Message with menu button")
        val list = ArrayList<BotCommand>()
        list.add(BotCommand("/hello","user data"))
        list.add(BotCommand("/inl","user data"))
        //list.add(BotCommand("/keys","user data"))
        //sender.execute(SetMyCommands(list, BotCommandScopeChat(chatId),null))
        execute(message)
        return SetMyCommands(list, BotCommandScopeChat(chatId),null)
    }

    /*fun replyMenu(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val stc = SetMyCommands(
                listOf<BotCommand>(BotCommand("/hello","user data")),
                BotCommandScopeChat(upd.message.chatId.toString()),
                null
            )
            sender.execute(stc)
        }

        return Reply.of(action, true)
    }*/

    /*НАЧИНАТЬ СМОТРЕТЬ ЭТОТ КЛАСС КОНТРОЛЛЕРА ОТСЮДА И НИЖЕ (если по replyMessage подход согласовываем как ниже то, часть функции в отдельную можно вынести)*/

    /**
     * Команда /dict_menu возвращает клавиатурное меню
     * и список словарей на изучении
     */
    fun dictionaryMenu() : Ability = Ability.builder()
        .name("dict_menu")
        .info("Получаем клавиатурное меню и список словарей на изучении")
        .locality(Locality.USER)
        .privacy(Privacy.PUBLIC)
        .action { ctx ->
            val msg = dictionaryMenuService.getDictMenuAndInfoFavDict(ctx)
            sender.execute(msg)
        }
        .build()

    fun replyMessage(message : String, upd: Update) {
        val msg = SendMessage(
            upd.message.chatId.toString(),
            "${upd.message.from.firstName}" + message
        )
        //Удалить клавиатуру
        msg.replyMarkup = ReplyKeyboardRemove(true)
        sender.execute(msg)
    }

    /**
     * Команда {В главное меню} отправляет пользователя в главное меню
     */
    fun replyMessageMainMenu(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            replyMessage(":", upd)
            //здесь переход в главное меню (пока эта функция в качестве демонстрации)
            val msg = sendInlineKeyboard(upd.message.chatId.toString())
            sender.execute(msg)
        }
        return Reply.of(action, isMainMenu())
    }

    private fun isMainMenu(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && upd.message.text == "В главное меню"
    }

    /**
     * Команда {Добавить из готовых} отправляет пользователя в список всех словарей
     */
    fun replyMessageListDictionaries(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            replyMessage(", выберите словарь, который хотите добавить:", upd)
            //здесь переход в список всех словарей
            val msg = sendInlineKeyboard(upd.message.chatId.toString())//additionMenuToFavoriteDictionariesService.getMenuAllDictionaries(upd)
            sender.execute(msg)
        }
        return Reply.of(action, isListDictionaries())
    }

    private fun isListDictionaries(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && upd.message.text == "Добавить из готовых"
    }

    /**
     * Команда {Создать новый словарь} отправляет пользователя в создание нового словаря
     */
    fun replyMessageCreateNewDictionary(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            replyMessage(", введите название нового словаря:", upd)
            //здесь переход в создание словаря (пока эта функция в качестве демонстрации)
            val msg = sendInlineKeyboard(upd.message.chatId.toString())
            sender.execute(msg)
        }
        return Reply.of(action, isCreateNewDictionary())
    }

    private fun isCreateNewDictionary(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && upd.message.text == "Создать новый словарь"
    }

    /**
     * Команда {Удалить словарь из списка изучаемых} отправляет пользователю список словарей на изучении
     * при нажатии на один из них идёт удаление этого словаря (по схеме из поведения пользователя)
     */
    fun replyMessageDeleteDictionaryFromFavorites(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            replyMessage(", нажмите на словарь, который хотите удалить из списка изучаемых: ", upd)
            //здесь переход в создание словаря (пока эта функция в качестве демонстрации)
            val msg = sendInlineKeyboard(upd.message.chatId.toString())
            sender.execute(msg)
        }
        return Reply.of(action, isDeleteDictionaryFromFavorites())
    }

    private fun isDeleteDictionaryFromFavorites(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && upd.message.text == "Удалить словарь из списка изучаемых"
    }
}