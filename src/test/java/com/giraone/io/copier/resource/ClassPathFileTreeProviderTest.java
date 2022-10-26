package com.giraone.io.copier.resource;

import com.giraone.io.copier.AbstractSourceFile;
import com.giraone.io.copier.model.FileTree;
import com.giraone.io.copier.web.WebServerFile;
import com.giraone.io.copier.web.WebServerFileTreeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ClassPathFileTreeProviderTest {

    @ParameterizedTest
    @CsvSource(value = {
        "resource-root/start,resource-root/start/file.txt,file.txt",
        "resource-root/start/,resource-root/start/file.txt,file.txt",
        "resource-root,resource-root/start/file.txt,start/file.txt",
    })
    void calculateRelativeTargetFilePath(String resourceRoot, String resourcePath, String expected) throws MalformedURLException {

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
        "/test-data/tree1/,1",
    })
    void provideTree(String resourceRoot, int expectedChildrenLevel1) {

        // arrange
        ClassPathFileTreeProvider fileTreeProvider = new ClassPathFileTreeProvider(resourceRoot);
        // act
        FileTree<ClassPathResourceFile> relativeTargetFilePath = fileTreeProvider.provideTree();
        // assert
        assertThat(relativeTargetFilePath).isNotNull();
        List<FileTree.FileTreeNode<ClassPathResourceFile>> list = relativeTargetFilePath.traverse().collect(Collectors.toList());
        assertThat(list).hasSize(expectedChildrenLevel1);
    }
}