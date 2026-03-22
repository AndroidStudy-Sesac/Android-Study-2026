# 디자인 패턴 & SOLID 원칙

---

## SOLID 원칙

객체지향 설계의 5가지 핵심 원칙. 유지보수성과 확장성이 높은 코드를 작성하기 위한 가이드라인.

### S — Single Responsibility Principle (단일 책임 원칙)
> 클래스는 **하나의 이유**로만 변경되어야 한다.

```kotlin
// ❌ 나쁜 예: UserManager가 너무 많은 책임을 가짐
class UserManager {
    fun getUser() { }
    fun saveToDatabase() { }
    fun sendEmail() { }
    fun formatUserJson() { }
}

// ✅ 좋은 예: 책임 분리
class UserRepository { fun getUser() { } }
class UserEmailService { fun sendEmail() { } }
class UserSerializer { fun toJson() { } }
```

---

### O — Open/Closed Principle (개방/폐쇄 원칙)
> 확장에는 열려 있고, 수정에는 닫혀 있어야 한다.

```kotlin
// ✅ 새 도형을 추가할 때 기존 코드 수정 없이 확장 가능
interface Shape {
    fun area(): Double
}

class Circle(val radius: Double) : Shape {
    override fun area() = Math.PI * radius * radius
}

class Rectangle(val w: Double, val h: Double) : Shape {
    override fun area() = w * h
}
```

---

### L — Liskov Substitution Principle (리스코프 치환 원칙)
> 자식 클래스는 **부모 클래스를 대체**할 수 있어야 한다.

```kotlin
// 부모 타입으로 선언해도 자식이 정상 동작해야 함
open class Bird { open fun fly() { println("날다") } }
class Sparrow : Bird() { override fun fly() { println("참새가 날다") } }

fun makeFly(bird: Bird) { bird.fly() } // Sparrow를 넣어도 문제 없어야 함
```

---

### I — Interface Segregation Principle (인터페이스 분리 원칙)
> 클라이언트는 자신이 사용하지 않는 메서드에 의존하면 안 된다.

```kotlin
// ❌ 모든 기능이 하나의 인터페이스에
interface Worker {
    fun work()
    fun eat()
    fun sleep()
}

// ✅ 필요한 인터페이스만 구현
interface Workable { fun work() }
interface Eatable { fun eat() }

class Robot : Workable { override fun work() { } } // eat()은 필요 없음
class Human : Workable, Eatable { override fun work() { } override fun eat() { } }
```

---

### D — Dependency Inversion Principle (의존성 역전 원칙)
> 고수준 모듈은 저수준 모듈에 의존하면 안 된다. **둘 다 추상화에 의존**해야 한다.

```kotlin
// ❌ 고수준 모듈이 구체 클래스에 의존
class UserViewModel {
    val repo = MySQLUserRepository() // 구체 클래스에 직접 의존
}

// ✅ 추상화(인터페이스)에 의존
class UserViewModel(private val repo: UserRepository) // 인터페이스에 의존
```

---

## 디자인 패턴

### 1. Singleton (싱글톤 패턴)

#### 개념
애플리케이션 전체에서 **인스턴스를 하나만** 생성하고 공유하는 패턴.

#### 언제 사용?
- 데이터베이스 연결, 네트워크 클라이언트, 앱 설정 등 전역으로 하나만 존재해야 할 때

#### Kotlin에서 구현
```kotlin
// Kotlin의 object 키워드로 간단하게 싱글톤 구현
object AppConfig {
    val baseUrl = "https://api.example.com"
    var isDebug = false
}

// 사용
AppConfig.baseUrl
```

```kotlin
// companion object를 사용한 Thread-safe 싱글톤
class DatabaseHelper private constructor() {
    companion object {
        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper().also { instance = it }
            }
        }
    }
}
```

#### 주의사항
- 전역 상태를 공유하므로 남용하면 테스트가 어려워짐
- Android에서는 Application 수명주기와 묶인 싱글톤에 Context를 사용할 때 주의

---

### 2. Builder Pattern (빌더 패턴)

#### 개념
복잡한 객체를 **단계적으로 조립**할 수 있게 해주는 패턴. 생성자에 파라미터가 많을 때 가독성을 높임.

#### 언제 사용?
- 선택적 파라미터가 많은 객체 생성 시
- AlertDialog, Notification, Retrofit 빌더 등

```kotlin
// Android에서 대표적인 빌더 패턴 사용 예시
val dialog = AlertDialog.Builder(context)
    .setTitle("제목")
    .setMessage("내용")
    .setPositiveButton("확인") { _, _ -> }
    .setNegativeButton("취소", null)
    .create()

// Kotlin에서 직접 구현
data class Pizza(
    val size: String,
    val cheese: Boolean = false,
    val pepperoni: Boolean = false,
    val mushrooms: Boolean = false
)

// Kotlin의 named argument로 빌더 패턴 효과
val pizza = Pizza(size = "Large", cheese = true, pepperoni = true)
```

---

### 3. Factory Pattern (팩토리 패턴)

#### 개념
객체 생성 로직을 별도의 클래스(팩토리)에 위임하는 패턴. 어떤 클래스의 인스턴스를 생성할지를 서브클래스가 결정.

#### 언제 사용?
- 생성할 객체의 타입이 런타임에 결정될 때
- 객체 생성 과정이 복잡할 때

```kotlin
interface Animal {
    fun speak(): String
}

class Dog : Animal { override fun speak() = "멍멍" }
class Cat : Animal { override fun speak() = "야옹" }

// 팩토리 함수
object AnimalFactory {
    fun create(type: String): Animal = when (type) {
        "dog" -> Dog()
        "cat" -> Cat()
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
}

val animal = AnimalFactory.create("dog")
println(animal.speak()) // 멍멍
```

---

### 4. Observer Pattern (옵저버 패턴)

#### 개념
객체의 상태 변화가 있을 때 **의존하는 모든 객체에 자동으로 알림**을 보내는 패턴.  
Android의 `LiveData`, `StateFlow`, `RxJava`가 모두 이 패턴 기반.

#### 구성 요소
- **Observable(Subject)**: 상태를 가지며 변경 시 알림
- **Observer**: 알림을 받아 처리

```kotlin
// Kotlin Flow 기반 옵저버 패턴
class NewsRepository {
    private val _news = MutableStateFlow<List<String>>(emptyList())
    val news: StateFlow<List<String>> = _news.asStateFlow()

    fun updateNews(items: List<String>) {
        _news.value = items // 값 변경 시 모든 구독자에게 자동 알림
    }
}

// 구독자(Observer)
viewModel.news.collect { newsList ->
    // 뉴스 업데이트 처리
}
```

---

### 5. Repository Pattern (레포지토리 패턴)

#### 개념
**데이터 소스(API, DB, 캐시)를 추상화**하여 비즈니스 로직이 데이터 출처를 몰라도 되게 하는 패턴.

#### 역할
- Presentation/Domain Layer에서는 Repository Interface만 사용
- 실제로 로컬 DB에서 가져올지, 네트워크에서 가져올지는 Repository 구현체가 결정

```kotlin
// Interface (Domain Layer)
interface UserRepository {
    suspend fun getUser(id: String): User
    suspend fun saveUser(user: User)
}

// 구현체 (Data Layer) - 단일 진실의 원천(Single Source of Truth) 구현
class UserRepositoryImpl(
    private val apiService: UserApiService,
    private val userDao: UserDao
) : UserRepository {
    override suspend fun getUser(id: String): User {
        // 로컬 캐시 우선, 없으면 네트워크 요청
        return userDao.getUser(id) ?: run {
            val user = apiService.fetchUser(id)
            userDao.insertUser(user) // 캐싱
            user
        }
    }
}
```

---

### 6. Dependency Injection (의존성 주입, DI)

#### 개념
객체가 직접 의존성을 생성하는 대신, **외부에서 주입**받는 패턴. SOLID의 D(의존성 역전)를 실현하는 방법.

#### 장점
- 클래스 간 결합도 감소
- 테스트 시 Mock 객체 주입 용이
- 코드 재사용성 향상

```kotlin
// ❌ 직접 생성 - 테스트 불가능, 결합도 높음
class UserViewModel {
    private val repo = UserRepositoryImpl(ApiService(), UserDao())
}

// ✅ 의존성 주입 - 외부에서 주입
class UserViewModel(private val repo: UserRepository) {
    // repo는 외부에서 주입됨 → 테스트 시 FakeRepository 주입 가능
}
```

#### Android에서의 DI: Hilt
```kotlin
// Hilt를 사용한 DI
@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) : ViewModel()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideUserRepository(
        api: UserApiService,
        dao: UserDao
    ): UserRepository = UserRepositoryImpl(api, dao)
}
```

---

## 패턴 요약 비교

| 패턴 | 목적 | Android 대표 사용처 |
|------|------|---------------------|
| Singleton | 인스턴스 하나만 유지 | `object`, Room DB, Retrofit |
| Builder | 복잡한 객체 단계적 생성 | AlertDialog, Notification |
| Factory | 객체 생성 로직 분리 | ViewModelProvider.Factory |
| Observer | 상태 변화 자동 알림 | LiveData, StateFlow, Flow |
| Repository | 데이터 소스 추상화 | Room + Retrofit 조합 |
| DI | 의존성 외부 주입 | Hilt, Koin |
