# 프레임워크 구현

* 우아한테크코스 레벨3 DI 프레임워크 구현 미션 저장소입니다.
* [Step2 피드백](https://github.com/woowacourse/jwp-di/pull/55)([Step1 피드백](https://github.com/woowacourse/jwp-di/pull/9))


## 구현 방식
`@Controller`, `@Service`같이 클래스로 만들어지는 빈 뿐 아니라 `@Configuration`처럼 함수로 만들어지는 빈도 추가해야 했습니다.

때문에 빈의 생성 방식을 추상화한 `InstantiationMethod` 인터페이스를 만들었습니다. 빈 생성 방식에 따라 빈을 스캔하며 `BeanCreateMatcher`에 `Class Type`과 `InstantiationMethod` 구현체를 가지게 합니다. `BeanCreateMatcher`를 모두 만들면 본격적으로 `InstantiationMethod` 구현체를 사용해 빈을 생성(`instantiation`)합니다. (`BeanFactory` 클래스에서 `BeanCreateMatcher`를 사용해 만듭니다.)

빈의 파라미터를 생성할 때도 `BeanCreateMatcher`에 빈 타입이 있는지 확인하고 있다면 현재 만든 빈 목록에 있는지 확인하고 만들어 추가하는 형식입니다.
