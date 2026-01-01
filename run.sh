#!/bin/bash

# Hot Deal MCP Server 실행 스크립트

echo "🔥 Hot Deal MCP Server 시작 중..."

# Java 21 설정
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)

if [ -z "$JAVA_HOME" ]; then
    echo "❌ Java 21이 설치되어 있지 않습니다."
    echo "Java 21을 설치해주세요: https://adoptium.net/"
    exit 1
fi

echo "✅ Java 버전: $($JAVA_HOME/bin/java -version 2>&1 | head -n 1)"

# JAR 파일이 없으면 빌드
if [ ! -f "build/libs/hot-deal-mcp-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 애플리케이션 빌드 중..."
    ./gradlew clean build -x test
    if [ $? -ne 0 ]; then
        echo "❌ 빌드 실패"
        exit 1
    fi
fi

# 서버 실행
echo "🚀 서버 실행 중... (포트: 8080)"
echo "🌐 접속 URL: http://localhost:8080"
echo "❤️  Health Check: http://localhost:8080/actuator/health"
echo ""
echo "종료하려면 Ctrl+C를 누르세요."
echo ""

$JAVA_HOME/bin/java -jar build/libs/hot-deal-mcp-0.0.1-SNAPSHOT.jar
