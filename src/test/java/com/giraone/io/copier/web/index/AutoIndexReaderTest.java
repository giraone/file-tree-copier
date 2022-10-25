package com.giraone.io.copier.web.index;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AutoIndexReaderTest {

    @Test
    void read() throws IOException {

        // arrange
        AutoIndexReader cut = new AutoIndexReader();
        String json = "[{\"name\":\"folder1\",\"type\":\"directory\"},{\"name\":\"file1.txt\",\"type\":\"file\"}]";
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        // act
        List<AutoIndexItem> items = cut.read(in);
        // assert
        assertThat(items).isNotNull();
        assertThat(items).hasSize(2);
        assertThat(items.get(0)).isNotNull();
        assertThat(items.get(0).getName()).isEqualTo("folder1");
        assertThat(items.get(0).getType()).isEqualTo(AutoIndexItemType.directory);
        assertThat(items.get(1)).isNotNull();
        assertThat(items.get(1).getName()).isEqualTo("file1.txt");
        assertThat(items.get(1).getType()).isEqualTo(AutoIndexItemType.file);
    }
}