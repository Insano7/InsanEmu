package objects;

import game.GameThread;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Timer;

import objects.HDV.HdvEntry;
import realm.RealmThread;

import common.CryptManager;
import common.CyonEmu;
import common.SQLManager;
import common.SocketManager;
import common.World;

public class Compte {

	private int _GUID;
	private String _name;
	private String _pass;
	private String _pseudo;
	private String _key;
	private String _lastIP = "";
	private String _question;
	private String _reponse;
	private boolean _banned = false;
	private int _gmLvl = 0;
	private int _vip = 0;
	private String _curIP = "";
	private String _lastConnectionDate = "";
	private GameThread _gameThread;
	private RealmThread _realmThread;
	private Personnage _curPerso;
	private long _bankKamas = 0;
	private Map<Integer,Objet> _bank = new TreeMap<Integer,Objet>();
	private ArrayList<Integer> _friendGuids = new ArrayList<Integer>();
	private ArrayList<Integer> _EnemyGuids = new ArrayList<Integer>();
	private boolean _mute = false;
	public Timer _muteTimer;
	public int _muteTime;
	public long _muteHeur = 0;
	public int _position = -1;//Position du joueur
	private int _regalo;
	private Map<Integer,ArrayList<HdvEntry>> _hdvsItems;// Contient les items des HDV format : <hdvID,<cheapestID>>
	private int _points;
	
	private Map<Integer, Personnage> _persos = new TreeMap<Integer, Personnage>();
	
	public Compte(int aGUID,String aName,String aPass, String aPseudo,String aQuestion,String aReponse,int aGmLvl, int vip, boolean aBanned, String aLastIp, String aLastConnectionDate,String bank,int bankKamas, String friends, String enemy, int regalo, int points)
	{
		this._GUID 		= aGUID;
		this._name 		= aName;
		this._pass		= aPass;
		this._pseudo 	= aPseudo;
		this._question	= aQuestion;
		this._reponse	= aReponse;
		this._gmLvl		= aGmLvl;
		this._vip 		= vip;
		this._banned	= aBanned;
		this._lastIP	= aLastIp;
		this._lastConnectionDate = aLastConnectionDate;
		this._bankKamas = bankKamas;
		this._hdvsItems = World.getMyItems(_GUID);
		//Chargement de la banque
		for(String item : bank.split("\\|"))
		{
			if(item.equals(""))continue;
			String[] infos = item.split(":");
			int guid = Integer.parseInt(infos[0]);

			Objet obj = World.getObjet(guid);
			if( obj == null)continue;
			_bank.put(obj.getGuid(), obj);
		}
		//Chargement de la liste d'amie
		for(String f : friends.split(";"))
		{
			try
			{
				_friendGuids.add(Integer.parseInt(f));
			}catch(Exception E){};
		}
		//Chargement de la liste d'Enemy
		for(String f : enemy.split(";"))
		{
			try
			{
				_EnemyGuids.add(Integer.parseInt(f));
			}catch(Exception E){};
		}
		_regalo = regalo;
		this._points = points;
	}
	
	public void setVIP(int nbr)
	{
	  this._vip = nbr;
	}

	public int getRegalo() {
		return _regalo;
	}
	
	public void setRegalo() {
		_regalo = 0;
	}
	
	public void setRegalo(int regalo) {
		_regalo = regalo;
	}
	
	public void setBankKamas(long i)
	{
		_bankKamas = i;
		SQLManager.UPDATE_ACCOUNT_DATA(this);
	}
	public boolean isMuted()
	{
		return _mute;
	}
	
	public int puntos()
	{
		return _points;
	}
	public void points(int x)
	{
		_points=x;
		SQLManager.UPDATE_POINTS(_GUID,_points);
	}
	public int get_points()
	{
		_points = SQLManager.LOAD_POINTS(_name); 
		return _points;
	}

	public void mute(boolean Mute, int Temps) {
		_mute = Mute;
		String msg = "";
		if (_mute)
			msg = "Vous avez �t� mut�.";
		else
			msg = "Vous avez �t� d�muter.";
		SocketManager.GAME_SEND_MESSAGE(_curPerso, msg, CyonEmu.CONFIG_MOTD_COLOR);
		if (Temps == 0)
			return;
		_muteTime = Temps * 1000;
		_muteHeur = System.currentTimeMillis();
	}
	
	public String parseBankObjetsToDB()
	{
		StringBuilder str = new StringBuilder();
		if(_bank.isEmpty())return "";
		for(Entry<Integer,Objet> entry : _bank.entrySet())
		{
			Objet obj = entry.getValue();
			str.append(obj.getGuid()).append("|");
		}
		return str.toString();
	}
	
	public Map<Integer, Objet> getBank() {
		return _bank;
	}

	public long getBankKamas()
	{
		return _bankKamas;
	}

	public void setGameThread(GameThread t)
	{
		_gameThread = t;
	}
	
	public void setCurIP(String ip)
	{
		_curIP = ip;
	}
	
	public String getLastConnectionDate() {
		return _lastConnectionDate;
	}
	
	public void setLastIP(String _lastip) {
		_lastIP = _lastip;
	}

	public void setLastConnectionDate(String connectionDate) {
		_lastConnectionDate = connectionDate;
	}

	public GameThread getGameThread()
	{
		return _gameThread;
	}
	
	public RealmThread getRealmThread()
	{
		return _realmThread;
	}
	
	public int get_GUID() {
		return _GUID;
	}
	
	public String get_name() {
		return _name;
	}

	public String get_pass() {
		return _pass;
	}

	public String get_pseudo() {
		if (_pseudo.isEmpty() || _pseudo == "")
			_pseudo = _name;
		return _pseudo;
	}

	public String get_key() {
		return _key;
	}

	public void setClientKey(String aKey)
	{
		_key = aKey;
	}
	
	public Map<Integer, Personnage> get_persos() {
		return _persos;
	}

	public String get_lastIP() {
		return _lastIP;
	}

	public String get_question() {
		return _question;
	}

	public Personnage get_curPerso() {
		return _curPerso;
	}

	public String get_reponse() {
		return _reponse;
	}

	public boolean isBanned() {
		return _banned;
	}

	public void setBanned(boolean banned) {
		_banned = banned;
	}

	public boolean isOnline()
	{
		if(_gameThread != null)return true;
		if(_realmThread != null)return true;
		return false;
	}

	public int get_gmLvl() {
		return _gmLvl;
	}

	public String get_curIP() {
		return _curIP;
	}
	
	public boolean isValidPass(String pass,String hash)
	{
		return pass.equals(CryptManager.CryptPassword(hash, _pass));
	}
	
	public int GET_PERSO_NUMBER()
	{
		return _persos.size();
	}
	
	public static boolean COMPTE_LOGIN(String name, String pass, String key)
    {
            if(World.getCompteByName(name) != null)
            {
                    World.addAccount(SQLManager.REFRESH_ACCOUNT_INFOS(World.getCompteByName(name)));
            }
            if(World.getCompteByName(name) != null && World.getCompteByName(name).isValidPass(pass,key))
            {
            	return true;
            }else
            {
                    return false;
            }
    }

	public void addPerso(Personnage perso)
	{
		_persos.put(perso.get_GUID(),perso);
	}
	
	public boolean createPerso(String name, int sexe, int classe,int color1, int color2, int color3)
	{
		
		Personnage perso = Personnage.CREATE_PERSONNAGE(name, sexe, classe, color1, color2, color3, this);
		if(perso==null)
		{
			return false;
		}
		_persos.put(perso.get_GUID(), perso);
		return true;
	}

	public void deletePerso(int guid)
	{
		if(!_persos.containsKey(guid))return;
		World.deletePerso(_persos.get(guid));
		_persos.remove(guid);
	}

	public void setRealmThread(RealmThread thread)
	{
		_realmThread = thread;
	}

	public void setCurPerso(Personnage perso)
	{
		_curPerso = perso;
	}

	public void updateInfos(int aGUID,String aName,String aPass, String aPseudo,String aQuestion,String aReponse,int aGmLvl, boolean aBanned)
	{
		this._GUID 		= aGUID;
		this._name 		= aName;
		this._pass		= aPass;
		this._pseudo 	= aPseudo;
		this._question	= aQuestion;
		this._reponse	= aReponse;
		this._gmLvl		= aGmLvl;
		this._banned	= aBanned;
	}

	public void deconnexion()
	{
		_curPerso = null;
		_gameThread = null;
		_realmThread = null;
		_curIP = "";
		SQLManager.LOG_OUT(get_GUID(), 0);
		resetAllChars(true);
		SQLManager.UPDATE_ACCOUNT_DATA(this);
	}

	public void resetAllChars(boolean save)
	{
		for(Personnage P : _persos.values())
		{
			//Si Echange avec un joueur
			if(P.get_curExchange() != null)P.get_curExchange().cancel();
			//Si en groupe
			if(P.getGroup() != null)P.getGroup().leave(P);
			
			if(P.get_isTradingWith() > 0)
			{
				Personnage p = World.getPersonnage(P.get_isTradingWith());
				if(p != null)
				{
					if(p.isOnline())
					{
						PrintWriter out = p.get_compte().getGameThread().get_out();
						SocketManager.GAME_SEND_EV_PACKET(out);
						p.set_isTradingWith(0);
					}
				}
			}
			//Si en combat
			if(P.get_fight() != null)P.get_fight().leftFight(P, null);
			else//Si hors combat
			{
				P.get_curCell().removePlayer(P.get_GUID());
				if(P.get_curCarte() != null && P.isOnline())SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(P.get_curCarte(), P.get_GUID());
			}
			
			P.set_Online(false);
			//Reset des vars du perso
			P.resetVars();
			if(save)SQLManager.SAVE_PERSONNAGE(P,true);
			World.unloadPerso(P.get_GUID());
		}
		_persos.clear();
	}
	
	public String parseFriendList()
	{
		StringBuilder str = new StringBuilder();
		if(_friendGuids.isEmpty())return "";
		for(int i : _friendGuids)
		{
			Compte C = World.getCompte(i);
			if(C == null)continue;
			str.append("|").append(C.get_pseudo());
			//on s'arrete la si aucun perso n'est connect�
			if(!C.isOnline())continue;
			Personnage P = C.get_curPerso();
			if(P == null)continue;
			str.append(P.parseToFriendList(_GUID));
		}
		return str.toString();
	}
	
	public void SendOnline()
	{
		for (int i : _friendGuids)
		{
			if (this.isFriendWith(i))
			{
				Personnage perso = World.getPersonnage(i);
				if (perso != null && perso.is_showFriendConnection() && perso.isOnline())
				SocketManager.GAME_SEND_FRIEND_ONLINE(this._curPerso, perso);
			}
		}
	}

	public void addFriend(int guid)
	{
		if(_GUID == guid)
		{
			SocketManager.GAME_SEND_FA_PACKET(_curPerso,"Ey");
			return;
		}
		if(!_friendGuids.contains(guid))
		{
			_friendGuids.add(guid);
			SocketManager.GAME_SEND_FA_PACKET(_curPerso,"K"+World.getCompte(guid).get_pseudo()+World.getCompte(guid).get_curPerso().parseToFriendList(_GUID));
			SQLManager.UPDATE_ACCOUNT_DATA(this);
		}
		else SocketManager.GAME_SEND_FA_PACKET(_curPerso,"Ea");
	}
	
	public void removeFriend(int guid)
	{
		if(_friendGuids.remove((Object)guid))SQLManager.UPDATE_ACCOUNT_DATA(this);
		SocketManager.GAME_SEND_FD_PACKET(_curPerso,"K");
	}
	
	public boolean isFriendWith(int guid)
	{
		return _friendGuids.contains(guid);
	}
	
	public String parseFriendListToDB()
	{
		String str = "";
		for(int i : _friendGuids)
		{
			if(!str.equalsIgnoreCase(""))str += ";";
			str += i+"";
		}
		return str;
	}
	
	public void addEnemy(String packet, int guid)
	{
		if(_GUID == guid)
		{
			SocketManager.GAME_SEND_FA_PACKET(_curPerso,"Ey");
			return;
		}
		if(!_EnemyGuids.contains(guid))
		{
			_EnemyGuids.add(guid);
			Personnage Pr = World.getPersoByName(packet);
			SocketManager.GAME_SEND_ADD_ENEMY(_curPerso, Pr);
			SQLManager.UPDATE_ACCOUNT_DATA(this);
		}
		else SocketManager.GAME_SEND_iAEA_PACKET(_curPerso);
	}
	
	public void removeEnemy(int guid)
	{
		if(_EnemyGuids.remove((Object)guid))SQLManager.UPDATE_ACCOUNT_DATA(this);
		SocketManager.GAME_SEND_iD_COMMANDE(_curPerso,"K");
	}
	
	public boolean isEnemyWith(int guid)
	{
		return _EnemyGuids.contains(guid);
	}
	
	public String parseEnemyListToDB()
	{
		String str = "";
		for(int i : _EnemyGuids)
		{
			if(!str.equalsIgnoreCase(""))str += ";";
			str += i+"";
		}
		return str;
	}
	
	public String parseEnemyList() 
	{
		StringBuilder str = new StringBuilder();
		if(_EnemyGuids.isEmpty())return "";
		for(int i : _EnemyGuids)
		{
			Compte C = World.getCompte(i);
			if(C == null)continue;
			str.append("|").append(C.get_pseudo());
			//on s'arrete la si aucun perso n'est connect�
			if(!C.isOnline())continue;
			Personnage P = C.get_curPerso();
			if(P == null)continue;
			str.append(P.parseToEnemyList(_GUID));
		}
		return str.toString();
	}
	
	public void setGmLvl(int gmLvl)
	{
		_gmLvl = gmLvl;
	}

	public int get_vip() {
		return _vip;
	}
	
	public boolean recoverItem(int ligneID, int amount)
	{
		if(_curPerso == null)
			return false;
		if(_curPerso.get_isTradingWith() >= 0)
			return false;
		
		int hdvID = Math.abs(_curPerso.get_isTradingWith());//R�cup�re l'ID de l'HDV
		
		HdvEntry entry = null;
		for(HdvEntry tempEntry : _hdvsItems.get(hdvID))//Boucle dans la liste d'entry de l'HDV pour trouver un entry avec le meme cheapestID que sp�cifi�
		{
			if(tempEntry.getLigneID() == ligneID)//Si la boucle trouve un objet avec le meme cheapestID, arrete la boucle
			{
				entry = tempEntry;
				break;
			}
		}
		if(entry == null)//Si entry == null cela veut dire que la boucle s'est effectu� sans trouver d'item avec le meme cheapestID
			return false;
		
		_hdvsItems.get(hdvID).remove(entry);//Retire l'item de la liste des objets a vendre du compte

		Objet obj = entry.getObjet();
		
		boolean OBJ = _curPerso.addObjet(obj,true);//False = Meme item dans l'inventaire donc augmente la qua
		if(!OBJ)
		{
			World.removeItem(obj.getGuid());
		}
		
		World.getHdv(hdvID).delEntry(entry);//Retire l'item de l'HDV
			
		return true;
		//Hdv curHdv = World.getHdv(hdvID);
		
	}
	
	public HdvEntry[] getHdvItems(int hdvID)
	{
		if(_hdvsItems.get(hdvID) == null)
			return new HdvEntry[1];
		
		HdvEntry[] toReturn = new HdvEntry[20];
		for (int i = 0; i < _hdvsItems.get(hdvID).size(); i++)
		{
			toReturn[i] = _hdvsItems.get(hdvID).get(i);
		}
		return toReturn;
	}
	
	public int countHdvItems(int hdvID)
	{
		if(_hdvsItems.get(hdvID) == null)
			return 0;
		
		return _hdvsItems.get(hdvID).size();
	}
}
