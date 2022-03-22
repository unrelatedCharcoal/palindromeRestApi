import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class BasicMessageLogicTests {

    @Test
    fun `Create a message`() = withTestApplication(Application::module) {
        val messageText = "Pomegranate"
        withCreateMessage(messageText) { response, message ->
            assertEquals(HttpStatusCode.Created, response.status())
            assertTrue(response.content?.startsWith("Message created") ?: false)
            assertNotNull(message)
            assertNotNull(message.text)
            assertEquals(messageText, message.text)
            assertEquals(message.dateEdited, message.datePosted)
        }
    }

    @Test
    fun `Retrieve existing message`() = withTestApplication(Application::module) {
        val messageText = "Tomato"
        var id: Int? = null
        withCreateMessage(messageText) { response, message ->
            assertEquals(HttpStatusCode.Created, response.status())
            id = message.id
        }
        id ?: return@withTestApplication
        withGetMessage(id!!) { response, message ->
            assertEquals(HttpStatusCode.Found, response.status())
            message ?: return@withGetMessage
            assertEquals(messageText, message.text)
        }
    }

    @Test
    fun `Get empty list of messages`() = withTestApplication(Application::module) {
        withGetAllMessages { response, messages ->
            assertEquals(HttpStatusCode.NotFound, response.status())
            assertTrue(messages.isEmpty())
        }
    }

    @Test
    fun `Get list of messages that isn't empty`() = withTestApplication(Application::module) {
        val messageText = "Apple"
        withCreateMessage(messageText) { response, message ->
            assertEquals(HttpStatusCode.Created, response.status())
        }
        withGetAllMessages { response, messages ->
            assertEquals(HttpStatusCode.Found, response.status())
            assertTrue(messages.isNotEmpty())
        }
    }


    @Test
    fun `Update`() = withTestApplication(Application::module) {
        val messageText = "Carrot"
        var messageId: Int? = null
        var updatedMessage: Message? = null
        withCreateMessage(messageText) { response, message ->
            assertEquals(HttpStatusCode.Created, response.status())
            messageId = message.id
        }
        messageId ?: return@withTestApplication

        withUpdateMessage(messageId!!, "Golden Carrot") { response, message ->
            assertEquals(HttpStatusCode.OK, response.status())
            updatedMessage = message
        }

        withGetMessage(messageId!!) { response, message ->
            assertEquals(HttpStatusCode.Found, response.status())
            assertEquals(messageId, updatedMessage!!.id)
            //verify that lastUpdated is indeed changed
        }
    }

    @Test
    fun `Delete`() = withTestApplication(Application::module) {
        var messageId: Int? = null
        val messageText = "Carrot"

        withCreateMessage(messageText) { response, message ->
            assertEquals(HttpStatusCode.Created, response.status())
            messageId = message.id
        }
        messageId ?: return@withTestApplication
        withGetMessage(messageId!!) { response, message ->
            assertEquals(HttpStatusCode.Found, response.status())
            assertEquals(messageId, message?.id)
        }

        withDeleteMessage(messageId!!) {
            assertEquals(HttpStatusCode.NoContent, it.status())
        }

        withGetMessage(messageId!!) { response, message ->
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `Not update none existing message`() = withTestApplication {
        withUpdateMessage(-1, "") { response, message ->
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `Not delete none existing message`() = withTestApplication {
        withDeleteMessage(-1) {
            assertEquals(HttpStatusCode.NotFound, it.status())
        }
    }

}

