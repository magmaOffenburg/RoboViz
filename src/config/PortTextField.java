package config;

public class PortTextField extends IntegerTextField {
    public PortTextField(int port) {
        super(port, 1, 65535);
    }
}
