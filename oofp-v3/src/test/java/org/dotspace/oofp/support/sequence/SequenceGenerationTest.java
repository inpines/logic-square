package org.dotspace.oofp.support.sequence;

import org.dotspace.oofp.support.expression.ExpressionEvaluation;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.msg.MessageSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequenceGenerationTest {

    @Mock
    private ExpressionEvaluations expressionEvaluations;
    
    @Mock
    private MessageSupport messageSupport;
    
    private String apiDataPathRoot;
    
    @TempDir
    Path tempDir;
    
    private SequenceGeneration sequenceGeneration;

    private final Function<Object[], RuntimeException> exceptionProvider = args -> {
        if (null == args || args.length == 0) {
            return new IllegalStateException("Sequence generation error");
        }
        if (args.length > 1) {
            return new IllegalStateException((String) args[0], (Throwable) args[1]);
        }
        return new IllegalStateException((String) args[0]);
    };

    @BeforeEach
    void setUp() {
        SequenceGenerationOptions options = SequenceGenerationOptions.builder()
                .withBasePath("test")
                .withSenderId("sender")
                .withTypeId("type")
                .withPatternOfFormat("${options.senderId}${options.typeId}#{value}")
                .build();

        apiDataPathRoot = tempDir.toString();
        sequenceGeneration = new SequenceGeneration(
                expressionEvaluations,
                messageSupport,
                apiDataPathRoot,
                "sequences",
                options
        );
    }

    @Test
    void next_shouldGenerateSequenceSuccessfully() {
        apiDataPathRoot = tempDir.toString();
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("sender-type-001");

        String result = sequenceGeneration.next(exceptionProvider);

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
        SequenceGenerationOptions options = SequenceGenerationOptions.builder()
                .withBasePath("test")
                .withSenderId("sender")
                .withTypeId("type")
                .withPatternOfFormat("${options.senderId}${options.typeId}#{value}")
                .build();

        sequenceGeneration = new SequenceGeneration(
                expressionEvaluations,
                messageSupport,
                null,
                "sequences",
                options
        );
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sequenceGeneration.next(
                exceptionProvider));
        assertEquals("acs-path-root.api-data-path=null, sequence.sequence-path=sequences",
                exception.getMessage());
    }

    @Test
    void next_shouldUseExpressionWhenProvided() {
        SequenceGenerationOptions optionsWithExpression = SequenceGenerationOptions.builder()
                .withBasePath("test")
                .withExpression("nextSeq * 2")
                .withPatternOfFormat("#{value}")
                .build();

        apiDataPathRoot = tempDir.toString();
        sequenceGeneration = new SequenceGeneration(
                expressionEvaluations,
                messageSupport,
                apiDataPathRoot,
                "sequences",
                optionsWithExpression
        );

        ExpressionEvaluation mockEval = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("nextSeq * 2")).thenReturn(mockEval);
        when(mockEval.getValue(any())).thenReturn("002");
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("002");

        String result = sequenceGeneration.next(exceptionProvider);

        assertNotNull(result);
        verify(expressionEvaluations).evaluate("nextSeq * 2");
        verify(messageSupport).getMessageUsingProperties(
                eq("#{value}"),
                any(),
                argThat(map -> "002".equals(map.get("value")))
        );
    }

    @Test
    void next_shouldCreateDirectoriesWhenTheyDontExist() {
       apiDataPathRoot = tempDir.toString();
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("result");

        sequenceGeneration.next(exceptionProvider);

        Path expectedPath = tempDir.resolve("sequences/testsendertype");
        assertTrue(Files.exists(expectedPath));
        assertTrue(Files.isDirectory(expectedPath));
    }

    @Test
    void next_shouldIncrementSequenceNumber() throws IOException {
        apiDataPathRoot = tempDir.toString();
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("result1", "result2");

        Path seqDir = tempDir.resolve("sequences/testsendertype");
        Files.createDirectories(seqDir);
        Files.createFile(seqDir.resolve("1"));

        String result1 = sequenceGeneration.next(exceptionProvider);
        String result2 = sequenceGeneration.next(exceptionProvider);

        assertNotNull(result1);
        assertNotNull(result2);
        verify(messageSupport, times(2)).getMessageUsingProperties(anyString(), any(), any());
    }

    @Test
    void next_shouldHandleEmptyDirectory() {
        apiDataPathRoot = tempDir.toString();
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("result");

        String result = sequenceGeneration.next(exceptionProvider);

        assertNotNull(result);
        verify(messageSupport).getMessageUsingProperties(
                anyString(),
                any(),
                argThat(map -> "001".equals(map.get("value")))
        );
    }

    @Test
    void next_shouldHandleNullOptions() {
        SequenceGenerationOptions nullOptions = SequenceGenerationOptions.builder()
                .build();
        apiDataPathRoot = tempDir.toString();
        sequenceGeneration = new SequenceGeneration(
                expressionEvaluations,
                messageSupport,
                apiDataPathRoot,
                "sequences",
                nullOptions
        );

        when(messageSupport.getMessageUsingProperties(any(), any(), any()))
                .thenReturn("result");

        String result = sequenceGeneration.next(exceptionProvider);

        assertNotNull(result);
    }

    @Test
    void next_shouldUseDefaultPaddingWhenNoExpression() {
        apiDataPathRoot = tempDir.toString();
        when(messageSupport.getMessageUsingProperties(anyString(), any(), any()))
                .thenReturn("result");

        sequenceGeneration.next(exceptionProvider);

        verify(messageSupport).getMessageUsingProperties(
                anyString(),
                any(),
                argThat(map -> "001".equals(map.get("value")))
        );
    }
}