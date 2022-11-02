package com.giraone.io.copier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CopierResultTest {

    @Test
    void assertCalculationWorks() throws InterruptedException {

        CopierResult copierResult = new CopierResult();
        Thread.sleep(1L);
        copierResult.finishedProvide();
        copierResult.directoryCreated();
        copierResult.directoryCreated();
        copierResult.fileCopied(123L);
        copierResult.fileCopied(234L);
        copierResult.fileCopied(1000L);
        Thread.sleep(1L);
        copierResult.finishedCopy();
        // assert
        assertThat(copierResult.getDirectoriesCreated()).isEqualTo(2);
        assertThat(copierResult.getFilesCopied()).isEqualTo(3);
        assertThat(copierResult.getBytesCopied()).isEqualTo(1357L);
        assertThat(copierResult.getProvideTimeMillis()).isGreaterThan(0L);
        assertThat(copierResult.getCopyTimeMillis()).isGreaterThan(0L);
        assertThat(copierResult.getTotalTimeMillis()).isEqualTo(copierResult.getProvideTimeMillis() + copierResult.getCopyTimeMillis());
    }
}