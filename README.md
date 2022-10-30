[![GitHub license](https://img.shields.io/github/license/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-1.0.3-blue)](https://mvnrepository.com/artifact/com.giraone.imaging/file-tree-copier)
[![GitHub issues](https://img.shields.io/github/issues/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/issues)
[![GitHub stars](https://img.shields.io/github/stars/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/stargazers)
[![Platform](https://img.shields.io/badge/platform-jre11%2B-blue)](https://github.com/giraone/file-tree-copier/pom.xml)

# File Tree Copier

Utility JAR for copying a file tree from web server or from classpath resource to a (local) file system.

## Features

- The source can be
  - a web server serving files and directory listings with JSON based indexes, e.g. `nginx` with `auto-index: on`.
  - a file tree within a resource folder of the running Java process, e.g. from a JAR
  - a file tree within the file system
- When using an HTTP source, the used HTTP client can be configured. The default implementation is the native HTTP client
  of Java 11.
- The source can be filtered, e.g. using a check for file name extensions.

## Usage

### Copy from a web server

```java
public class HowToCopy
{
    public void copy() {
        
        URL url = new URL("https://my-nginx-server/public/");
        WebServerFileTreeProvider source = new WebServerFileTreeProvider(url);
        // Here: optional filter hidden directories
        source.withDirectoryFilter(sourceDirectory -> !sourceDirectory.getName().startsWith("."));
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

        int numberOfFilesCopied = fileTreeCopier.copy().getFilesCopied();
    }
}
```

## Build

## Release Notes

- V1.0.0 (2022-10-25)
  - Initial version