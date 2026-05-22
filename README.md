# BE-A. 수강 신청 시스템

## 프로젝트 개요
수강신청 시스템을 구현한 Spring Boot 기반 백엔드 프로젝트입니다.

강의 개설, 시간표 등록, 수강 신청, 결제 확정, 수강 취소, 대기열 승격, 강의 상태 자동 변경 기능을 포함합니다.
정원 초과 상황과 동시 신청 상황을 고려해 비관적 락과 대기열 로직을 적용했습니다.

## 목차
- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [실행 방법](#실행-방법)
- [요구사항 해석 및 가정](#요구사항-해석-및-가정)
- [설계 결정과 이유](#설계-결정과-이유)
- [AI 활용 범위](#ai-활용-범위)
- [API 목록 및 예시](#api-목록-및-예시)
- [데이터 모델 설명](#데이터-모델-설명)
- [테스트 실행 방법](#테스트-실행-방법)

## 기술 스택
- Java 17
- Spring Boot
- Spring Data JPA
- MySQL 8
- Docker Compose
- JUnit 5
- Mockito

## 실행 방법

### 1. Docker로 MySQL 실행
```bash
docker compose up -d
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

기본 접속 정보는 다음과 같습니다.
- DB Host: `localhost`
- DB Port: `3306`
- DB Name: `course_registration_system`
- DB User: `root`
- DB Password: `1234`

## 요구사항 해석 및 가정
- 강의는 제목, 설명, 가격, 정원, 수강 기간을 가진다.
- 강의 상태는 `DRAFT`, `OPEN`, `CLOSED`로 관리한다.
- `DRAFT`와 `CLOSED` 상태에서는 수강 신청이 불가능하다.
- 수강 신청은 `PENDING`, `CONFIRMED`, `WAITLISTED`, `CANCELLED` 상태로 관리한다.
- `PENDING`과 `CONFIRMED`는 좌석 점유 상태로 본다.
- 정원을 초과한 신청은 `WAITLISTED`로 등록한다.
- 취소 시 대기열 1순위를 `PENDING`으로 승격한다.
- 수강 기간이 겹칠 때만 요일/시간 충돌을 검사한다.
- 수강 취소는 결제 후 7일 이내만 가능하다.
- 강의 시작일이 지나면 더 이상 수강신청이 불가능하도록 스케줄러가 `OPEN` 강의를 `CLOSED`로 변경한다.

## 설계 결정과 이유
- 도메인별 패키지 구조로 나누어 강의, 수강신청, 시간표 책임을 분리했습니다.
- 요청 DTO는 기능별로 분리해 유지보수성을 높였습니다.
- 정원 초과와 대기열 처리는 서비스 레이어에서 처리하고, 동시성 문제는 비관적 쓰기로 제어했습니다.
- 시간표 중복 검증은 수강 기간이 겹치는 경우에만 수행해 과도한 충돌 판단을 줄였습니다.
- 강의 상태 자동 변경은 스케줄러로 분리해 수동 수정과 자동 전환 책임을 구분했습니다.
- H2는 사용하지 않고 MySQL 기준으로만 검증되도록 구성했습니다.

## 미구현 / 제약사항
- 사용자 회원가입과 로그인 API는 과제의 핵심 로직이 아니라고 판단하여 제외했습니다.
- 강의 시간표는 강사가 운영 중 조정할 수 있다고 보았기 때문에, 시간표 수정 자체는 허용했습니다.
  다만 수정 후 기존 수강생들의 다른 강의와의 충돌 여부까지 처리하는 로직은 구현하지 않았습니다.

## AI 활용 범위
AI 도구는 구현 순서를 정리하고, 테스트 코드와 README 초안을 빠르게 잡는 과정에서 활용했습니다.
특히 도메인 분리 방식, 예외 처리 흐름, 핵심 비즈니스 규칙을 어떤 단위로 테스트할지 정리할 때 도움을 받았습니다.
다만 최종 코드는 직접 수정했고, 실행과 테스트는 실제로 다양한 데이터 예시를 입력해가며 검증했습니다.

## API 목록 및 예시

### 강의(Class) 관리

<details>
<summary>POST /api/course-classes - 강의 생성</summary>

요청

```json
{
  "creatorId": 1,
  "title": "자바 기초",
  "description": "자바 문법과 객체지향을 학습합니다.",
  "price": 100000,
  "capacity": 20,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30"
}
```

응답

```json
{
  "id": 1,
  "creatorId": 1,
  "title": "자바 기초",
  "description": "자바 문법과 객체지향을 학습합니다.",
  "price": 100000,
  "capacity": 20,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30",
  "status": "DRAFT",
  "currentEnrollmentCount": 0,
  "createdAt": "2026-05-22T00:00:00",
  "updatedAt": "2026-05-22T00:00:00"
}
```
</details>

<details>
<summary>GET /api/course-classes - 강의 목록 조회</summary>

요청 예시

```bash
GET /api/course-classes
GET /api/course-classes?status=OPEN
```

응답

```json
[
  {
    "id": 1,
    "creatorId": 1,
    "title": "자바 기초",
    "description": "자바 문법과 객체지향을 학습합니다.",
    "price": 100000,
    "capacity": 20,
    "startDate": "2026-06-01",
    "endDate": "2026-06-30",
    "status": "OPEN",
    "currentEnrollmentCount": 7,
    "createdAt": "2026-05-22T00:00:00",
    "updatedAt": "2026-05-22T00:00:00"
  }
]
```
</details>

<details>
<summary>GET /api/course-classes/{courseClassId} - 강의 상세 조회</summary>

응답

```json
{
  "id": 1,
  "creatorId": 1,
  "title": "자바 기초",
  "description": "자바 문법과 객체지향을 학습합니다.",
  "price": 100000,
  "capacity": 20,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30",
  "status": "OPEN",
  "currentEnrollmentCount": 7,
  "createdAt": "2026-05-22T00:00:00",
  "updatedAt": "2026-05-22T00:00:00"
}
```
</details>

<details>
<summary>PUT /api/course-classes/{courseClassId} - 강의 수정</summary>

요청

```json
{
  "title": "자바 기초 2",
  "description": "강의 내용을 보강했습니다.",
  "price": 120000,
  "capacity": 25,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30"
}
```

응답

```json
{
  "id": 1,
  "creatorId": 1,
  "title": "자바 기초 2",
  "description": "강의 내용을 보강했습니다.",
  "price": 120000,
  "capacity": 25,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30",
  "status": "OPEN",
  "currentEnrollmentCount": 7,
  "createdAt": "2026-05-22T00:00:00",
  "updatedAt": "2026-05-22T00:00:00"
}
```
</details>

<details>
<summary>PATCH /api/course-classes/{courseClassId}/status - 강의 상태 변경</summary>

요청

```json
{
  "status": "OPEN"
}
```

응답

```json
{
  "id": 1,
  "creatorId": 1,
  "title": "자바 기초",
  "description": "자바 문법과 객체지향을 학습합니다.",
  "price": 100000,
  "capacity": 20,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30",
  "status": "OPEN",
  "currentEnrollmentCount": 7,
  "createdAt": "2026-05-22T00:00:00",
  "updatedAt": "2026-05-22T00:00:00"
}
```
</details>

<details>
<summary>DELETE /api/course-classes/{courseClassId} - 강의 삭제</summary>

응답

```http
204 No Content
```
</details>

### 시간표(Class Schedule) 관리

<details>
<summary>POST /api/course-classes/{courseClassId}/schedules - 시간표 생성</summary>

요청

```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "room": "A101"
}
```

응답

```json
{
  "id": 1,
  "dayOfWeek": "MONDAY",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "room": "A101"
}
```
</details>

<details>
<summary>GET /api/course-classes/{courseClassId}/schedules - 시간표 목록 조회</summary>

응답

```json
[
  {
    "id": 1,
    "dayOfWeek": "MONDAY",
    "startTime": "14:00:00",
    "endTime": "16:00:00",
    "room": "A101"
  }
]
```
</details>

<details>
<summary>PUT /api/schedules/{scheduleId} - 시간표 수정</summary>

요청

```json
{
  "dayOfWeek": "WEDNESDAY",
  "startTime": "10:00:00",
  "endTime": "12:00:00",
  "room": "B201"
}
```

응답

```json
{
  "id": 1,
  "dayOfWeek": "WEDNESDAY",
  "startTime": "10:00:00",
  "endTime": "12:00:00",
  "room": "B201"
}
```
</details>

<details>
<summary>DELETE /api/schedules/{scheduleId} - 시간표 삭제</summary>

응답

```http
204 No Content
```
</details>

### 수강신청(Enrollment) 관리

<details>
<summary>POST /api/course-classes/{courseClassId}/enrollments - 수강 신청</summary>

요청

```json
{
  "classmateId": 2
}
```

응답

```json
{
  "id": 10,
  "courseClassId": 3,
  "classmateId": 2,
  "status": "PENDING",
  "waitlistOrder": null,
  "appliedAt": "2026-05-22T01:01:19.742606",
  "paidAt": null,
  "cancelledAt": null,
  "createdAt": "2026-05-22T01:01:19.742616",
  "updatedAt": "2026-05-22T01:01:19.742618"
}
```
</details>

<details>
<summary>PATCH /api/enrollments/{enrollmentId}/confirm - 결제 확정</summary>

요청

```json
{
  "classmateId": 2
}
```

응답

```json
{
  "id": 10,
  "courseClassId": 3,
  "classmateId": 2,
  "status": "CONFIRMED",
  "waitlistOrder": null,
  "appliedAt": "2026-05-22T01:01:19.742606",
  "paidAt": "2026-05-22T01:02:19.742606",
  "cancelledAt": null,
  "createdAt": "2026-05-22T01:01:19.742616",
  "updatedAt": "2026-05-22T01:02:19.742618"
}
```
</details>

<details>
<summary>PATCH /api/enrollments/{enrollmentId}/cancel - 수강 취소</summary>

요청

```json
{
  "classmateId": 2
}
```

응답

```http
204 No Content
```
</details>

<details>
<summary>GET /api/users/{userId}/enrollments - 내 수강 신청 목록 조회</summary>

요청 예시

```bash
GET /api/users/2/enrollments?page=0&size=10
```

응답

```json
{
  "content": [
    {
      "id": 12,
      "courseClassId": 4,
      "classmateId": 2,
      "status": "WAITLISTED",
      "waitlistOrder": 1,
      "appliedAt": "2026-05-22T01:05:44.035462",
      "paidAt": null,
      "cancelledAt": null,
      "createdAt": "2026-05-22T01:05:44.035486",
      "updatedAt": "2026-05-22T01:05:44.035492"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "number": 0
}
```
</details>

<details>
<summary>GET /api/course-classes/{courseClassId}/enrollments?creatorId={creatorId} - 강의별 수강 신청 목록 조회</summary>

요청 예시

```bash
GET /api/course-classes/4/enrollments?creatorId=1
```

응답

```json
[
  {
    "id": 11,
    "courseClassId": 4,
    "classmateId": 3,
    "status": "PENDING",
    "waitlistOrder": null,
    "appliedAt": "2026-05-22T01:05:44.020594",
    "paidAt": null,
    "cancelledAt": null,
    "createdAt": "2026-05-22T01:05:44.020621",
    "updatedAt": "2026-05-22T01:05:44.020628"
  }
]
```
</details>

## 데이터 모델 설명

<img width="600" alt="image" src="https://github.com/user-attachments/assets/08e16347-b293-45bc-a55c-ad9cd89bce70" />


- `users`는 강사와 수강생을 모두 포함하는 사용자 테이블입니다.
- `course_classes`는 강의의 기본 정보와 강의를 개설한 강사 정보를 연결합니다.
- `course_class_schedules`는 하나의 강의에 여러 개의 요일/시간표를 연결할 수 있도록 분리했습니다.
- `enrollments`는 수강생이 특정 강의에 신청한 이력을 저장하며, 신청 상태와 대기열 순번을 함께 관리합니다.
- 강의와 수강신청은 1:N 관계이고, 사용자와 수강신청도 1:N 관계입니다.
- 시간표는 강의에 종속되며, 기간이 겹치는 다른 강의와의 수강 충돌 검증에 사용됩니다.

이 구조를 통해 강의 개설, 시간표 등록, 수강 신청, 대기열 승격, 취소 제한을 도메인 단위로 분리해서 처리할 수 있습니다.

## 테스트 실행 방법
테스트는 Gradle을 통해 실행합니다. 전체 검증은 아래 명령어로 수행합니다.

```bash
./gradlew test
```

특정 테스트만 실행할 수도 있습니다.

```bash
./gradlew test --tests com.example.courseregistration.domain.enrollment.service.EnrollmentServiceTest
```

### 현재 테스트 구성

#### 1. `EnrollmentServiceTest`
수강 신청과 수강 취소의 핵심 비즈니스 규칙을 검증합니다.
- 정원 내 신청 시 `PENDING`으로 저장되는지 확인
- 정원 초과 시 `WAITLISTED`로 저장되는지 확인
- 중복 신청이 거부되는지 확인
- 시간표 충돌이 거부되는지 확인
- 결제 후 7일 이내에만 취소되는지 확인
- 취소 시 대기열 1순위가 승격되는지 확인
- 내 수강 신청 목록 조회가 페이지 단위로 동작하는지 확인

#### 2. `CourseClassStatusSchedulerTest`
강의 상태 자동 변경 로직을 검증합니다.
- 강의 시작일이 지난 `OPEN` 강의가 자동으로 `CLOSED`로 변경되는지 확인

### 테스트 결과 확인
테스트 실행 결과는 터미널에서 바로 확인할 수 있습니다.
실패가 있으면 `build/reports/tests/test/index.html`에 HTML 리포트가 생성되고, 어떤 테스트가 왜 실패했는지 확인할 수 있습니다.
