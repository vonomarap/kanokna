# test-support

Shared Testcontainers and test utility classes for backend modules.

## Usage

Always depend on this module with test scope:

```xml
<dependency>
  <groupId>com.kanokna</groupId>
  <artifactId>test-support</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
```

This module intentionally keeps its own dependencies in compile scope so they are available transitively on downstream test classpaths.
