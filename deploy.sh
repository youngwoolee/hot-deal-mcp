#!/bin/bash

# Hot Deal MCP Server 배포 스크립트
# 사용법: ./deploy.sh <서버IP> <SSH키경로>

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 파라미터 체크
if [ $# -lt 2 ]; then
    echo -e "${RED}사용법: ./deploy.sh <서버IP> <SSH키경로>${NC}"
    echo "예시: ./deploy.sh 123.456.78.90 ~/Downloads/ssh-key.key"
    exit 1
fi

SERVER_IP=$1
SSH_KEY=$2
SSH_USER="ubuntu"

echo -e "${GREEN}=== Hot Deal MCP Server 배포 시작 ===${NC}"
echo "서버 IP: $SERVER_IP"
echo "SSH 키: $SSH_KEY"
echo ""

# SSH 키 권한 확인
chmod 400 "$SSH_KEY"

# 1. 빌드
echo -e "${YELLOW}[1/4] 애플리케이션 빌드 중...${NC}"
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo $JAVA_HOME)
./gradlew clean build -x test

if [ ! -f "build/libs/hot-deal-mcp-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${RED}빌드 실패!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 빌드 완료${NC}"
echo ""

# 2. JAR 파일 전송
echo -e "${YELLOW}[2/4] JAR 파일 서버로 전송 중...${NC}"
scp -i "$SSH_KEY" -o StrictHostKeyChecking=no \
    build/libs/hot-deal-mcp-0.0.1-SNAPSHOT.jar \
    ${SSH_USER}@${SERVER_IP}:~/app.jar.new

echo -e "${GREEN}✓ 파일 전송 완료${NC}"
echo ""

# 3. 서버에서 배포
echo -e "${YELLOW}[3/4] 서버에 배포 중...${NC}"
ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no ${SSH_USER}@${SERVER_IP} << 'ENDSSH'
    # 디렉토리 생성
    mkdir -p ~/hot-deal-mcp

    # 기존 서비스 중지
    if systemctl is-active --quiet hot-deal-mcp; then
        echo "기존 서비스 중지..."
        sudo systemctl stop hot-deal-mcp
    fi

    # 백업
    if [ -f ~/hot-deal-mcp/app.jar ]; then
        echo "기존 JAR 백업..."
        cp ~/hot-deal-mcp/app.jar ~/hot-deal-mcp/app.jar.backup
    fi

    # 새 JAR 이동
    mv ~/app.jar.new ~/hot-deal-mcp/app.jar

    echo "✓ 배포 완료"
ENDSSH

echo -e "${GREEN}✓ 배포 완료${NC}"
echo ""

# 4. 서비스 재시작
echo -e "${YELLOW}[4/4] 서비스 재시작 중...${NC}"
ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no ${SSH_USER}@${SERVER_IP} << 'ENDSSH'
    if systemctl list-unit-files | grep -q hot-deal-mcp.service; then
        sudo systemctl start hot-deal-mcp
        echo "서비스 시작됨"
        sleep 3
        sudo systemctl status hot-deal-mcp --no-pager
    else
        echo "⚠ systemd 서비스가 설정되지 않았습니다."
        echo "수동 실행: cd ~/hot-deal-mcp && java -jar app.jar"
    fi
ENDSSH

echo ""
echo -e "${GREEN}=== 배포 완료! ===${NC}"
echo ""
echo "서버 확인:"
echo "  - Health Check: http://${SERVER_IP}:8080/actuator/health"
echo "  - Test API: http://${SERVER_IP}:8080/test/hot-deals"
echo ""
echo "로그 확인:"
echo "  ssh -i ${SSH_KEY} ${SSH_USER}@${SERVER_IP} 'sudo journalctl -u hot-deal-mcp -f'"
