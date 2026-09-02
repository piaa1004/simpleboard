# SimpleBoard

`codecrews` 프로젝트에서 **회원가입(이메일 인증 포함) · 로그인/로그아웃 · 게시판**만 남긴 신규 프로젝트다.
OAuth2 소셜 로그인, 모임, 채팅, 파일 업로드(S3·첨부파일·프로필 이미지)는 들어 있지 않다.

```
simpleboard/
├── backend/    Spring Boot 3.5 · Java 21 · JPA + QueryDSL · MySQL · JWT(HttpOnly 쿠키)
├── frontend/   React 19 · Vite · React Router · RTK Query · Tailwind + daisyUI
└── docker-compose.yml   개발용 MySQL
```

## 실행

```bash
cp .env.example .env      # DB 비밀번호와 JWT 키를 채운다
docker compose up -d      # MySQL (localhost:3306, DB 이름 simpleboard)

cd backend  && ./gradlew bootRun          # http://localhost:8080
cd frontend && npm install && npm run dev # http://localhost:5173
```

DB 없이 화면만 보고 싶으면 백엔드를 H2 인메모리로 띄운다.

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'
```

테이블은 `ddl-auto: update` 로 엔티티에서 만들어진다. 운영에서는 `validate` 로 바꾸고
스키마는 마이그레이션 도구로 관리하는 것이 맞다.

## 검증

```bash
cd backend  && ./gradlew test   # 회원가입 → 로그인 → 글/댓글 → 삭제 전체 흐름
cd frontend && npm run lint && npm test && npm run build
```

## 구조

### 백엔드 (`com.example.simpleboard`)

| 패키지 | 역할 |
| --- | --- |
| `account.user` | 회원가입, 로그인, 로그아웃, 내 정보 |
| `smtp` | 회원가입 이메일 인증번호 발송·확인 |
| `post.post` | 게시글 CRUD, 검색, 태그, 조회수, 추천 |
| `post.comment` | 댓글 |
| `post.tag` | 게시글 태그 |
| `security` | JWT 발급·검증, 쿠키, 시큐리티 설정 |
| `common` | 공통 응답 봉투, 예외 처리, QueryDSL 설정 |
| `cors` | 프론트를 다른 도메인에 띄울 때를 위한 CORS |

### 인증 방식

- 로그인하면 액세스 토큰(30분)과 리프레시 토큰(14일)을 **HttpOnly 쿠키**로 내려준다.
  자바스크립트가 토큰을 읽을 수 없다.
- 액세스 토큰이 만료되면 `JwtAuthenticationFilter` 가 리프레시 토큰으로 새 토큰을 쿠키에 심고
  응답 코드 `4` 를 돌려준다. 프론트의 `baseApi` 가 그 신호를 보고 같은 요청을 **한 번만** 재시도한다.
- 액세스 토큰과 리프레시 토큰은 **서로 다른 키로 서명**한다. 같은 키를 쓰면 액세스 토큰을
  리프레시 토큰 자리에 넣어도 통과하기 때문이다.
- 로그인 상태를 프론트 스토어에 따로 두지 않는다. `useGetMeQuery()` 의 결과가 곧 로그인 상태다.

### 회원가입 이메일 인증

`/mail/send` → `/mail/check` → `/registers` 순서로 진행한다.

- 인증번호는 6자리 숫자이고 5분간 유효하다. 재발송은 30초 간격으로 막고, 5번 틀리면 그 번호는 못 쓴다.
- 인증에 성공해도 인증 기록을 지우지 않고 `verified` 로 표시만 한다. `/registers` 가 그 표시를 확인한
  뒤에야 계정을 만들고, 그때 기록을 지운다. **화면 단계 진행에 맡기지 않으므로 `/registers` 를 직접
  호출해 인증을 건너뛸 수 없다.** 인증 후 30분 안에 가입해야 한다.
- 개발 중에는 `MAIL_ENABLED=false`(기본값)로 두면 메일을 보내지 않고 인증번호를 서버 로그에 찍는다.
  실제로 보내려면 `.env` 에서 `MAIL_ENABLED=true` 와 `MAIL_USERNAME`/`MAIL_PASSWORD` 를 채운다.
  켜 놓고 SMTP 호스트를 빠뜨리면 기동 시점에 바로 실패한다.

관련 설정은 `application.yaml` 의 `app.mail` 에 모여 있다.

### API

모든 응답은 `{ code, message, data }` 봉투로 감싼다.

| 메서드 | 경로 | 인증 | 설명 |
| --- | --- | --- | --- |
| POST | `/mail/send` | | 인증번호 발송 (이미 가입된 이메일이면 409) |
| POST | `/mail/check` | | 인증번호 확인 |
| POST | `/registers` | | 회원가입 (인증 안 된 이메일이면 400, 중복이면 409) |
| GET | `/users/exists?id=&email=` | | 아이디·이메일 중복 확인 (사용 중이면 409) |
| POST | `/logins` | | 로그인 (쿠키 발급) |
| POST | `/user/logout` | | 로그아웃 (쿠키 만료) |
| GET | `/users` | ✔ | 내 정보 |
| GET | `/posts?type=&data=&page=&size=` | | 목록 / 검색(`type=search`) / 태그(`type=tag`) |
| GET | `/posts/count?type=&data=` | | 전체 건수 (페이지네이션용) |
| GET | `/posts/{postId}` | | 상세 (비공개 글은 작성자만) |
| POST | `/posts` | ✔ | 등록 |
| PUT | `/posts/{postId}` | ✔ | 수정 (작성자만) |
| DELETE | `/posts/{postId}` | ✔ | 삭제 (작성자만, soft delete) |
| PATCH | `/posts/views/{postId}` | | 조회수 증가 |
| PATCH | `/posts/likes/{postId}` | ✔ | 추천 (자기 글·중복 불가) |
| GET | `/comments/{postId}` | | 댓글 목록 |
| POST | `/comments` | ✔ | 댓글 등록 |
| DELETE | `/comments/{commentId}` | ✔ | 댓글 삭제 (작성자만) |

### 프론트엔드

```
src/
├── api/baseApi.js        RTK Query 기본 설정 (토큰 재발급 재시도, 에러 메시지 정규화)
├── app/                  store, router
├── components/           layout(AppLayout·Header), ui(공용 컴포넌트)
├── features/auth/        로그인·회원가입
├── features/post/        목록·상세·작성/수정·댓글
└── lib, hooks/
```

API 호출은 모두 `/api` 로 나가고 Vite 프록시가 접두사를 떼어 백엔드로 넘긴다.
브라우저 입장에서 같은 오리진이라 쿠키 인증에 별도 CORS 설정이 필요 없다.
운영에서도 리버스 프록시가 같은 규칙(`/api/*` → 백엔드 루트)을 지켜야 한다.

## 원본에서 고친 것

- `CookieSupport` 가 static 필드에 `@Value` 를 걸어 도메인 값이 항상 `null` 이었다. 평범한 빈으로 바꿨다.
- 쿠키 `maxAge` 에 밀리초를 넣고 있었다(30분 대신 약 20일). 초 단위로 맞췄다.
- 게시글 수정/삭제의 소유자 검사가 `new PostException(...)` 으로 예외를 만들기만 하고 던지지 않았다.
- 액세스·리프레시 토큰의 서명 키가 같은 값이었다.
- `UserResponse` 가 비밀번호 해시를 그대로 내려보내고 있었다.
- 토큰 재발급 신호를 프론트가 `data === '4'` 로 확인해 항상 거짓이었다. `code === '4'` 로 고쳤다.
- 사용자 조회가 `JpaRepository.findById(Long)` 를 `findById(String)` 으로 덮어쓰고 있었다. `findByUserId` 로 분리했다.
- 인증 필터가 `Filter` 빈이라 시큐리티 체인과 서블릿 컨테이너에 이중 등록되어 요청마다 두 번 돌았다.
  이제 `SecurityConfig` 안에서 직접 만든다.
- 스프링 시큐리티 6 은 ERROR 디스패치(`/error`)에도 인가 규칙을 적용한다. 열어두지 않으면
  400 이어야 할 응답이 401 로 덮인다. 실제로 확인하고 `/error` 를 permitAll 로 열었다.

이메일 인증도 원본을 그대로 옮기지 않고 다음을 고쳤다.

- 인증에 성공하면 인증 기록을 지워버려서, `/registers` 를 직접 호출하면 인증을 건너뛸 수 있었다.
  이제 회원가입이 인증 여부를 직접 확인한다.
- 인증번호가 36자짜리 UUID 라 사용자가 옮겨 적을 수 없었다. 6자리 숫자로 바꿨다.
- 재발송해도 저장된 옛 코드를 그대로 보내서 번호가 갱신되지 않았다.
- 만료 시간, 시도 횟수 제한, 재발송 간격이 전혀 없었다.
- `MailCertRepository.findById(String)` 이 `JpaRepository.findById(Long)` 을 덮어쓰고 있었다.
- 실패 횟수를 올린 뒤 예외를 던지면 그 증가분까지 함께 롤백되어 시도 제한이 무력화된다.
  `@Transactional(noRollbackFor = MailException.class)` 로 막았고, 회귀 테스트로 고정해 뒀다.
