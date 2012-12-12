package objects;

import common.Constants;
import common.CyonEmu;
import common.Formulas;
import common.SQLManager;
import common.SocketManager;
import common.World;

import game.GameServer;
import game.GameThread;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Timer;

public class Metier
{
  private int _id;
  private ArrayList<Integer> _tools = new ArrayList<Integer>();
  private Map<Integer, ArrayList<Integer>> _crafts = new TreeMap<Integer, ArrayList<Integer>>();

  public Metier(int id, String tools, String crafts)
  {
    _id = id;
    if (!tools.equals(""))
    {
      for (String str : tools.split(","))
      {
        try
        {
          int tool = Integer.parseInt(str);
          _tools.add(Integer.valueOf(tool));
        } catch (Exception localException) {
        }
      }
    }
    if (!crafts.equals(""))
    {
      for (String str : crafts.split("\\|"))
      {
        try
        {
          int skID = Integer.parseInt(str.split(";")[0]);
          ArrayList<Integer> list = new ArrayList<Integer>();
          for (String str2 : str.split(";")[1].split(",")) list.add(Integer.valueOf(Integer.parseInt(str2)));
          _crafts.put(Integer.valueOf(skID), list); } catch (Exception localException1) {
        }
      }
    }
  }

  public ArrayList<Integer> getListBySkill(int skID) {
    return (ArrayList<Integer>)_crafts.get(Integer.valueOf(skID));
  }

	public boolean canCraft(int skill, int template) {
		if (_crafts.get(skill) != null) {
			for (int a : _crafts.get(skill)) {
				if (a == template) {
					return true;
				}
			}
		}
		return false;
	}

  public int getId()
  {
    return _id;
  }

	public boolean isValidTool(int t) {
		for (int a : _tools) {
			if (t == a) {
				return true;
			}
		}
		return false;
	}

  public static byte ViewActualStatsItem(Objet obj, String stats) {
    if (!obj.parseStatsString().isEmpty())
    {
      for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet())
      {
        if (Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo(stats) > 0)
        {
          if ((Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo("98") == 0) && (stats.compareTo("7b") == 0))
          {
            return 2;
          }
          if ((Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo("9a") == 0) && (stats.compareTo("77") == 0))
          {
            return 2;
          }
          if ((Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo("9b") == 0) && (stats.compareTo("7e") == 0))
          {
            return 2;
          }
          if ((Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo("9d") == 0) && (stats.compareTo("76") == 0))
          {
            return 2;
          }
          if ((Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo("74") == 0) && (stats.compareTo("75") == 0))
          {
            return 2;
          }
          if ((Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo("99") == 0) && (stats.compareTo("7d") == 0))
          {
            return 2;
          }

        }
        else if (Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo(stats) == 0)
        {
          return 1;
        }
      }
      return 0;
    }

    return 0;
  }

  public static byte ViewBaseStatsItem(Objet obj, String ItemStats)
  {
    String[] splitted = obj.getTemplate().getStrTemplate().split(",");
    for (String s : splitted)
    {
      String[] stats = s.split("#");
      if (stats[0].compareTo(ItemStats) > 0)
      {
        if ((stats[0].compareTo("98") == 0) && (ItemStats.compareTo("7b") == 0))
        {
          return 2;
        }
        if ((stats[0].compareTo("9a") == 0) && (ItemStats.compareTo("77") == 0))
        {
          return 2;
        }
        if ((stats[0].compareTo("9b") == 0) && (ItemStats.compareTo("7e") == 0))
        {
          return 2;
        }
        if ((stats[0].compareTo("9d") == 0) && (ItemStats.compareTo("76") == 0))
        {
          return 2;
        }
        if ((stats[0].compareTo("74") == 0) && (ItemStats.compareTo("75") == 0))
        {
          return 2;
        }
        if ((stats[0].compareTo("99") == 0) && (ItemStats.compareTo("7d") == 0))
        {
          return 2;
        }

      }
      else if (stats[0].compareTo(ItemStats) == 0)
      {
        return 1;
      }
    }
    return 0;
  }

  public static int getBaseMaxJet(int templateID, String statsModif)
  {
    Objet.ObjTemplate t = World.getObjTemplate(templateID);
    String[] splitted = t.getStrTemplate().split(",");
    for (String s : splitted)
    {
      String[] stats = s.split("#");
      if (stats[0].compareTo(statsModif) > 0)
      {
        continue;
      }
      if (stats[0].compareTo(statsModif) != 0)
        continue;
      int max = Integer.parseInt(stats[2], 16);
      if (max == 0) max = Integer.parseInt(stats[1], 16);
      return max;
    }

    return 0;
  }

  public static int getActualJet(Objet obj, String statsModif)
  {
    for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet())
    {
      if (Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo(statsModif) > 0)
      {
        continue;
      }
      if (Integer.toHexString(((Integer)entry.getKey()).intValue()).compareTo(statsModif) != 0)
        continue;
      int JetActual = ((Integer)entry.getValue()).intValue();
      return JetActual;
    }

    return 0;
  }

  public static class JobAction
  {
    private int _skID;
    private int _min = 1;
    private int _max = 1;
    private boolean _isCraft;
    private int _chan = 100;
    private int _time = 0;
    private int _xpWin = 0;
    private long _startTime;
    private Map<Integer, Integer> _ingredients = new TreeMap<Integer, Integer>();
    private Map<Integer, Integer> _lastCraft = new TreeMap<Integer, Integer>();
    private Timer _craftTimer;
    private Personnage _P;

    public JobAction(int sk, int min, int max, boolean craft, int arg, int xpWin)
    {
      _skID = sk;
      _min = min;
      _max = max;
      _isCraft = craft;
      if (craft) _chan = arg; else
        _time = arg;
      _xpWin = xpWin;

      _craftTimer = new Timer(100, new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          craft();
          _craftTimer.stop();
        }
      });
    }

    public void endAction(Personnage P, Carte.InteractiveObject IO, GameThread.GameAction GA, Carte.Case cell) {
        if (!_isCraft)
        {
          if (_startTime - System.currentTimeMillis() > 500L) return;
          IO.setState(3);
          IO.startTimer();

          SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.get_curCarte(), cell);

          boolean special = Formulas.getRandomValue(0, 99) == 0;

          int qua = _max > _min ? Formulas.getRandomValue(_min, _max) : _min;
          int tID = Constants.getObjectByJobSkill(_skID, special);

          Objet.ObjTemplate T = World.getObjTemplate(tID);
          if (T == null) return;
          Objet O = T.createNewItem(qua, false);

          if (P.addObjet(O, true))
            World.addObjet(O, true);
          SocketManager.GAME_SEND_IQ_PACKET(P, P.get_GUID(), qua);
          SocketManager.GAME_SEND_Ow_PACKET(P);
  		int maxPercent = 20+(P.getMetierBySkill(_skID).get_lvl()-20);//40(fixe)+(lvl metier - 20)
  		if(P.getMetierBySkill(_skID).get_lvl() >= 20 && Formulas.getRandomValue(1, maxPercent) == maxPercent)
  		{
  			int[][] protectors = Constants.JOB_PROTECTORS;
  			for(int i = 0; i < protectors.length; i++)
  			{
  				if(tID == protectors[i][1])
  				{
  					int monsterId = protectors[i][0];
  					int monsterLvl = Constants.getProtectorLvl(P.get_lvl());		
  					P.get_curCarte().startFigthVersusMonstres(P, new Monstre.MobGroup(P.get_curCarte()._nextObjectID, cell.getID(), monsterId+","+monsterLvl+","+monsterLvl));
  					break;
  				}
  			}
  		}
  	}
  }

    public void startAction(Personnage P, Carte.InteractiveObject IO, GameThread.GameAction GA, Carte.Case cell)
    {
      _P = P;
      if (!_isCraft)
      {
        IO.setInteractive(false);
        IO.setState(2);
        SocketManager.GAME_SEND_GA_PACKET_TO_MAP(P.get_curCarte(), ""+GA._id, 501, ""+P.get_GUID(), cell.getID() + "," + _time);
        SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.get_curCarte(), cell);
        _startTime = (System.currentTimeMillis() + _time);
      }
      else {
        P.set_away(true);
        IO.setState(2);
        P.setCurJobAction(this);
        SocketManager.GAME_SEND_ECK_PACKET(P, 3, _min + ";" + _skID);
        SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.get_curCarte(), cell);
      }
    }

    public int getSkillID()
    {
      return _skID;
    }

    public int getMin() {
      return _min;
    }

    public int getXpWin() {
      return _xpWin;
    }

    public int getMax() {
      return _max;
    }

    public int getChance() {
      return _chan;
    }

    public int getTime() {
      return _time;
    }

    public boolean isCraft() {
      return _isCraft;
    }

    public void modifIngredient(Personnage P, int guid, int qua)
    {
      int q = _ingredients.get(Integer.valueOf(guid)) == null ? 0 : ((Integer)_ingredients.get(Integer.valueOf(guid))).intValue();

      _ingredients.remove(Integer.valueOf(guid));

      q += qua;
      if (q > 0)
      {
        _ingredients.put(Integer.valueOf(guid), Integer.valueOf(q));
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P, 'O', "+", guid + "|" + q); } else {
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P, 'O', "-", ""+guid);
      }
    }

    public void craft() {
      if (!_isCraft) return;
      boolean signed = false;
      try
      {
        Thread.sleep(250L);
      } catch (Exception localException) {
      }
      if ((_skID == 1) || 
        (_skID == 113) || 
        (_skID == 115) || 
        (_skID == 116) || 
        (_skID == 117) || 
        (_skID == 118) || 
        (_skID == 119) || 
        (_skID == 120) || (
        (_skID >= 163) && (_skID <= 169)))
      {
        doFmCraft();
        return;
      }

      Map<Integer, Integer> items = new TreeMap<Integer, Integer>();

      for (Entry<Integer, Integer> e : _ingredients.entrySet())
      {
        if (!_P.hasItemGuid(((Integer)e.getKey()).intValue()))
        {
          SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
          GameServer.addToLog("/!\\ " + _P.get_name() + " essaye de crafter avec un objet qu'il n'a pas");
          return;
        }

        Objet obj = World.getObjet(((Integer)e.getKey()).intValue());
        if (obj == null)
        {
          SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
          GameServer.addToLog("/!\\ " + _P.get_name() + " essaye de crafter avec un objet qui n'existe pas");
          return;
        }

        if (obj.getQuantity() < ((Integer)e.getValue()).intValue())
        {
          SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
          GameServer.addToLog("/!\\ " + _P.get_name() + " essaye de crafter avec un objet dont la quantite est trop faible");
          return;
        }

        int newQua = obj.getQuantity() - ((Integer)e.getValue()).intValue();

        if (newQua < 0) return;
        if (newQua == 0)
        {
          _P.removeItem(((Integer)e.getKey()).intValue());
          World.removeItem(((Integer)e.getKey()).intValue());
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, ((Integer)e.getKey()).intValue());
        }
        else {
          obj.setQuantity(newQua);
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, obj);
        }

        items.put(Integer.valueOf(obj.getTemplate().getID()), (Integer)e.getValue());
      }

      if (items.containsKey(Integer.valueOf(7508))) signed = true;
      items.remove(Integer.valueOf(7508));

      SocketManager.GAME_SEND_Ow_PACKET(_P);

      Metier.StatsMetier SM = _P.getMetierBySkill(_skID);
      int tID = World.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(_skID), items);

      if ((tID == -1) || (!SM.getTemplate().canCraft(_skID, tID)))
      {
        SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "-");
        _ingredients.clear();

        return;
      }

      int chan = Constants.getChanceByNbrCaseByLvl(SM.get_lvl(), _ingredients.size());
      int jet = Formulas.getRandomValue(1, 100);
      boolean success = chan >= jet;

      if (!success)
      {
        SocketManager.GAME_SEND_Ec_PACKET(_P, "EF");
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "-" + tID);
        SocketManager.GAME_SEND_Im_PACKET(_P, "0118");
      }
      else {
        Objet newObj = World.getObjTemplate(tID).createNewItem(1, false);

        if (signed) newObj.addTxtStat(988, _P.get_name());
        boolean add = true;
        int guid = newObj.getGuid();

        for (Entry<Integer, Objet> entry : _P.getItems().entrySet())
        {
          Objet obj = (Objet)entry.getValue();
          if ((obj.getTemplate().getID() != newObj.getTemplate().getID()) || 
            (!obj.getStats().isSameStats(newObj.getStats())) || 
            (obj.getPosition() != -1))
            continue;
          obj.setQuantity(obj.getQuantity() + newObj.getQuantity());
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, obj);
          add = false;
          guid = obj.getGuid();
        }

        if (add)
        {
          _P.getItems().put(Integer.valueOf(newObj.getGuid()), newObj);
          SocketManager.GAME_SEND_OAKO_PACKET(_P, newObj);
          World.addObjet(newObj, true);
        }

        SocketManager.GAME_SEND_Ow_PACKET(_P);
        SocketManager.GAME_SEND_Em_PACKET(_P, "KO+" + guid + "|1|" + tID + "|" + newObj.parseStatsString().replace(";", "#"));
        SocketManager.GAME_SEND_Ec_PACKET(_P, "K;" + tID);
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "+" + tID);
      }

      int winXP = Constants.calculXpWinCraft(SM.get_lvl(), _ingredients.size()) * CyonEmu.XP_METIER;
      if (success)
      {
        SM.addXp(_P, winXP);
        ArrayList<StatsMetier> SMs = new ArrayList<StatsMetier>();
        SMs.add(SM);
        SocketManager.GAME_SEND_JX_PACKET(_P, SMs);
      }

      _lastCraft.clear();
      _lastCraft.putAll(_ingredients);
      _ingredients.clear();
    }

    private void doFmCraft()
    {
      boolean signed = false;
      Objet obj = null; Objet sign = null; Objet mod = null;
      int isElementChanging = 0; int stat = -1; int isStatsChanging = 0; int add = 0;
      double poid = 0.0D;
      String stats = "-1";
      int runeID = -1;
		for (Entry<Integer,Integer> ingID : _ingredients.entrySet()) {
            int guid = ingID.getKey();
        Objet ing = World.getObjet(guid);
        if ((!_P.hasItemGuid(guid)) || (ing == null))
        {
          SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
          SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "-");
          _ingredients.clear();
          return;
        }
        int id = ing.getTemplate().getID();
        runeID = id;
        switch (id)
        {
        case 1333:
          stat = 99;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1335:
          stat = 96;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1337:
          stat = 98;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1338:
          stat = 97;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1340:
          stat = 97;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1341:
          stat = 96;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1342:
          stat = 98;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1343:
          stat = 99;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1345:
          stat = 99;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1346:
          stat = 96;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1347:
          stat = 98;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1348:
          stat = 97;
          isElementChanging = ing.getTemplate().getLevel();
          mod = ing;
          break;
        case 1519:
          mod = ing;
          stats = "76";
          add = 1;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1521:
          mod = ing;
          stats = "7c";
          add = 1;
          poid = 3.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1522:
          mod = ing;
          stats = "7e";
          add = 1;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1523:
          mod = ing;
          stats = "7d";
          add = 3;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1524:
          mod = ing;
          stats = "77";
          add = 1;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1525:
          mod = ing;
          stats = "7b";
          add = 1;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1545:
          mod = ing;
          stats = "77";
          add = 3;
          poid = 3.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1546:
          mod = ing;
          stats = "7c";
          add = 3;
          poid = 9.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1547:
          mod = ing;
          stats = "7e";
          add = 3;
          poid = 3.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1548:
          mod = ing;
          stats = "7d";
          add = 10;
          poid = 3.3D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1549:
          mod = ing;
          stats = "77";
          add = 3;
          poid = 3.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1550:
          mod = ing;
          stats = "7b";
          add = 3;
          poid = 3.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1551:
          mod = ing;
          stats = "76";
          add = 10;
          poid = 10.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1552:
          mod = ing;
          stats = "7c";
          add = 10;
          poid = 30.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1553:
          mod = ing;
          stats = "7e";
          add = 10;
          poid = 10.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1554:
          mod = ing;
          stats = "7d";
          add = 30;
          poid = 10.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1555:
          mod = ing;
          stats = "77";
          add = 10;
          poid = 10.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1556:
          mod = ing;
          stats = "7b";
          add = 10;
          poid = 10.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1557:
          mod = ing;
          stats = "6f";
          add = 1;
          poid = 100.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 1558:
          mod = ing;
          stats = "80";
          add = 1;
          poid = 90.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7433:
          mod = ing;
          stats = "73";
          add = 1;
          poid = 30.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7434:
          mod = ing;
          stats = "b2";
          add = 1;
          poid = 20.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7435:
          mod = ing;
          stats = "70";
          add = 1;
          poid = 20.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7436:
          mod = ing;
          stats = "8a";
          add = 1;
          poid = 2.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7437:
          mod = ing;
          stats = "dc";
          add = 1;
          poid = 2.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7438:
          mod = ing;
          stats = "75";
          add = 1;
          poid = 50.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7442:
          mod = ing;
          stats = "b6";
          add = 1;
          poid = 30.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7443:
          mod = ing;
          stats = "9e";
          add = 10;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7444:
          mod = ing;
          stats = "9e";
          add = 30;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7445:
          mod = ing;
          stats = "9e";
          add = 100;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7446:
          mod = ing;
          stats = "e1";
          add = 1;
          poid = 15.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7447:
          mod = ing;
          stats = "e2";
          add = 1;
          poid = 2.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7448:
          mod = ing;
          stats = "ae";
          add = 10;
          poid = 1.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7449:
          mod = ing;
          stats = "ae";
          add = 30;
          poid = 3.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7450:
          mod = ing;
          stats = "ae";
          add = 100;
          poid = 10.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7451:
          mod = ing;
          stats = "b0";
          add = 1;
          poid = 3.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7452:
          mod = ing;
          stats = "f3";
          add = 1;
          poid = 4.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7453:
          mod = ing;
          stats = "f2";
          add = 1;
          poid = 4.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7454:
          mod = ing;
          stats = "f1";
          add = 1;
          poid = 4.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7455:
          mod = ing;
          stats = "f0";
          add = 1;
          poid = 4.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7456:
          mod = ing;
          stats = "f4";
          add = 1;
          poid = 4.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7457:
          mod = ing;
          stats = "d5";
          add = 1;
          poid = 5.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7458:
          mod = ing;
          stats = "d4";
          add = 1;
          poid = 5.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7459:
          mod = ing;
          stats = "d2";
          add = 1;
          poid = 5.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7460:
          mod = ing;
          stats = "d6";
          add = 1;
          poid = 5.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7560:
          mod = ing;
          stats = "d3";
          add = 1;
          poid = 5.0D;
          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 8379:
          mod = ing;

          isStatsChanging = ing.getTemplate().getLevel();
          break;
        case 7508:
          signed = true;
          sign = ing;
          break;
        default:
          if (ing.getTemplate().getPACost() > 0) obj = ing;
          if ((ing.getTemplate().getType() != 1) && 
            (ing.getTemplate().getType() != 2) && 
            (ing.getTemplate().getType() != 3) && 
            (ing.getTemplate().getType() != 4) && 
            (ing.getTemplate().getType() != 5) && 
            (ing.getTemplate().getType() != 6) && 
            (ing.getTemplate().getType() != 7) && 
            (ing.getTemplate().getType() != 8) && 
            (ing.getTemplate().getType() != 9) && 
            (ing.getTemplate().getType() != 10) && 
            (ing.getTemplate().getType() != 11) && 
            (ing.getTemplate().getType() != 16) && 
            (ing.getTemplate().getType() != 17) && 
            (ing.getTemplate().getType() != 19) && 
            (ing.getTemplate().getType() != 20) && 
            (ing.getTemplate().getType() != 21) && 
            (ing.getTemplate().getType() != 22) && 
            (ing.getTemplate().getType() != 81) && 
            (ing.getTemplate().getType() != 102) && 
            (ing.getTemplate().getType() != 114)) continue; obj = ing;
        }
      }

      Metier.StatsMetier SM = _P.getMetierBySkill(_skID);

      if ((SM == null) || (obj == null) || (mod == null))
      {
        SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "-");
        _ingredients.clear();
        return;
      }
      
      if (SM.get_lvl() * 2 < obj.getTemplate().getLevel())
      {
        isElementChanging = 0;
        isStatsChanging = 0;
      }

      int chan = 0;
      double Coef;
      if ((isElementChanging > 0) && (isStatsChanging == 0))
      {
        chan = Formulas.calculElementChangeChance(SM.get_lvl(), obj.getTemplate().getLevel(), isElementChanging);

        if (chan > 100 - SM.get_lvl() / 20) chan = 100 - SM.get_lvl() / 20;
        if (chan < SM.get_lvl() / 20) chan = SM.get_lvl() / 20;
      }
      else if ((isStatsChanging > 0) && (isElementChanging == 0))
      {
        int poidActual = 1;
        int ActualJet = 1;
        int nivelOficio = SM.get_lvl();
        if (!obj.parseStatsString().isEmpty())
        {
          poidActual = Objet.getPoidOfActualItem(obj.parseStatsString().replace(";", "#"));
          ActualJet = Metier.getActualJet(obj, stats);
        }
        int poidBase = Objet.getPoidOfBaseItem(obj.getTemplate().getID());
        int BaseMaxJet = Metier.getBaseMaxJet(obj.getTemplate().getID(), stats);
        //int Puis = poidBase - poidActual;

        if (poidBase <= 0)
        {
          poidBase = 0;
        }
        if (BaseMaxJet <= 0)
        {
          BaseMaxJet = 0;
        }
        if (ActualJet <= 0)
        {
          ActualJet = 0;
        }
        if (poidActual <= 0)
        {
          poidActual = 0;
        }
        if (poid <= 0.0D)
        {
          poid = 0.0D;
        }

        Coef = 1.0D;
        if (((Metier.ViewBaseStatsItem(obj, stats) == 1) && (Metier.ViewActualStatsItem(obj, stats) == 1)) || ((Metier.ViewBaseStatsItem(obj, stats) == 1) && (Metier.ViewActualStatsItem(obj, stats) == 0)))
        {
          Coef = 1.0D;
        } else if ((Metier.ViewBaseStatsItem(obj, stats) == 2) && (Metier.ViewActualStatsItem(obj, stats) == 2))
        {
          Coef = 0.75D;
        } else if (((Metier.ViewBaseStatsItem(obj, stats) == 0) && (Metier.ViewActualStatsItem(obj, stats) == 0)) || ((Metier.ViewBaseStatsItem(obj, stats) == 0) && (Metier.ViewActualStatsItem(obj, stats) == 1)))
        {
          Coef = 0.25D;
        }

        double JetMax = BaseMaxJet * (2 - obj.getTemplate().getLevel() / 100);
        if (JetMax <= 0.0D) JetMax = 1.0D;

        Coef *= (JetMax - ActualJet) / 25.0D;
        if (Coef <= 0.0D) Coef = 0.0D;
        if (runeID == 1557 || runeID == 1558 )
    	{
    	chan = 20 - (int) (Math.sqrt(130 - nivelOficio) * 2D);
    	}else
    	chan = 20 - (int) (Math.sqrt(80 - nivelOficio) * 2D);

        if (chan <= 0)
		chan = 1;
	    if (chan >= 60)
		chan = 60;
	
	    SocketManager.GAME_SEND_MESSAGE(_P,
		"Votre chance de succès était de " + chan + "%",
		"009900");
      }

      int jet = Formulas.getRandomValue(1, 100);
      boolean success = chan >= jet;
      int tID = obj.getTemplate().getID();

      if ((success) && 
        (Formulas.getRandomValue(1, 5) == 3)) success = false;

      if (!success)
      {
        String statsnegatif = "";
        if (Metier.ViewBaseStatsItem(obj, "98") == 1)
        {
          if ((Metier.ViewActualStatsItem(obj, "98") == 0) && (Metier.ViewActualStatsItem(obj, "7b") == 0))
          {
            statsnegatif = statsnegatif + ",98#" + Integer.toHexString(1) + "#0#0#0d0+1";
          }
        }
        if (Metier.ViewBaseStatsItem(obj, "9a") == 1)
        {
          if ((Metier.ViewActualStatsItem(obj, "9a") == 0) && (Metier.ViewActualStatsItem(obj, "77") == 0))
          {
            statsnegatif = statsnegatif + ",9a#" + Integer.toHexString(1) + "#0#0#0d0+1";
          }
        }
        if (Metier.ViewBaseStatsItem(obj, "9b") == 1)
        {
          if ((Metier.ViewActualStatsItem(obj, "9b") == 0) && (Metier.ViewActualStatsItem(obj, "7e") == 0))
          {
            statsnegatif = statsnegatif + ",9b#" + Integer.toHexString(1) + "#0#0#0d0+1";
          }
        }
        if (Metier.ViewBaseStatsItem(obj, "9d") == 1)
        {
          if ((Metier.ViewActualStatsItem(obj, "9d") == 0) && (Metier.ViewActualStatsItem(obj, "76") == 0))
          {
            statsnegatif = statsnegatif + ",9d#" + Integer.toHexString(1) + "#0#0#0d0+1";
          }
        }
        if (Metier.ViewBaseStatsItem(obj, "74") == 1)
        {
          if ((Metier.ViewActualStatsItem(obj, "74") == 0) && (Metier.ViewActualStatsItem(obj, "75") == 0))
          {
            statsnegatif = statsnegatif + ",74#" + Integer.toHexString(1) + "#0#0#0d0+1";
          }
        }
        if (Metier.ViewBaseStatsItem(obj, "99") == 1)
        {
          if ((Metier.ViewActualStatsItem(obj, "99") == 0) && (Metier.ViewActualStatsItem(obj, "7d") == 0))
          {
            statsnegatif = statsnegatif + ",99#" + Integer.toHexString(1) + "#0#0#0d0+1";
          }
        }
        if ((obj.parseStatsString().isEmpty()) && (!statsnegatif.isEmpty()))
        {
          obj.setStats(obj.generateNewStatsFromTemplate(statsnegatif.substring(1), false));
        }
        else if (!obj.parseStatsString().isEmpty())
        {
          obj.setStats(obj.generateNewStatsFromTemplate(obj.parseFMEchecStatsString(obj, poid).replace(";", "#") + statsnegatif, false));
        }
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, obj.getGuid());
        SocketManager.GAME_SEND_Ow_PACKET(_P);
        SocketManager.GAME_SEND_OAKO_PACKET(_P, obj);
        SocketManager.GAME_SEND_Em_PACKET(_P, "EO+" + obj.getGuid() + "|1|" + tID + "|" + obj.parseStatsString().replace(";", "#"));
        SocketManager.GAME_SEND_Ec_PACKET(_P, "EF");
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "-" + tID);
        SocketManager.GAME_SEND_Im_PACKET(_P, "0183");

        if (_P.getMetierBySkill(getSkillID()).get_lvl() <= 99)
          _P.getMetierBySkill(getSkillID()).addXp(_P, 50L);
      }
      else {
        int coef = 0;
        if (isElementChanging == 1) coef = 50;
        if (isElementChanging == 25) coef = 65;
        if (isElementChanging == 50) coef = 85;

        if (signed) obj.addTxtStat(985, _P.get_name());

        if ((isElementChanging > 0) && (isStatsChanging == 0))
        {
          for (SpellEffect SE : obj.getEffects())
          {
            if (SE.getEffectID() == 100) {
              String[] infos = SE.getArgs().split(";");
              try
              {
                int min = Integer.parseInt(infos[0], 16);
                int max = Integer.parseInt(infos[1], 16);
                int newMin = min * coef / 100;
                int newMax = max * coef / 100;

                if (newMin == 0) newMin = 1;
                String newJet = "1d" + (newMax - newMin + 1) + "+" + (newMin - 1);
                String newArgs = Integer.toHexString(newMin) + ";" + Integer.toHexString(newMax) + ";-1;-1;0;" + newJet;

                SE.setArgs(newArgs);
                SE.setEffectID(stat);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        } else if ((isStatsChanging > 0) && (isElementChanging == 0))
        {
          System.out.println("Changement de STATS");
          System.out.println("Chance : " + chan);
          System.out.println("Element a modifier : " + stats);

          boolean negatif = false;

          if (Metier.ViewActualStatsItem(obj, stats) == 2)
          {
            if (stats.compareTo("7b") == 0) {
              stats = "98";
              negatif = true;
            }
            if (stats.compareTo("77") == 0) {
              stats = "9a";
              negatif = true;
            }
            if (stats.compareTo("7e") == 0) {
              stats = "9b";
              negatif = true;
            }
            if (stats.compareTo("76") == 0) {
              stats = "9d";
              negatif = true;
            }
            if (stats.compareTo("75") == 0) {
              stats = "74";
              negatif = true;
            }
            if (stats.compareTo("7d") == 0) {
              stats = "99";
              negatif = true;
            }

          }

          if ((Metier.ViewActualStatsItem(obj, stats) == 1) || (Metier.ViewActualStatsItem(obj, stats) == 2))
          {
            if (CyonEmu.CONFIG_DEBUG) System.out.println("Modification d'un stat existant : " + stats + ". Ajout de " + add);
            obj.setStats(obj.generateNewStatsFromTemplate(obj.parseFMStatsString(stats, obj, add, negatif).replace(";", "#"), false));
          }
          else
          {
            if (CyonEmu.CONFIG_DEBUG) System.out.println("Ajout d'un stat inexistant : " + stats + ". Ajout de " + add);
            if (obj.parseStatsString().isEmpty())
            {
              obj.setStats(obj.generateNewStatsFromTemplate(stats + "#" + Integer.toHexString(add) + "#0#0#0d0+" + add, false));
            }
            else
            {
              obj.setStats(obj.generateNewStatsFromTemplate(obj.parseFMStatsString(stats, obj, add, negatif).replace(";", "#") + "," + stats + "#" + Integer.toHexString(add) + "#0#0#0d0+" + add, false));
            }
          }

        }

        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, obj.getGuid());
        SocketManager.GAME_SEND_Ow_PACKET(_P);
        SocketManager.GAME_SEND_OAKO_PACKET(_P, obj);
        SocketManager.GAME_SEND_Em_PACKET(_P, "KO+" + obj.getGuid() + "|1|" + tID + "|" + obj.parseStatsString().replace(";", "#"));
        SocketManager.GAME_SEND_Ec_PACKET(_P, "K;" + tID);
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "+" + tID);
        SQLManager.SAVE_ITEM(obj);

        if (_P.getMetierBySkill(getSkillID()).get_lvl() <= 99) {
          _P.getMetierBySkill(getSkillID()).addXp(_P, 150L);
        }

      }

      if (sign != null)
      {
        int newQua = sign.getQuantity() - 1;

        if (newQua <= 0)
        {
          _P.removeItem(sign.getGuid());
          World.removeItem(sign.getGuid());
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, sign.getGuid());
        }
        else {
          sign.setQuantity(newQua);
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, sign);
        }
      }

      if (mod != null)
      {
        int newQua = mod.getQuantity() - 1;

        if (newQua <= 0)
        {
          _P.removeItem(mod.getGuid());
          World.removeItem(mod.getGuid());
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, mod.getGuid());
        }
        else {
          mod.setQuantity(newQua);
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, mod);
        }

      }

      _lastCraft.clear();
      _lastCraft.putAll(_ingredients);
      _ingredients.clear();
    }

    public void repeat(int time, Personnage P)
    {
      _craftTimer.stop();

      _lastCraft.clear();
      _lastCraft.putAll(_ingredients);
      for (int a = time; a >= 0; a--)
      {
        SocketManager.GAME_SEND_EA_PACKET(P, ""+a);
        _ingredients.clear();
        _ingredients.putAll(_lastCraft);
        craft();
      }
      SocketManager.GAME_SEND_Ea_PACKET(P, "1");
    }

    public void startCraft(Personnage P)
    {
      _craftTimer.start();
    }

    public void putLastCraftIngredients()
    {
      if (_P == null) return;
      if (_lastCraft == null) return;
      if (!_ingredients.isEmpty()) return;
      _ingredients.clear();
      _ingredients.putAll(_lastCraft);
      for (Entry<Integer, Integer> e : _ingredients.entrySet())
      {
        if (World.getObjet(((Integer)e.getKey()).intValue()) == null) return;
        if (World.getObjet(((Integer)e.getKey()).intValue()).getQuantity() < ((Integer)e.getValue()).intValue()) return;
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(_P, 'O', "+", e.getKey() + "|" + e.getValue());
      }
    }

    public void resetCraft()
    {
      _ingredients.clear();
      _lastCraft.clear();
    }
  }

  public static class StatsMetier
  {
    private int _id;
    private Metier _template;
    private int _lvl;
    private long _xp;
    private ArrayList<Metier.JobAction> _posActions = new ArrayList<JobAction>();
    private boolean _isCheap = false;
    private boolean _freeOnFails = false;
    private boolean _noRessource = false;
    private Metier.JobAction _curAction;
    private int _slotsPublico;
    private int _posicion;

    public StatsMetier(int id, Metier tp, int lvl, long xp)
    {
      _id = id;
      _template = tp;
      _lvl = lvl;
      _xp = xp;
      _posActions = Constants.getPosActionsToJob(tp.getId(), lvl);
    }

    public int get_lvl() {
      return _lvl;
    }
    public boolean isCheap() {
      return _isCheap;
    }

    public void setIsCheap(boolean isCheap) {
      _isCheap = isCheap;
    }

    public boolean isFreeOnFails() {
      return _freeOnFails;
    }

    public void setFreeOnFails(boolean freeOnFails) {
      _freeOnFails = freeOnFails;
    }

    public boolean isNoRessource() {
      return _noRessource;
    }

    public void setNoRessource(boolean noRessource) {
      _noRessource = noRessource;
    }

    public void setSlotsPublico(int slots) {
		_slotsPublico = slots;
	}
	
	public int getSlotsPublico() {
		return _slotsPublico;
	}
	
	public int getPosicion() {
		return _posicion;
	}
	
	public void levelUp(Personnage P, boolean send)
    {
      _lvl += 1;
      _posActions = Constants.getPosActionsToJob(_template.getId(), _lvl);

      if (send)
      {
        ArrayList<StatsMetier> list = new ArrayList<StatsMetier>();
        list.add(this);
        SocketManager.GAME_SEND_JS_PACKET(P, list);
        SocketManager.GAME_SEND_STATS_PACKET(P);
        SocketManager.GAME_SEND_Ow_PACKET(P);
        SocketManager.GAME_SEND_JN_PACKET(P, _template.getId(), _lvl);
        SocketManager.GAME_SEND_JO_PACKET(P, list);
      }
    }

    public String parseJS() {
      String str = "|" + _template.getId() + ";";
      boolean first = true;
      for (Metier.JobAction JA : _posActions)
      {
        if (!first) str = str + ","; else
          first = false;
        str = str + JA.getSkillID() + "~" + JA.getMin() + "~";
        if (JA.isCraft()) str = str + "0~0~" + JA.getChance(); else
          str = str + JA.getMax() + "~0~" + JA.getTime();
      }
      return str;
    }

    public long getXp() {
      return _xp;
    }

    public void startAction(int id, Personnage P, Carte.InteractiveObject IO, GameThread.GameAction GA, Carte.Case cell)
    {
      for (Metier.JobAction JA : _posActions)
      {
        if (JA.getSkillID() != id)
          continue;
        _curAction = JA;
        JA.startAction(P, IO, GA, cell);
        return;
      }
    }

    public void endAction(int id, Personnage P, Carte.InteractiveObject IO, GameThread.GameAction GA, Carte.Case cell)
    {
      if (_curAction == null) return;
      _curAction.endAction(P, IO, GA, cell);
      addXp(P, _curAction.getXpWin() * CyonEmu.XP_METIER);

      ArrayList<StatsMetier> list = new ArrayList<StatsMetier>();
      list.add(this);
      SocketManager.GAME_SEND_JX_PACKET(P, list);
    }

    public void addXp(Personnage P, long xp)
    {
      if (_lvl > 99) return;
      int exLvl = _lvl;
      _xp += xp;

      while ((_xp >= World.getExpLevel(_lvl + 1).metier) && (_lvl < 100)) {
        levelUp(P, false);
      }

      if ((_lvl > exLvl) && (P.isOnline()))
      {
        ArrayList<StatsMetier> list = new ArrayList<StatsMetier>();
        list.add(this);

        SocketManager.GAME_SEND_JS_PACKET(P, list);
        SocketManager.GAME_SEND_JN_PACKET(P, _template.getId(), _lvl);
        SocketManager.GAME_SEND_STATS_PACKET(P);
        SocketManager.GAME_SEND_Ow_PACKET(P);
        SocketManager.GAME_SEND_JO_PACKET(P, list);
      }
    }

    public String getXpString(String s)
    {
      String str = World.getExpLevel(_lvl).metier + s;
      str = str + _xp + s;
      str = str + World.getExpLevel(_lvl < 100 ? _lvl + 1 : _lvl).metier;
      return str;
    }

    public Metier getTemplate() {
      return _template;
    }

    public int getOptBinValue()
    {
      int nbr = 0;
      nbr += (_isCheap ? 1 : 0);
      nbr += (_freeOnFails ? 2 : 0);
      nbr += (_noRessource ? 4 : 0);
      return nbr;
    }

    public boolean isValidMapAction(int id)
    {
      for (Metier.JobAction JA : _posActions) if (JA.getSkillID() == id) return true;
      return false;
    }

    public void setOptBinValue(int bin)
    {
      _isCheap = false;
      _freeOnFails = false;
      _noRessource = false;

      if (bin - 4 >= 0)
      {
        bin -= 4;
        _isCheap = true;
      }
      if (bin - 2 >= 0)
      {
        bin -= 2;
        _freeOnFails = true;
      }
      if (bin - 1 >= 0)
      {
        bin--;
        _noRessource = true;
      }
    }

    public int getID()
    {
      return _id;
    }
  }
}