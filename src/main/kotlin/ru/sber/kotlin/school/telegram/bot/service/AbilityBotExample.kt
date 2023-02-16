package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.service.menu.AdditionAndDeleterMenusToFavoriteDictionariesService
import ru.sber.kotlin.school.telegram.bot.service.menu.DictionaryMenuService


@Component
class AbilityBotExample(
    @Value("\${telegram.bot.token}")
    private val token: String,
    @Value("\${telegram.bot.name}")
    private val name: String,
    val dictionaryMenuService: DictionaryMenuService,
    val additionAndDeleterMenusToFavoriteDictionariesService: AdditionAndDeleterMenusToFavoriteDictionariesService
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
        .info("Получаем меню в зоне текста и список словарей на изучении")
        .locality(Locality.USER)
        .privacy(Privacy.PUBLIC)
        .action { ctx ->
            val msg = dictionaryMenuService.getDictMenuAndInfoFavDict(ctx)
            sender.execute(msg)
        }
        .build()

    /**
     * Команда inlineQuery {allDictionaries} отправляет пользователю развернутый список всех словарей (при нажатии Добавить из готовых)
     */

    fun inlineReplyListDictionaries(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            //здесь получение списка всех словарей
            val partOfMessageText = "добавлен в словари"
            val answer = additionAndDeleterMenusToFavoriteDictionariesService.getMenuAllDictionaries(upd, partOfMessageText)
            sender.execute(answer)
        }
        return Reply.of(action, isInlineQueryAllDictionaries())
    }

    private fun isInlineQueryAllDictionaries(): (Update) -> Boolean = { upd: Update ->
        upd.hasInlineQuery() && upd.inlineQuery.query == "allDictionaries"
    }

    //ловим ответ пользователя по словарю, который надо добавить на изучение
    fun replyMessageAdditionDictionaryToFavorites(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            //здесь добавление словаря в словари на изучении
            additionAndDeleterMenusToFavoriteDictionariesService.addDictionaryToFavorites(upd)
        }
        return Reply.of(action, isAdditionDictionaryToFavorites())
    }

    private fun isAdditionDictionaryToFavorites(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && upd.message.text.endsWith("добавлен в словари для изучения")
    }

    /**
     * Команда inlineQuery {allFavDictionaries} отправляет пользователю развернутый список всех словарей (при нажатии Удалить словарь из списка изучаемых)
     */
    fun inlineReplyListFavDictionaries(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            //здесь получение списка всех словарей
            val partOfMessageText = "удален из словарей"
            val answer = additionAndDeleterMenusToFavoriteDictionariesService.getMenuAllDictionaries(upd, partOfMessageText)
            sender.execute(answer)
        }
        return Reply.of(action, isInlineQueryAllFavDictionaries())
    }

    private fun isInlineQueryAllFavDictionaries(): (Update) -> Boolean = { upd: Update ->
        upd.hasInlineQuery() && upd.inlineQuery.query == "allFavDictionaries"
    }

    //ловим ответ пользователя по словарю, который надо удалить из изучения
    fun replyMessageDeleterDictionaryFromFavorites(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            //здесь удаление словаря из словарей на изучении
            additionAndDeleterMenusToFavoriteDictionariesService.deleteDictionaryFromFavorites(upd)
        }
        return Reply.of(action, isDeleterDictionaryFromFavorites())
    }

    private fun isDeleterDictionaryFromFavorites(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && upd.message.text.endsWith("удален из словарей для изучения")
    }

    /*override fun onUpdateReceived(update: Update?) {
        super.onUpdateReceived(update)
        if(update?.callbackQuery?.data == "temporary"){
            val chatId = update.callbackQuery.message.chat.id.toString()
            sender.execute(SendMessage(chatId,
                "Временно, далее здесь методы перехода в меню или указания на создание темы нового словаря " +
                        "(можно ввод пользователем темы сделать через inlinequery как в примере с fails - " +
                        "вываливается fails (переименованный типо после двоеточия введите тему словаря: ) " +
                        "и пользователь вводит тему и и направляет весь этот кусок в чат)"))
        }
    }*/

    fun inlineReplyTemporary(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            //временная затычка
            sender.execute(SendMessage(
                getChatId(upd).toString(),
                "Временно, далее здесь методы перехода в меню или указания на создание темы нового словаря " +
                        "(можно ввод пользователем темы сделать через inlinequery как в примере с fails - " +
                        "вываливается fails (переименованный типо после двоеточия введите тему словаря: ) " +
                        "и пользователь вводит тему и и направляет весь этот кусок в чат)"))
        }
        return Reply.of(action, isInlineQueryTemporary())
    }

    private fun isInlineQueryTemporary(): (Update) -> Boolean = { upd: Update ->
        upd.hasCallbackQuery() && upd.callbackQuery.data == "temporary"
    }

}
