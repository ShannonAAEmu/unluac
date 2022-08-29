package unluac.entity;

import java.io.File;
import java.util.Objects;

public class AlbFile {

    private final File file;
    private final String argument;
    private final boolean isOverwrite;
    private String compiledPath;
    private String decompiledPath;

    public AlbFile(File file, String argument, boolean isOverwrite) {
        this.file = file;
        this.argument = argument;
        this.isOverwrite = isOverwrite;
        setPaths();
    }

    private void setPaths() {
        setCompiledPath(file.getAbsolutePath());
        setDecompiledPath(file.getAbsolutePath().replaceAll(".alb$", ".lua"));
    }

    public File getFile() {
        return file;
    }

    public String getCompiledPath() {
        return compiledPath;
    }

    private void setCompiledPath(String compiledPath) {
        this.compiledPath = compiledPath;
    }

    public String getDecompiledPath() {
        return decompiledPath;
    }

    private void setDecompiledPath(String decompiledPath) {
        this.decompiledPath = decompiledPath;
    }

    public String getArgument() {
        return argument;
    }

    public boolean isOverwrite() {
        return isOverwrite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbFile albFile = (AlbFile) o;
        return isOverwrite == albFile.isOverwrite && Objects.equals(file, albFile.file)
                && Objects.equals(compiledPath, albFile.compiledPath)
                && Objects.equals(decompiledPath, albFile.decompiledPath)
                && Objects.equals(argument, albFile.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, compiledPath, decompiledPath, argument, isOverwrite);
    }

    @Override
    public String toString() {
        return "AlbFile{" +
                "file=" + file +
                ", compiledPath='" + compiledPath + '\'' +
                ", decompiledPath='" + decompiledPath + '\'' +
                ", argument='" + argument + '\'' +
                ", isOverwrite=" + isOverwrite +
                '}';
    }
}
