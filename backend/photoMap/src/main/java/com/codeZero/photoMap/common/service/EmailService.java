package com.codeZero.photoMap.common.service;

import com.codeZero.photoMap.common.exception.EmailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 초대 이메일 전송 메소드
     * @param toEmail 초대받는 멤버 email
     * @param acceptLink 초대 수락 링크
     * @param inviterName 초대하는 멤버(그룹장)의 이름
     * @param groupName 초대하는 그룹의 이름
     * @param expiryDate 초대 토큰의 만료 시간
     */
    public void sendInvitationEmail(String toEmail, String acceptLink, String inviterName, String groupName, LocalDateTime expiryDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("\"" + inviterName + "\"님이 \"" + groupName + "\"에 초대했습니다.");
            message.setText(
                    "\n안녕하세요, \"" + inviterName + "\"님이 \"" + groupName + "\"에 초대했습니다.\n" +
                    "아래 링크를 클릭하여 그룹 초대를 수락하세요:\n" +
                            "\n" +
                    "초대 수락 하기: " + acceptLink + "\n" +
                    "초대는 " + expiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "까지 유효합니다.\n");

//            + "\n" +
//                    "거절 링크: " + declineLink);
            mailSender.send(message);

        }catch (Exception e){

            throw new EmailSendException("이메일 전송 중 오류가 발생했습니다.", e);

        }

    }
}

