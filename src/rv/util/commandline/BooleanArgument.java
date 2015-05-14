package rv.util.commandline;

/**
 * Flag argument (--flag). Returns whether or not the flag is present, has no value.
 */
public class BooleanArgument extends Argument<Boolean> {
    public BooleanArgument(String name) {
        super(name, false);
    }

    @Override
    protected boolean matchesName(String arg) {
        return arg.equals(getFormattedName());
    }

    @Override
    protected String extractStringValue(String arg) {
        return "true";
    }

    @Override
    protected Boolean extractValue(String value) {
        return true;
    }

    @Override
    protected String getFormattedName() {
        return "--" + name;
    }
}
