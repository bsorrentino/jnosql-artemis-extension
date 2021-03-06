# Cassandra-extension

![Cassandra Project](https://jnosql.github.io/img/logos/cassandra.png)


Cassandra extension has implementations to use specific behavior that is beyond the API such as Cassandra Query Language, consistency level.

## Set dependency


```xml

  <dependency>
     <groupId>org.jnosql.artemis</groupId>
     <artifactId>cassandra-extension</artifactId>
     <version>${jnosql.version}</version>
  </dependency>
```

## Make Cassandra manager available to container

```java

public class CassandraProducer {


    @Produces
    public CassandraColumnFamilyManager getManager() {
        CassandraColumnFamilyManager manager = ...;
        return manager;
    }

    @Produces
    public CassandraColumnFamilyManagerAsync getManagerAsync() {
        CassandraColumnFamilyManagerAsync managerAsync = ...;
        return managerAsync;
    }
}


```


## CassandraRepository

CassandraRepository is an extension of Repository that allows using CQL annotation that executes Cassandra Query Language and also Consistency Level.


```java
    interface PersonRepository extends CassandraRepository<Person, String> {

        Person findByName(String name, ConsistencyLevel level);

        void deleteByName(String name, ConsistencyLevel level);

        @CQL("select * from Person")
        List<Person> findAll();

        @CQL("select * from Person where name = ?")
        List<Person> findByName(String name);
        
        @CQL("select * from Person where age = :age")
        List<Person> findByAge(@Param("age") Integer age);

    }
```

## CassandraRepositoryAsync

CassandraRepositoryAsync is an extension of RepositoryAsync that allows using CQL annotation that executes Cassandra Query Language and also Consistency Level.


```java
    interface PersonAsyncRepository extends CassandraRepositoryAsync<Person, String> {

        void findByName(String name, ConsistencyLevel level, Consumer<List<Person>> callBack);

        void deleteByName(String name, ConsistencyLevel level, Consumer<Void> callBack);

        @CQL("select * from Person where name= ?")
        void queryName(String name);

        @CQL("select * from Person where name= ?")
        void queryName(String name, Consumer<List<Person>> callBack);
    }
```

## @UDT

The UDT annotations is a mapping annotation that allows defining a field to be stored as User defined type in Cassandra.

```java
@Entity
public class Person {

    @Id("name")
    private String name;

    @Column
    private Integer age;

    @UDT("address")
    @Column
    private Address home;
    }
```

## Converts

* TimestampConverter: That converts to/from java.util.Date
* LocalDateConverter: That converts to/from com.datastax.driver.core.LocalDate

```java

    @Column
    @Convert(value = TimestampConverter.class)
    private LocalDateTime localDateTime;
   
    @Column
    @Convert(value = LocalDateConverter.class)
    private Calendar calendar;

```

## CassandraTemplate and CassandraTemplateAsync

CassandraTemplate and CassandraTemplateAsync are a specializations of Column Template that allows using CQL from both synchronous and asynchronous.

```java
    template.save(person, level);
    templateAsync.save(person, level);
    
    template.save("select * from Person");

```

