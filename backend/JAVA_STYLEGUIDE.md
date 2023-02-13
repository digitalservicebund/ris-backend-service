# Java Styleguide 🎨

## Formatting

Java code is being formatted according to the [Google Java Style](https://google.github.io/styleguide/javaguide.html).

## Programming Practices

### Builder Pattern

When creating objects with many fields, prefer Builder pattern:

```java
// ❌ Bad
DocUnit docUnit = new DocUnit();
docUnit.setUuid(UUID.randomUUID());
docUnit.setCreationtimestamp(Instant.now());
docUnit.setDocumentnumber(...)

// ✅ Good
DocUnit docUnit = DocUnit.builder()
  .uuid(UUID.randomUUID())
  .creationtimestamp(Instant.now())
  .documentnumber(...)
  .build();
```

### Test Expectations

Use [AssertJ](https://assertj.github.io/doc/):

```java
// ❌ Bad
assertNotNull(responseEntity);
assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

// ✅ Good
assertThat(responseEntity).isNotNull();
assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
```

Integration tests(WebTestClient and database):

```java
// ❌ Bad
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "otc.obs.bucket-name=testBucket",
      "otc.obs.endpoint=testUrl",
      "local.file-storage=.local-storage",
      "mail.from.address=test@test.com"
    })
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)

// ✅ Good
@RISIntegrationTest(controllers={...}, imports={...}, timeout="...")
```

The second way only use the bean definition for the imported classes.
The application context contains only needed resources.

Ṕarameters:

- controllers - the used controllers in the web test client
- imports - the needed imports for the application context
- timeout - max time to wait for a response (good for debuggging)

The import parameter is the only mandatory parameter.
