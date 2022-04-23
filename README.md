# Kotlin UInt 타입은 JPA와 쓸 수 있을까?

## 배경

Java에서는 `unsigned` 자료형은 지원하지 않는다. (사실 JVM 스펙 상에서도 `unsigned`는 `char`를 제외하고는 지원되지 않는다.)
반면, Kotlin 에서는 `UInt` 타입을 지원한다. Kotlin에서는 어떻게 `UInt` 타입을 지원하냐면, `Int` 타입을 래핑한 클래스로 제공한다.

그렇다면, `JPA`는 `Kotlin` `UInt` 타입을 지원해줄까? `UInt` 와는 어떻게 동작할까? (`UInt` 만이 아닌 다른 `unsigned` 모두)

## 가설

1. `UInt` 타입은 `unsigned` 자료형과 원활하게 동작할 것이다.
2. `UInt` 타입은 결국 `Int` 타입을 `wrapping` 한 것으로 잘 동작하지 않을 것이다.
3. `UInt` 타입은 `signed int` 타입과 함께 사용해 `unsigned` value 를 다룰 수 있을 것이다.

## 목표

제일 최고의 상황은 `JPA` 가 `unsigned` 자료형을 잘 지원해서 코틀린에서 `JPA` 를 쓸 때, 매핑문제를 해소해주는 것이다.

## 사용 기술

- Kotlin 1.6.21
- Spring Boot 2.6.7
- Spring Web MVC
- Spring Data JPA
- Gradle 7.4.1

## 개발 환경

- Mac OS Monterey 12.3.1
- IntelliJ IDEA 2021.3.3
- Amazon Corretto JDK 11.1
- Docker Desktop 4.6.1
  - MySql 5.7

## 검증

### 1. `UInt` 타입은 `unsigned` 자료형과 원활하게 동작할 것이다.

거짓. JPA는 `UInt`타입을 지원하지 않는다.

```text
'Basic' attribute type should not be 'UInt' 
 Inspection info: Reports property type mismatch for JPA attributes.
```

### 2. `UInt` 에 대한 `Converter` 를 만들면 원활하게 동작할 것이다.(`UintTest` 참조)

일단 Spring이 `UInt` 타입에 대한 `Convert` 를 지원하지 않아 `Long` 타입으로 받은 후 변환을 거쳤다.

```http request
POST http://localhost:8080/uint?mau=2147483648
```

발생한 로그 및 예외 로그

```text
2022-04-24 00:48:41.917 DEBUG 66735 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : POST "/uint?mau=2147483648", parameters={masked}
2022-04-24 00:48:41.923 DEBUG 66735 --- [nio-8080-exec-1] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to dev.idion.kotlinuint.uint.UintTestController#create(Long)
2022-04-24 00:48:41.993  INFO 66735 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티 값: 2147483648
2022-04-24 00:48:41.993  INFO 66735 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장될 값: -2147483648
2022-04-24 00:48:41.993  INFO 66735 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장된 값: -2147483648
2022-04-24 00:48:41.993  INFO 66735 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티로 반환될 값: 2147483648
2022-04-24 00:48:41.999 DEBUG 66735 --- [nio-8080-exec-1] org.hibernate.SQL                        : 
    insert 
    into
        uint_test
        (mau) 
    values
        (?)
2022-04-24 00:48:42.013  INFO 66735 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티 값: 2147483648
2022-04-24 00:48:42.013  INFO 66735 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장될 값: -2147483648
2022-04-24 00:48:42.028  WARN 66735 --- [nio-8080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 1264, SQLState: 22001
2022-04-24 00:48:42.028 ERROR 66735 --- [nio-8080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : Data truncation: Out of range value for column 'mau' at row 1
2022-04-24 00:48:42.047 DEBUG 66735 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Failed to complete request: org.springframework.dao.DataIntegrityViolationException: could not execute statement; SQL [n/a]; nested exception is org.hibernate.exception.DataException: could not execute statement
2022-04-24 00:48:42.057 ERROR 66735 --- [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.springframework.dao.DataIntegrityViolationException: could not execute statement; SQL [n/a]; nested exception is org.hibernate.exception.DataException: could not execute statement] with root cause

com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Out of range value for column 'mau' at row 1
```

값 자체가 음수가 되어 DB와 타입 불일치로 실패하는 모습을 볼 수 있다.

#### 컨버터 구현을 바꿔볼까?
