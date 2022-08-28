package unluac;

import unluac.assemble.Assembler;
import unluac.assemble.AssemblerException;
import unluac.decompile.Decompiler;
import unluac.decompile.Disassembler;
import unluac.decompile.Output;
import unluac.decompile.OutputProvider;
import unluac.entity.AlbFile;
import unluac.gui.Gui;
import unluac.parse.BHeader;
import unluac.parse.LuaFunction;
import unluac.util.FileUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static unluac.Configuration.Mode.ASSEMBLE;
import static unluac.Configuration.Mode.DISASSEMBLE;

public class Main {

    //private static final String[] argsArray = {"--output", "--rawstring", "--disassemble", "--assemble", "--opmap", "--nodebug"};
    private static final String[] argsArray = {"--output"};
    public static String version = "1.2.3.491";
    public static String revision = "ArcheAge (ArcheWorld) x32/x64";

    public static void main(String[] args) {
        boolean isGuiMod = isGuiMod(args);
        selectAppMod(args, isGuiMod);
    }

    private static boolean isGuiMod(String[] args) {
        return 0 == args.length;
    }

    private static void selectAppMod(String[] args, boolean isGuiMod) {
        if (isGuiMod) {
            new Gui(argsArray);
        } else {
            Configuration config = new Configuration();
            consoleMod(args, config, true);
        }
    }

    public static void analyseAlbFiles(List<AlbFile> albFileList, Configuration config) {
        String[] args;
        for (AlbFile albFile : albFileList) {
            args = new String[]{albFile.getCompiledPath(), albFile.getArgument(), albFile.isOverwrite() ? albFile.getCompiledPath() : albFile.getDecompiledPath()};
            consoleMod(args, config, false);
        }
        if (config.autoClose) {
            System.exit(0);
        }
    }

    private static void consoleMod(String[] args, Configuration config, boolean endApplication) {
        String inputFileName = checkArgumentsAndGetInputFileName(args, config);
        switch (config.mode) {
            case DECOMPILE: {
                decompile(inputFileName, config);
                break;
            }
            case DISASSEMBLE: {
                disassemble(inputFileName, config);
                break;
            }
            case ASSEMBLE: {
                assemble(inputFileName, config);
                break;
            }
            default:
                throw new IllegalStateException();
        }
        System.out.println(inputFileName);
        if (endApplication) {
            System.exit(0);
        }
    }

    private static String checkArgumentsAndGetInputFileName(String[] args, Configuration config) {
        String fileName = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                switch (arg) {
                    case "--rawstring":
                        config.rawstring = true;
                        break;
                    case "--nodebug":
                        config.variable = Configuration.VariableMode.NODEBUG;
                        break;
                    case "--disassemble":
                        config.mode = DISASSEMBLE;
                        break;
                    case "--assemble":
                        config.mode = ASSEMBLE;
                        break;
                    case "--output":
                    case "-o":
                        if (i + 1 < args.length) {
                            config.output = args[i + 1];
                            i++;
                        } else {
                            error("option \"" + arg + "\" doesn't have an argument", true);
                        }
                        break;
                    case "--opmap":
                        if (i + 1 < args.length) {
                            config.opMap = args[i + 1];
                            i++;
                        } else {
                            error("option \"" + arg + "\" doesn't have an argument", true);
                        }
                        break;
                    default:
                        error("unrecognized option: " + arg, true);
                        break;
                }
            } else if (null == fileName) {
                fileName = arg;
            } else {
                error("too many arguments: " + arg, true);
            }
        }
        if (null == fileName) {
            error("no input file provided", true);
        }
        return fileName;
    }

    private static LuaFunction createLFunction(String inputFileName, Configuration config) {
        LuaFunction luaMain = null;
        try {
            luaMain = fileToFunction(inputFileName, config);
        } catch (IOException e) {
            error(e.getMessage(), false);
        }
        return luaMain;
    }

    public static void error(String err, boolean usage) {
        System.err.println("unluac v" + version);
        System.err.println("revision: " + revision);
        System.err.print("  error: ");
        System.err.println(err);
        if (usage) {
            System.err.println("  usage: java -jar unluac.jar [options] <file>");
        }
        System.exit(1);
    }

    private static LuaFunction fileToFunction(String fn, Configuration config) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(fn, "r")) {
            ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int len = (int) file.length();
            FileChannel in = file.getChannel();
            while (len > 0) len -= in.read(buffer);
            buffer.rewind();
            BHeader header = new BHeader(buffer, config);
            return header.main;
        }
    }

    private static void decompile(String inputFileName, Configuration config) {
        LuaFunction luaMain = createLFunction(inputFileName, config);
        Decompiler d = new Decompiler(luaMain);
        Decompiler.State result = d.decompile();
        d.print(result, config.getOutput());
    }

    public static void decompile(String in, String out, Configuration config) throws IOException {
        LuaFunction lmain = fileToFunction(in, config);
        Decompiler d = new Decompiler(lmain);
        Decompiler.State result = d.decompile();
        final PrintStream pout = new PrintStream(out);
        d.print(result, new Output(new OutputProvider() {

            @Override
            public void print(String s) {
                pout.print(s);
            }

            @Override
            public void print(byte b) {
                pout.write(b);
            }

            @Override
            public void println() {
                pout.println();
            }

        }));
        pout.flush();
        pout.close();
    }

    private static void disassemble(String inputFileName, Configuration config) {
        LuaFunction luaMain = createLFunction(inputFileName, config);
        Disassembler d = new Disassembler(luaMain);
        d.disassemble(config.getOutput());
    }

    public static void disassemble(String in, String out) throws IOException {
        LuaFunction lmain = fileToFunction(in, new Configuration());
        Disassembler d = new Disassembler(lmain);
        final PrintStream pout = new PrintStream(out);
        d.disassemble(new Output(new OutputProvider() {

            @Override
            public void print(String s) {
                pout.print(s);
            }

            @Override
            public void print(byte b) {
                pout.print(b);
            }

            @Override
            public void println() {
                pout.println();
            }

        }));
        pout.flush();
        pout.close();
    }

    public static void assemble(String inputFileName, Configuration config) {
        if (config.output == null) {
            error("assembler mode requires an output file", true);
        } else {
            try {
                Assembler a = new Assembler(
                        FileUtils.createSmartTextFileReader(new File(inputFileName)),
                        Files.newOutputStream(Paths.get(config.output))
                );
                a.assemble();
            } catch (IOException | AssemblerException e) {
                error(e.getMessage(), false);
            }
        }
    }

    public static void assemble(String in, String out) throws IOException, AssemblerException {
        OutputStream outstream = new BufferedOutputStream(Files.newOutputStream(new File(out).toPath()));
        Assembler a = new Assembler(FileUtils.createSmartTextFileReader(new File(in)), outstream);
        a.assemble();
        outstream.flush();
        outstream.close();
    }

}
