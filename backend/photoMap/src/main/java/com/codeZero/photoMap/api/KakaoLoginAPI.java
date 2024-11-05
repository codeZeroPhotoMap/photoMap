package com.codeZero.photoMap.api;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.codeZero.photoMap.common.exception.KakaoApiException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

@Component
public class KakaoLoginAPI {

    //Access Token을 가져오는 메서드
    public String getAccessToken(String code) {
        String accessToken = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                StringBuilder sb = new StringBuilder();
                sb.append("grant_type=authorization_code");
                sb.append("&client_id=575409f65ea9a7c089dbf8eed93a6c98");
                sb.append("&redirect_uri=http://localhost:8080/api/members/login/kakao");
                sb.append("&code=").append(code);

                bw.write(sb.toString());
                bw.flush();
            }

            //응답 코드 확인
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String result = br.lines().reduce("", String::concat);
                    JsonObject element = JsonParser.parseString(result).getAsJsonObject();

                    // Access Token 추출
                    accessToken = element.get("access_token").getAsString();

                    //TODO : test용, 삭제예정
                    System.out.println("Access Token: " + accessToken);
                }

            }else {
                //TODO : test용, 삭제예정
                System.out.println("카카오 access_token 발급 실패, responseCode : " + responseCode);
                throw new KakaoApiException("카카오 로그인 중 토큰 발급에 실패했습니다.");
            }

        } catch (IOException e) {
            throw new KakaoApiException("카카오 로그인 중 오류 발생");
        }

        return accessToken;

    }

    //사용자 정보를 가져오는 메서드
    public HashMap<String, Object> getUserInfo(String accessToken) {

        HashMap<String, Object> userInfo = new HashMap<>();
        String reqURL = "https://kapi.kakao.com/v2/user/me";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            //TODO : test용, 삭제예정
            System.out.println("Kakao user info response code: " + responseCode);   //

            if (responseCode == HttpURLConnection.HTTP_OK) {

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String result = br.lines().reduce("", String::concat);

                    JsonObject element = JsonParser.parseString(result).getAsJsonObject();
                    JsonObject properties = element.has("properties") ? element.getAsJsonObject("properties") : null;
                    JsonObject kakaoAccount = element.has("kakao_account") ? element.getAsJsonObject("kakao_account") : null;

                    if (properties == null || kakaoAccount == null) {
                        throw new KakaoApiException("필수 사용자 정보가 응답에 포함되지 않았습니다.");
                    }

                    // nickname과 email 확인 및 예외 처리
                    String nickname = properties.has("nickname") ? properties.get("nickname").getAsString() : null;
                    String email = kakaoAccount.has("email") && !kakaoAccount.get("email").isJsonNull()
                            ? kakaoAccount.get("email").getAsString()
                            : null;

                    if (nickname == null || email == null) {
                        throw new KakaoApiException("필수 사용자 정보(nickname 또는 email)가 누락되었습니다.");
                    }

                    //TODO : test용, 삭제예정
                    System.out.println("Received email: " + email);
                    System.out.println("Received nickname: " + nickname);

                    userInfo.put("nickname", nickname);
                    userInfo.put("email", email);
                }
            }else {
                throw new KakaoApiException("카카오 사용자 정보 요청에 실패했습니다.");
            }

        } catch (IOException e) {
            throw new KakaoApiException("사용자 정보 요청 중 오류 발생");
        }

        return userInfo;

    }
}
