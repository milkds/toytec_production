package toytec;

import org.apache.commons.mail.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EmailSender {

    public static void sendMail(List<File> files){
        MultiPartEmail  email = new MultiPartEmail ();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(587);
        email.setAuthenticator(new DefaultAuthenticator("servergrisha@gmail.com", "ServerGrisha18"));
        email.setSSLOnConnect(true);
        try {
            for (EmailAttachment attachment: getAttachments(files)){
                email.attach(attachment);
            }
            email.setFrom("servergrisha@gmail.com");
            email.setSubject("TestMail");
            email.setMsg("This is a test mail ... :-)");
            email.addTo("evp@artpolymer.com");
            email.addTo("dmitriy.orders@gmail.com");
            email.send();
        } catch (EmailException e) {
            e.printStackTrace();
        }

    }

    private static List<EmailAttachment> getAttachments(List <File> files){
        List<EmailAttachment> attachments = new ArrayList<>();
        for (File file : files){
            EmailAttachment attachment = new EmailAttachment();
            attachment.setPath(file.getPath());
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            attachment.setName(file.getName());
            attachments.add(attachment);
        }

        return attachments;
    }

}
