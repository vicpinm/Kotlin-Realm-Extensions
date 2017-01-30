
[![Sin título.png](https://s23.postimg.org/3sg28rkor/Sin_t_tulo.png)](https://postimg.org/image/lv94zzgjb/)

 Kotlin extensions for simplifying Realm API.

## Description

Simplify your code to its minimum expression with this set of kotlin extensions for Realm. Forget all boilerplate related with Realm API and perform database operations in one line of code with this set of extensions.

## Download

Grab via Gradle:
```groovy
repositories {
    mavenCentral()
}

compile 'com.github.vicpinm:krealmextensions:1.0.2'
```

## Usage
### Store entities

All your entities should extend RealmObject.

#### Before (java)
````
User user = new User("John");

Realm realm = Realm.getDefaultInstance();
realm.beginTransaction();
realm.copyToRealmOrUpdate(user);  
realm.commitTransaction();
realm.close();
````
#### After (Kotlin + extensions)

````
User("John").save()
````

Save method creates or updates your entity into database. You can also use create() method, which only create a new entity into database. If a previous one exists with the same primary key, it will throw an exception.

#### Save list: Before (java)
````
List<User> users = new ArrayList<User>(...);

Realm realm = Realm.getDefaultInstance();
realm.beginTransaction();
realm.copyToRealmOrUpdate(users);  
realm.commitTransaction();
realm.close();
````
#### Save list: After (Kotlin + extensions)

````
listOf<User>(...).saveAll()
````


### Query entities

All query extensions return detached realm objects, using copyFromRealm() method. 

#### Get first entity: Before (java)
````
Realm realm = Realm.getDefaultInstance();
Event firstEvent = realm.where(Event.class).findFirst();
firstEvent = realm.copyFromRealm(event);
realm.close();
````
#### Get first entity: After (Kotlin + extensions)
````
val firstEvent = Event().firstItem
````

You can use lastItem extension too.

#### Get all entities: Before (java)
````
Realm realm = Realm.getDefaultInstance();
List<Event> events = realm.where(Event.class).findAll();
events = realm.copyFromRealm(event);
realm.close();
````
#### Get  all entities: After (Kotlin + extensions)
````
val events = Event().allItems
````

#### Get entities with conditions: Before (java)
````
Realm realm = Realm.getDefaultInstance();
List<Event> events = realm.where(Event.class).equalTo("id",1).findAll();
events = realm.copyFromRealm(event);
realm.close();
````

#### Get entities with conditions: After (Kotlin + extensions)
````
val events = Event().query { query -> query.equalTo("id",1) }
````


### Delete entities

#### Delete all: Before (java)
````
Realm realm = Realm.getDefaultInstance();
List<Event> events = realm.where(Event.class).findAll().deleteAllFromRealm();
events = realm.copyFromRealm(event);
realm.close();
````
#### Delete all: After (Kotlin + extensions)
````
Event().deleteAll()
````

#### Delete with condition: Before (java)
````
Realm realm = Realm.getDefaultInstance();
List<Event> events = realm.where(Event.class).equalTo("id",1).findAll().deleteAllFromRealm();
events = realm.copyFromRealm(event);
realm.close();
````
#### Delete with condition: After (Kotlin + extensions)
````
Event().delete { query -> query.equalTo("id", 1) }
````


### Observe data changes

#### Before (java)

````
Realm realm = Realm.getDefaultInstance();
Observable<List<Event>> obs =  realm.where(Event.class).findAllAsync()
.asObservable()
.filter(RealmResults::isLoaded)
.map(realm::copyFromRealm)
.doOnUnsubscribe(() -> realm.close());
````

#### After (Kotlin + extensions)

````
val obs = Event().allItemsAsObservable
````

#### Observe query with condition: Before (java)

````
Realm realm = Realm.getDefaultInstance();
Observable<List<Event>> obs =  realm.where(Event.class).equalTo("id",1).findAllAsync()
.asObservable()
.filter(RealmResults::isLoaded)
.map(realm::copyFromRealm)
.doOnUnsubscribe(() -> realm.close());
````

#### Observe query with condition: After (Kotlin + extensions)

````
val obs = Event().queryAsObservable { query -> query.equalTo("id",1) }
````

