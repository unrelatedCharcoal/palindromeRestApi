package logicFields

import model.Message

fun palindrome(message: Message): String {
    return (message.text.reversed() == message.text).toString()
}