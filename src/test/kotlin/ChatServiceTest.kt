import org.junit.Test

import org.junit.Assert.*

class ChatServiceTest {

    //автотест отправки сообщения от одного пользователя другому
    @Test
    fun sendMsg() {
        ChatService.eraseAll()
        val id = ChatService.sendMsg(1,2,"Text")
        assertEquals(1,id)
        val id2 = ChatService.sendMsg(1,2,"Text")
        assertEquals(2,id2)
    }

    //автотест показа чата
    @Test
    fun showChat() {
        ChatService.eraseAll()
        ChatService.sendMsg(1,2,"Text")
        ChatService.sendMsg(2,1,"Text")
        val list = listOf(Message(1,2,"Text"),Message(2,1,"Text",1))
        val listExpect = ChatService.showChat(1,2)
        assertEquals(list,listExpect)
    }

    //автотест показа чата(обработка исключений)
    @Test(expected = UserNotFoundException:: class)
    fun showChatException() {
        ChatService.eraseAll()
        ChatService.sendMsg(1,2,"Text")
        ChatService.sendMsg(2,1,"Text")
        ChatService.showChat(3,4)
    }

    //автотест удаления чата
    @Test
    fun deleteChat() {
        ChatService.eraseAll()
        ChatService.sendMsg(1,2,"Text")
        ChatService.sendMsg(2,1,"Text")
        val deleteResult = ChatService.deleteChat(1,2)
        assertEquals(0,deleteResult)
    }

    //автотест удаления чата(обработка исключений)
    @Test(expected = ChatsNotFoundException:: class)
    fun deleteChatException() {
        ChatService.eraseAll()
        ChatService.sendMsg(1,2,"Text")
        ChatService.sendMsg(2,1,"Text")
        val deleteResult = ChatService.deleteChat(2,3)

    }

    //автотест получения списка сообщений
    @Test
    fun getMessages() {
        ChatService.eraseAll()
        ChatService.sendMsg(1,2,"Text")
        ChatService.sendMsg(2,1,"Text")
        val getChat = ChatService.getMessages(1,2)
        assertEquals(0,getChat)
    }

    //автотест получения списка сообщений(обработка исключений)
    @Test(expected = UserNotFoundException:: class)
    fun getMessagesException() {
        ChatService.eraseAll()
        ChatService.sendMsg(1,2,"Text")
        ChatService.sendMsg(2,1,"Text")
        val getChat = ChatService.getMessages(1,22)
    }


}