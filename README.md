[![Sin título.png](https://s23.postimg.org/3sg28rkor/Sin_t_tulo.png)](https://postimg.org/image/lv94zzgjb/)

 Kotlin extensions to simplify Realm API.

## Description

Simplify your code to its minimum expression with this set of Kotlin extensions for Realm. Forget all boilerplate related with Realm API and perform database operations in one line of code with this lightweight library. Full test coverage.

## Download for Kotlin 1.1.x and Realm 3.1.3

Grab via Gradle:

```groovy
repositories {
    mavenCentral()
}

compile 'com.github.vicpinm:krealmextensions:1.0.8'
```

## Download for Kotlin 1.0.x and Realm 2.2.1

Grab via Gradle:

```groovy
repositories {
    mavenCentral()
}

compile 'com.github.vicpinm:krealmextensions:1.0.6'
```
## Features

Forget about:
- Realm instances management
- Transactions
- Threads limitations
- Boilerplate related with Realm API

## Usage
### Store entities

All your entities should extend RealmObject.

#### Before (java)

```java
User user = new User("John");

Realm realm = Realm.getDefaultInstance();
try{
   realm.executeTransaction(realm -> {
      realm.copyToRealmOrUpdate(user);  
   });  
} finally {
   realm.close();
}
```

#### After (Kotlin + extensions)

````kotlin
User("John").save()
````

Save method creates or updates your entity into database. You can also use create() method, which only create a new entity into database. If a previous one exists with the same primary key, it will throw an exception.

#### Save list: Before (java)

```java
List<User> users = new ArrayList<User>(...);

Realm realm = Realm.getDefaultInstance();
try {
    realm.executeTransaction(realm -> {
        realm.copyToRealmOrUpdate(users);  
    });
} finally {
    realm.close();
}
```

#### Save list: After (Kotlin + extensions)

```kotlin
listOf<User>(...).saveAll()
```

If you need to provide your own Realm instance, you can use the saveManaged(Realm) and saveAllManaged(Realm) methods. These methods return managed objects. You should close manually your Realm instance when you finish with them. 

### Query entities

All query extensions return detached realm objects, using copyFromRealm() method. 

#### Get first entity: Before (java)
```java
Realm realm = Realm.getDefaultInstance();
try {
   Event firstEvent = realm.where(Event.class).findFirst();
   firstEvent = realm.copyFromRealm(event);
} finally {
   realm.close();
}
```

#### Get first entity: After (Kotlin + extensions)
```kotlin
val firstEvent = Event().queryFirst()
```

You can use lastItem extension too.

#### Get all entities: Before (java)
```java
Realm realm = Realm.getDefaultInstance();
try {
    List<Event> events = realm.where(Event.class).findAll();
    events = realm.copyFromRealm(event);
} finally {
    realm.close();
}
```

#### Get  all entities: After (Kotlin + extensions)
```kotlin
val events = Event().queryAll()
```

#### Get entities with conditions: Before (java)

```java
Realm realm = Realm.getDefaultInstance();
try{
    List<Event> events = realm.where(Event.class).equalTo("id",1).findAll();
    events = realm.copyFromRealm(event);
} finally {
    realm.close();
}
```

#### Get entities with conditions: After (Kotlin + extensions)
```kotlin
val events = Event().query { query -> query.equalTo("id",1) }
```

If you only need the first or last result, you can also use:

```kotlin
val first = Event().queryFirst { query -> query.equalTo("id",1) }
val last = Event().queryLast { query -> query.equalTo("id",1) }
```

#### Get sorted entities
```kotlin
val sortedEvents = Event().querySorted("name",Sort.DESCENDING) 
```

```kotlin
val sortedEvents = Event().querySorted("name",Sort.DESCENDING) { query -> query.equalTo("id",1) }
```


### Delete entities

#### Delete all: Before (java)
```kotlin
Realm realm = Realm.getDefaultInstance();
try{
    List<Event> events = realm.where(Event.class).findAll();
    realm.executeTransaction(realm -> {
        events.deleteAllFromRealm();
    });
} finally {
    realm.close();
}
```

#### Delete all: After (Kotlin + extensions)
```kotlin
Event().deleteAll()
```

#### Delete with condition: Before (java)

```kotlin
Realm realm = Realm.getDefaultInstance();
try{
    List<Event> events = realm.where(Event.class).equalTo("id",1).findAll().deleteAllFromRealm();
    events = realm.copyFromRealm(event);
} finally {
    realm.close();
}
```

#### Delete with condition: After (Kotlin + extensions)
```kotlin
Event().delete { query -> query.equalTo("id", 1) }
```


### Observe data changes

#### Before (java)

```java
Realm realm = Realm.getDefaultInstance();
Observable<List<Event>> obs =  realm.where(Event.class).findAllAsync()
.asObservable()
.filter(RealmResults::isLoaded)
.map(realm::copyFromRealm)
.doOnUnsubscribe(() -> realm.close());
```

#### After (Kotlin + extensions)

```kotlin
val obs = Event().allItemsAsObservable
```

#### Observe query with condition: Before (java)

```java
Realm realm = Realm.getDefaultInstance();
Observable<List<Event>> obs =  realm.where(Event.class).equalTo("id",1).findAllAsync()
.asObservable()
.filter(RealmResults::isLoaded)
.map(realm::copyFromRealm)
.doOnUnsubscribe(() -> realm.close());
```

#### Observe query with condition: After (Kotlin + extensions)

```kotlin
val obs = Event().queryAsObservable { query -> query.equalTo("id",1) }
```

These kind of observable queries have to be performed on a thread with a looper attached to it. If you perform an observable query on the main thread, it will run on this thread. If you perform the query on a background thread, a new thread with a looper attached will be created for you to perform the query. This thread will be listen for data changes and it will terminate when you call unsubscribe() on your subscription. 


### Proguard

You need to add these rules if you use proguard, for rxjava and realm:


```bash
# rxjava
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-dontnote rx.internal.util.PlatformDependent

-keepnames public class * extends io.realm.RealmObject
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn io.realm.**````
```
