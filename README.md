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
