package ttaomae.connectn.network.server;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length != 1) {
            printUsage();
            System.exit(1);
        }

        try {
            new Server(Integer.parseInt(args[0])).run();
        } catch (NumberFormatException e) {
            printUsage();
            System.exit(1);
        }
    }

    private static void printUsage()
    {
        System.err.println(String.format("Usage java %s <port number>%n",
                Main.class.getName()));
    }
}
