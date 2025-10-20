package org.dotspace.oofp.utils.file;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.joinable.Violations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

@UtilityClass
public class NioFileUtils {

    public Validation<Violations, Path> resolvePath(Path folderPath, String filename) {
        // Mitigation per OWASP/CWE Path Traversal:
        // 1) Fix base dir to a real (canonical) path (no symlinks).
        // 2) Resolve user input under base and normalize.
        // 3) Ensure the normalized path still starts with base.
        Validation<Violations, Path> pathValidation = validatePath(folderPath);

        return Maybe.given(filename)
                .map(fn -> resolveFilePath(fn, pathValidation))
                .orElse(pathValidation);
    }

    public Validation<Violations, Path> validatePath(Path folderPath) {
        return Maybe.given(folderPath)
                .filter(path -> isValidatedPath(path.toString()))
                .toValidation(Violations.violate("folderPath",
                        "folderPath is blank or contains null character"))
                .filter(Path::isAbsolute, Violations.violate("folderPath validation",
                        "folderPath must be absolute"))
                .flatMap(NioFileUtils::createDirectoryIfNotPresent)
                .flatMap(path -> {
                    try {
                        return Validation.valid(path.toRealPath(NOFOLLOW_LINKS));
                    } catch (IOException e) {
                        return Validation.invalid(Violations.violate("toRealPath",
                                String.format("Failed to resolve folder path: %s => throws %s", folderPath, e)));
                    }
                })
                ;
    }

    private boolean isValidatedPath(String pathname) {
        return Maybe.just(pathname)
                .filter(StringUtils::isNotBlank) // 檢查是否為空
                .filter(s -> !s.contains("\0")) // 檢查是否包含 null 字元
                .isPresent();
    }

    /**
     * 安全解析路徑：固定 baseDir 為安全根，將 subPath（必填）與 filename（選填）組成最終路徑。
     * - filename 為 null/blank 時：回傳目錄 Path
     * - 否則：回傳檔案 Path
     */
    public Validation<Violations, Path> resolvePath(String baseFolder, String subPath, String filename) {
        // Mitigation per OWASP/CWE Path Traversal:
        // 1) Fix base dir to a real (canonical) path (no symlinks).
        // 2) Resolve user input under base and normalize.
        // 3) Ensure the normalized path still starts with base.
        // Ref: PortSwigger path traversal prevention; CWE-22/23.
        Validation<Violations, Path> folderPath = Maybe.given(baseFolder)
                .filter(NioFileUtils::isValidatedPath)
                .toValidation(Violations.violate("baseFolder",
                        "baseFolder is blank or contains null character"))
                .flatMap(base -> resolveFolderPath(subPath, base))
                .flatMap(NioFileUtils::createDirectoryIfNotPresent);

        // 驗證 filename：僅允許白名單字元
        return Maybe.given(filename)
                .map(fn -> resolveFilePath(fn, folderPath))
                .orElse(folderPath);
    }

    private Validation<Violations, Path> createDirectoryIfNotPresent(Path path) {
        // 檢查目錄是否存在，若不存在則建立
        if (!Files.exists(path)) {
            try {
                return Validation.valid(Files.createDirectories(path));
            } catch (IOException e) {
                return Validation.invalid(Violations.violate("createDirectories",
                        String.format("Failed to create directory: %s => throws %s",
                                path, e)));
            }
        }
        return Validation.valid(path);
    }

    private Validation<Violations, Path> resolveFilePath(
            String fn, Validation<Violations, Path> folderPath) {
        return folderPath.flatMap(path -> {
            if (!isValidatedPath(fn) || ".".equals(fn) || "..".equals(fn)) {
                // 檢查 filename 是否為空、包含 null 字元或是 "." 或
                return Validation.invalid(Violations.violate("filename",
                        "filename is blank, contains null character, or is '.' or '..'"));
            }
            // 驗證 filename：僅允許白名單字元
            if (fn.contains("/") || fn.contains("\\") || fn.contains("..")) {
                return Validation.invalid(Violations.violate("filename validation",
                        String.format("Invalid filename: %s", fn)));
            }
            Path filePath = path.resolve(fn).normalize();
            if (!filePath.startsWith(path)) {
                return Validation.invalid(Violations.violate("file path validation",
                        "File path escapes base directory"));
            }
            if (Files.isSymbolicLink(filePath)) {
                return Validation.invalid(Violations.violate("file path validation",
                        "File path must not be a symbolic link"));
            }
            return Validation.valid(filePath);
        });
    }

    private Validation<Violations, Path> resolveFolderPath(String subPath, String base) {
        if (StringUtils.isBlank(subPath) || subPath.contains("\0")) {
            // 檢查 subPath 是否為空或包含 null 字元
            return Validation.invalid(Violations.violate("subPath",
                    "subPath is blank or contains null character"));
        }
        // 驗證 subPath：僅允許相對子路徑與白名單字元
        if (Paths.get(subPath).isAbsolute()) {
            return Validation.invalid(Violations.violate(
                    "subPath validation", String.format("Invalid subPath: %s", subPath)));
        }

        Path basePath = Paths.get(base);
        return Validation.<Violations, Path>valid(basePath)
                .filter(Path::isAbsolute, Violations.violate("basePath validation",
                        "Base path must be absolute"))
                .flatMap(path -> {
                    try {
                        return Validation.valid(path.toRealPath(NOFOLLOW_LINKS));
                    } catch (IOException e) {
                        return Validation.invalid(Violations.violate("toRealPath",
                                String.format("Failed to resolve base path: %s => throws %s", base, e)));
                    }
                })
                .flatMap(safeBase -> {
                    Path path = safeBase.resolve(subPath).normalize();
                    if (!path.startsWith(safeBase)) {
                        return Validation.invalid(Violations.violate("path validation",
                                "Resolved path escapes base directory"));
                    }

                    return Validation.valid(path);
                });
    }

}
