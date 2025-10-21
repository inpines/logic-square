package org.dotspace.oofp.support.sequence;

import org.dotspace.oofp.support.expression.ExpressionEvaluation;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.msg.MessageSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequenceGenerationTest {

    @Mock
    private ExpressionEvaluations expressionEvaluations;
    
    @Mock
    private MessageSupport messageSupport;

    @TempDir
    Path tempDir;
    
    private SequenceGeneration sequenceGeneration;

    private SequenceGeneration getSequenceGeneration(String folderPathRoot) {
        SequenceGenerationOptions options = SequenceGenerationOptions.builder()
                .withBasePath("test")
                .withSenderId("sender")
                .withTypeId("type")
                .withPatternOfFormat("${options.senderId}${options.typeId}#{value}")
                .build();

        return new SequenceGeneration(
                expressionEvaluations,
                messageSupport,
                folderPathRoot,
                "sequences",
                options
        );
    }

    private SequenceGeneration getSequenceGenerationWhichExpressionForNextSeqX2(String folderPathRoot) {
        SequenceGenerationOptions options = SequenceGenerationOptions.builder()
                .withBasePath("test")
                .withSenderId("sender")
                .withTypeId("type")
                .withExpression("nextSeq * 2")
                .withPatternOfFormat("#{value}")
                .build();

        return new SequenceGeneration(
                expressionEvaluations,
                messageSupport,
                folderPathRoot,
                "sequences",
                options
        );
    }

    private SequenceGeneration getSequenceGenerationWithEmptyOptions(String folderPathRoot) {
        return new SequenceGeneration(
                expressionEvaluations,
                messageSupport,
                folderPathRoot,
                "sequences",
                SequenceGenerationOptions.builder()
                        .build()
        );
    }

    @Test
    void next_shouldGenerateSequenceSuccessfully() {
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("sender-type-001");

        sequenceGeneration = getSequenceGeneration(tempDir.toString());
        String result = sequenceGeneration.next();

        assertNotNull(result);
        assertEquals("sender-type-001", result);
        verify(messageSupport).getMessageUsingProperties(
                eq("${options.senderId}${options.typeId}#{value}"),
                any(SequenceGenerationContext.class),
                argThat(map -> map.containsKey("value"))
        );
    }

    @Test
    void next_shouldThrowExceptionWhenApiDataPathIsNull() {
        sequenceGeneration = getSequenceGeneration(null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> sequenceGeneration.next());
        assertEquals("folder.path.root=null, sequence.sequence-path=sequences", exception.getMessage());
    }

    @Test
    void next_shouldUseExpressionWhenProvided() {
        ExpressionEvaluation mockEval = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.parse("nextSeq * 2")).thenReturn(mockEval);
        when(mockEval.getValue(any())).thenReturn("002");
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("002");

        sequenceGeneration = getSequenceGenerationWhichExpressionForNextSeqX2(tempDir.toString());
        String result = sequenceGeneration.next();

        assertNotNull(result);
        verify(expressionEvaluations).parse("nextSeq * 2");
        verify(messageSupport).getMessageUsingProperties(
                eq("#{value}"),
                any(),
                argThat(map -> "002".equals(map.get("value")))
        );
    }

    @Test
    void next_shouldCreateDirectoriesWhenTheyDontExist() {
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("result");

        sequenceGeneration = getSequenceGeneration(tempDir.toString());
        sequenceGeneration.next();

        Path expectedPath = tempDir.resolve("sequences/testsendertype");
        assertTrue(Files.exists(expectedPath));
        assertTrue(Files.isDirectory(expectedPath));
    }

    @Test
    void next_shouldIncrementSequenceNumber() throws IOException {
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("result1", "result2");

        Path seqDir = tempDir.resolve("sequences/testsendertype");
        Files.createDirectories(seqDir);
        Files.createFile(seqDir.resolve("1"));

        sequenceGeneration = getSequenceGeneration(tempDir.toString());
        String result1 = sequenceGeneration.next();
        String result2 = sequenceGeneration.next();

        assertNotNull(result1);
        assertNotNull(result2);
        verify(messageSupport, times(2)).getMessageUsingProperties(anyString(), any(), any());
    }

    @Test
    void next_shouldHandleEmptyDirectory() {
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("result");

        sequenceGeneration = getSequenceGeneration(tempDir.toString());
        String result = sequenceGeneration.next();

        assertNotNull(result);
        verify(messageSupport).getMessageUsingProperties(
                anyString(),
                any(),
                argThat(map -> "001".equals(map.get("value")))
        );
    }

    @Test
    void next_shouldHandleNullOptions() {
        when(messageSupport.getMessageUsingProperties(any(), any(), any()))
                .thenReturn("result");

        sequenceGeneration = getSequenceGenerationWithEmptyOptions(tempDir.toString());
        String result = sequenceGeneration.next();

        assertNotNull(result);
    }

    @Test
    void next_shouldUseDefaultPaddingWhenNoExpression() {
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("result");

        sequenceGeneration = getSequenceGeneration(tempDir.toString());
        sequenceGeneration.next();

        verify(messageSupport).getMessageUsingProperties(
                anyString(),
                any(),
                argThat(map -> "001".equals(map.get("value")))
        );
    }
}