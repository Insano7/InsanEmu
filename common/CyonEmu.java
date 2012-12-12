package common;

import game.GameServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;

import realm.RealmServer;

import common.Consolex.ConsoleColorEnum;

public class CyonEmu {
	
	private static final String CONFIG_FILE = "CyonConfig.txt";
	public static String IP = "127.0.0.1";
	public static boolean isInit = false;
	public static String DB_HOST;
	public static String DB_USER;
	public static String DB_PASS;
	public static String STATIC_DB_NAME;
	public static String OTHER_DB_NAME;
	//Couleurs
	public static String CONFIG_COLOR_GLOBAL = "FF0000";
	public static String COLOR_BLEU = "3366FF";
	public static String COLOR_BLEU2 = "0561dd";
	public static String COLOR_VERT = "0d8b0d";
	public static String COLOR_RED = "FF0000";
	public static short CONFIG_MAP_1PVM = 19013;
	public static int CONFIG_CELL_1PVM = 300;
	public static final short CONFIG_MAP_ATELIER = 25021;
	public static final int CONFIG_CELL_ATELIER = 268;
//Zobal
	public static int CONFIG_LEVEL_REQUIERT_ZOBAL = 100;
	public static boolean CONFIG_ACTIV_ZOBAL = true;
//Prisme
	public static ArrayList<Integer> mapasNoPrismas = new ArrayList<Integer>();
	public static boolean ACTIVER_COMMANDE_PRISME = true;
	public static boolean COMMANDE_PRISME_VIP = false;
	public static int LEVEL_REQUIS_COMMANDE_PRISME = 101;
	public static int NOMBRE_COMMANDE_PRISME = 10;
	public static String GAMESERVER_IP;
	public static String CONFIG_MOTD = "";
	public static String CONFIG_MOTD_COLOR = "";
	public static boolean CONFIG_DEBUG = false;
	public static PrintStream PS;
	public static boolean CONFIG_POLICY = false;
	public static int CONFIG_REALM_PORT = 443;
	public static int CONFIG_GAME_PORT 	= 5555;
	//Baskwo
	public static int MORPHID_SKIN = 300;
	public static int MORPH_VIP = 0;
	public static short MAP_VIP = 0;
	public static int CELL_VIP = 0;
	public static short MAP_EVENT = 0;
	public static int CELL_EVENT = 0;
	public static short MAP_POUNTCH = 0;
	public static int CELL_POUNTCH = 0;
	public static short MAP_SQUATTE = 0;
	public static int CELL_SQUATTE = 0;
	
	public static short CONFIG_MAP_ENCLOS = 8750;
	public static int CONFIG_CELL_ENCLOS = 468;
	
	public static boolean CONFIG_ALLOW_MULTI = false;
	public static int CONFIG_START_LEVEL = 1;
	public static int CONFIG_START_KAMAS = 0;
	public static String START_ITEMS = "";
	
	public static long INCARNAM_TIME = 30000;
	public static int CONFIG_SAVE_TIME = 10*60*10000;
	public static int CONFIG_DROP = 1;
	public static boolean CONFIG_ZAAP = false;
	public static int CONFIG_PLAYER_LIMIT = 30;
	public static boolean CONFIG_IP_LOOPBACK = true;
	
	public static int XP_PVP = 10;
	public static int LVL_PVP = 15;
	public static boolean ALLOW_MULE_PVP = false;
	public static int XP_PVM = 1;
	public static int KAMAS = 1;
	public static int HONOR = 1;
	public static int XP_METIER = 1;
	public static int PORC_FM = 1;
	public static int CONFIG_LVLMAXMONTURE = 100;
	
	public static boolean CONFIG_USE_MOBS = false;
	public static boolean CONFIG_USE_IP = false;
	public static GameServer gameServer;
	public static RealmServer realmServer;
	public static boolean isRunning = false;
	
	public static boolean isSaving = false;
	public static boolean AURA_SYSTEM = false;
	
	public static ArrayList<Integer> arenaMap = new ArrayList<Integer>(8);
	public static int CONFIG_ARENA_TIMER = 10*60*1000;// 10 minutes
	public static int CONFIG_DB_COMMIT = 30*1000;
	public static int CONFIG_MAX_IDLE_TIME = 1800000;//En millisecondes
	public static ArrayList<Integer> NOTINHDV = new ArrayList<Integer>();
	
	public static boolean SHOW_RECV = false;
	public static int PLAYER_IP = 3;
	
	public static int serverID = 1;
	
	public static int pa = 12;
	public static int pm = 6;
	
	public static int CONFIG_TIME_REBOOT = 10800000;
	public static boolean CONFIG_REBOOT_AUTO = false;
	public static int CONFIG_LOAD_DELAY = 60000;
	
	public static boolean CONFIG_ACTIVER_STATS_2 = false;
	
	public static short CONFIG_START_MAP = 7411;
	public static int CONFIG_START_CELL = 250;
	public static short CONFIG_MAP_PVP = 7411;
	public static int CONFIG_CELL_PVP = 250;
	public static short CONFIG_MAP_PVM = 7411;
	public static int CONFIG_CELL_PVM = 250;
	public static short CONFIG_MAP_SHOP = 7411;
	public static int CONFIG_CELL_SHOP = 250;
	
    public static String PUB1 = "";
	public static String PUB2 = "";
	public static String PUB3 = "";
	public static boolean CONFIG_PUB = false;
	
	public static long CONFIG_MS_PER_TURN = 30000;
	public static long CONFIG_MS_FOR_START_FIGHT = 45000;
	
    public static ArrayList<Integer> FEED_MOUNT_ITEM = new ArrayList<Integer>();
	public static ArrayList<Integer> CartesNoPrismes = new ArrayList<Integer>();
	
	public static void main(String[] args)
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				CyonEmu.closeServers();
			}
		}
		);
		Consolex.clear();
		PrintStream ps;
        try {
            ps = new PrintStream(System.out, true, "IBM850");
            System.setOut(ps);
        } catch (Exception e) {
            System.out.println("Erreur de conversion du format des caracteres de la console.");
        }
        Consolex.setTitle("CyonEmu - Version 2.7 - Chargement...");
        System.out.println("==============================================================\n");
		System.out.println(makeHeader());
		System.out.println("==============================================================\n");
		System.out.println("Chargement de la configuration..");
		loadConfiguration();
		isInit = true;
		System.out.println("Configuration OK.");
		System.out.println("Connexion au MySQL server.");
		if(SQLManager.setUpConnexion()) System.out.println("Connexion OK.");
		else
		{
			System.out.println("Connexion invalide.");
			CyonEmu.closeServers();
			System.exit(0);
		}
		System.out.println("Creation du Monde.");
		long startTime = System.currentTimeMillis();
		World.createWorld();
		long endTime = System.currentTimeMillis();
		long differenceTime = (endTime - startTime)/1000;
		System.out.println("Emulator OK en : "+differenceTime+" s");
		isRunning = true;
		System.out.println("==============================================================\n");
		System.out.println("Lancement du serveur, PORT DU JEU: "+CONFIG_GAME_PORT);
		String Ip = "";
		try
		{
			Ip = InetAddress.getLocalHost().getHostAddress();
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {}
			System.exit(1);
		}
		Ip = IP;
		gameServer = new GameServer(Ip);
		System.out.println("Lancement du serveur, PORT DU CONNEXION: "+CONFIG_REALM_PORT);
		realmServer = new RealmServer();
		System.out.println("IP du serveur: " + IP);
		refreshTitle();
		EmuStart();
		}
	
	private static void loadConfiguration()
	{
		try {
			BufferedReader config = new BufferedReader(new FileReader(CONFIG_FILE));
			String line = "";
			while ((line=config.readLine())!=null)
			{
				if(line.split("=").length == 1) continue ;
				String param = line.split("=")[0].trim();
				String value = line.split("=")[1].trim();
				if(param.equalsIgnoreCase("DEBUG"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						CyonEmu.CONFIG_DEBUG = true;
						System.out.println("Modo Debug: ON");
					}
				}else if(param.equalsIgnoreCase("SEND_POLICY"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						CyonEmu.CONFIG_POLICY = true;
					}
				}else if (param.equalsIgnoreCase("START_ITEMS")) {
		            if (value == "")
			              START_ITEMS = null;
			        else
			              START_ITEMS = value;
				}
				else if(param.equalsIgnoreCase("START_KAMAs"))
				{
					CyonEmu.CONFIG_START_KAMAS = Integer.parseInt(value);
					if(CyonEmu.CONFIG_START_KAMAS < 0 )
						CyonEmu.CONFIG_START_KAMAS = 0;
					if(CyonEmu.CONFIG_START_KAMAS > 1000000000)
						CyonEmu.CONFIG_START_KAMAS = 1000000000;
				}else if(param.equalsIgnoreCase("START_LEVEL"))
				{
					CyonEmu.CONFIG_START_LEVEL = Integer.parseInt(value);
					if(CyonEmu.CONFIG_START_LEVEL < 1 )
						CyonEmu.CONFIG_START_LEVEL = 1;
					if(CyonEmu.CONFIG_START_LEVEL > 200)
						CyonEmu.CONFIG_START_LEVEL = 200;
				}
		        else if (param.equalsIgnoreCase("PUB1"))
		          {
		                  CyonEmu.PUB1 = value;
		        
		          }
		        else if (param.equalsIgnoreCase("PUB2"))
		          {
		                  CyonEmu.PUB2 = value;
		          }
		        else if (param.equalsIgnoreCase("PUB3"))
		          {
		                  CyonEmu.PUB3 = value;
		          }
		        else if(param.equalsIgnoreCase("ACTIV_PUB"))
					{
						if(value.equalsIgnoreCase("true"))
						{
							CyonEmu.CONFIG_PUB = true;
				}
		      }else if(param.equalsIgnoreCase("START_MAP"))
				{
					CyonEmu.CONFIG_START_MAP = Short.parseShort(value);
				}else if(param.equalsIgnoreCase("START_CELL"))
				{
					CyonEmu.CONFIG_START_CELL = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("SHOP_MAP"))
				{
					CyonEmu.CONFIG_MAP_SHOP = Short.parseShort(value);
				}else if(param.equalsIgnoreCase("SHOP_CELL"))
				{
					CyonEmu.CONFIG_CELL_SHOP = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("PVM_MAP"))
				{
					CyonEmu.CONFIG_MAP_PVM = Short.parseShort(value);
				}else if(param.equalsIgnoreCase("PVM_CELL"))
				{
					CyonEmu.CONFIG_CELL_PVM = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("LOAD_ACTION_DELAY"))
				{
					CyonEmu.CONFIG_LOAD_DELAY = (Integer.parseInt(value) * 1000);
				}else if(param.equalsIgnoreCase("PVP_MAP"))
				{
					CyonEmu.CONFIG_MAP_PVP = Short.parseShort(value);
				}else if(param.equalsIgnoreCase("PVP_CELL"))
				{
					CyonEmu.CONFIG_CELL_PVP = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("KAMAS"))
				{
					CyonEmu.KAMAS = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("HONOR"))
				{
					CyonEmu.HONOR = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("SAVE_TIME"))
				{
					CyonEmu.CONFIG_SAVE_TIME = Integer.parseInt(value)*60*1000000000;
				}else if(param.equalsIgnoreCase("XP_PVM"))
				{
					CyonEmu.XP_PVM = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("XP_PVP"))
				{
					CyonEmu.XP_PVP = Integer.parseInt(value);
				}
				else if (param.equalsIgnoreCase("MAX_LEVEL_MONTURE"))
			    {	
			        CyonEmu.CONFIG_LVLMAXMONTURE=(Integer.parseInt(value));
			        	  
			    }else if(param.equalsIgnoreCase("LVL_PVP"))
				{
					CyonEmu.LVL_PVP = Integer.parseInt(value);
				}
				else if (param.equalsIgnoreCase("PLAYER_IP"))
				{
				CyonEmu.PLAYER_IP = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("DROP"))
				{
					CyonEmu.CONFIG_DROP = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("MOTD"))
				{
					CyonEmu.CONFIG_MOTD = line.split("=",2)[1];
				}
				else if(param.equalsIgnoreCase("PORC_FM"))
				{
					CyonEmu.PORC_FM = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("LOCALIP_LOOPBACK"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						CyonEmu.CONFIG_IP_LOOPBACK = true;
					}
				} else if (param.equalsIgnoreCase("MAP_NO_PRISMES")) {
					for (String curID : value.split(",")) {
						CartesNoPrismes.add(Integer.parseInt(curID));
					}
	            }
				else if(param.equalsIgnoreCase("ZAAP"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						CyonEmu.CONFIG_ZAAP = true;
					}
				}else if(param.equalsIgnoreCase("ACTIV_CARACT_2"))
                {
                    if(value.equalsIgnoreCase("true"))
                    {
                            CyonEmu.CONFIG_ACTIVER_STATS_2 = true;
                    }
               }
               else if(param.equalsIgnoreCase("ACTIV_REBOOT"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						CyonEmu.CONFIG_REBOOT_AUTO = true;
					}  
				}else if(param.equalsIgnoreCase("USE_IP"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						CyonEmu.CONFIG_USE_IP = true;
					}
				}else if(param.equalsIgnoreCase("MOTD_COLOR"))
				{
					CyonEmu.CONFIG_MOTD_COLOR = value;
				}else if(param.equalsIgnoreCase("XP_METIER"))
				{
					CyonEmu.XP_METIER = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("GAME_PORT"))
				{
					CyonEmu.CONFIG_GAME_PORT = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("REALM_PORT"))
				{
					CyonEmu.CONFIG_REALM_PORT = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("HOST_IP"))
				{
					CyonEmu.IP = value;
				}
				else if(param.equalsIgnoreCase("DB_HOST"))
				{
					CyonEmu.DB_HOST = value;
				}else if(param.equalsIgnoreCase("DB_USER"))
				{
					CyonEmu.DB_USER = value;
				}else if(param.equalsIgnoreCase("DB_PASS"))
				{
					if(value == null) value = "";
					CyonEmu.DB_PASS = value;
				}else if(param.equalsIgnoreCase("STATIC_DB_NAME"))
				{
					CyonEmu.STATIC_DB_NAME = value;
				}else if(param.equalsIgnoreCase("OTHER_DB_NAME"))
				{
					CyonEmu.OTHER_DB_NAME = value;
				}else if (param.equalsIgnoreCase("USE_MOBS"))
				{
					CyonEmu.CONFIG_USE_MOBS = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("ALLOW_MULTI_ACCOUNT"))
				{
					CyonEmu.CONFIG_ALLOW_MULTI = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("PLAYER_LIMIT"))
				{
					CyonEmu.CONFIG_PLAYER_LIMIT = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("ARENA_MAP"))
				{
					for(String curID : value.split(","))
					{
						CyonEmu.arenaMap.add(Integer.parseInt(curID));
					}
				}else if (param.equalsIgnoreCase("ARENA_TIMER"))
				{
					CyonEmu.CONFIG_ARENA_TIMER = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("AURA_SYSTEM"))
				{
					CyonEmu.AURA_SYSTEM = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("ALLOW_MULE_PVP"))
				{
					CyonEmu.ALLOW_MULE_PVP = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("MAX_IDLE_TIME"))
				{
					CyonEmu.CONFIG_MAX_IDLE_TIME = (Integer.parseInt(value)*60000);
				}else if (param.equalsIgnoreCase("SERVER_ID"))
		          {
		            serverID = Integer.parseInt(value);
		          }
				else if (param.equalsIgnoreCase("NOT_IN_HDV"))
				{
					for(String curID : value.split(","))
					{
						CyonEmu.NOTINHDV.add(Integer.parseInt(curID));
					}
				}else if(param.equalsIgnoreCase("MAXPA"))
				{
					CyonEmu.pa = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("MAXPM"))
				{
					CyonEmu.pm = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("REBOOT_TIME"))
		          {
		        	  CyonEmu.CONFIG_TIME_REBOOT = Integer.parseInt(value);
		        }
				else if (param.equalsIgnoreCase("FEED_MOUNT")) {
					for (String curID : value.split(",")) {
						FEED_MOUNT_ITEM.add(Integer.parseInt(curID));
					}
				}
				else if (param.equalsIgnoreCase("SHOW_RECV"))
		        
				{  CyonEmu.SHOW_RECV = value.equalsIgnoreCase("true");
			    }
				}
			
			if(STATIC_DB_NAME == null || OTHER_DB_NAME == null || DB_HOST == null || DB_PASS == null || DB_USER == null)
			{
				throw new Exception();
			}
		} catch (Exception e) {
            System.out.println(e.getMessage());
			System.out.println("Fichier de configuration non existant ou illisible !");
			System.out.println("Fermeture du serveur...");
			System.exit(1);
		}
	}
	
	public static void closeServers()
	{
		System.out.println("Fermeture du serveur...");
		if(isRunning)
		{
			isRunning = false;
			CyonEmu.gameServer.kickAll();
			World.saveAll(null);
			SQLManager.closeCons();
		}
		System.out.println("Serveur fermé.");
		isRunning = false;
	}
	
	public static String makeHeader()
	{
		StringBuilder mess = new StringBuilder();
		mess.append("-");
		mess.append("\nCyoneEmu Remake v"+Constants.SERVER_VERSION);
		mess.append("\nBy "+Constants.SERVER_MAKER+".");
		mess.append("\nMerci a Diabu et DeathDown pour sa base.");
		mess.append("\n-");
		return mess.toString();
	}
	
	public static void EmuStart()
	{
		Consolex.clear();
		System.out.println("==============================================================\n");
		System.out.println(makeHeader());
		System.out.println("==============================================================\n");
		System.out.print("\nCyoneEmu prêt! En attente de connexion...");
		Consolex.println("\nHelp ou ? pour voir la liste du commande disponible dans cette console.", ConsoleColorEnum.RED);
		new Consolex();
	}
	
	public static void ReStart()
	{
		Consolex.clear();
		System.out.println("==============================================================\n");
		System.out.println(makeHeader());
		System.out.println("==============================================================\n");
		Consolex.println("Help ou ? pour voir la liste du commande disponible dans cette console.", ConsoleColorEnum.YELLOW);
		new Consolex();
	}
	
	public static void refreshTitle()
	{
		if(!isRunning)return;
		StringBuilder title = new StringBuilder();
		title.append("CyoneEmu - REALMPort: ").append(CONFIG_REALM_PORT).append(" GAMEPort: ").append(CONFIG_GAME_PORT);
		title.append(" En ligne: ").append(gameServer.getPlayerNumber()).append(" Statut: ");
	    switch(World.get_state())
	    {
	    case (short)1:title.append("Disponible");break;
	    case (short)2:title.append("Save");break;
	    default:title.append("Indisponnible");break;
	    }
		Consolex.setTitle(title.toString());
	}
}
