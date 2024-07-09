#!/usr/bin/env bash

REPOSITORY="/home/ubuntu/app"
CONSOLE_LOG="$REPOSITORY/console.log"
ERROR_LOG="$REPOSITORY/error.log"
DEPLOY_LOG="$REPOSITORY/deploy.log"

echo "> 현재 구동 중인 애플리케이션 pid 확인" >> $DEPLOY_LOG

CURRENT_PID=$(pgrep -fla java | grep BangWool | awk '{print $1}')

echo "현재 구동 중인 애플리케이션 pid: $CURRENT_PID" >> $DEPLOY_LOG

if [ -z "$CURRENT_PID" ]; then
  echo "현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다." >> $DEPLOY_LOG
else
  echo "> kill -9 $CURRENT_PID" >> $DEPLOY_LOG
  kill -9 $CURRENT_PID
fi

echo "> 새 애플리케이션 배포" >> $DEPLOY_LOG

JAR_NAME=$(ls -tr $REPOSITORY/*SNAPSHOT.jar | tail -n 1)

echo "> JAR NAME: $JAR_NAME" >> $DEPLOY_LOG

echo "> $JAR_NAME 에 실행권한 추가" >> $DEPLOY_LOG

chmod +x $JAR_NAME

echo "> $JAR_NAME 실행" >> $DEPLOY_LOG

nohup java -jar $JAR_NAME > $CONSOLE_LOG 2> $ERROR_LOG &