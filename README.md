[![GitHub license](https://img.shields.io/github/license/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-1.0.1-blue)](https://mvnrepository.com/artifact/com.giraone.io/file-tree-copier)
[![GitHub issues](https://img.shields.io/github/issues/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/issues)
[![GitHub stars](https://img.shields.io/github/stars/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/stargazers)
[![Platform](https://img.shields.io/badge/platform-jre11%2B-blue)](https://github.com/giraone/file-tree-copier/pom.xml)

# File Tree Copier

Utility JAR for copying a file tree from web server or from classpath resource to a (local) file system.

## Features

- The source can be
  - a web server serving files and directory listings with JSON based indexes, e.g. `nginx` with `{ autoindex: on; autoindex_format json; }`.
  - a file tree within a resource folder of the running Java process, e.g. from a JAR
  - a file tree within the file system
- When using an HTTP source, the used HTTP client can be configured. The default implementation is the native HTTP client
  of Java 11.
- The source can be filtered on files, e.g. using a check for file name extensions.
- The source can be filtered on directories/folder, that should not be traversed, e.g. hidden directories, starting with a dot (.). 
- There is a flat copy mode, where the source files are copied directly to the target directory without re-building the source directory tree.

## Usage

### Maven dependency

```xml
    <dependency>
      <groupId>com.giraone.io</groupId>
      <artifactId>file-tree-copier</artifactId>
      <version>1.0.1</version>
    </dependency>
```

### Copy from a web server

```java
public class HowToCopy
{
    public void copy() {
        
        URL url = new URL("https://my-nginx-server/public/");
        WebServerFileTreeProvider source = new WebServerFileTreeProvider(url);
        // Here: optional filter to suppress traversal of hidden directories
        source.withTraverseFilter(sourceDirectory -> !sourceDirectory.getName().startsWith("."));
        // Here: optional filter CSS files
        source.withFileFilter(sourceFile -> sourceFile.getName().endsWith(".css"));
        
        FileTreeCopier<WebServerFile> fileTreeCopier = new FileTreeCopier<>();
        fileTreeCopier.withFileTreeProvider(source);
        File target = new File("/tmp/download");
        fileTreeCopier.withTargetDirectory(target);

        int numberOfFilesCopied = fileTreeCopier.copy().getFilesCopied();
    }
}
```

### Copy from a resource folder

```java
public class HowToCopy
{
    public void copy() {
        
        ClassPathFileTreeProvider source = new ClassPathFileTreeProvider("classpath:fonts/");
        // Here: optional filter for True Type Font files
        source.withFileFilter(sourceFile -> sourceFile.getName().endsWith(".ttf"));
        FileTreeCopier<ClassPathResourceFile> fileTreeCopier = new FileTreeCopier<>();
        fileTreeCopier.withFileTreeProvider(source);
        fileTreeCopier.withTargetDirectory(fontsDirectory);
        // Flat copy mode
        fileTreeCopier.withFlatCopy();

        int numberOfFilesCopied = fileTreeCopier.copy().getFilesCopied();
    }
}
```

## Build

The project uses "normal" unit tests, with [mock-server](https://www.mock-server.com/) and also
*integration tests* with [TestContainers](https://www.testcontainers.org/) ("Real" *nginx* via *Docker* or *Podman*). 

```script
mvn package
```

If there is no *Docker* or *Podman* for [TestContainers](https://www.testcontainers.org/):

```script
mvn -DskipITs package
```

### Test containers and docker/podman

The directory [nginx-docker](nginx-docker) contains a [docker-compose.yml](nginx-docker/docker-compose.yml) file
to start a *NGINX* server with *autoindex* in JSON format enabled and serving the test file tree
from [src/test/resources/test-data](src/test/resources/test-data).

The docker-compose.yml is using the same [src/test/resources/nginx.conf](src/test/resources/nginx.conf)
used within the integration test based on [testcontainers.org's NGINX module](https://www.testcontainers.org/modules/nginx/).
The docker-compose.yml file can be used also with a *WebServerFileTreeProvider*, to show the working component.

## Release Notes

- V1.0.1 (2022-11-02)
  - Refactoring (getChildren to getChildrenNodesAsStream)
  - Bugfix and tests for handling directories with no data
  - Upgrade of `com.fasterxml.jackson.core:jackson-databind`
- V1.0.0 (2022-10-25)
  - Initial version