package com.giraone.io.copier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AbstractFileTreeProviderTest {

    @ParameterizedTest
    @CsvSource(value = {
        "C:/test-classes/testdata/file-util/subdir1,testdata/file-util/subdir1,''",
        "C:/test-classes/testdata/file-util/subdir1,testdata/file-util,subdir1/",
        "C:/test-classes/testdata/file-util/subdir1,testdata,file-util/subdir1/",
        "C:/test-classes/testdata/file-util/subdir1,xxx,",
        // be tolerant with trailing slashes
        "C:/test-classes/testdata/file-util/subdir1/,testdata/file-util,subdir1/",
        "C:/test-classes/testdata/file-util/subdir1,testdata/file-util/,subdir1/",
        "C:/test-classes/testdata/file-util/subdir1/,testdata/file-util/,subdir1/",
    })
    void extractNeededParentPath(String parentSourcePath, String resourceRoot, String expected) {

        String actual = AbstractFileTreeProvider.extractNeededParentPath(parentSourcePath, resourceRoot);
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "C:/test-classes/testdata/file-util/subdir1,subdir1/file.txt,file.txt",
        "C:/test-classes/testdata/file-util/subdir1,subdir1/subdir2/file.txt,subdir2/file.txt",
        "C:/test-classes/testdata/file-util/subdir1/,subdir1/file.txt,file.txt",
        "C:/test-classes/testdata/file-util/subdir1/,subdir1/subdir2/file.txt,subdir2/file.txt",
        "C:/test-classes/testdata/file-util/subdir1/,subdir1/subdir2,subdir2",
        "C:/test-classes/testdata/file-util/subdir1/,subdir1/subdir2/,subdir2",
        "C:/test-classes/testdata/file-util/subdir1,xxx,",
        "C:/test-classes/testdata/file-util/subdir1,/xxx,",
    })
    void extractNeededChildPath(String parentSourcePath, String resourceRoot, String expected) {

        String actual = AbstractFileTreeProvider.extractNeededChildPath(parentSourcePath, resourceRoot);
        assertThat(actual).isEqualTo(expected);
    }
}