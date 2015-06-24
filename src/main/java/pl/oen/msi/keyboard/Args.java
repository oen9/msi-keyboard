package pl.oen.msi.keyboard;

import org.kohsuke.args4j.Option;

public class Args {
    public static final String HIDE_PARAM_NAME = "-hide";

    @Option(name = "-c1",
            aliases = {"-colour1", "--colour1", "--c1"},
            usage = "Sets a colour1")
    protected Byte colour1 = 1;

    @Option(name = "-c2",
            aliases = {"-colour2", "--colour2", "--c2"},
            usage = "Sets a colour2")
    protected Byte colour2;

    @Option(name = "-c3",
            aliases = {"-colour3", "--colour3", "--c3"},
            usage = "Sets a colour3")
    protected Byte colour3;

    @Option(name = "-x",
            aliases = {"-X", "--x", "--X"},
            usage = "turn OFF graphics mode")
    protected boolean x = false;

    @Option(name = HIDE_PARAM_NAME,
            usage = "Hides app to tray on startup")
    protected boolean hide;

    public Byte getColour1() {
        return colour1;
    }

    public void setColour1(final Byte colour1) {
        this.colour1 = colour1;
    }

    public Byte getColour2() {
        return colour2;
    }

    public void setColour2(final Byte colour2) {
        this.colour2 = colour2;
    }

    public Byte getColour3() {
        return colour3;
    }

    public void setColour3(final Byte colour3) {
        this.colour3 = colour3;
    }

    public boolean isX() {
        return x;
    }

    public void setX(final boolean x) {
        this.x = x;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(final boolean hide) {
        this.hide = hide;
    }
}
