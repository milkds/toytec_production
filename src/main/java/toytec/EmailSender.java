package toytec;

import org.apache.commons.mail.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EmailSender {

    public static void sendMail(List<File> files, Statistics statistics){
        MultiPartEmail  email = new MultiPartEmail ();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(587);
        email.setAuthenticator(new DefaultAuthenticator("servergrisha@gmail.com", "ServerGrisha18"));
        email.setSSLOnConnect(true);

        String subject = getSubject(statistics);
        try {
            for (EmailAttachment attachment: getAttachments(files)){
                email.attach(attachment);
            }
            email.setFrom("servergrisha@gmail.com");
            email.setSubject(subject);
            email.setMsg(statistics.getStatisticsKeeper().toString());
            email.addTo("evp@artpolymer.com");
            email.send();
        } catch (EmailException e) {
            e.printStackTrace();
        }

    }

    private static String getSubject(Statistics statistics) {
        StringBuilder sb = new StringBuilder();
        sb.append("ToyTec ");
        sb.append(statistics.getTotalItemsQuantityBeforeCheck());
        sb.append("/");
        sb.append(statistics.getTotalItemsQuantityAfterCheck());
        sb.append(" itemsBeforeCheck/afterCheck parse results ");
        sb.append(Statistics.formatTime(statistics.getFinish()));

        return sb.toString();
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
