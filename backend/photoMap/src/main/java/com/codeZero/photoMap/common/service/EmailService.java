package com.codeZero.photoMap.common.service;

import com.codeZero.photoMap.common.exception.EmailSendException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String htmlMsg = "<div style=\"text-align: center; padding: 20px; font-family: Arial, sans-serif;\">" +
                    "<p style=\"margin-bottom: 20px; font-size: 18px;\">안녕하세요, <strong>" + inviterName + "</strong>님이 <strong>" + groupName + "</strong>에 초대했습니다.</p>" +
                    "<p style=\"margin-bottom: 30px;\">아래 버튼을 클릭하여 그룹 초대를 수락하세요:</p>" +
                    "<a href=\"" + acceptLink + "\" style=\"display: inline-block; padding: 15px 30px; font-size: 18px; color: white; background-color: #28a745; text-decoration: none; border-radius: 5px; margin-bottom: 30px;\">초대 수락 하기</a>" +
                    "<p style=\"margin-top: 20px; font-size: 14px; color: #555;\">초대는 " + expiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "까지 유효합니다.</p>" +
                    "</div>";

            helper.setTo(toEmail);
            helper.setSubject("\"" + inviterName + "\"님이 \"" + groupName + "\"에 초대했습니다.");
            helper.setText(htmlMsg, true);  //true로 설정해야 HTML 형식으로 보냄

            mailSender.send(message);

        }catch (Exception e){

            throw new EmailSendException("이메일 전송 중 오류가 발생했습니다.", e);

        }

    }
}

