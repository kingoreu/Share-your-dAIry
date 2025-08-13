package com.share.dairy.service.diary_analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import okhttp3.*;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;

public class DiaryAnalysisService {

    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY"/* key 넣기 */);
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_MODEL = "gpt-4o-mini";

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/diary?characterEncoding=UTF-8&serverTimezone=Asia/Seoul";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "1234";

    private static final OkHttpClient HTTP = new OkHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    //  main 메서드로 실행 가능
    // 이 메서드는 테스트용으로, 실제 서비스에서는 스케줄러나 이벤트 리스너에서 호출될 수 있습니다.
    // 예시: java -cp your-jar-file.jar com.share.dairy.service.di
    public static void main(String[] args) throws Exception {
        long entryId = 1L; // 분석할 일기 PK
        new DiaryAnalysisService().process(entryId); // ← 클래스명 수정
        System.out.println("Analyzed entry_id=" + entryId + " at " + Instant.now());
    }

    /** diary_entries.entry_id를 분석해서 diary_analysis에 upsert */
    public void process(long entryId) throws Exception {
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 비어 있습니다.");
        }
        // entry_id는 diary_entries 테이블의 PK로, diary_analysis 테이블의 FK
        String content = getDiaryContent(entryId);
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("일기 내용이 없습니다: entry_id=" + entryId);
        }
            // 일기 내용이 비어있지 않으면 OpenAI API 호출
        AnalysisResult result = callChatGPT(content);
        saveAnalysis(entryId, result);
    }
    
        // 일기 내용 조회
    // entry_id는 diary_entries 테이블의 PK로, diary_analysis 테이블의 FK
    private String getDiaryContent(long entryId) throws SQLException {
        String sql = "SELECT diary_content FROM diary_entries WHERE entry_id = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, entryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
                return null;
            }
        }
    }

    private AnalysisResult callChatGPT(String diaryContent) throws IOException {
        // 시스템 프롬프트: JSON 강제
        String systemPrompt =
                "너는 일기 분석기다. 다음 JSON 형식으로만 응답해.\n" +
                "{ \"analysis_keywords\": string, \"happiness_score\": number, \"summary\": string }\n" +
                "analysis_keywords: 일기 주제를 하나의 키워드로\n" +
                "happiness_score: 1~10 정수 (10 행복, 1 우울).\n" +
                "summary: 3~5줄 요약.";
            // 사용자 프롬프트: 일기 내용
        String userPrompt = "일기 내용:\n" + diaryContent + "\n\nJSON만 반환해.";
            // JSON 객체 생성
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", OPENAI_MODEL);
            // 응답 형식 지정
        ObjectNode respFmt = MAPPER.createObjectNode().put("type", "json_object");
        root.set("response_format", respFmt);
            // 메시지 배열 생성
        ArrayNode messages = MAPPER.createArrayNode();
        messages.add(MAPPER.createObjectNode().put("role", "system").put("content", systemPrompt));
        messages.add(MAPPER.createObjectNode().put("role", "user").put("content", userPrompt));
        root.set("messages", messages);

        String bodyJson = root.toString();

            // OpenAI API 요청 생성
        Request request = new Request.Builder()
                .url(OPENAI_URL)
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(bodyJson, MediaType.get("application/json")))
                .build();
        
                // OpenAI API 호출
        try (Response resp = HTTP.newCall(request).execute()) {
            if (!resp.isSuccessful()) {
                String err = resp.body() != null ? resp.body().string() : "";
                throw new IOException("OpenAI API 오류: " + resp.code() + " - " + err);
            }
            String body = resp.body().string();
            JsonNode apiRoot = MAPPER.readTree(body);
            String content = apiRoot.path("choices").get(0).path("message").path("content").asText();
            JsonNode data = MAPPER.readTree(content);

            String keyword = data.path("analysis_keywords").asText("");
            int score = data.path("happiness_score").asInt(5);
            String summary = data.path("summary").asText("");

            // 점수 보정(1~10)
            if (score < 1) score = 1;
            if (score > 10) score = 10;

            return new AnalysisResult(keyword, score, summary);
        }
    }
        // 데이터베이스에 분석 결과 저장
    private void saveAnalysis(long entryId, AnalysisResult r) throws SQLException {
        String sql = """
            INSERT INTO diary_analysis (entry_id, summary, happiness_score, analysis_keyword, analyzed_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE
              summary = VALUES(summary),
              happiness_score = VALUES(happiness_score),
              analysis_keyword = VALUES(analysis_keyword),
              analyzed_at = CURRENT_TIMESTAMP
            """;
            // entry_id는 diary_entries의 PK로, diary_analysis의 FK
        try (java.sql.Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, entryId);
            ps.setString(2, r.summary);
            ps.setInt(3, r.happinessScore);
            ps.setString(4, r.keyword);
            ps.executeUpdate();
        }
    }
        // 분석 결과 클래스
    static class AnalysisResult {
        final String keyword;
        final int happinessScore;
        final String summary;
        AnalysisResult(String keyword, int happinessScore, String summary) {
            this.keyword = keyword;
            this.happinessScore = happinessScore;
            this.summary = summary;
        }
    }
}
