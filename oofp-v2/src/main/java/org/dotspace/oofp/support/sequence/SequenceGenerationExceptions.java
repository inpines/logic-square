package org.dotspace.oofp.support.sequence;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class SequenceGenerationExceptions {

    public RuntimeException generateResolveSequencePathException(String folderPathRoot, String sequencePath) {
        return new RuntimeException(String.format("folder.path.root=%s, sequence.sequence-path=%s",
                folderPathRoot, sequencePath));
    }

    public RuntimeException generateNextSequenceNotPresentException(AtomicInteger nextSeq) {
        return new RuntimeException(String.format("nextSeq = %s", nextSeq));
    }

    public RuntimeException generateCreatingDirectoryException(Path root, String path) {
        return new RuntimeException(String.format("getNextSequenceNumber(..) fail!! while root:%s, path:%s",
                root, path));
    }
}
