package common;

import game.GameServer;
import game.GameThread;
import game.GameThread.GameAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import objects.Carte;
import objects.Carte.Case;
import objects.Fight;
import objects.Fight.Fighter;
import objects.Sort;
import objects.Sort.SortStats;
import objects.SpellEffect;

public class IA
{
  public static class IAThread
    implements Runnable
  {
    private Fight _fight;
    private Fight.Fighter _fighter;
    private static boolean stop = false;
    private Thread _t;

    public IAThread(Fight.Fighter fighter, Fight fight)
    {
      _fighter = fighter;
      _fight = fight;
      _t = new Thread(this);
      _t.setDaemon(true);
      _t.start();
    }

    
    public void run() {
      stop = false;
      if (_fighter.getMob() == null)
      {
        if (_fighter.isDouble()) {
          try {
            apply_type5(_fighter, _fight); } catch (Exception localException) {
          }try {
            Thread.sleep(1000L); } catch (InterruptedException localInterruptedException) {
          }
          _fight.endTurn();
        }
        else if (_fighter.isPerco())
        {
          apply_typePerco(_fighter, _fight);
          try {
            Thread.sleep(1000L); } catch (InterruptedException localInterruptedException1) {
          }
          _fight.endTurn();
        }
        else
        {
          try {
            Thread.sleep(1000L); } catch (InterruptedException localInterruptedException2) {
          }
          _fight.endTurn();
        }
      }
      else if (_fighter.getMob().getTemplate() == null)
      {
        _fight.endTurn();
      }
      else {
        int IA_Type = _fighter.getMob().getTemplate().getIAType();
        switch (IA_Type)
        {
          case 0:
          apply_type0(_fighter, _fight);
          break;
          case 1:
          try { apply_type1(_fighter, _fight); } catch (Exception localException1) {
          }case 2:
          try {
            apply_type2(_fighter, _fight); } catch (Exception localException2) {
          }case 3:
          try {
            apply_type3(_fighter, _fight); } catch (Exception localException3) {
          }case 4:
          try {
            apply_type4(_fighter, _fight); } catch (Exception localException4) {
          }case 5:
          try {
            apply_type5(_fighter, _fight); } catch (Exception localException5) {
          }case 6:
          try {
            apply_type6(_fighter, _fight); } catch (Exception localException6) {
          }case 7:
          try {
            apply_type7(_fighter, _fight); } catch (Exception localException7) {
          }case 8:
          try {
            apply_type8(_fighter, _fight); } catch (Exception localException8) {
          }case 9:
          try {
            apply_type9(_fighter, _fight); } catch (Exception localException9) {
          }case 10:
          try {
            apply_type10(_fighter, _fight); } catch (Exception localException10) {
          }case 11:
          try {
            apply_type11(_fighter, _fight); } catch (Exception localException11) {
          }case 12:
          try {
        	apply_type12(_fighter, _fight); } catch (Exception localException12) {
            }
        }
        try {
          if (IA_Type == 0)
            Thread.sleep(250L);
          else
            Thread.sleep(1000L);
        } catch (InterruptedException localInterruptedException3) {
        }
        if (!_fighter.isDead())
          _fight.endTurn();
      }
    }

    private static void apply_type0(Fight.Fighter F, Fight fight)
    {
      while ((!stop) && (F.canPlay()))
      {
        stop = true;
      }
    }

    private static void apply_type1(Fight.Fighter F, Fight fight)
    {
      int noBoucle = 0;
      while ((!stop) && (F.canPlay()))
      {
        noBoucle++;
        if (noBoucle >= 12) stop = true;
        if (noBoucle > 15) return;

        int PDVPER = F.getPDV() * 100 / F.getPDVMAX();
        Fight.Fighter T = getNearestEnnemy(fight, F);
        Fight.Fighter T2 = getNearestFriend(fight, F);
        if (T == null) {
          return;
        }
        if (PDVPER > 15)
        {
          int attack = attackIfPossible(fight, F);
          if (attack == 0)
            continue;
          if (attack == 5) stop = true;
          if (moveToAttackIfPossible(fight, F))
            continue;
          if (buffIfPossible(fight, F, F))
            continue;
          if (HealIfPossible(fight, F, false))
            continue;
          if (buffIfPossible(fight, F, T2))
            continue;
          if (moveNearIfPossible(fight, F, T))
            continue;
          if (invocIfPossible(fight, F))
            continue;
          stop = true;
        }
        else
        {
          if (HealIfPossible(fight, F, true))
            continue;
          int attack = attackIfPossible(fight, F);
          if (attack == 0)
            continue;
          if (attack == 5) stop = true;
          if (buffIfPossible(fight, F, F))
            continue;
          if (HealIfPossible(fight, F, false))
            continue;
          if (buffIfPossible(fight, F, T2))
            continue;
          if (invocIfPossible(fight, F))
            continue;
          if (!moveFarIfPossible(fight, F))
            stop = true;
        }
      }
    }

    private static void apply_type2(Fight.Fighter F, Fight fight)
    {
      int noBoucle = 0;
      while ((!stop) && (F.canPlay()))
      {
        noBoucle++;
        if (noBoucle >= 12) stop = true;
        if (noBoucle > 15) return;
        Fight.Fighter T = getNearestFriend(fight, F);
        if (HealIfPossible(fight, F, false))
          continue;
        if (buffIfPossible(fight, F, T))
          continue;
        if (moveNearIfPossible(fight, F, T))
          continue;
        if (HealIfPossible(fight, F, true))
          continue;
        if (buffIfPossible(fight, F, F))
          continue;
        if (invocIfPossible(fight, F))
          continue;
        T = getNearestEnnemy(fight, F);
        int attack = attackIfPossible(fight, F);
        if (attack == 0)
          continue;
        if (attack == 5) stop = true;
        if (!moveFarIfPossible(fight, F))
          stop = true;
      }
    }

    private static void apply_type3(Fight.Fighter F, Fight fight)
    {
      int noBoucle = 0;
      while ((!stop) && (F.canPlay()))
      {
        noBoucle++;
        if (noBoucle >= 12) stop = true;
        if (noBoucle > 15) return;
        Fight.Fighter T = getNearestFriend(fight, F);
        if (moveNearIfPossible(fight, F, T))
          continue;
        if (HealIfPossible(fight, F, false))
          continue;
        if (buffIfPossible(fight, F, T))
          continue;
        if (HealIfPossible(fight, F, true))
          continue;
        if (invocIfPossible(fight, F))
          continue;
        if (buffIfPossible(fight, F, F))
          continue;
        stop = true;
      }
    }

    private static void  apply_type4(Fighter F, Fight fight) { 
		int noBoucle = 0;
		while (!stop && F.canPlay()) {
			if (++noBoucle >= 8)
				stop = true;
			if (noBoucle > 15)
				return;
			Fighter enemy = getNearestEnnemy(fight, F);
			if (enemy == null) {
				moveFarIfPossible(fight, F);
				return;
			}
			int attack = attackIfPossible2(fight, F);
			if (attack == 0 && !stop) {
				while (attack == 0 && !stop) {
					if (attack == 5)
						stop = true;
					attack = attackIfPossible2(fight, F);
				}
			} else if (moveToAttackIfPossible(fight, F)) {
				attack = attackIfPossible2(fight, F);
				if (attack == 0 && !stop) {
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible2(fight, F);
					}
				}
			} else {
				enemy = getNearestEnnemy(fight, F);
				if (enemy == null) {
					moveFarIfPossible(fight, F);
					return;
				}
				if (moveNearIfPossible(fight, F, enemy)) {
					attack = attackIfPossible2(fight, F);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible2(fight, F);
					}
				}
			}
			moveFarIfPossible(fight, F);
			stop = true;
		}
	}
   

    private static void apply_type5(Fight.Fighter F, Fight fight)
    {
      int noBoucle = 0;
      while ((!stop) && (F.canPlay()))
      {
        noBoucle++;
        if (noBoucle >= 12) stop = true;
        if (noBoucle > 15) return;
        Fight.Fighter T = getNearestEnnemy(fight, F);
        if (T == null) return;

        if (moveNearIfPossible(fight, F, T))
          continue;
        stop = true;
      }
    }
    
    private static void apply_type6(Fight.Fighter F, Fight fight)// Cambiar para dopul osa
    {
      int noBoucle = 0;
      while ((!stop) && (F.canPlay()))
      {
        noBoucle++;
        if (noBoucle >= 12) stop = true;
        if (noBoucle > 15) return;
        if (invocIfPossible(fight, F))
          continue;
        Fight.Fighter T = getNearestFriend(fight, F);
        if (HealIfPossible(fight, F, false))
          continue;
        if (buffIfPossible(fight, F, T))
          continue;
        if (buffIfPossible(fight, F, F))
          continue;
        if (HealIfPossible(fight, F, true))
          continue;
        int attack = attackIfPossible(fight, F);
        if (attack == 0)
          continue;
        if (attack == 5) stop = true;
        if (!moveFarIfPossible(fight, F))
          stop = true;
      }
    }

    private static void apply_type7(Fighter fighter, Fight fight)
	{
		while(!stop && fighter.canPlay())
		{
			Fighter friend = getNearestFriend(fight, fighter);
			if(friend == null) continue;
			if(!buffIfPossible(fight, fighter, friend))
			{
				if(!moveNearIfPossible(fight, fighter, friend))
				{
					stop = true;
				}
			}
		}
	}
	
	private static void apply_type8 (Fighter fighter, Fight fight) { 
		int veces = 0;
		while (!stop && fighter.canPlay()) {
			if (++veces >= 12)
				stop = true;
			if (veces > 15)
				return;
			Fighter T = getNearestEnnemy(fight, fighter);
			if (T == null)
				return;
			int ataque = attackIfPossible2(fight, fighter);
			while (ataque == 0 && !stop) {
				if (ataque == 5)
					stop = true;
				ataque = attackIfPossible2(fight, fighter);
			}
			if (!moveNearIfPossible(fight, fighter, T))
				stop = true;
		}
	}
	
	private static void apply_type9(Fighter fighter, Fight fight) { 
		int veces = 0;
		while (!stop && fighter.canPlay()) {
			if (++veces >= 12)
				stop = true;
			if (veces > 15)
				return;
			if (!buffIfPossible2(fight, fighter, fighter)) {// auto
																	// buff
				stop = true;
			}
		}
	}
	
	private static void apply_type10 (Fighter fighter, Fight fight) { // esfera xelor
		int veces = 0;
		while (!stop && fighter.canPlay()) {
			if (++veces >= 8)
				stop = true;
			if (veces > 15)
				return;
			Fighter enemigo = getNearestEnnemy(fight, fighter);
			if (enemigo == null)
				return;
			int ataque = attackIfPossible2(fight, fighter);
			while (ataque == 0 && !stop) {
				if (ataque == 5)
					stop = true;
				ataque = attackIfPossible2(fight, fighter);
			}
			stop = true;
		}
	}
	
	private static void apply_type11(Fighter fighter, Fight fight) {// vasija
		int veces = 0;
		while (!stop && fighter.canPlay()) {
			if (++veces >= 8)
				stop = true;
			if (veces > 15)
				return;
			if (!buffIfPossible2(fight, fighter, fighter)) {// auto boost
				int ataque = attackIfPossible2(fight, fighter);
				while (ataque == 0 && !stop) {
					if (ataque == 5)
						stop = true;
					ataque = attackIfPossible2(fight, fighter);
				}
				stop = true;
			}
		}
	}
	
	private static void apply_type12(Fighter fighter, Fight fight) { // chafer y chaferloko
		int boucle = 0;
		while (!stop && fighter.canPlay()) {
			if (++boucle >= 8)
				stop = true;
			if (boucle > 15)
				return;
			Fighter enemigo = getNearestEnnemy(fight, fighter); // Enemigos
			if (enemigo == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			int ataque = attackIfPossible3(fight, fighter);
			while (ataque == 0 && !stop) {
				if (ataque == 5)
					stop = true;
				ataque = attackIfPossible3(fight, fighter);
			}
			while (buffIfPossible(fight, fighter, fighter)) {}// auto-buff
			enemigo = getNearestEnnemy(fight, fighter);
			if (enemigo == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			if (!moveNearIfPossible(fight, fighter, enemigo)) {
				stop = true;
			}
		}
	}
	
	private static void apply_typePerco(Fighter F, Fight fight)
	{
		while(!stop && F.canPlay())
		{
			Fighter T = getNearestEnnemy(fight, F);
			if(T == null) return;
			int attack = attackIfPossible(fight,F);
			if(attack != 0)//Attaque
			{
				if(attack == 5) stop = true;//EC
				if(!moveFarIfPossible(fight, F))//fuite
				{
					if(!HealIfPossible(fight,F,false))//soin allié
					{
						if(!buffIfPossible(fight,F,T))//buff allié
						{
							if(!HealIfPossible(fight,F,true))//auto-soin
							{
								if(!buffIfPossible(fight,F,F))//auto-buff
								{
									stop = true;
								}
							}
						}
					}
				}
			}
		}
	}

    private static boolean moveFarIfPossible(Fight fight, Fight.Fighter F) {
      int[] dist = { 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000 }; int[] cell = new int[10];
      for (int i = 0; i < 10; i++)
      {
        for (Fight.Fighter f : fight.getFighters(3))
        {
          if ((f.isDead()) || 
            (f == F) || (f.getTeam() == F.getTeam())) continue;
          int cellf = f.get_fightCell().getID();
          if ((cellf != cell[0]) && (cellf != cell[1]) && (cellf != cell[2]) && (cellf != cell[3]) && (cellf != cell[4]) && (cellf != cell[5]) && (cellf != cell[6]) && (cellf != cell[7]) && (cellf != cell[8]) && (cellf != cell[9])) {
            int d = 0;
            d = Pathfinding.getDistanceBetween(fight.get_map(), F.get_fightCell().getID(), f.get_fightCell().getID());
            if (d != 0) {
              if (d < dist[i])
              {
                dist[i] = d;
                cell[i] = cellf;
              }
              if (dist[i] != 1000)
                continue;
              dist[i] = 0;
              cell[i] = F.get_fightCell().getID();
            }
          }
        }
      }
      if (dist[0] == 0) return false;
      int[] dist2 = new int[10];
      int PM = F.getCurPM(fight); int caseDepart = F.get_fightCell().getID(); int destCase = F.get_fightCell().getID();
      for (int i = 0; i <= PM; i++)
      {
        if (destCase > 0)
          caseDepart = destCase;
        int curCase = caseDepart;
        curCase += 15;
        int infl = 0; int inflF = 0;
        for (int a = 0; (a < 10) && (dist[a] != 0); a++)
        {
          dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
          if (dist2[a] > dist[a]) {
            infl++;
          }
        }
        if ((infl > inflF) && (curCase > 0) && (curCase < 478) && (testCotes(destCase, curCase)))
        {
          inflF = infl;
          destCase = curCase;
        }

        curCase = caseDepart + 14;
        infl = 0;

        for (int a = 0; (a < 10) && (dist[a] != 0); a++)
        {
          dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
          if (dist2[a] > dist[a]) {
            infl++;
          }
        }
        if ((infl > inflF) && (curCase > 0) && (curCase < 478) && (testCotes(destCase, curCase)))
        {
          inflF = infl;
          destCase = curCase;
        }

        curCase = caseDepart - 15;
        infl = 0;
        for (int a = 0; (a < 10) && (dist[a] != 0); a++)
        {
          dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
          if (dist2[a] > dist[a]) {
            infl++;
          }
        }
        if ((infl > inflF) && (curCase > 0) && (curCase < 478) && (testCotes(destCase, curCase)))
        {
          inflF = infl;
          destCase = curCase;
        }

        curCase = caseDepart - 14;
        infl = 0;
        for (int a = 0; (a < 10) && (dist[a] != 0); a++)
        {
          dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
          if (dist2[a] > dist[a]) {
            infl++;
          }
        }
        if ((infl <= inflF) || (curCase <= 0) || (curCase >= 478) || (!testCotes(destCase, curCase)))
          continue;
        inflF = infl;
        destCase = curCase;
      }

      if ((destCase < 0) || (destCase > 478) || (destCase == F.get_fightCell().getID()) || (!fight.get_map().getCase(destCase).isWalkable(false))) return false;
      if (F.getPM() <= 0) return false;
      ArrayList<?> path = Pathfinding.getShortestPathBetween(fight.get_map(), F.get_fightCell().getID(), destCase, 0);
      if (path == null) return false;

      ArrayList<Case> finalPath = new ArrayList<Case>();
      for (int a = 0; a < F.getPM(); a++)
      {
        if (path.size() == a) break;
        finalPath.add((Carte.Case)path.get(a));
      }
      String pathstr = "";
      try {
        int curCaseID = F.get_fightCell().getID();
        int curDir = 0;
        for (Carte.Case c : finalPath)
        {
          char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
          if (d == 0) return false;
          if (curDir != d)
          {
            if (finalPath.indexOf(c) != 0)
              pathstr = pathstr + CryptManager.cellID_To_Code(curCaseID);
            pathstr = pathstr + d;
          }
          curCaseID = c.getID();
        }
        if (curCaseID != F.get_fightCell().getID())
          pathstr = pathstr + CryptManager.cellID_To_Code(curCaseID); 
      } catch (Exception e) {
        e.printStackTrace();
      }
      GameThread.GameAction GA = new GameThread.GameAction(0, 1, "");
      GA._args = pathstr;
      boolean result = fight.fighterDeplace(F, GA);
      try {
        Thread.sleep(100L); } catch (InterruptedException localInterruptedException) {
      }
      return result;
    }

    private static boolean testCotes(int cell1, int cell2)
    {
      if ((cell1 == 15) || (cell1 == 44) || (cell1 == 73) || (cell1 == 102) || (cell1 == 131) || (cell1 == 160) || (cell1 == 189) || (cell1 == 218) || (cell1 == 247) || (cell1 == 276) || (cell1 == 305) || (cell1 == 334) || (cell1 == 363) || (cell1 == 392) || (cell1 == 421) || (cell1 == 450))
      {
        if ((cell2 == cell1 + 14) || (cell2 == cell1 - 15))
          return false;
      }
      if ((cell1 == 28) || (cell1 == 57) || (cell1 == 86) || (cell1 == 115) || (cell1 == 144) || (cell1 == 173) || (cell1 == 202) || (cell1 == 231) || (cell1 == 260) || (cell1 == 289) || (cell1 == 318) || (cell1 == 347) || (cell1 == 376) || (cell1 == 405) || (cell1 == 434) || (cell1 == 463))
      {
        if ((cell2 == cell1 + 15) || (cell2 == cell1 - 14))
          return false;
      }
      return true;
    }

    private static boolean invocIfPossible(Fight fight, Fight.Fighter fighter)
    {
      Fight.Fighter nearest = getNearestEnnemy(fight, fighter);
      if (nearest == null)
        return false;
      int nearestCell = Pathfinding.getNearestCellAround(fight.get_map(), fighter.get_fightCell().getID(), nearest.get_fightCell().getID(), null);
      if (nearestCell == -1)
        return false;
      Sort.SortStats spell = getInvocSpell(fight, fighter, nearestCell);
      if (spell == null)
        return false;
      int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
      if (invoc != 0) return false;

      try
      {
        switch (Formulas.getRandomValue(1, 5)) {
        case 1:
          SocketManager.GAME_SEND_EMOTICONE_TO_FIGHT(fight, 7, fighter.getGUID(), 1); break;
        case 2:
          SocketManager.GAME_SEND_EMOTICONE_TO_FIGHT(fight, 7, fighter.getGUID(), 4); break;
        case 3:
          SocketManager.GAME_SEND_EMOTICONE_TO_FIGHT(fight, 7, fighter.getGUID(), 11);
        }
      }
      catch (Exception localException)
      {
      }
      return true;
    }

    private static Sort.SortStats getInvocSpell(Fight fight, Fight.Fighter fighter, int nearestCell)
    {
      if (fighter.getMob() == null) return null;
      for (Entry<Integer, SortStats> SS : fighter.getMob().getSpells().entrySet())
      {
        if (!fight.CanCastSpell3(fighter, (Sort.SortStats)SS.getValue(), fight.get_map().getCase(nearestCell)))
          continue;
        for (SpellEffect SE : ((Sort.SortStats)SS.getValue()).getEffects())
        {
          if (SE.getEffectID() == 181)
            return (Sort.SortStats)SS.getValue();
        }
      }
      return null;
    }

    private static boolean HealIfPossible(Fight fight, Fighter f, boolean autoSoin)//boolean pour choisir entre auto-soin ou soin allié
	{
		if(autoSoin && (f.getPDV()*100)/f.getPDVMAX() > 95 )return false;
		Fighter target = null;
		SortStats SS = null;
		if(autoSoin)
		{
			target = f;			
			SS = getHealSpell(fight,f,target);
		}
		else//sélection joueur ayant le moins de pv
		{
			Fighter curF = null;
			int PDVPERmin = 100;
			SortStats curSS = null;
			for(Fighter F : fight.getFighters(3))
			{					
				if(f.isDead())continue;
				if(F == f)continue;
				if(F.getTeam() == f.getTeam())
				{
					int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
					if( PDVPER < PDVPERmin && PDVPER < 95)
					{
						int infl = 0;
						if(f.isPerco())
						{
							for(Entry<Integer, SortStats> ss : World.getGuild(f.getPerco().GetPercoGuildID()).getSpells().entrySet())
							{
								if(ss.getValue() == null) continue;
								if(infl < calculInfluenceHeal(ss.getValue()) && calculInfluenceHeal(ss.getValue()) != 0 && fight.CanCastSpell(f, ss.getValue(), F.get_fightCell(), -1))//Si le sort est plus interessant
								{
									infl = calculInfluenceHeal(ss.getValue());
									curSS = ss.getValue();
								}
							}
						}else
						{
							for(Entry<Integer, SortStats> ss : f.getMob().getSpells().entrySet())
							{
								if(infl < calculInfluenceHeal(ss.getValue()) && calculInfluenceHeal(ss.getValue()) != 0 && fight.CanCastSpell(f, ss.getValue(), F.get_fightCell(), -1))//Si le sort est plus interessant
								{
									infl = calculInfluenceHeal(ss.getValue());
									curSS = ss.getValue();
								}
							}
						}
						if(curSS != SS && curSS != null)
						{
							curF = F;
							SS = curSS;
							PDVPERmin = PDVPER;
						}
					}
				}
			}
			target = curF;			
		}
		if(target == null)return false;
		if(SS == null)return false;
		int heal = fight.tryCastSpell(f, SS, target.get_fightCell().getID());
		if(heal != 0)
			return false;
		
		return true;
	}

    private static boolean buffIfPossible(Fight fight, Fight.Fighter fighter, Fight.Fighter target)
    {
      if (target == null) return false;
      Sort.SortStats SS = getBuffSpell(fight, fighter, target);
      if (SS == null) return false;
      int buff = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
      return buff == 0;
    }

    private static boolean buffIfPossible2(Fight pelea, Fighter lanzador, Fighter objetivo) {
		if (objetivo == null)
			return false;
		SortStats SS = getBuffSpell2(pelea, lanzador, objetivo);
		if (SS == null)
			return false;
		int buff = pelea.tryCastSpell(lanzador, SS, objetivo
				.get_fightCell().getID());
		if (buff != 0)
			return false;
		return true;
      }

    private static Sort.SortStats getBuffSpell(Fight fight, Fight.Fighter F, Fight.Fighter T)
    {
      int infl = 0;
      Sort.SortStats ss = null;
      if(F.isPerco())
		{
			for(Entry<Integer, SortStats> SS : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
			{
				if(SS.getValue() == null) continue;
				if(infl < calculInfluence(SS.getValue(),F,T) && calculInfluence(SS.getValue(),F,T) > 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))//Si le sort est plus interessant
				{
					infl = calculInfluence(SS.getValue(),F,T);
					ss = SS.getValue();
				}
			}
		}else
	   {
		for (Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
	    {
        if ((infl >= calculInfluence((Sort.SortStats)SS.getValue(), F, T)) || (calculInfluence((Sort.SortStats)SS.getValue(), F, T) <= 0) || (!fight.CanCastSpell3(F, (Sort.SortStats)SS.getValue(), T.get_fightCell())))
          continue;
        infl = calculInfluence((Sort.SortStats)SS.getValue(), F, T);
        ss = (Sort.SortStats)SS.getValue();
            }
	    }
	    return ss;
    }

    private static SortStats getBuffSpell2(Fight pelea, Fighter lanzador,
			Fighter objetivo) {
		ArrayList<SortStats> hechizos = new ArrayList<SortStats>();
		SortStats ss = null;
		Case celdaObj = objetivo.get_fightCell();
		for (Entry<Integer, SortStats> SS : lanzador.getMob().getSpells()
				.entrySet()) {
			if (pelea.CanCastSpell(lanzador, SS.getValue(), celdaObj, -1))
				hechizos.add(SS.getValue());
		}
		if (hechizos.size() <= 0)
			return null;
		if (hechizos.size() == 1)
			return hechizos.get(0);
		ss = hechizos.get(Formulas.getRandomValue(0, hechizos.size() - 1));
		return ss;
	}

    private static Sort.SortStats getHealSpell(Fight fight, Fight.Fighter F, Fight.Fighter T)
    {
      int infl = 0;
      Sort.SortStats ss = null;
      if(F.isPerco())
		{
			for(Entry<Integer, SortStats> SS : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
			{
				if(SS.getValue() == null) continue;
				if(infl < calculInfluenceHeal(SS.getValue()) && calculInfluenceHeal(SS.getValue()) != 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))//Si le sort est plus interessant
				{
					infl = calculInfluenceHeal(SS.getValue());
					ss = SS.getValue();
				}
			}
		}else
		{
		for (Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
		{
        if ((infl >= calculInfluenceHeal((Sort.SortStats)SS.getValue())) || (calculInfluenceHeal((Sort.SortStats)SS.getValue()) == 0) || (!fight.CanCastSpell3(F, (Sort.SortStats)SS.getValue(), T.get_fightCell())))
          continue;
        infl = calculInfluenceHeal((Sort.SortStats)SS.getValue());
        ss = (Sort.SortStats)SS.getValue();
		}
	 }

      return ss;
    }
    
    
    private static boolean moveNearIfPossible(Fight fight, Fighter F, Fighter T)
	{
    	if(F.getCurPM(fight) <= 0)
    		return false;
    	if(Pathfinding.isNextTo(F.get_fightCell().getID(), T.get_fightCell().getID()))
    		return false;
		GameServer.addToLog("Tentative d'approche par "+F.getPacketsName()+" de "+T.getPacketsName());
		int cellID = Pathfinding.getNearestCellAround(fight.get_map(),T.get_fightCell().getID(),F.get_fightCell().getID(),null);
		//On demande le chemin plus court
		ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),F.get_fightCell().getID(),cellID,0);
		if(path == null || path.size() == 0)return false;

		ArrayList<Case> finalPath = new ArrayList<Case>();
		for(int a = 0; a<F.getPM();a++)
		{
			if(path.size() == a)break;
			finalPath.add(path.get(a));
		}
		String pathstr = "";
		try{
		int curCaseID = F.get_fightCell().getID();
		int curDir = 0;
		for(Case c : finalPath)
		{
			char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
			if(d == 0)return false;//Ne devrait pas arriver :O
			if(curDir != d)
			{
				if(finalPath.indexOf(c) != 0)
					pathstr += CryptManager.cellID_To_Code(curCaseID);
				pathstr += d;
			}
			curCaseID = c.getID();
		}
		if(curCaseID != F.get_fightCell().getID())
			pathstr += CryptManager.cellID_To_Code(curCaseID);
		}catch(Exception e){e.printStackTrace();};
		//Création d'une GameAction
		GameAction GA = new GameAction(0,1, "");
		GA._args = pathstr;
		boolean result = fight.fighterDeplace(F, GA);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		return result;
	}
    
    private static Fight.Fighter getNearestFriend(Fight fight, Fight.Fighter fighter)
    {
      int dist = 1000;
      Fight.Fighter curF = null;
      for (Fight.Fighter f : fight.getFighters(3))
      {
        if ((f.isDead()) || 
          (f == fighter) || 
          (f.getTeam2() != fighter.getTeam2()))
          continue;
        int d = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
        if (d >= dist)
          continue;
        dist = d;
        curF = f;
      }
      return curF;
    }

    private static Fight.Fighter getNearestEnnemy(Fight fight, Fight.Fighter fighter)
    {
      int dist = 1000;
      Fight.Fighter curF = null;
      for (Fight.Fighter f : fight.getFighters(3))
      {
        if ((f.isDead()) || 
          (f.getTeam2() == fighter.getTeam2()))
          continue;
        int d = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
        if (d >= dist)
          continue;
        dist = d;
        curF = f;
      }

      return curF;
    }

    private static Map<Integer, Fight.Fighter> getLowHpEnnemyList(Fight fight, Fight.Fighter fighter)
    {
      Map<Integer,Fighter> list = new TreeMap<Integer,Fighter>();
      Map<Integer,Fighter> ennemy = new TreeMap<Integer,Fighter>();
      for (Fight.Fighter f : fight.getFighters(3))
      {
        if ((f.isDead()) || 
          (f == fighter) || 
          (f.getTeam2() == fighter.getTeam2()))
          continue;
        ennemy.put(Integer.valueOf(f.getPDV()), f);
      }

      int i = 0; int i2 = ennemy.size();
      int curHP = 10000;

      while (i < i2)
      {
        curHP = 200000;
        for (Entry<Integer, Fighter> t : ennemy.entrySet())
        {
          if (((Fight.Fighter)t.getValue()).getPDV() < curHP)
            curHP = ((Fight.Fighter)t.getValue()).getPDV();
        }
        Fight.Fighter test = (Fight.Fighter)ennemy.get(Integer.valueOf(curHP));
        list.put(Integer.valueOf(test.getPDV()), test);
        ennemy.remove(Integer.valueOf(curHP));
        i++;
      }
      return list;
    }

    private static Map<Integer, Fighter> getLowHpEnnemyList2(Fight pelea,
			Fighter lanzador) {
		Fighter enemigoMasCercano = getNearestEnnemy(pelea, lanzador);
		Map<Integer, Fighter> listaEnemigos = new TreeMap<Integer, Fighter>();
		Map<Integer, Fighter> enemigosNoInvo = new TreeMap<Integer, Fighter>();
		Map<Integer, Fighter> enemigosInvo = new TreeMap<Integer, Fighter>();
		for (Fighter f : pelea.getFighters(3)) {
			if (f.hasLeft())
				continue;
			if (f.isDead())
				continue;
			if (f.isInvocation())
				enemigosInvo.put(f.getPDV(), f);
			else
				enemigosNoInvo.put(f.getPDV(), f);
		}
		if (enemigoMasCercano != null)
			listaEnemigos.put(enemigoMasCercano.getPDV(), enemigoMasCercano);
		int i = 0, i2 = enemigosNoInvo.size(), i3 = enemigosInvo.size();
		int curHP;
		while (i < i2) {
			curHP = 200000;
			for (Entry<Integer, Fighter> t : enemigosNoInvo.entrySet()) {
				if (t.getValue().getPDV() < curHP)
					curHP = t.getValue().getPDV();
			}
			Fighter test = enemigosNoInvo.get(curHP);
			listaEnemigos.put(test.getPDV(), test);
			enemigosNoInvo.remove(curHP);
			i++;
		}
		i = 0;
		while (i < i3) {
			curHP = 200000;
			for (Entry<Integer, Fighter> t : enemigosInvo.entrySet()) {
				if (t.getValue().getPDV() < curHP)
					curHP = t.getValue().getPDV();
			}
			Fighter test = enemigosInvo.get(curHP);
			listaEnemigos.put(test.getPDV(), test);
			enemigosInvo.remove(curHP);
			i++;
		}
		return listaEnemigos;
	}
    
    private static int attackIfPossible(Fight fight, Fight.Fighter fighter)
    {
    
    	
    	Map<Integer,Fighter> ennemyList = getLowHpEnnemyList(fight,fighter);
		SortStats SS = null;
		Fighter target = null;
		for(Entry<Integer, Fighter> t : ennemyList.entrySet())
      {
        SS = getBestSpellForTarget(fight, fighter, (Fight.Fighter)t.getValue());
        if (SS == null)
          continue;
        target = (Fight.Fighter)t.getValue();
        break;
      }

      int curTarget = 0; int cell = 0;
      Sort.SortStats SS2 = null;
      if(fighter.isPerco())
		{
			for(Entry<Integer, SortStats> S : World.getGuild(fighter.getPerco().GetPercoGuildID()).getSpells().entrySet())
			{
				if(S.getValue() == null) continue;
				int targetVal = getBestTargetZone(fight,fighter,S.getValue(),fighter.get_fightCell().getID());
				if(targetVal == -1 || targetVal == 0)
					continue;
				int nbTarget = targetVal / 1000;
				int cellID = targetVal - nbTarget * 1000;
				if(nbTarget > curTarget)
				{
					curTarget = nbTarget;
					cell = cellID;
					SS2 = S.getValue();
				}
			}
		}else
		{
		for (Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
        {
        int targetVal = getBestTargetZone(fight, fighter, (Sort.SortStats)S.getValue(), fighter.get_fightCell().getID());
        if ((targetVal == -1) || (targetVal == 0))
          continue;
        int nbTarget = targetVal / 1000;
        int cellID = targetVal - nbTarget * 1000;
        if (nbTarget <= curTarget)
          continue;
        curTarget = nbTarget;
        cell = cellID;
        SS2 = (Sort.SortStats)S.getValue();
      }
	}

      if ((curTarget > 0) && (cell > 0) && (cell < 480) && (SS2 != null))
      {
        int attack = fight.tryCastSpell(fighter, SS2, cell);
        if (attack != 0)
          return attack;
      }
      else
      {
        if ((target == null) || (SS == null))
          return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
        if (attack != 0)
          return attack;
      }
      return 0;
    }
	
	private static int attackIfPossible2(Fight fight, Fighter fighter) {
		Map<Integer, Fighter> ennemyList = getLowHpEnnemyList(fight,
				fighter);
		SortStats SH = null;
		Fighter objetivo = null;
		for (Entry<Integer, Fighter> t : ennemyList.entrySet()) {
			SH = getBestSpellForTarget2(fight, fighter, t.getValue());
			if (SH != null) {
				objetivo = t.getValue();
				break;
			}
		}
		if (objetivo == null || SH == null)
			return 666;
		int ataque = fight.tryCastSpell(fighter, SH, objetivo.get_fightCell()
				.getID());
		if (ataque != 0)
			return ataque;
		return 0;
	} 

	private static int attackIfPossible3(Fight fight, Fighter fighter) {
		Map<Integer, Fighter> ennemyList = getLowHpEnnemyList2(fight,
				fighter);
		SortStats SS = null;
		Fighter objetivo = null;
		for (Entry<Integer, Fighter> t : ennemyList.entrySet()) {
			SS = getBestSpellForTarget(fight, fighter, t.getValue());
			if (SS != null) {
				objetivo = t.getValue();
				break;
			}
		}
		if (objetivo == null || SS == null)
			return 666;
		int ataque = fight.tryCastSpell(fighter, SS, objetivo.get_fightCell()
				.getID());
		if (ataque != 0)
			return ataque;
		return 0;
	}
	
	private static boolean moveToAttackIfPossible(Fight fight, Fight.Fighter fighter)
    {
      ArrayList<?> cells = Pathfinding.getListCaseFromFighter(fight, fighter);
      if (cells == null)
        return false;
      int distMin = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), getNearestEnnemy(fight, fighter).get_fightCell().getID());
      ArrayList <SortStats> sorts = getLaunchableSort(fighter,fight,distMin);
      if (sorts == null)
        return false;
      ArrayList <Fighter> targets = getPotentialTarget(fight,fighter,sorts);
      if (targets == null) {
        return false;
      }
      int CellDest = 0;
      boolean found = false;
      for (Iterator<?> localIterator1 = cells.iterator(); localIterator1.hasNext(); ) { int i = ((Integer)localIterator1.next()).intValue();

        for (Sort.SortStats S : sorts)
        {
          for (Fight.Fighter T : targets)
          {
            if (fight.CanCastSpell2(fighter, S, T.get_fightCell(), i))
            {
              CellDest = i;
              found = true;
            }
            int targetVal = getBestTargetZone(fight, fighter, S, i);
            if (targetVal > 0)
            {
              int nbTarget = targetVal / 1000;
              int cellID = targetVal - nbTarget * 1000;
              if (fight.get_map().getCase(cellID) != null)
              {
                if (fight.CanCastSpell2(fighter, S, fight.get_map().getCase(cellID), i))
                {
                  CellDest = i;
                  found = true;
                }
              }
            }
            if (found)
              break;
          }
          if (found)
            break;
        }
        if (found)
          break;
      }
      if (CellDest == 0)
        return false;
		ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),fighter.get_fightCell().getID(),CellDest, 0);
      if (path == null)
        return false;
      String pathstr = "";
      try {
        int curCaseID = fighter.get_fightCell().getID();
        int curDir = 0;
        for(Case c : path)
        {
          char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
          if (d == 0) return false;
          if (curDir != d)
          {
            if (path.indexOf(c) != 0)
              pathstr = pathstr + CryptManager.cellID_To_Code(curCaseID);
            pathstr = pathstr + d;
          }
          curCaseID = c.getID();
        }
        if (curCaseID != fighter.get_fightCell().getID())
          pathstr = pathstr + CryptManager.cellID_To_Code(curCaseID); 
      } catch (Exception e) {
        e.printStackTrace();
      }
      GameThread.GameAction GA = new GameThread.GameAction(0, 1, "");
      GA._args = pathstr;
      boolean result = fight.fighterDeplace(fighter, GA);
      try {
        Thread.sleep(100L); } catch (InterruptedException localInterruptedException) {
      }
      return result;
    }

    private static ArrayList<Sort.SortStats> getLaunchableSort(Fight.Fighter fighter, Fight fight, int distMin)
    {
      ArrayList<SortStats> sorts = new ArrayList<SortStats>();
      if (fighter.getMob() == null)
        return null;
      for (Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
      {
        if (((Sort.SortStats)S.getValue()).getPACost() > fighter.getCurPA(fight))
        {
          continue;
        }
        if (!Fight.LaunchedSort.coolDownGood(fighter, ((Sort.SortStats)S.getValue()).getSpellID()))
          continue;
        if ((((Sort.SortStats)S.getValue()).getMaxLaunchbyTurn() - Fight.LaunchedSort.getNbLaunch(fighter, ((Sort.SortStats)S.getValue()).getSpellID()) <= 0) && (((Sort.SortStats)S.getValue()).getMaxLaunchbyTurn() > 0))
          continue;
        if (calculInfluence((Sort.SortStats)S.getValue(), fighter, fighter) >= 0)
          continue;
        sorts.add((Sort.SortStats)S.getValue());
      }
      ArrayList<SortStats> finalS = TriInfluenceSorts(fighter, sorts);

      return finalS;
    }

    private static ArrayList<Sort.SortStats> TriInfluenceSorts(Fight.Fighter fighter, ArrayList<Sort.SortStats> sorts)
    {
      if (sorts == null) {
        return null;
      }
      ArrayList<SortStats> finalSorts = new ArrayList<SortStats>();
      Map <Integer,SortStats> copie = new TreeMap <Integer,SortStats>();
      for (Sort.SortStats S : sorts)
      {
        copie.put(Integer.valueOf(S.getSpellID()), S);
      }

      int curInfl = 0;
      int curID = 0;

      while (copie.size() > 0)
      {
        curInfl = 0;
        curID = 0;
        for (Entry<Integer, SortStats> S : copie.entrySet())
        {
          int infl = -calculInfluence((Sort.SortStats)S.getValue(), fighter, fighter);
          if (infl <= curInfl)
            continue;
          curID = ((Sort.SortStats)S.getValue()).getSpellID();
          curInfl = infl;
        }

        if ((curID == 0) || (curInfl == 0))
          break;
        finalSorts.add((Sort.SortStats)copie.get(Integer.valueOf(curID)));
        copie.remove(Integer.valueOf(curID));
      }

      return finalSorts;
    }

    private static ArrayList<Fight.Fighter> getPotentialTarget(Fight fight, Fight.Fighter fighter, ArrayList<Sort.SortStats> sorts)
    {
      ArrayList<Fighter> targets = new ArrayList<Fighter>();
      int distMax = 0;
      for (Sort.SortStats S : sorts)
      {
        if (S.getMaxPO() > distMax)
          distMax = S.getMaxPO();
      }
      distMax += fighter.getCurPM(fight);
      //Map potentialsT = getLowHpEnnemyList(fight, fighter);
      Map<Integer,Fighter> potentialsT = getLowHpEnnemyList(fight,fighter);

      for (Entry<Integer, Fighter> T : potentialsT.entrySet())
      {
        int dist = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), ((Fight.Fighter)T.getValue()).get_fightCell().getID());
        if (dist < distMax) {
          targets.add((Fight.Fighter)T.getValue());
        }
      }
      return targets;
    }

    private static Sort.SortStats getBestSpellForTarget(Fight fight, Fight.Fighter F, Fight.Fighter T)
    {
      int inflMax = 0;
      Sort.SortStats ss = null;
      if(F.isPerco())
		{
			for(Entry<Integer, SortStats> SS : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
			{
				if(SS.getValue() == null) continue;
				int curInfl = 0, Infl1 = 0, Infl2 = 0;
				int PA = 6;
				int usedPA[] = {0,0};
				if(!fight.CanCastSpell(F, SS.getValue(), F.get_fightCell(), T.get_fightCell().getID()))continue;
				curInfl = calculInfluence(SS.getValue(),F,T);
				if(curInfl == 0)continue;
				if(curInfl > inflMax)
				{
					ss = SS.getValue();
					usedPA[0] = ss.getPACost();
					Infl1 = curInfl;
					inflMax = Infl1;
				}
				for(Entry<Integer, SortStats> SS2 : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
				{
					if(SS2.getValue() == null) continue;
					if((PA - usedPA[0]) < SS2.getValue().getPACost())continue;
					if(!fight.CanCastSpell(F, SS2.getValue(), F.get_fightCell(), T.get_fightCell().getID()))continue;
					curInfl = calculInfluence(SS2.getValue(),F,T);
					if(curInfl == 0)continue;
					if((Infl1 + curInfl) > inflMax)
					{
						ss = SS.getValue();
						usedPA[1] = SS2.getValue().getPACost();
						Infl2 = curInfl;
						inflMax = Infl1 + Infl2;
					}
					for(Entry<Integer, SortStats> SS3 : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
					{
						if(SS3.getValue() == null) continue;
						if( (PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())continue;
						if(!fight.CanCastSpell(F, SS3.getValue(), F.get_fightCell(), T.get_fightCell().getID()))continue;
						curInfl = calculInfluence(SS3.getValue(),F,T);
						if(curInfl == 0)continue;
						if((curInfl+Infl1+Infl2) > inflMax)
						{
							ss = SS.getValue();
							inflMax = curInfl + Infl1 + Infl2;
						}
					}				
				}			
			}
		}else
		{
	 for (Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
      {
        int curInfl = 0; int Infl1 = 0; int Infl2 = 0;
        int PA = F.getMob().getPA();
        int[] usedPA = new int[2];
        if (fight.CanCastSpell3(F, (Sort.SortStats)SS.getValue(), T.get_fightCell())) {
          curInfl = calculInfluence((Sort.SortStats)SS.getValue(), F, T);
          if (curInfl != 0) {
            if (curInfl > inflMax)
            {
              ss = (Sort.SortStats)SS.getValue();
              usedPA[0] = ss.getPACost();
              Infl1 = curInfl;
              inflMax = Infl1;
            }

            for (Entry<Integer, SortStats> SS2 : F.getMob().getSpells().entrySet())
            {
              if ((PA - usedPA[0] < ((Sort.SortStats)SS2.getValue()).getPACost()) || 
                (!fight.CanCastSpell3(F, (Sort.SortStats)SS2.getValue(), T.get_fightCell()))) continue;
              curInfl = calculInfluence((Sort.SortStats)SS2.getValue(), F, T);
              if (curInfl != 0) {
                if (Infl1 + curInfl > inflMax)
                {
                  ss = (Sort.SortStats)SS.getValue();
                  usedPA[1] = ((Sort.SortStats)SS2.getValue()).getPACost();
                  Infl2 = curInfl;
                  inflMax = Infl1 + Infl2;
                }
                for (Entry<Integer, SortStats> SS3 : F.getMob().getSpells().entrySet())
                {
                  if ((PA - usedPA[0] - usedPA[1] < ((Sort.SortStats)SS3.getValue()).getPACost()) || 
                    (!fight.CanCastSpell3(F, (Sort.SortStats)SS3.getValue(), T.get_fightCell()))) continue;
                  curInfl = calculInfluence((Sort.SortStats)SS3.getValue(), F, T);
                  if ((curInfl == 0) || 
                    (curInfl + Infl1 + Infl2 <= inflMax))
                    continue;
                  ss = (Sort.SortStats)SS.getValue();
                  inflMax = curInfl + Infl1 + Infl2;
                }
              }
            }
          }
        }
      }
	 }
      return ss;
    }
   
    private static SortStats getBestSpellForTarget2(Fight pelea, Fighter lanzador,
			Fighter objetivo) {
		SortStats ss = null;
		ArrayList<SortStats> posibles = new ArrayList<SortStats>();
		for (Entry<Integer, SortStats> SS : lanzador.getMob().getSpells()
				.entrySet()) {
			if (!pelea.CanCastSpell(lanzador, SS.getValue(),
					objetivo.get_fightCell(), -1))
				continue;
			posibles.add(SS.getValue());
		}
		if (posibles.isEmpty())
			return ss;
		if (posibles.size() == 1)
			return posibles.get(0);
		ss = posibles.get(Formulas.getRandomValue(0, posibles.size() - 1));
		return ss;
	}

    private static int getBestTargetZone(Fight fight, Fight.Fighter fighter, Sort.SortStats spell, int launchCell)
    {
      if ((spell.getPorteeType().charAt(0) == 'P') && (spell.getPorteeType().charAt(1) == 'a'))
        return 0;
      ArrayList<Case> possibleLaunch = new ArrayList<Case>();
   
      int CellF = -1;
      char arg2;
      if (spell.getMaxPO() != 0)
      {
        char arg1 = 'a';
        if (spell.isLineLaunch())
          arg1 = 'X';
        else
          arg1 = 'C';
        char[] table = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v' };
        arg2 = 'a';
        if (spell.getMaxPO() > 20)
          arg2 = 'u';
        else
          arg2 = table[spell.getMaxPO()];
        String args = Character.toString(arg1) + Character.toString(arg2);
        possibleLaunch = Pathfinding.getCellListFromAreaString(fight.get_map(), launchCell, launchCell, args, 0, false);
      }
      else
      {
        possibleLaunch.add(fight.get_map().getCase(launchCell));
      }
      if (possibleLaunch == null)
        return -1;
      int nbTarget = 0;
      for (Carte.Case cell : possibleLaunch)
        try
        {
          if (!fight.CanCastSpell2(fighter, spell, cell, launchCell))
            continue;
          int num = 0;
          int curTarget = 0;
          ArrayList<SpellEffect> test = new ArrayList<SpellEffect>();
          //ArrayList test = new ArrayList();
          test.addAll(spell.getEffects());

          for (SpellEffect SE : test)
          {
            try {
              if (SE == null)
                continue;
              if (SE.getValue() == -1)
                continue;
              int POnum = num * 2;
              ArrayList<Case> cells= Pathfinding.getCellListFromAreaString(fight.get_map(), cell.getID(), launchCell, spell.getPorteeType(), POnum, false);
              //ArrayList cells = Pathfinding.getCellListFromAreaString(fight.get_map(), cell.getID(), launchCell, spell.getPorteeType(), POnum, false);
              for (Carte.Case c : cells)
              {
                if (c.getFirstFighter() == null)
                  continue;
                if (c.getFirstFighter().getTeam2() != fighter.getTeam2())
                  curTarget++; 
              }
            } catch (Exception localException) {
            }
            num++;
          }
          if (curTarget <= nbTarget)
            continue;
          nbTarget = curTarget;
          CellF = cell.getID();
        }
        catch (Exception localException1)
        {
        }
      if ((nbTarget > 0) && (CellF != -1)) {
        return CellF + nbTarget * 1000;
      }
      return 0;
    }

    private static int calculInfluenceHeal(Sort.SortStats ss)
    {
      int inf = 0;
      for (SpellEffect SE : ss.getEffects())
      {
        if (SE.getEffectID() != 108) return 0;
        inf += 100 * Formulas.getMiddleJet(SE.getJet());
      }

      return inf;
    }

    private static int calculInfluence(Sort.SortStats ss, Fight.Fighter C, Fight.Fighter T)
    {
      int infTot = 0;
      for (SpellEffect SE : ss.getEffects())
      {
        int inf = 0;
        switch (SE.getEffectID())
        {
        case 5:
          inf = 500 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 89:
          inf = 200 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 91:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 92:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 93:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 94:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 95:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 96:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 97:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 98:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 99:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 100:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 101:
          inf = 1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 127:
          inf = 1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 84:
          inf = 1500 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 77:
          inf = 1500 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 111:
          inf = -1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 128:
          inf = -1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 121:
          inf = -100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 131:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 132:
          inf = 2000;
          break;
        case 138:
          inf = -50 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 150:
          inf = -2000;
          break;
        case 168:
          inf = 1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 169:
          inf = 1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 210:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 211:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 212:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 213:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 214:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 215:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 216:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 217:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 218:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 219:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 265:
          inf = -250 * Formulas.getMiddleJet(SE.getJet());
        case 765://Sacrifice
			inf = -1000;
		break;
		case 786://Arbre de vie
			inf = -1000;
		break;
		}

        if (C.getTeam() == T.getTeam())
          infTot -= inf;
        else
          infTot += inf;
      }
      return infTot;
    }
  }
  
  public static void launchIA(Fighter fighter, Fight fight)
  {
          if(fighter.getMob() == null)
          {
                  return;
          }
          switch(fighter.getMob().getTemplate().getIAType())
          {
                  case 0:
                  		IAThread.apply_type0(fighter, fight);
                          return;
                  case 1:
                  		IAThread.apply_type1(fighter, fight);
                          break;
                  case 2:
                  		IAThread.apply_type2(fighter, fight);
                          break;
                  case 3:
                  		IAThread.apply_type3(fighter, fight);
                      	break;
                  case 4:
                  		IAThread.apply_type4(fighter, fight);
                      	break;
                  case 5:
                  		IAThread.apply_type5(fighter, fight);
	                    	break;
                  case 6:
                  		IAThread.apply_type6(fighter, fight);
	                    	break;
                  default:
                  		IAThread.apply_type1(fighter, fight);
                          break;
          }
  }
  
  
}