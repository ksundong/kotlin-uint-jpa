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

바뀐 컨버터 구현

```kotlin
@Converter
class UIntConverter : AttributeConverter<UInt?, Long?> {

    private val logger: Logger = LoggerFactory.getLogger(UIntConverter::class.java)

    override fun convertToDatabaseColumn(attribute: UInt?): Long? {
        logger.info("엔티티 값: {}", attribute)
        logger.info("DB에 저장될 값: {}", attribute?.toLong())
        return attribute?.toLong()
    }

    override fun convertToEntityAttribute(dbData: Long?): UInt? {
        logger.info("DB에 저장된 값: {}", dbData)
        logger.info("엔티티로 반환될 값: {}", dbData?.toUInt())
        return dbData?.toUInt()
    }
}
```

발생한 로그 (`valiate` 여서 발생한다.)

```text
2022-04-24 00:57:41.614 ERROR 66961 --- [  restartedMain] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: Invocation of init method failed; nested exception is javax.persistence.PersistenceException: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.tool.schema.spi.SchemaManagementException: Schema-validation: wrong column type encountered in column [mau] in table [uint_test]; found [int (Types#INTEGER)], but expecting [bigint (Types#BIGINT)]

```

반환타입은 `Long` 이어서 `bigint` 를 기대하는데, `unsigned int` 는 `int` 로 판단한다.

`application.propeties` 에 수정을 가하자

```properties
...
spring.jpa.hibernate.ddl-auto=none
...
```

놀랍게도 동작한다.

```text
2022-04-24 01:00:31.947 DEBUG 67036 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : POST "/uint?mau=2147483648", parameters={masked}
2022-04-24 01:00:31.952 DEBUG 67036 --- [nio-8080-exec-1] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to dev.idion.kotlinuint.uint.UintTestController#create(Long)
2022-04-24 01:00:32.023  INFO 67036 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티 값: 2147483648
2022-04-24 01:00:32.024  INFO 67036 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장될 값: 2147483648
2022-04-24 01:00:32.024  INFO 67036 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장된 값: 2147483648
2022-04-24 01:00:32.024  INFO 67036 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티로 반환될 값: 2147483648
2022-04-24 01:00:32.029 DEBUG 67036 --- [nio-8080-exec-1] org.hibernate.SQL                        : 
    insert 
    into
        uint_test
        (mau) 
    values
        (?)
2022-04-24 01:00:32.045  INFO 67036 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티 값: 2147483648
2022-04-24 01:00:32.045  INFO 67036 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장될 값: 2147483648
2022-04-24 01:00:32.085 DEBUG 67036 --- [nio-8080-exec-1] m.m.a.RequestResponseBodyMethodProcessor : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/json, application/*+json]
2022-04-24 01:00:32.086 DEBUG 67036 --- [nio-8080-exec-1] m.m.a.RequestResponseBodyMethodProcessor : Nothing to write: null body
2022-04-24 01:00:32.087 DEBUG 67036 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

조회가 안된다.. 원인은 컴파일된 클래스에 no args constructor가 없어서다. (Spring 은 잘 되는데... 버근가??)

```text
2022-04-24 01:19:13.194 DEBUG 67723 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : GET "/uint", parameters={}
2022-04-24 01:19:13.202 DEBUG 67723 --- [nio-8080-exec-1] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to dev.idion.kotlinuint.uint.UintTestController#all()
2022-04-24 01:19:13.409 DEBUG 67723 --- [nio-8080-exec-1] org.hibernate.SQL                        : 
    select
        uinttest0_.id as id1_0_,
        uinttest0_.mau as mau2_0_ 
    from
        uint_test uinttest0_
2022-04-24 01:19:13.472 DEBUG 67723 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Failed to complete request: org.springframework.orm.jpa.JpaSystemException: No default constructor for entity:  : dev.idion.kotlinuint.uint.UintTest; nested exception is org.hibernate.InstantiationException: No default constructor for entity:  : dev.idion.kotlinuint.uint.UintTest
2022-04-24 01:19:13.477 ERROR 67723 --- [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.springframework.orm.jpa.JpaSystemException: No default constructor for entity:  : dev.idion.kotlinuint.uint.UintTest; nested exception is org.hibernate.InstantiationException: No default constructor for entity:  : dev.idion.kotlinuint.uint.UintTest] with root cause

org.hibernate.InstantiationException: No default constructor for entity:  : dev.idion.kotlinuint.uint.UintTest
```

이것저것 해봤는데 안돼서 코틀린으로는 포기하고 자바로 변경...

```text
2022-04-24 01:47:45.967 DEBUG 68713 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : GET "/uint", parameters={}
2022-04-24 01:47:45.971 DEBUG 68713 --- [nio-8080-exec-1] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to dev.idion.kotlinuint.uint.UintTestController#all()
2022-04-24 01:47:46.081 DEBUG 68713 --- [nio-8080-exec-1] org.hibernate.SQL                        : 
    select
        uinttest0_.id as id1_0_,
        uinttest0_.mau as mau2_0_ 
    from
        uint_test uinttest0_
2022-04-24 01:47:46.109  INFO 68713 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장된 값: null
2022-04-24 01:47:46.110  INFO 68713 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티로 반환될 값: null
2022-04-24 01:47:46.110  INFO 68713 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장된 값: 2147483648
2022-04-24 01:47:46.111  INFO 68713 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티로 반환될 값: 2147483648
2022-04-24 01:47:46.111  INFO 68713 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장된 값: 2147483648
2022-04-24 01:47:46.111  INFO 68713 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티로 반환될 값: 2147483648
2022-04-24 01:47:46.111  INFO 68713 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : DB에 저장된 값: 2147483648
2022-04-24 01:47:46.111  INFO 68713 --- [nio-8080-exec-1] dev.idion.kotlinuint.uint.UIntConverter  : 엔티티로 반환될 값: 2147483648
2022-04-24 01:47:46.187 DEBUG 68713 --- [nio-8080-exec-1] m.m.a.RequestResponseBodyMethodProcessor : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/json, application/*+json]
2022-04-24 01:47:46.188 DEBUG 68713 --- [nio-8080-exec-1] m.m.a.RequestResponseBodyMethodProcessor : Writing [[dev.idion.kotlinuint.uint.UintTest@a0e311f, dev.idion.kotlinuint.uint.UintTest@3af9f2bb, dev.idion. (truncated)...]
2022-04-24 01:47:46.295 DEBUG 68713 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

결과

```json
[
  {
    "id": 1,
    "mau": null
  },
  {
    "id": 2,
    "mau": 2147483648
  },
  {
    "id": 3,
    "mau": 2147483648
  },
  {
    "id": 4,
    "mau": 2147483648
  }
]
```

## 결론

`Converter` 를 이용한 방법으로 값을 다룰 수는 있다. 하지만 `validate` 옵션을 꺼야하는 단점이 존재한다.

## p.s.

kotlin jpa 플러그인이 혼자 오동작한다. no args constructor도 안만들어주고, open도 안해준다... 나한테 왜그래!!

갓바의 힘으로 PoC를 마무리 할 수 있게되었다.
