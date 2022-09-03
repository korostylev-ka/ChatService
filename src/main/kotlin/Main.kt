//класс исключений при отсутствии чата
class ChatsNotFoundException(message: String): RuntimeException(message)
//класс исключений при отсутствии пользователя
class UserNotFoundException(message: String): RuntimeException(message)

//класс чата двух различных пользователей, хешкод объекта которого будет ключем в Мапе
data class Chats(
    val idFirstUser: Long,
    val idSecondUser: Long
) {
    //переопределяем хешкод(чтоб был одинаков для двух одинаковых пользователей, независимо от порядка их указания)
    override fun hashCode(): Int {
        if (idFirstUser < idSecondUser) return idFirstUser.hashCode()*idSecondUser.hashCode()*(idFirstUser.hashCode() + 111)
            else return idSecondUser.hashCode()*idFirstUser.hashCode()*(idSecondUser.hashCode() + 111)
    }
    //переопределяем функцию сравнения. Т.е. главное, чтобы id пользователей были одинаковые, независимо от порядка указания
    override fun equals(other: Any?): Boolean {
        if (other is Chats) return ((other.idFirstUser === this.idFirstUser && other.idSecondUser === this.idSecondUser) ||
                (other.idFirstUser === this.idSecondUser && other.idSecondUser === this.idFirstUser))
        return false
    }
}

//дата класс сообщений
data class Message(
    val fromId: Long, //id автора
    val toId: Long, //id адресата
    val text: String,  //текст сообщения
    var id: Long = 0 //уникальный id сообщения
)

object ChatService{
    //оригинальный id для каждого сообщения
    var originalMsgId: Long = 0
    //общий лист сообщений
    var messageList: MutableList<Message> = mutableListOf<Message>()
    //Мапа чатов, ключ это хешкод чата двух пользователей
    var chatList: MutableMap<Int, MutableList<Message>?> = mutableMapOf<Int, MutableList<Message>?>()
    //Мапа непрочитанных сообщений
    var unreadMessages: MutableMap<Int, MutableList<Int>?> = mutableMapOf<Int, MutableList<Int>?>()
    //лямбда для проверки существования автора
    val isAuthorExist = {id: Long, list: List<Message>? ->
        val listFilter = list?.filter{it.fromId == id}
        if (listFilter?.size == 0) false else true
    }
    //лямбда для проверки существования адресата
    val isSenderExist = {id: Long, list: List<Message>? ->
        val listFilter = list?.filter{it.toId == id}
        if (listFilter?.size == 0) false else true
    }
    //лямюда для проверки существования id в списке адресатов или авторов
    val isUserExist = {id: Long, list: List<Message>? ->
        if ((isSenderExist(id, list) == true || isAuthorExist(id, list) == true)) true else false
    }

    //функция отправки сообщения
    fun sendMsg(
        fromId: Long,
        toId: Long,
        text: String
    ): Long {
        //создаем сообщение
        val message: Message = Message(fromId, toId,text, id = originalMsgId )
        //создаем объект чата для проверки хешкода
        val chat = Chats(fromId, toId)
        //создаем список сообщений
        val listMessages: MutableList<Message>?
        //оператор Элвиса для проверки есть ли уже данный чат
        listMessages = chatList.get(chat.hashCode()) ?: mutableListOf<Message>()
        listMessages?.add(message)
        //добавляем сообщение в общий лист сообщений
        messageList.add(message)
        originalMsgId++
        chatList.put(chat.hashCode(),listMessages)
        //добавляем в мапу непрочитанных сообщений
        val listUnread: MutableList<Int>?
        listUnread = unreadMessages.get(toId.toInt()) ?: mutableListOf<Int>()
        listUnread?.add(fromId.toInt())
        unreadMessages.put(toId.toInt(),listUnread)
    return originalMsgId
    }

    //функция показа чата между конкретными пользователями
    fun showChat(
        idFirst: Long, //id первого пользователя
        idSecond: Long // id второго пользователя
    ): List<Message>? {
        if (isUserExist(idFirst, messageList) == false) throw UserNotFoundException("Такого пользователя нет")
        if (isUserExist(idSecond, messageList) == false) throw UserNotFoundException("Такого пользователя нет")
        println("Chat user $idFirst with user $idSecond: ")
        //фильтруем список сообщений по id пользователей
        val list = messageList?.filter { (it.fromId == idFirst || it.fromId == idSecond) && (it.toId == idFirst || it.toId == idSecond)}
        for (i in 0..list!!.size - 1) {
            println("User ${list.get(i).fromId} write:")
            println(list.get(i).text)
        }
        return list
    }
    //выводим количество непрочитанных чатов у пользователя
    fun getUnreadChatsCount(id: Long): Int {
        println("У пользователя $id непрочитанных чатов:")
        //проверяем есть ли в мапе непрочитанных сообщений элементы с ключем id пользователя
        if (unreadMessages.get(id.toInt()) != null) {
            return unreadMessages.get(id.toInt())!!.size
        } else return 0
    }

    //удаление чата
    fun deleteChat(idFirstUser: Long, idSecondUser: Long): Int {
        val chat = Chats(idFirstUser,idSecondUser)
        if (chatList.containsKey(chat.hashCode())) {
            chatList.remove(chat.hashCode())
            //фильтруем список сообщений без удаляемых id
            var list = messageList.filter { (it.fromId != idFirstUser && it.fromId != idSecondUser) || (it.toId != idFirstUser && it.toId != idSecondUser)}
            messageList = list as MutableList<Message>
            return 0
        } else throw ChatsNotFoundException("Нет такого чата")
    }

    //получаем список пользователей, с которыми пользователь общается
    fun getChats(id: Long): Map<Long, List<Message>>? {
        //проверяем наличие пользователя
        if (isUserExist(id, messageList) == false) throw UserNotFoundException("Такого пользователя нет")
        //фильтруем список сообщений, где требуемый id указан в качестве автора или адресата
        val list = messageList?.filter { it.fromId == id || it.toId == id }
        //выкидываем исключение, если нет сообщений
        if (list?.size == 0) throw ChatsNotFoundException("Нет такого чата")
        //группируем список, где ключ - хешкод id пользователей
        val listFilered = list?.groupBy{Chats(it.fromId, it.toId).hashCode().toLong()}
        println("Список чатов пользователя $id")
        return listFilered

    }

    //получаем список сообщений из чата
    fun getMessages(idFirstUser: Long, idSecondUser: Long): Int {
        //проверяем наличие пользователей
        if (isUserExist(idFirstUser, messageList) == false) throw UserNotFoundException("Такого пользователя нет")
        if (isUserExist(idSecondUser, messageList) == false) throw UserNotFoundException("Такого пользователя нет")
        //лямбда для удаления элемента коллекции
        val lambdaDelete = {id: Long, map: MutableMap<Int, MutableList<Int>?>  ->
            if (map.containsKey(id.toInt())) map.remove(id.toInt())
        }
        lambdaDelete(idFirstUser, unreadMessages)
        lambdaDelete(idSecondUser, unreadMessages)
        val chat = Chats(idFirstUser, idSecondUser)
        //Список сообщений пользователей
        val list = chatList.get(chat.hashCode())
        //находим последнее сообщение(с наибольшим id)
        val maxId = list?.maxBy(Message::id)?.id
        println("у пользователя $idFirstUser и пользователя $idSecondUser id чата: ${chat.hashCode()}")
        println("у пользователей ${list?.size} сообщений")
        println("у последнего сообщения id: $maxId")
        return 0
    }

    //очистка данных для автотестов
    fun eraseAll(): Unit{
        originalMsgId = 0
        chatList.clear()
        messageList.clear()
        unreadMessages.clear()
    }
}
fun main() {
    println("Домашнее задание к занятию «3.3. Лямбды, extension-функции, операторы»")

}