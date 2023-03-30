import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage

fun main(args: Array<String>) {
    val fromEmail = args[0]
    val password = args[1]
    val toEmail = args[2]

    val props = Properties()
    props["mail.smtp.host"] = "smtp.gmail.com"
    props["mail.smtp.port"] = "25"
    props["mail.smtp.auth"] = "true"
    props["mail.smtp.starttls.enable"] = "true"

    val session = Session.getDefaultInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(fromEmail, password)
        }
    })

    try {
        val mimeMessage = MimeMessage(session)
        mimeMessage.setFrom(fromEmail)
        mimeMessage.setRecipients(Message.RecipientType.TO, toEmail)
        mimeMessage.subject = "test"
        mimeMessage.sentDate = Date()
        mimeMessage.setContent("<div style=\"color:red;\">text</div>", "text/html; charset=utf-8")

        Transport.send(mimeMessage)
    } catch (messagingException: MessagingException) {
        messagingException.printStackTrace()
    }
}