package game;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import objects.Compte;
import objects.Personnage;

import common.CryptManager;
import common.CyonEmu;
import common.Formulas;
import common.SQLManager;
import common.SocketManager;
import common.World;

public class GameServer implements Runnable{

	private ServerSocket _SS;
	private Thread _t;
	private ArrayList<GameThread> _clients = new ArrayList<GameThread>();
	private ArrayList<Compte> _waitings = new ArrayList<Compte>();
	private Timer _saveTimer;
	private Timer _lastPacketTimer;
	private long _startTime;
	private int _maxPlayer = 0;
	private Timer _ReboTimer; // Reboot automatique
	private Timer _loadActionTimer;
    private Timer _loadPubTimer; // Pub 
	
	public GameServer(String Ip)
	{
		try {
			_saveTimer = new Timer();
			_saveTimer.schedule(new TimerTask()
			{
				public void run()
				{
					if(!CyonEmu.isSaving)
					{
						Thread t = new Thread(new SaveThread());
						t.start();
					}
				}
			}, CyonEmu.CONFIG_SAVE_TIME,CyonEmu.CONFIG_SAVE_TIME);
			
			if (CyonEmu.CONFIG_PUB == true) {
                _loadPubTimer = new Timer();
                _loadPubTimer.schedule(new TimerTask() {

                    public void run() {
                        int rand = Formulas.getRandomValue(1, 3);
                        switch (rand) {
                            case 1:
                                SocketManager.GAME_SEND_MESSAGE_TO_ALL(CyonEmu.PUB1, CyonEmu.CONFIG_MOTD_COLOR);
                                break;
                            case 2:
                                SocketManager.GAME_SEND_MESSAGE_TO_ALL(CyonEmu.PUB2, CyonEmu.CONFIG_MOTD_COLOR);
                                break;
                            case 3:
                                SocketManager.GAME_SEND_MESSAGE_TO_ALL(CyonEmu.PUB3, CyonEmu.CONFIG_MOTD_COLOR);
                                break;
                        }


                    }
                }, 1800000, 1800000);
			}
                if (CyonEmu.CONFIG_REBOOT_AUTO == true) {
				 _ReboTimer = new Timer();
	            _ReboTimer.schedule(new TimerTask() {

	                public void run() {
	                    Thread t = new Thread(new SaveThread());
	                    t.start();
	                    SocketManager.PACKET_POPUP("Le reboot automatique va avoir lieu dans 30 minutes.");
	                    return;
	                }
	            }, CyonEmu.CONFIG_TIME_REBOOT - 1800000, CyonEmu.CONFIG_TIME_REBOOT - 1800000);
	            
	            _ReboTimer = new Timer();
	            _ReboTimer.schedule(new TimerTask() {

	                public void run() {
	                    Thread t = new Thread(new SaveThread());
	                    t.start();
	                    SocketManager.PACKET_POPUP("</b>Le reboot automatique va avoir lieu dans 5 minutes.<br />Sauvegardez votre personnage avec .save !");
	                    return;
	                }
	            }, CyonEmu.CONFIG_TIME_REBOOT - 300000, CyonEmu.CONFIG_TIME_REBOOT - 300000);

	            _loadActionTimer = new Timer();
	            _loadActionTimer.schedule(new TimerTask() {

	                public void run() {
	                    SQLManager.LOAD_ACTION();
	                    GameServer.addToLog("Les live actions ont ete appliquees");
	                }
	            }, CyonEmu.CONFIG_LOAD_DELAY, CyonEmu.CONFIG_LOAD_DELAY);
	            
	            _ReboTimer = new Timer();
	            _ReboTimer.schedule(new TimerTask() {

	                public void run() {
	                    System.exit(0);
	                }
	            }, CyonEmu.CONFIG_TIME_REBOOT, CyonEmu.CONFIG_TIME_REBOOT);
	            }
	            
	        _lastPacketTimer = new Timer();
			_lastPacketTimer.schedule(new TimerTask()
			{
				public void run()
				{
					for(Personnage perso : World.getOnlinePersos()) 
					{ 
						if (perso.getLastPacketTime() + CyonEmu.CONFIG_MAX_IDLE_TIME < System.currentTimeMillis())
						{
							
							if(perso != null && perso.get_compte().getGameThread() != null && perso.isOnline())
							{
								SocketManager.REALM_SEND_MESSAGE(perso.get_compte().getGameThread().get_out(),"01|"); 
								perso.get_compte().getGameThread().closeSocket();
							}
						}
					}
					World.MoveMobsOnMaps();
					}
			}, 60000,60000);
			
			_SS = new ServerSocket(CyonEmu.CONFIG_GAME_PORT);
			if(CyonEmu.CONFIG_USE_IP)
				CyonEmu.GAMESERVER_IP = CryptManager.CryptIP(Ip)+CryptManager.CryptPort(CyonEmu.CONFIG_GAME_PORT);
			_startTime = System.currentTimeMillis();
			_t = new Thread(this);
			_t.start();
		} catch (IOException e) {
			addToLog("IOException: "+e.getMessage());
			CyonEmu.closeServers();
		}
	}
	
	public static class SaveThread implements Runnable
	{
		public void run()
		{
			if(CyonEmu.isSaving == false)
			{
				World.saveAll(null);;
			}
		}
	}
	
	public ArrayList<GameThread> getClients() {
		return _clients;
	}

	public long getStartTime()
	{
		return _startTime;
	}
	
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	public int getPlayerNumber()
	{
		return _clients.size();
	}
	public void run()
	{	
		while(CyonEmu.isRunning)//bloque sur _SS.accept()
		{
			try
			{
				_clients.add(new GameThread(_SS.accept()));
				if(_clients.size() > _maxPlayer)_maxPlayer = _clients.size();
			}catch(IOException e)
			{
				addToLog("IOException: "+e.getMessage());
				try
				{
					if(!_SS.isClosed())_SS.close();
					CyonEmu.closeServers();
				}
				catch(IOException e1){}
			}
		}
	}
	
	public void kickAll()
	{
		try {
			_SS.close();
		} catch (IOException e) {}
		//Copie
		ArrayList<GameThread> c = new ArrayList<GameThread>();
		c.addAll(_clients);
		for(GameThread GT : c)
		{
			try
			{
				GT.closeSocket();
			}catch(Exception e){};	
		}
	}
	
	public synchronized static void addToLog(String str)
	{
		if (CyonEmu.SHOW_RECV)
			System.out.println(str);
		
	}
	
	public synchronized static void addToSockLog(String str)
	{
		if(CyonEmu.CONFIG_DEBUG && CyonEmu.SHOW_RECV)
		System.out.println(str);
	}
	
	public void delClient(GameThread gameThread)
	{
		_clients.remove(gameThread);
		if(_clients.size() > _maxPlayer)_maxPlayer = _clients.size();
	}

	public synchronized Compte getWaitingCompte(int guid)
	{
		for (int i = 0; i < _waitings.size(); i++)
		{
			if(_waitings.get(i).get_GUID() == guid)
				return _waitings.get(i);
		}
		return null;
	}
	
	public synchronized void delWaitingCompte(Compte _compte)
	{
		_waitings.remove(_compte);
	}
	
	public synchronized void addWaitingCompte(Compte _compte)
	{
		_waitings.add(_compte);
	}
	
	public static String getServerTime()
	{
		Date actDate = new Date();
		return "BT"+(actDate.getTime()+3600000);
	}
	public static String getServerDate()
	{
		Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		String jour = Integer.parseInt(dateFormat.format(actDate))+"";
		while(jour.length() <2)
		{
			jour = "0"+jour;
		}
		dateFormat = new SimpleDateFormat("MM");
		String mois = (Integer.parseInt(dateFormat.format(actDate))-1)+"";
		while(mois.length() <2)
		{
			mois = "0"+mois;
		}
		dateFormat = new SimpleDateFormat("yyyy");
		String annee = (Integer.parseInt(dateFormat.format(actDate))-1370)+"";
		return "BD"+annee+"|"+mois+"|"+jour;
	}

	public Thread getThread()
	{
		return _t;
	}

}