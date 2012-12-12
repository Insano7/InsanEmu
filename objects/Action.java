package objects;

import game.GameServer;

import java.io.PrintWriter;
import java.util.ArrayList;

import objects.Metier.StatsMetier;
import objects.Monstre.MobGroup;
import objects.NPC_tmpl.NPC_question;
import objects.Objet.ObjTemplate;
import objects.Personnage.Stats;
import objects.Personnage.traque;

import common.ConditionParser;
import common.Constants;
import common.CyonEmu;
import common.Formulas;
import common.SQLManager;
import common.SocketManager;
import common.World;

public class Action {

	private int ID;
	private String args;
	private String cond;
	
	public Action(int id, String args, String cond)
	{
		this.ID = id;
		this.args = args;
		this.cond = cond;
	}


	public void apply(Personnage perso, Personnage target, int itemID, int cellid)
	{
		if(perso == null)return;
		if (perso.get_fight() != null)
	    {
	      SocketManager.GAME_SEND_MESSAGE(perso, "<b>Action impossible.</b> Vous etes en combat.", "000000");
	      return;
	    }
		if(!cond.equalsIgnoreCase("") && !cond.equalsIgnoreCase("-1")&& !ConditionParser.validConditions(perso,cond))
		{
			SocketManager.GAME_SEND_Im_PACKET(perso, "119");
			return;
		}
		if(perso.get_compte().getGameThread() == null) return;
		PrintWriter out = perso.get_compte().getGameThread().get_out();	
		switch(ID)
		{
		case -3://Lanzar una mision
			if (CyonEmu.SHOW_RECV)
			System.out.println("Action imposible( Lancé un mision)");
	   break;
			case -2://créer guilde
			if(perso.is_away())return;
			if(perso.get_guild() != null || perso.getGuildMember() != null)
			{
				SocketManager.GAME_SEND_gC_PACKET(perso, "Ea");
				return;
			}
			if(perso.hasItemGuid(1575)) {
				SocketManager.GAME_SEND_gn_PACKET(perso);
				perso.removeByTemplateID(1575,-1);
				SocketManager.GAME_SEND_Im_PACKET(perso, "022;"+-1+"~"+1575);
			} else {
				SocketManager.GAME_SEND_MESSAGE(perso, "Pour pouvoir créer une guilde, il faut possèder une Guildalogemme", CyonEmu.CONFIG_MOTD_COLOR);
			}
		break;
			case -1://Ouvrir banque
	            //Sauvagarde du perso et des item avant.              
	            SQLManager.SAVE_PERSONNAGE(perso,true);
	            if(perso.getDeshonor() >= 1)
	            {
	                    SocketManager.GAME_SEND_Im_PACKET(perso, "183");
	                    return;
	            }
	            final int cost = perso.getBankCost();
	            if(cost > 0)
	            {
	 
	                    final long playerKamas = perso.get_kamas();
	                    final long kamasRemaining = playerKamas - cost;
	                    final long bankKamas = perso.get_compte().getBankKamas();
	                    final long totalKamas = bankKamas+playerKamas;
	 
	                    if (kamasRemaining < 0)//Si le joueur n'a pas assez de kamas SUR LUI pour ouvrir la banque
	                    {
	                            if(bankKamas >= cost)
	                            {
	                                    perso.setBankKamas(bankKamas-cost); //On modifie les kamas de la banque
	                            }
	                            else if(totalKamas >= cost)
	                            {
	                                    perso.set_kamas( 0 ); //On puise l'entièreter des kamas du joueurs. Ankalike ?
	                                    perso.setBankKamas(totalKamas-cost); //On modifie les kamas de la banque
	                                    SocketManager.GAME_SEND_STATS_PACKET(perso);
	                                    SocketManager.GAME_SEND_Im_PACKET(perso, "020;"+playerKamas);
	                            }else
	                            {
	                                    SocketManager.GAME_SEND_MESSAGE_SERVER(perso, "10|"+cost);
	                                    return;
	                            }
	                    } else //Si le joueur a les kamas sur lui on lui retire directement
	                    {
	                            perso.set_kamas(kamasRemaining);
	                            SocketManager.GAME_SEND_STATS_PACKET(perso);
	                            SocketManager.GAME_SEND_Im_PACKET(perso, "020;"+cost);
	                    }
	            }
	            SocketManager.GAME_SEND_ECK_PACKET(perso.get_compte().getGameThread().get_out(), 5, "");
	            SocketManager.GAME_SEND_EL_BANK_PACKET(perso);
	            perso.set_away(true);
	            perso.setInBank(true);
	            break;
			
			case 0://Téléportation
				try
				{
					short newMapID = Short.parseShort(args.split(",",2)[0]);
					int newCellID = Integer.parseInt(args.split(",",2)[1]);
					
					perso.teleport(newMapID,newCellID);	
				}catch(Exception e ){return;};
			break;
			
			case 1://Discours NPC
				out = perso.get_compte().getGameThread().get_out();
				if(args.equalsIgnoreCase("DV"))
				{
					SocketManager.GAME_SEND_END_DIALOG_PACKET(out);
					perso.set_isTalkingWith(0);
				}else
				{
					int qID = -1;
					try
					{
						qID = Integer.parseInt(args);
					}catch(NumberFormatException e){};
					
					NPC_question  quest = World.getNPCQuestion(qID);
					if(quest == null)
					{
						SocketManager.GAME_SEND_END_DIALOG_PACKET(out);
						perso.set_isTalkingWith(0);
						return;
					}
					SocketManager.GAME_SEND_QUESTION_PACKET(out, quest.parseToDQPacket(perso));
				}
			break;
			
			case 4://Kamas
				try
				{
					int count = Integer.parseInt(args);
					long curKamas = perso.get_kamas();
					long newKamas = curKamas + count;
					if(newKamas <0) newKamas = 0;
					perso.set_kamas(newKamas);
					
					//Si en ligne (normalement oui)
					if(perso.isOnline())
						SocketManager.GAME_SEND_STATS_PACKET(perso);
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 5://objet
				try
				{
					int tID = Integer.parseInt(args.split(",")[0]);
					int count = Integer.parseInt(args.split(",")[1]);
					boolean send = true;
					if(args.split(",").length >2)send = args.split(",")[2].equals("1");
					
					//Si on ajoute
					if(count > 0)
					{
						ObjTemplate T = World.getObjTemplate(tID);
						if(T == null)return;
						Objet O = T.createNewItem(count, false);
						//Si retourne true, on l'ajoute au monde
						if(perso.addObjet(O, true))
							World.addObjet(O, true);
					}else
					{
						perso.removeByTemplateID(tID,-count);
					}
					//Si en ligne (normalement oui)
					if(perso.isOnline())//on envoie le packet qui indique l'ajout//retrait d'un item
					{
						SocketManager.GAME_SEND_Ow_PACKET(perso);
						if(send)
						{
							if(count >= 0){
								SocketManager.GAME_SEND_Im_PACKET(perso, "021;"+count+"~"+tID);
							}
							else if(count < 0){
								SocketManager.GAME_SEND_Im_PACKET(perso, "022;"+-count+"~"+tID);
							}
						}
					}
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 6://Apprendre un métier
				try
				{
					int mID = Integer.parseInt(args);
					if(World.getMetier(mID) == null)return;
					// Si c'est un métier 'basic' :
					if(mID == 	2 || mID == 11 ||
					   mID == 13 || mID == 14 ||
					   mID == 15 || mID == 16 ||
					   mID == 17 || mID == 18 ||
					   mID == 19 || mID == 20 ||
					   mID == 24 || mID == 25 ||
					   mID == 26 || mID == 27 ||
					   mID == 28 || mID == 31 ||
					   mID == 36 || mID == 41 ||
					   mID == 56 || mID == 58 ||
					   mID == 60 || mID == 65)
					{
						if(perso.getMetierByID(mID) != null)//Métier déjà appris
						{
							SocketManager.GAME_SEND_Im_PACKET(perso, "111");
						}
						
						if(perso.totalJobBasic() > 2)//On compte les métiers déja acquis si c'est supérieur a 2 on ignore
						{
							SocketManager.GAME_SEND_Im_PACKET(perso, "19");
						}else//Si c'est < ou = à 2 on apprend
						{
							perso.learnJob(World.getMetier(mID));
						}
					}
					// Si c'est une specialisations 'FM' :
					if(mID == 	43 || mID == 44 ||
					   mID == 45 || mID == 46 ||
					   mID == 47 || mID == 48 ||
					   mID == 49 || mID == 50 ||
					   mID == 62 || mID == 63 ||
					   mID == 64)
					{
						//Es necesario nivel 65 en oficio basico
						/*if(perso.getMetierByID(17) != null && perso.getMetierByID(17).get_lvl() >= 65 && mID == 43
						|| perso.getMetierByID(11) != null && perso.getMetierByID(11).get_lvl() >= 65 && mID == 44
						|| perso.getMetierByID(14) != null && perso.getMetierByID(14).get_lvl() >= 65 && mID == 45
						|| perso.getMetierByID(20) != null && perso.getMetierByID(20).get_lvl() >= 65 && mID == 46
						|| perso.getMetierByID(31) != null && perso.getMetierByID(31).get_lvl() >= 65 && mID == 47
						|| perso.getMetierByID(13) != null && perso.getMetierByID(13).get_lvl() >= 65 && mID == 48
						|| perso.getMetierByID(19) != null && perso.getMetierByID(19).get_lvl() >= 65 && mID == 49
						|| perso.getMetierByID(18) != null && perso.getMetierByID(18).get_lvl() >= 65 && mID == 50
						|| perso.getMetierByID(15) != null && perso.getMetierByID(15).get_lvl() >= 65 && mID == 62
						|| perso.getMetierByID(16) != null && perso.getMetierByID(16).get_lvl() >= 65 && mID == 63
						|| perso.getMetierByID(27) != null && perso.getMetierByID(27).get_lvl() >= 65 && mID == 64)*/
						{
							//On compte les specialisations déja acquis si c'est supérieur a 2 on ignore
							if(perso.getMetierByID(mID) != null)//Métier déjà appris
							{
								SocketManager.GAME_SEND_Im_PACKET(perso, "111");
							}
							
							if(perso.totalJobFM() > 2)//On compte les métiers déja acquis si c'est supérieur a 2 on ignore
							{
								SocketManager.GAME_SEND_Im_PACKET(perso, "19");
							}
							else//Si c'est < ou = à 2 on apprend
							{
								perso.learnJob(World.getMetier(mID));
								//perso.getMetierByID(mID).addXp(perso, 582000);//Level 100 direct
							}	
						/*}else
						{
							SocketManager.GAME_SEND_Im_PACKET(perso, "12");*/
						}
					}
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 7://retour au point de sauvegarde
				perso.warpToSavePos();
			break;
			case 8://Ajouter une Stat
				try {
					int statID = Integer.parseInt(args.split(",", 2)[0]);
					int cantidad = Integer.parseInt(args.split(",", 2)[1]);
					int mensajeID = 0;
					switch (statID) 
			    	{
					case Constants.STATS_ADD_SAGE:
						if (perso.getScrollSabiduria() >= 201) 
						{
							SocketManager.GAME_SEND_MESSAGE(perso,"Vous avez arrivé aux maximum du scroll dans c'est caracteristique.",CyonEmu.CONFIG_MOTD_COLOR);
							return;
						}
						perso.addScrollSabiduria(cantidad);
						mensajeID = 9;
						break;
					case Constants.STATS_ADD_FORC:
						if (perso.getScrollFuerza() >= 201) {
							SocketManager.GAME_SEND_MESSAGE(perso,"Vous avez arrivé aux maximum du scroll dans c'est caracteristique.",CyonEmu.CONFIG_MOTD_COLOR);
							return;
						}
						perso.addScrollFuerza(cantidad);
						mensajeID = 10;
						break;
					case Constants.STATS_ADD_CHAN:
						if (perso.getScrollSuerte() >= 201) {
							SocketManager.GAME_SEND_MESSAGE(perso,"Vous avez arrivé aux maximum du scroll dans c'est caracteristique.",CyonEmu.CONFIG_MOTD_COLOR);
							return;
						}
						perso.addScrollSuerte(cantidad);
						mensajeID = 11;
						break;
					case Constants.STATS_ADD_AGIL:
						if (perso.getScrollAgilidad() >= 201) {
							SocketManager.GAME_SEND_MESSAGE(perso,"Vous avez arrivé aux maximum du scroll dans c'est caracteristique.",CyonEmu.CONFIG_MOTD_COLOR);
							return;
						}
						perso.addScrollAgilidad(cantidad);
						mensajeID = 12;
						break;
					case Constants.STATS_ADD_VITA:
						if (perso.getScrollVitalidad() >= 201) {
							SocketManager.GAME_SEND_MESSAGE(perso,"Vous avez arrivé aux maximum du scroll dans c'est caracteristique.",CyonEmu.CONFIG_MOTD_COLOR);
							return;
						}
						perso.addScrollVitalidad(cantidad);
						mensajeID = 13;
						break;
					case Constants.STATS_ADD_INTE:
						if (perso.getScrollInteligencia() >= 201) {
							SocketManager.GAME_SEND_MESSAGE(perso,"Vous avez arrivé aux maximum du scroll dans c'est caracteristique.",
											CyonEmu.CONFIG_MOTD_COLOR);
							return;
						}
						perso.addScrollInteligencia(cantidad);
						mensajeID = 14;
						break;
				}
					perso.get_baseStats().addOneStat(statID, cantidad);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					if (mensajeID > 0)
						SocketManager.GAME_SEND_Im_PACKET(perso, "0" + mensajeID
								+ ";" + cantidad);
				} catch (Exception e) {
					return;
				}
				break;
			case 9://Apprendre un sort
				try
				{
					int sID = Integer.parseInt(args);
					if(World.getSort(sID) == null)return;
					perso.learnSpell(sID,1, true,true);
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 10://Pain/potion/viande/poisson
				try
				{
					int min = Integer.parseInt(args.split(",",2)[0]);
					int max = Integer.parseInt(args.split(",",2)[1]);
					if(max == 0) max = min;
					int val = Formulas.getRandomValue(min, max);
					if(target != null)
					{
						if(target.get_PDV() + val > target.get_PDVMAX())val = target.get_PDVMAX()-target.get_PDV();
						target.set_PDV(target.get_PDV()+val);
						SocketManager.GAME_SEND_STATS_PACKET(target);
					}
					else
					{
						if(perso.get_PDV() + val > perso.get_PDVMAX())val = perso.get_PDVMAX()-perso.get_PDV();
						perso.set_PDV(perso.get_PDV()+val);
						SocketManager.GAME_SEND_STATS_PACKET(perso);
					}
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 11://Definir l'alignement
				try
				{
					byte newAlign = Byte.parseByte(args.split(",",2)[0]);
					boolean replace = Integer.parseInt(args.split(",",2)[1]) == 1;
					if(perso.get_align() != Constants.ALIGNEMENT_NEUTRE && !replace)return;
					perso.modifAlignement(newAlign);
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
		    case 13: //Reset Carac
		        try
		        {
		          perso.resetearStats();
		          perso.addCapital((perso.get_lvl() - 1) * 5 - perso.get_capital());
                  SocketManager.GAME_SEND_STATS_PACKET(perso);
		        }catch(Exception e){GameServer.addToLog(e.getMessage());};
		    break;
		    case 14://Ouvrir l'interface d'oublie de sort
		    	perso.setisForgetingSpell(true);
				SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+', perso);
			break;
			case 15://Téléportation donjon
				try
				{
					short newMapID = Short.parseShort(args.split(",")[0]);
					int newCellID = Integer.parseInt(args.split(",")[1]);
					int ObjetNeed = Integer.parseInt(args.split(",")[2]);
					int MapNeed = Integer.parseInt(args.split(",")[3]);
					if(ObjetNeed == 0)
					{
						//Téléportation sans objets
						perso.teleport(newMapID,newCellID);
					}else if(ObjetNeed > 0)
					{
					if(MapNeed == 0)
					{
						//Téléportation sans map
						perso.teleport(newMapID,newCellID);
					}else if(MapNeed > 0)
					{
					if (perso.hasItemTemplate(ObjetNeed, 1) && perso.get_curCarte().get_id() == MapNeed)
					{
						//Le perso a l'item
						//Le perso est sur la bonne map
						//On téléporte, on supprime après
						perso.teleport(newMapID,newCellID);
						perso.removeByTemplateID(ObjetNeed, 1);
						SocketManager.GAME_SEND_Ow_PACKET(perso);
					}
					else if(perso.get_curCarte().get_id() != MapNeed)
					{
						//Le perso n'est pas sur la bonne map
						SocketManager.GAME_SEND_MESSAGE(perso, "Vous n'êtes pas sur la bonne map du donjon pour être téléporter.", "009900");
					}
					else
					{
						//Le perso ne possède pas l'item
						SocketManager.GAME_SEND_MESSAGE(perso, "Vous ne possedez pas la clef nécessaire.", "009900");
					}
					}
					}
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 17://Xp métier JobID,XpValue
				try
				{
					int JobID = Integer.parseInt(args.split(",")[0]);
					int XpValue = Integer.parseInt(args.split(",")[1]);
					if(perso.getMetierByID(JobID) != null)
					{
						perso.getMetierByID(JobID).addXp(perso, XpValue);
					}
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 18://Téléportation chez sois
				if(House.AlreadyHaveHouse(perso))//Si il a une maison
				{
					Objet obj = World.getObjet(itemID);
					if (perso.hasItemTemplate(obj.getTemplate().getID(), 1))
					{
						perso.removeByTemplateID(obj.getTemplate().getID(),1);
						House h = House.get_HouseByPerso(perso);
						if(h == null) return;
						perso.teleport((short)h.get_mapid(), h.get_caseid());
					}
				}
			break;
			case 19://Téléportation maison de guilde (ouverture du panneau de guilde)
				SocketManager.GAME_SEND_GUILDHOUSE_PACKET(perso);
			break;
			case 20://+Points de sorts
				try
				{
					int pts = Integer.parseInt(args);
					if(pts < 1) return;
					perso.addSpellPoint(pts);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 21://+Energie
				try
				{
					int Energy = Integer.parseInt(args);
					if(Energy < 1) return;
					
					int EnergyTotal = perso.get_energy()+Energy;
					if(EnergyTotal > 10000) EnergyTotal = 10000;
					
					perso.set_energy(EnergyTotal);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 22://+Xp
				try
				{
					long XpAdd = Integer.parseInt(args);
					if(XpAdd < 1) return;
					
					long TotalXp = perso.get_curExp()+XpAdd;
					perso.set_curExp(TotalXp);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 23://UnlearnJob
				try
				{
					int Job = Integer.parseInt(args);
					if(Job < 1) return;
					StatsMetier m = perso.getMetierByID(Job);
					if(m == null) return;
					perso.unlearnJob(ID);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					SQLManager.SAVE_PERSONNAGE(perso, false);
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 24://SimpleMorph
				try
				{
					int morphID = Integer.parseInt(args);
					if(morphID < 0)return;
					perso.set_gfxID(morphID);
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.get_curCarte(), perso.get_GUID());
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.get_curCarte(), perso);
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 25://SimpleUnMorph
				int UnMorphID = perso.get_classe()*10 + perso.get_sexe();
				perso.set_gfxID(UnMorphID);
				SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.get_curCarte(), perso.get_GUID());
				SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.get_curCarte(), perso);
			break;
			case 26://Téléportation enclo de guilde (ouverture du panneau de guilde)
				SocketManager.GAME_SEND_GUILDENCLO_PACKET(perso);
			break;
			case 27://startFigthVersusMonstres args : monsterID,monsterLevel| ...
				String ValidMobGroup = "";
				try
		        {
					for(String MobAndLevel : args.split("\\|"))
					{
						int monsterID = -1;
						int monsterLevel = -1;
						String[] MobOrLevel = MobAndLevel.split(",");
						monsterID = Integer.parseInt(MobOrLevel[0]);
						monsterLevel = Integer.parseInt(MobOrLevel[1]);
						
						if(World.getMonstre(monsterID) == null || World.getMonstre(monsterID).getGradeByLevel(monsterLevel) == null)
						{
							if(CyonEmu.CONFIG_DEBUG)
							continue;
						}
						ValidMobGroup += monsterID+","+monsterLevel+","+monsterLevel+";";
					}
					if(ValidMobGroup.isEmpty()) return;
					MobGroup group  = new MobGroup(perso.get_curCarte()._nextObjectID,perso.get_curCell().getID(),ValidMobGroup);
					perso.get_curCarte().startFigthVersusMonstres(perso, group);
		        }catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			case 31:// cambiar clase
				try {
					int clase = Integer.parseInt(args);
					if (clase == perso.get_classe()) {
						SocketManager.GAME_SEND_MESSAGE(perso, "Vous ete déjà du cette classe.", CyonEmu.CONFIG_MOTD_COLOR);
						return;
					}
					int nivel = perso.get_lvl();
					perso.setClase(clase);
					Stats baseStats = perso.get_baseStats();
					baseStats.addOneStat(125, -perso._baseStats.getEffect(125));
					baseStats.addOneStat(124, -perso._baseStats.getEffect(124));
					baseStats.addOneStat(118, -perso._baseStats.getEffect(118));
					baseStats.addOneStat(123, -perso._baseStats.getEffect(123));
					baseStats.addOneStat(119, -perso._baseStats.getEffect(119));
					baseStats.addOneStat(126, -perso._baseStats.getEffect(126));
					baseStats.addOneStat(125, perso.getScrollVitalidad());
					baseStats.addOneStat(124, perso.getScrollSabiduria());
					baseStats.addOneStat(118, perso.getScrollFuerza());
					baseStats.addOneStat(123, perso.getScrollSuerte());
					baseStats.addOneStat(119, perso.getScrollAgilidad());
					baseStats.addOneStat(126, perso.getScrollInteligencia());
					Thread.sleep(150);
					perso.setCapital(0);
					perso.set_spellPts(0);
					perso.setHechizos(Constants.getStartSorts(clase));
					Thread.sleep(150);
					perso.set_lvl(1);
					while (perso.get_lvl() < nivel) {
						perso.levelUp(false, false);
					}
					int deformaID = clase * 10 + perso.get_sexe();
					perso.set_gfxID(deformaID);
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.get_curCarte(), perso.get_GUID());
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.get_curCarte(), perso);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					SocketManager.GAME_SEND_ASK(out, perso);
					SocketManager.GAME_SEND_SPELL_LIST(perso);
					Thread.sleep(150);
					SQLManager.CAMBIAR_SEXO_CLASE(perso);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			   case 32: // cambiar sexo
				try {
					perso.cambiarSexo();
					Thread.sleep(300);
					int deformaID = perso.get_classe() * 10 + perso.get_sexe();
					perso.set_gfxID(deformaID);
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.get_curCarte(), perso.get_GUID());
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.get_curCarte(), perso);
					SQLManager.CAMBIAR_SEXO_CLASE(perso);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				case 50:// Agresion Mision
				if (perso.get_align() == 0 || perso.get_align() == 3) {
					
					return;
				}
				if (perso.get_traque() == null) {
					traque mision = new traque(0, null);
					perso.set_traque(mision);
				}
				if (perso.get_traque().get_time() < System.currentTimeMillis() - 600000
						|| perso.get_traque().get_time() == 0) {
					Personnage tempP = null;
					ArrayList<Personnage> victimas = new ArrayList<Personnage>();
					for (Personnage victima : World.getOnlinePersos()) {
						if (victima == null || victima == perso)
							continue;
						if (victima.get_compte().get_curIP()
								.compareTo(perso.get_compte().get_curIP()) == 0)
							continue;
						if (victima.get_align() == perso.get_align()
								|| victima.get_align() == 0
								|| victima.get_align() == 3
								|| !victima.is_showWings())
							continue;
						if (((perso.get_lvl() + 20) >= victima.get_lvl())
								&& ((perso.get_lvl() - 20) <= victima.get_lvl()))
							victimas.add(victima);
					}
					if (victimas.size() == 0) {
						SocketManager.GAME_SEND_MESSAGE(perso,"Nous n'avons pas trouve de cible a ta hauteur, reviens plus tard.", "000000");
						break;
					}
					tempP = victimas.get(Formulas.getRandomValue(0,
							victimas.size() - 1));
					SocketManager.GAME_SEND_MESSAGE(perso, "Vous etes desormais en chasse de : " + tempP.get_name(), "000000");
					perso.get_traque().set_traqued(tempP);
					perso.get_traque().set_time(System.currentTimeMillis());
					ObjTemplate T = World.getObjTemplate(10085);
					Objet nuevoObj = T.createNewItem(20, false);
					nuevoObj.addTxtStat(989, tempP.get_name());
					if (perso.addObjet(nuevoObj, true)) {
						World.addObjet(nuevoObj, true);
					} else {
						perso.removeByTemplateID(T.getID(), 20);
					}
				} else {
					SocketManager.GAME_SEND_MESSAGE(perso, "Vous venez juste de signer un contrat, vous devez vous reposer.", "000000");
				}
				break;
			case 51://Cible sur la géoposition
				String perr = "";
				
				perr = World.getObjet(itemID).getTraquedName();
				if(perr == null)
				{
					break;	
				}
				Personnage cible = World.getPersoByName(perr);
				if(cible==null)break;
				if(!cible.isOnline())
				{
					SocketManager.GAME_SEND_Im_PACKET(perso, "1198");
					break;
				}
				SocketManager.GAME_SEND_FLAG_PACKET(perso, cible);
			break;
			case 100://Donner l'abilité 'args' à une dragodinde
                Dragodinde mount = perso.getMount();
                World.addDragodinde(
                  new Dragodinde(
                 mount.get_id(),
                 mount.get_color(),
                 mount.get_sexe(),
                 mount.get_amour(),
                 mount.get_endurance(),
                 mount.get_level(),
                 mount.get_exp(),
                 mount.get_nom(),
                 mount.get_fatigue(),
                 mount.get_energie(),
                 mount.get_reprod(),
                 mount.get_maturite(),
                 mount.get_serenite(),
                 mount.getItemsId(),
                 mount.get_ancetres(),
                 args));
                 perso.setMount(World.getDragoByID(mount.get_id()));
                 SocketManager.GAME_SEND_Re_PACKET(perso, "+", World.getDragoByID(mount.get_id()));
                 SQLManager.UPDATE_MOUNT_INFOS(mount);
                 break;
                case 101://Arriver sur case de mariage
				if((perso.get_sexe() == 0 && perso.get_curCell().getID() == 282) || (perso.get_sexe() == 1 && perso.get_curCell().getID() == 297))
				{
					World.AddMarried(perso.get_sexe(), perso);
				}else 
				{
					SocketManager.GAME_SEND_Im_PACKET(perso, "1102");
				}
			break;
			case 102://Marier des personnages
				World.PriestRequest(perso, perso.get_curCarte(), perso.get_isTalkingWith());
			break;
			case 103://Divorce
				if(perso.get_kamas() < 50000)
				{
					return;
				}else
				{
					perso.set_kamas(perso.get_kamas()-50000);
					Personnage wife = World.getPersonnage(perso.getWife());
					wife.Divorce();
					perso.Divorce();
				}
			break;
			case 170:// Dar un titulo
				try
			    {	
					byte titulo = (byte) Integer.parseInt(args); 
					target= World.getPersoByName(perso.get_name());
					target.set_title(titulo);
                        SocketManager.GAME_SEND_MESSAGE(perso, "<b>¡Maintnaint vous posséde un nouveaux title!</b>", CyonEmu.CONFIG_MOTD_COLOR);
						SocketManager.GAME_SEND_STATS_PACKET(perso);
						SQLManager.SAVE_PERSONNAGE(perso, false);
						if(target.get_fight() == null) 
						SocketManager.GAME_SEND_ALTER_GM_PACKET(perso.get_curCarte(), perso);
					}catch(Exception e){GameServer.addToLog(e.getMessage());};
				break;
			case 201:// Poser un Prisme
				try {
					int cellperso = perso.get_curCell().getID();
					Carte tCarte = perso.get_curCarte();
					common.World.SubArea subarea = tCarte.getSubArea();
					common.World.Area area = subarea.get_area();
					int alignement = perso.get_align();
					if (cellperso <= 0) {
						return;
					}
					if (alignement == 0 || alignement == 3) {
						SocketManager.GAME_SEND_MESSAGE(perso,
								"Vous ne possedez pas l'alignement necessaire pour poser un prisme", CyonEmu.CONFIG_MOTD_COLOR);
						return;
					}
					if (!perso.is_showWings()) {
						SocketManager.GAME_SEND_MESSAGE(perso,
								"Vos ailes doivent être actives afin de poser un prisme.", CyonEmu.CONFIG_MOTD_COLOR);
						return;
					}
					if (CyonEmu.CartesNoPrismes.contains(tCarte.get_id())) {
						SocketManager.GAME_SEND_MESSAGE(perso, "Vous ne pouvez pas poser un prisme dans cette map", CyonEmu.CONFIG_MOTD_COLOR);
						return;
					}
					if (subarea.get_alignement() != 0 || !subarea.getConquistable()) {
						SocketManager.GAME_SEND_MESSAGE(perso,
								"L'alignement de cette sous-zone est en conquète ou n'est pas neutre !", CyonEmu.CONFIG_MOTD_COLOR);
						return;
					}
					Prisme Prisme = new Prisme(World.getNextIDPrisme(), alignement, 1, tCarte.get_id(), cellperso, 0, -1);
					subarea.setalignement(alignement);
					subarea.setPrismeID(Prisme.getID());
					for (Personnage z : World.getOnlinePersos()) {
						if (z == null)
							continue;
						if (z.get_align() == 0) {
							SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(z, subarea.get_id() + "|" + alignement + "|1");
							if (area.getalignement() == 0)
								SocketManager.GAME_SEND_aM_ALIGN_PACKET_TO_AREA(z, area.get_id() + "|" + alignement);
							continue;
						}
						SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(z, subarea.get_id() + "|" + alignement + "|0");
						if (area.getalignement() == 0)
							SocketManager.GAME_SEND_aM_ALIGN_PACKET_TO_AREA(z, area.get_id() + "|" + alignement);
					}
					if (area.getalignement() == 0) {
						area.setPrismeID(Prisme.getID());
						area.setalignement(alignement);
						Prisme.setAreaConquistada(area.get_id());
					}
					World.addPrisme(Prisme);
					SQLManager.ADD_PRISME(Prisme);
					SocketManager.GAME_SEND_PRISME_TO_MAP(tCarte, Prisme);
				} catch (Exception e) {}
				break;
		  case 228://Faire animation Hors Combat
				try
				{
					int AnimationId = Integer.parseInt(args);
					Animations animation = World.getAnimation(AnimationId);
					if(perso.get_fight() != null) return;
					perso.changeOrientation(1);
					SocketManager.GAME_SEND_GA_PACKET_TO_MAP(perso.get_curCarte(), "0", 228, perso.get_GUID()+";"+cellid+","+Animations.PrepareToGA(animation), "");
				}catch(Exception e){GameServer.addToLog(e.getMessage());};
			break;
			default:
			break;
		}
	}


	public int getID()
	{
		return ID;
	}
}
