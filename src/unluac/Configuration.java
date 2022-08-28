package unluac;

import unluac.decompile.FileOutputProvider;
import unluac.decompile.Output;

import java.io.FileOutputStream;
import java.io.IOException;

public class Configuration {

    public boolean rawstring;
    public Mode mode;
    public VariableMode variable;
    public boolean strict_scope;
    public String opMap;

    public String output;

    public boolean autoClose;

    public Configuration() {
        rawstring = false;
        mode = Mode.DECOMPILE;
        variable = VariableMode.DEFAULT;
        strict_scope = false;
        opMap = null;
        output = null;
        autoClose = false;
    }

    public Configuration(Configuration other) {
        rawstring = other.rawstring;
        mode = other.mode;
        variable = other.variable;
        strict_scope = other.strict_scope;
        opMap = other.opMap;
        output = other.output;
        autoClose = false;
    }

    public Output getOutput() {
        if (output != null) {
            try {
                return new Output(new FileOutputProvider(new FileOutputStream(output)));
            } catch (IOException e) {
                Main.error(e.getMessage(), false);
                return null;
            }
        } else {
            return new Output();
        }
    }

    public enum Mode {
        DECOMPILE,
        DISASSEMBLE,
        ASSEMBLE,
    }

    public enum VariableMode {
        NODEBUG,
        DEFAULT,
        FINDER,
    }

}
