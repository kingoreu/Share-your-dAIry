# Share-your-Dairy
AI융합 인지중재 실버케어 솔루션 개발자 교육과정 중 제작한 Java Project입니다. 
mainscreen 구현은 했는데 아직 장애가 좀 있다 사진 위치 수정 필요 화면 전환 할때 이미지 안보이게 하거나 창을 전환


----------
# DB 연결 (mysql 8.0.33 사용) 08.11
1. 스키마 이름 : dairy
2. port 번호는 각자 mysql 할당된 포트로 설정
3. root, password도 각자 설정한 것으로 properties 변경하여 연결
4. pom.xml에 의존성 추가
```
<dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.0.33</version>
</dependency>
```
5. 프로젝트와 데이터 저장소 연결 후, /src/sql 내부에 있는 **dairy.sql, dairy_trigger.sql문 Mysql Workbench에서 실행**
6. DBTest 파일을 통해 연결 확인

-----------
# Server 연결 (Spring 3.3.2 사용) 08.12
1. sohyun 브랜치 코드를 그대로 가져와서 연결해도 가능
2. 실행 시 루트 디렉토리에서 mvn clean javafx:run
3. maven reload도 해주면 좋음
4. pom.xml은 이미 추가되어 있지만 하단에 작성함. (parent, dependency, plugin)
5. **module-info.java 파일을 삭제**해야 스프링까지 연결될 거임 아마도.. (저는 삭제했어요)
```
<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.2</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
// parent는 properties보다 상단에 위치해야 함.
...
        <!-- Spring Boot Web (REST API) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot JDBC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <!-- JSON 처리 (Jackson) - spring-boot-starter-web에도 포함되어 있음 -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Lombok (디버깅) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
...
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
```


