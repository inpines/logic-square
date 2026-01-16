package org.dotspace.oofp.utils.file;

import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NioFileUtilsTest {

    @TempDir
    Path tempDir;

    private Path testFolder;

    @BeforeEach
    void setUp() throws IOException {
        testFolder = tempDir.resolve("testFolder");
        Files.createDirectories(testFolder);
    }

    @Test
    void resolvePath_withValidPathAndFilename_shouldReturnValidPath() {
        String filename = "test.txt";
        Validation<Violations, Path> result = NioFileUtils.resolvePath(testFolder, filename);
        
        assertTrue(result.isValid());
        assertEquals(testFolder.resolve(filename).normalize(), result.get().orElse(null));
    }

    @Test
    void resolvePath_withValidPathAndNullFilename_shouldReturnFolderPath() {
        Validation<Violations, Path> result = NioFileUtils.resolvePath(testFolder, null);
        
        assertTrue(result.isValid());
        assertEquals(testFolder.toAbsolutePath().normalize(), result.get().orElse(null));
    }

    @Test
    void validatePath_withValidAbsolutePath_shouldReturnValid() {
        Validation<Violations, Path> result = NioFileUtils.validatePath(testFolder);
        
        assertTrue(result.isValid());
        assertTrue(result.get().map(Path::isAbsolute).orElse(false));
    }

    @Test
    void validatePath_withNullPath_shouldReturnInvalid() {
        Validation<Violations, Path> result = NioFileUtils.validatePath(null);
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::toString)
                .map(s -> s.contains("folderPath is blank or contains null character"))
                .orElse(false));
    }

    @Test
    void validatePath_withPathContainingNullCharacter_shouldThrowException() {
        assertThrows(java.nio.file.InvalidPathException.class, () -> Paths.get("test\0path"));
    }

    @Test
    void validatePath_withRelativePath_shouldReturnInvalid() {
        Path relativePath = Paths.get("relative/path");
        Validation<Violations, Path> result = NioFileUtils.validatePath(relativePath);
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::toString)
                .map(s -> s.contains("folderPath must be absolute"))
                .orElse(false));
    }

    @Test
    void resolvePath_withBaseSubAndFilename_shouldReturnValidFilePath() {
        String baseFolder = testFolder.toString();
        String subPath = "sub/folder";
        String filename = "test.txt";
        
        Validation<Violations, Path> result = NioFileUtils.resolvePath(baseFolder, subPath, filename);
        
        assertTrue(result.isValid());
        Path expected = testFolder.resolve(subPath).resolve(filename).normalize();
        assertEquals(expected, result.get().orElse(null));
    }

    @Test
    void resolvePath_withBlankBaseFolder_shouldReturnInvalid() {
        Validation<Violations, Path> result = NioFileUtils.resolvePath(
                "", "sub", "file.txt");
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::toString)
                .map(s -> s.contains("baseFolder is blank or contains null character"))
                .orElse(false));
    }

    @Test
    void resolvePath_withAbsoluteSubPath_shouldReturnInvalid() {
        String baseFolder = testFolder.toString();
        String absoluteSubPath = tempDir.resolve("absolute").toString();
        
        Validation<Violations, Path> result = NioFileUtils.resolvePath(baseFolder, absoluteSubPath, "file.txt");
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::toString)
                .map(s -> s.contains("Invalid subPath"))
                .orElse(false));
    }

    @Test
    void resolvePath_withPathTraversalInSubPath_shouldReturnInvalid() {
        String baseFolder = testFolder.toString();
        String maliciousSubPath = "../../../etc";
        
        Validation<Violations, Path> result = NioFileUtils.resolvePath(baseFolder, maliciousSubPath, "passwd");
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::toString)
                .map(s -> s.contains("Resolved path escapes base directory"))
                .orElse(false));
    }

    @Test
    void resolvePath_withInvalidFilename_shouldReturnInvalid() {
        String filename = "file/with/slash.txt";
        Validation<Violations, Path> result = NioFileUtils.resolvePath(testFolder, filename);
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::toString)
                .map(s -> s.contains("Invalid filename"))
                .orElse(false));
    }

    @Test
    void resolvePath_withDotFilename_shouldReturnInvalid() {
        Validation<Violations, Path> result = NioFileUtils.resolvePath(testFolder, ".");
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::toString)
                .map(s -> s.contains("filename is blank, contains null character, or is '.' or '..'"))
                .orElse(false));
    }

    @Test
    void resolvePath_withDotDotFilename_shouldReturnInvalid() {
        Validation<Violations, Path> result = NioFileUtils.resolvePath(testFolder, "..");
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::toString)
                .map(s -> s.contains("filename is blank, contains null character, or is '.' or '..'"))
                .orElse(false));
    }

    @Test
    void validatePath_createsDirectoryIfNotExists() {
        Path nonExistentPath = tempDir.resolve("newFolder");
        assertFalse(Files.exists(nonExistentPath));
        
        Validation<Violations, Path> result = NioFileUtils.validatePath(nonExistentPath);
        
        assertTrue(result.isValid());
        assertTrue(Files.exists(nonExistentPath));
        assertTrue(Files.isDirectory(nonExistentPath));
    }

    @Test
    void validateFile_withValidFilePath_shouldReturnValidFile() throws IOException {
        Path filePath = testFolder.resolve("test/validFile.txt");
        Files.createDirectories(testFolder.resolve("test"));
        Files.createFile(filePath);

        Validation<Violations, File> result = NioFileUtils.validateFile(
                testFolder.toString(), "test", "validFile.txt");

        assertTrue(result.isValid());
        assertEquals(filePath.toFile(), result.get().orElse(null));
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    void validateFile_shouldReturnInvalid(String finename, String expectedErrorMessage) {
        Validation<Violations, File> result = NioFileUtils.validateFile(testFolder.toString(), "test", finename);

        assertTrue(result.isInvalid());
        assertTrue(result.error().map(Violations::collectMessages)
                .map(s -> s.contains(expectedErrorMessage))
                .orElse(false));
    }

    static Stream<Arguments> provideArguments() {
        return Stream.of(
                Arguments.of("nonExistentFile.txt", "File does not exist or is not a file"),
                Arguments.of("testFolder", "File does not exist or is not a file"),
                Arguments.of("invalid/file.txt", "Invalid filename"),
                Arguments.of("../etc/passwd", "Invalid filename: ../etc/passwd"),
                Arguments.of("", "filename is blank, contains null character, or is '.' or '..'"),
                Arguments.of(".", "filename is blank, contains null character, or is '.' or '..'"),
                Arguments.of("..", "filename is blank, contains null character, or is '.' or '..'")
        );
    }

}