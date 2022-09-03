//класс исключений при отсутствии чата
class ChatsNotFoundException(message: String): RuntimeException(message)

//класс чата двух различных пользователей, хешкод объекта которого будет ключем в Мапе
data class Chats(
    val idFirstUser: Long,
    val idSecondUser: Long
) {
    //переопределяем хешкод
    override fun hashCode(): Int {
        if (idFirstUser < idSecondUser) return idFirstUser.hashCode()*idSecondUser.hashCode()*(idFirstUser.hashCode() + 111) else return idSecondUser.hashCode()*idFirstUser.hashCode()*(idSecondUser.hashCode() + 111)
    }
    //переопределяем функцию сравнения. Т.е. главное, чтобы id пользователей были одинаковые, независимо от порядка указания
    override fun equals(other: Any?): Boolean {
        if (other is Chats) return ((other.idFirstUser === this.idFirstUser && other.idSecondUser === this.idSecondUser) || (other.idFirstUser === this.idSecondUser && other.idSecondUser === this.idFirstUser))
        return false

    }
}
//ддата класс сообщений
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

    fun getChats(id: Long): Map<Long, List<Message>>? {
        //фильтруем список сообщений, где требуемый id указан в качестве автора или адресата
        val list = messageList?.filter { it.fromId == id || it.toId == id }
        //выкидываем исключение, если нет сообщений
        if (list?.size == 0) throw ChatsNotFoundException("Нет такого чата")
        //группируем список, где ключ - хешкод id пользователей
        val listFilered = list?.groupBy{Chats(it.fromId, it.toId).hashCode().toLong()}
        println("Список чатов пользователя $id")
        return listFilered

    }

    fun getMessages(idUser: Long): Int {
        if (unreadMessages.containsKey(idUser.toInt())) unreadMessages.remove(idUser.toInt())
        return 1
    }
}
fun main() {
    ChatService.sendMsg(1,2,"Привет")
    ChatService.sendMsg(2,4,"Привет")
    ChatService.sendMsg(1,3,"Привет")
    ChatService.sendMsg(4,2,"Привет")
    ChatService.sendMsg(1,2,"Привет2")
    ChatService.sendMsg(2,1,"Привет3")
    //ChatService.showChat(1,2)

    /*val list = ChatService.messageList?.filter { it.id == 1L }

    //println(ChatService.messageList)
    println(ChatService.chatList)
    println(list)
    println(ChatService.showChat(1,2))
    */
    println(ChatService.messageList)

    ChatService.showChat(4,2)
    println(ChatService.unreadMessages)
    println(ChatService.getUnreadChatsCount(3))
    //ChatService.deleteChat(1,2)
    println(ChatService.chatList)
    println(ChatService.messageList)
    println(ChatService.getChats(3))
    ChatService.getMessages(2)
    println(ChatService.getUnreadChatsCount(2))



}