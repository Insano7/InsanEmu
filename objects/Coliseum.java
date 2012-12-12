package objects;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import objects.Personnage.Group;

import common.CyonEmu;
import common.SocketManager;
import common.World;

public class Coliseum extends Thread {
	
	private static ArrayList<Coliseum> Coliseums = new ArrayList<Coliseum>(); // Liste des Kolizeum 
	private static Timer kick;
	private ArrayList<Personnage> team1 = new ArrayList<Personnage>(); // Team 1
	private ArrayList<Personnage> team2 = new ArrayList<Personnage>(); // Team 2
	private int lvl_moyen; // level moyen
	private boolean lock = false; // Si le koli est lancé
	public int PLAYERS_IN_KOLI; // Nombre de joueur
	
	public static final int LEVEL_RANGE = 25; // tranche de level max pour koli
	public static final int MAX_PLAYERS_PER_KOLI = 3; // Nombre de joueur max dans un koli
	
	private Coliseum(Personnage p) { // Constructeur pour un personnage seul
		Coliseums.add(this);
		lvl_moyen = p.get_lvl();
		team1.add(p);
		Random bs = new Random();
		PLAYERS_IN_KOLI = bs.nextInt(2)+MAX_PLAYERS_PER_KOLI-1;
		SocketManager.GAME_SEND_MESSAGE(p, "Vous avez ete inscrit dans un Kolizeum de type : " + PLAYERS_IN_KOLI + " vs " + PLAYERS_IN_KOLI, CyonEmu.CONFIG_MOTD_COLOR);
		setDaemon(true);
		start();
	}
	
	private Coliseum(Group g) {
		PLAYERS_IN_KOLI = g.getPersosNumber();
		groupJoinTeam(g,team1,this);
		lvl_moyen = (int)(team1.get(0).get_lvl() + team1.get(1).get_lvl())/2;
		Coliseums.add(this);
		setDaemon(true);
		start();
	}

	public void run() {
		while (!lock) {
			while (team1.size() < PLAYERS_IN_KOLI || team2.size() < PLAYERS_IN_KOLI)
				{ try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); } }
			lock = this.ready();
			if (!lock) {
				sendBusyMessage(team1);
				sendBusyMessage(team2);
				try { Thread.sleep(20000); } catch (InterruptedException e) { e.printStackTrace(); }
				kickAllBusy(team1);
				kickAllBusy(team2);
			}
		}
		teleport();
		SocketManager.GAME_SEND_MAP_NEW_DUEL_TO_MAP(team1.get(0).get_curCarte(), team1.get(0).get_GUID(), team2.get(0).get_GUID());
		try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
		SocketManager.GAME_SEND_MAP_START_DUEL_TO_MAP(team2.get(0).get_curCarte(), team1.get(0).get_GUID(), team2.get(0).get_GUID());
		@SuppressWarnings("unused")
		Fight f = team1.get(0).get_curCarte().startKolizeumFight(team1, team2);
		try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
		for (int i=1; i<PLAYERS_IN_KOLI; i++) {
			team1.get(0).get_fight().joinFight(team1.get(i), team1.get(0).get_GUID());
			team2.get(0).get_fight().joinFight(team2.get(i), team2.get(0).get_GUID());
		}
		
	}
	
	public static void setUpTimer() {
		kick = new Timer();
		kick.schedule(new TimerTask() {
			public void run() {
				for (Coliseum a : Coliseums) {
					a.kickAllBusy(a.team1);
					a.kickAllBusy(a.team2);
				}
			}
		}, 240000L, 240000L);
	}

	private void kickAllBusy(ArrayList<Personnage> team) {
		for (Personnage p : team) {
			if (p==null || p.get_fight()!=null || !p.isOnline()) {
				SocketManager.GAME_SEND_MESSAGE(p, "Vous avez ete desinscrit du Kolizeum automatiquement pour cause d'inactivite.", CyonEmu.CONFIG_MOTD_COLOR);
				delPerso(p);
			}
		}
		
	}

	private void sendBusyMessage(ArrayList<Personnage> team) {
		for (Personnage p : team) {
			if (p.get_fight()==null && !p.is_away())
				SocketManager.GAME_SEND_MESSAGE(p, "Les deux equipes du Kolizeum sont completes, mais certains joueurs sont occupes.", CyonEmu.CONFIG_MOTD_COLOR);
			else
				SocketManager.GAME_SEND_MESSAGE(p, "Merci de vous rendre disponible, le Kolizeum va bientot commencer.", CyonEmu.CONFIG_MOTD_COLOR);
		}
		
	}
	
	private int getRandomMap() {
		return 7811;
	}
	
	/*private int getRandomMap() {
		Random rand = new Random();
		switch(rand.nextInt(4)+1) {
		case 1 : return 20000;
		case 2 : return 20001;
		case 3 : return 20002;
		case 4 : return 20004;
		default : return 20007;
		}
	}*/
	
	private void teleport() {
		short MAP_ID = (short) getRandomMap();
		for (Personnage p : team1)
			p.teleport(MAP_ID, World.getCarte(MAP_ID).getRandomFreeCellID());
		for (Personnage p : team2)
			p.teleport(MAP_ID, World.getCarte(MAP_ID).getRandomFreeCellID());
	}
	
	private boolean ready() {
		boolean ready = true;
		for ( Personnage p : team1 )
			ready = p.isOnline() && p.get_fight()==null && ready;
		for ( Personnage p : team2 )
			ready = p.isOnline() && p.get_fight()==null && ready;
		return ready;
	}
	
	private static Coliseum getColiseum(int lvl) {
		for (Coliseum a : Coliseums) {
			if ((((a.lvl_moyen - lvl) < LEVEL_RANGE && (a.lvl_moyen - lvl) > -LEVEL_RANGE)) && !a.lock)
				return a;
		}
		return null;
	}
	
	private static Coliseum getColiseumForGroupeBySize(Group group) {
		for (Coliseum a : Coliseums) {
			if (a.team1.size()+group.getPersosNumber()<=a.PLAYERS_IN_KOLI) {
				groupJoinTeam(group,a.team1,a);
				return a;
			}
			else if (a.team2.size()+group.getPersosNumber()<=a.PLAYERS_IN_KOLI) {
				groupJoinTeam(group,a.team2,a);					
				return a;
			}
		}
		return null;
	}
	
	private static void groupJoinTeam(Group g,ArrayList<Personnage> team, Coliseum a) {
		for (Personnage p : g.getPersos()) {
			team.add(p);
			p.setColiseum(a);
			SocketManager.GAME_SEND_MESSAGE(p, "Vous avez ete inscrit dans un Kolizeum de type : " + a.PLAYERS_IN_KOLI + " vs " + a.PLAYERS_IN_KOLI, CyonEmu.CONFIG_MOTD_COLOR);		
		}
		
	}

	public synchronized void delPerso(Personnage p) {
		if (team1.contains(p))
			team1.remove(p);
		else
			team2.remove(p);
		if (this.isEmpty())
			delColiseum(this);
		p.setColiseum(null);
	}
	
	private static void delColiseum(Coliseum a) {
		a.interrupt();
		Coliseums.remove(a);
	}
	
	private boolean isEmpty() {
		return team1.size()==0 && team2.size()==0;
	}
	
	public long getKamasReward() {
		return this.PLAYERS_IN_KOLI*10000;
	}
	
	public long getXpReward(Personnage p) {
		long xp;
		if (p.get_lvl()<50)
			xp = 100000*this.PLAYERS_IN_KOLI/2;
		else if (p.get_lvl()<100)
			xp = 10000000*this.PLAYERS_IN_KOLI/2;
		else if (p.get_lvl()<150)
			xp = 60000000*this.PLAYERS_IN_KOLI/2;
		else if (p.get_lvl()<200)
			xp = 100000000*this.PLAYERS_IN_KOLI/2;
		else if (p.get_lvl()<300)
			xp = 120000000*this.PLAYERS_IN_KOLI/2;
		else if (p.get_lvl()<400)
			xp = 180000000*this.PLAYERS_IN_KOLI/2;
		else if (p.get_lvl()<500)
			xp = 200000000*this.PLAYERS_IN_KOLI/2;
		else
			xp = 210000000*this.PLAYERS_IN_KOLI/2;
		return xp;
	}
	
	public static synchronized void addPerso(Personnage p) {
		Coliseum a = getColiseum(p.get_lvl());
		if (a==null)
			a = new Coliseum(p);
		else {
			if (a.team1.size()<a.PLAYERS_IN_KOLI) {
				a.team1.add(p);
				SocketManager.GAME_SEND_MESSAGE(p, "Vous avez ete inscrit dans un Kolizeum de type : " + a.PLAYERS_IN_KOLI + " vs " + a.PLAYERS_IN_KOLI, CyonEmu.CONFIG_MOTD_COLOR);
			}
			else if (a.team2.size()<a.PLAYERS_IN_KOLI) {
				SocketManager.GAME_SEND_MESSAGE(p, "Vous avez ete inscrit dans un Kolizeum de type : " + a.PLAYERS_IN_KOLI + " vs " + a.PLAYERS_IN_KOLI, CyonEmu.CONFIG_MOTD_COLOR);
				a.team2.add(p);
			}
			else
				a = new Coliseum(p);
		}
		p.setColiseum(a);
	}

	public static synchronized void addGroup(Group group) {
		Coliseum a = getColiseumForGroupeBySize(group);
		if (a==null)
			a = new Coliseum(group);		
	}

	public boolean isLock() {
		return this.lock;
	}

	public String infos(Personnage p) {
		kickAllBusy(team1);
		kickAllBusy(team2);
		StringBuilder infos = new StringBuilder("Vous êtes dans un Kolizeum de type "+PLAYERS_IN_KOLI+" vs "+PLAYERS_IN_KOLI+".\nDans votre team il y a :");
		int count;
		if (team1.contains(p)) {
			infos.append(team1.size());
			count = team2.size();
		}
		else {
			infos.append(team2.size());
			count = team1.size();
		}
		infos.append(" joueurs, dans la team adverse il y a : "+count+" joueurs.");
		return infos.toString();
	}
	
}