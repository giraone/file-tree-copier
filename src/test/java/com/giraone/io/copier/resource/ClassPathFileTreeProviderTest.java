package com.giraone.io.copier.resource;

import com.giraone.io.copier.AbstractSourceFile;
import com.giraone.io.copier.SourceFile;
import com.giraone.io.copier.model.FileTree;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ClassPathFileTreeProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathFileTreeProviderTest.class);

    @ParameterizedTest
    @CsvSource(value = {
        "resource-root/start,resource-root/start/file.txt,file.txt",
        "resource-root/start/,resource-root/start/file.txt,file.txt",
        "resource-root,resource-root/start/file.txt,start/file.txt",
    })
    void calculateRelativeTargetFilePath(String resourceRoot, String resourcePath, String expected) {

        // arrange
        ClassPathFileTreeProvider fileTreeProvider = new ClassPathFileTreeProvider(resourceRoot);
        String name = AbstractSourceFile.lastPart(resourcePath);
        ClassPathResourceFile fileTreeNode = new ClassPathResourceFile(resourcePath, name, false);
        // act
        String relativeTargetFilePath = fileTreeProvider.calculateRelativeTargetFilePath(fileTreeNode);
        // assert
        assertThat(relativeTargetFilePath).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "classpath:test-data/tree1/,3",
        "classpath:test-data/tree1/dir1/,4",
    })
    void provideTree(String resourceRoot, int expectedChildrenLevel1) {

        // arrange
        ClassPathFileTreeProvider fileTreeProvider = new ClassPathFileTreeProvider(resourceRoot);
        // act
        FileTree<ClassPathResourceFile> relativeTargetFilePath = fileTreeProvider.provideTree();
        // assert
        assertThat(relativeTargetFilePath).isNotNull();
        List<FileTree.FileTreeNode<ClassPathResourceFile>> list = relativeTargetFilePath.getChildren().collect(Collectors.toList());
        assertThat(list).hasSize(expectedChildrenLevel1);
    }

    @ParameterizedTest
    @CsvSource(value = {
        ",^file1.*,5",
        "^dir2.*,,2",
        "^dir1$|^dir12$,^file11.*,1",
        "^dir1$|^dir12$,^file111.txt,0",
    })
    void provideTreeWithFilter(String dirContains, String fileContains, int expectedNumberOfFiles) {

        // arrange
        String resourceRoot = "classpath:test-data/tree1/";
        ClassPathFileTreeProvider fileTreeProvider = new ClassPathFileTreeProvider(resourceRoot);
        // Hint: files in root are not filtered via this matches!
        if (dirContains != null) {
            Function<SourceFile, Boolean> traverseFilterFct = sourceFile -> {
                boolean ret = sourceFile.getName().matches(dirContains);
                LOGGER.debug("Applied traverseFilter for {} returns {}", sourceFile, ret);
                return ret;
            };
            fileTreeProvider.withTraverseFilter(traverseFilterFct);
        }
        if (fileContains != null) {
            Function<SourceFile, Boolean> fileFilterFct = sourceFile -> {
                boolean ret = sourceFile.getName().matches(fileContains);
                LOGGER.debug("Applied fileFilter for {} returns {}", sourceFile, ret);
                return ret;
            };
            fileTreeProvider.withFileFilter(fileFilterFct);
        }
        // act
        FileTree<ClassPathResourceFile> relativeTargetFilePath = fileTreeProvider.provideTree();
        // assert
        assertThat(relativeTargetFilePath).isNotNull();
        List<FileTree.FileTreeNode<ClassPathResourceFile>> list = relativeTargetFilePath.getRecursiveFileList();
        assertThat(list).hasSize(expectedNumberOfFiles);
    }
}