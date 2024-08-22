# 💵 계좌 관리 시스템 개발 프로젝트

+ 사용자 계좌 관리 및 거래 처리 시스템 구현

# 🛠️ 기술 스택
+ **Language** : JAVA
+ **Framework** : Spring Boot
+ **Database** : H2 (Memory DB)
+ **Build** : Maven
+ **Cache** : Embedded Redis
+ **Persistence** : Spring Data JPA
+ **Testing** : JUnit
+ **JDK** : OpenJDK-17

# 🔴 주요 기능 설명

+ 계좌 생성: 사용자당 최대 10개의 계좌 생성 가능
+ 계좌 해지: 잔액이 0이어야 해지 가능, 해지 시 해지일시 기록
+ 계좌 확인: 사용자 아이디로 해당 사용자의 모든 계좌와 잔액 확인
+ 거래 생성: 계좌 잔액에서 거래 금액만큼 차감 (잔액 부족 시 실패)
+ 거래 취소: 이전 거래의 금액을 취소하여 잔액 복구
+ 거래 확인: 거래 ID로 해당 거래의 상세 정보 조회
