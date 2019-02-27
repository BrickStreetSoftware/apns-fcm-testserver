package net.brickst.android;

/**
 * Tracer class.
 */
public class Tracer
{
    private boolean enabled = false;
    private String prefix = "";
    private static Tracer ANSIM = new Tracer("ansim", false);
    public static Tracer getANSIM() { return ANSIM;}
    public Tracer(String prefix, boolean enabled)
    {
        this.prefix = prefix;
        this.enabled = enabled;
    }
    public boolean isEnabled() { return this.enabled;}
    public void setEnabled(boolean e) { this.enabled = e;}

    public void println(String msg)
    {
        if(isEnabled())
            System.out.println(prefix + msg);
    }

    public void println(Throwable cause)
    {
        if(isEnabled())
        {
            System.out.print(prefix + ":");
            cause.printStackTrace();
        }
    }
}
