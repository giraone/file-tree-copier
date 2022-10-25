[![GitHub license](https://img.shields.io/github/license/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-1.0.3-blue)](https://mvnrepository.com/artifact/com.giraone.imaging/file-tree-copier)
[![GitHub issues](https://img.shields.io/github/issues/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/issues)
[![GitHub stars](https://img.shields.io/github/stars/giraone/file-tree-copier)](https://github.com/giraone/file-tree-copier/stargazers)
[![Platform](https://img.shields.io/badge/platform-jre11%2B-blue)](https://github.com/giraone/file-tree-copier/pom.xml)

# File Tree Copier

Utility JAR for copying a file tree from web server or from classpath resource to a (local) file system.

## Usage

```java
public class HowToCopy
{
    public void copy() {
        
        URL url = new URL("https://my-nginx-server/public/");
        WebServerFileTreeProvider source = new WebServerFileTreeProvider(url);

        FileTreeCopier<WebServerFile> fileTreeCopier = new FileTreeCopier<>();
        fileTreeCopier.withFileTreeProvider(source);
        File target = new File("/tmp/download");
        fileTreeCopier.withTargetDirectory(target);

        int copied = fileTreeCopier.copy();
    }
}
```
## Build

## Release Notes

- V1.0.0 (2022-10-25)
  - Initial version