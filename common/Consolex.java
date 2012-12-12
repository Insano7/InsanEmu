package common;
import game.GameServer.SaveThread;

import java.io.Console;
import java.io.PrintStream;

import org.fusesource.jansi.AnsiConsole;



public class Consolex implements Runnable{
	private Thread _t;

	public Consolex()
	{
		this._t = new Thread(this);
		_t.setDaemon(true);
		_t.start();
	}
	
	public void run() {
		while (CyonEmu.isRunning){
			Console console = System.console();
		    String command = console.readLine();
		    try{
		    evalCommand(command);
		    }catch(Exception e){}
		    finally
		    {
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
		    }
		}
	}
	public void evalCommand(String command)
	{
		String[] args = command.split(" ");
		String fct =args[0].toUpperCase();
		if(fct.equals("SAVE"))
		{
			Thread t = new Thread(new SaveThread());
			t.start();
		}
		else
			if(fct.equals("EXIT"))
			{
				System.exit(0);
			}
	    else
		if(fct.equalsIgnoreCase("ANNOUNCE"))
		{	
				String announce = command.substring(4);
				String PrefixConsole = "<b>Serveur</b> : ";
				SocketManager.GAME_SEND_MESSAGE_TO_ALL(PrefixConsole+announce, CyonEmu.CONFIG_MOTD_COLOR);
				sendEcho("<Announce:> "+announce);
		}else
		if(fct.equals("?")||command.equals("HELP"))
		{
			sendInfo("------------Commandes:------------");
			sendInfo("- SAVE pour sauvegarder le serveur.");
			sendInfo("- EXIT pour fermer le serveur.");
			sendInfo("- INFOS pour afficher les informations comme en jeu.");
			sendInfo("- CLEAR pour nettoyer la console.");
			sendInfo("- ANNOUNCE pour envoyer un message aux joueurs.");
			sendInfo("- HELP ou ? pour afficher cette liste.");
			sendInfo("----------------------------------");
		}else
			if(fct.equals("CLEAR"))
			{
				CyonEmu.ReStart();
			}
		else
		if(fct.equals("INFOS"))
		{
			long uptime = System.currentTimeMillis() - CyonEmu.gameServer.getStartTime();
			int jour = (int) (uptime/(1000*3600*24));
			uptime %= (1000*3600*24);
			int hour = (int) (uptime/(1000*3600));
			uptime %= (1000*3600);
			int min = (int) (uptime/(1000*60));
			uptime %= (1000*60);
			int sec = (int) (uptime/(1000));
			
			String mess =	"===========\n"+CyonEmu.makeHeader()
					+			"UpTime: "+jour+"d "+hour+"h "+min+"m "+sec+"s\n"
					+			"En Ligne: "+CyonEmu.gameServer.getPlayerNumber()+"\n"
					+			"Record de Connexions: "+CyonEmu.gameServer.getMaxPlayer()+"\n"
					+			"===========";		
			sendInfo(mess);
		}
		else
		{
			sendError("Commande non reconnue ou incomplete.");
		}
	}

	public static void sendInfo(String msg)
	{
		common.Consolex.println(msg, ConsoleColorEnum.GREEN);
	}
	public static void sendError(String msg)
	{
		common.Consolex.println(msg, ConsoleColorEnum.RED);
	}
	public static void sendEcho(String msg)
	{
		common.Consolex.println(msg, ConsoleColorEnum.BLUE);
	}
	public enum ConsoleColorEnum
	{
		//FONT
		BOLD(1),
		UNDERLINE(4),
		BLACK(30),
		RED(31),
		GREEN(32),
		YELLOW(33),
		BLUE(34),
		WHITE(37),
		//SPECIAL
		RESET(0);
		private int color;
		private ConsoleColorEnum(int color)
		{
			this.color = color;
		}
		public int get()
		{
			return color;
		}
	}
	public static void clear() {
        AnsiConsole.out.print("\033[H\033[2J");
    }
    
    public static void setTitle(String title) {
        AnsiConsole.out.append("\033]0;").append(title).append("\007");
    }
    
    public static void println(String message)
    {
    	AnsiConsole.out.println(message);
    }
    public static PrintStream out()
    {
    	return AnsiConsole.out;
    }
    
    public static void println(String msg, ConsoleColorEnum color)
    {
    	AnsiConsole.out.println(new StringBuilder().append("\033[").append(color.get()).append("m").append(msg).append("\033[").append(ConsoleColorEnum.RESET.get()).append("m").toString());
    }
    public static void print(String msg, ConsoleColorEnum color)
    {
    	AnsiConsole.out.print(new StringBuilder().append("\033[").append(color.get()).append("m").append(msg).append("\033[").append(ConsoleColorEnum.RESET.get()).append("m").toString());
    }
}


