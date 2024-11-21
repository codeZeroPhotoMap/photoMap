package com.codeZero.photoMap.api;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.codeZero.photoMap.common.exception.KakaoApiException;
import com.codeZero.photoMap.dto.member.response.KakaoUserInfoResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

@Component
public class KakaoLoginAPI {

    //사용자 정보를 가져오는 메서드
    public KakaoUserInfoResponse getUserInfo(String accessToken) {

        String reqURL = "https://kapi.kakao.com/v2/user/me";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String result = br.lines().reduce("", String::concat);

                    JsonObject element = JsonParser.parseString(result).getAsJsonObject();
                    JsonObject properties = element.has("properties") ? element.getAsJsonObject("properties") : null;
                    JsonObject kakaoAccount = element.has("kakao_account") ? element.getAsJsonObject("kakao_account") : null;

                    if (properties == null || kakaoAccount == null) {
                        throw new KakaoApiException("필수 사용자 정보가 응답에 포함되지 않았습니다.");
                    }

                    //nickname과 email 확인 및 예외 처리
                    String nickname = properties.get("nickname").getAsString();
                    String email = kakaoAccount.get("email").getAsString();

                    if (nickname == null || email == null) {
                        throw new KakaoApiException("필수 사용자 정보(nickname 또는 email)가 누락되었습니다.");
                    }

                    //KakaoUserInfoResponse 객체 생성 후 반환
                    return KakaoUserInfoResponse.builder()
                            .email(email)
                            .nickname(nickname)
                            .build();

                }
            }else {
                throw new KakaoApiException("카카오 사용자 정보 요청에 실패했습니다.");
            }

        } catch (IOException e) {
            throw new KakaoApiException("사용자 정보 요청 중 오류 발생");
        }
    }
}
