package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Charlatan extends StdCharClass
{
	public String ID(){return "Charlatan";}
	public String name(){return "Charlatan";}
	public String baseClass(){return "Bard";}
	public int getMaxHitPointsLevel(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusManaLevel(){return 8;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.DEXTERITY;}
	public int getLevelsPerBonusDamage(){ return 4;}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	
	public Charlatan()
	{
		super();
		maxStat[CharStats.CHARISMA]=22;
		maxStat[CharStats.WISDOM]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Nothing",true);
	
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Haggle",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),4,"Song_Charm",false);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Disguise",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Warrants",true);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_ReadMagic",false);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Songcraft",false);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_Distract",false);
			CMAble.addCharAbilityMapping(ID(),10,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),11,"Song_Comprehension",false);
			CMAble.addCharAbilityMapping(ID(),12,"Ranger_Track",false);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Map",true);
			CMAble.addCharAbilityMapping(ID(),15,"Song_Protection",false);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Chantcraft",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),18,"Skill_Prayercraft",false);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Swipe",false);
		}
	}
	
	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Charisma 9+, Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Charlatan.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.WISDOM) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Charlatan.");
			return false;
		}
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
		{
			if(!quiet)
				mob.tell("You must be Human or Half Elf to be a Bard");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
	}
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, ranged, thrown, natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear non-metal armor to avoid skill failure.";}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "Receives 2% resistance per level to mind affects, 4% resistance per level to divination spells.  Gains a random non-class skill or spell every other level!";}
	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!(myHost instanceof MOB)) return super.okAffect(myHost,affect);
		MOB myChar=(MOB)myHost;
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if(((affect.sourceMajor()&Affect.MASK_DELICATE)>0)
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> fumble(s) in <S-HIS-HER> maneuver!");
					return false;
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				int classification=((Weapon)affect.tool()).weaponClassification();
				switch(classification)
				{
				case Weapon.CLASS_SWORD:
				case Weapon.CLASS_RANGED:
				case Weapon.CLASS_THROWN:
				case Weapon.CLASS_NATURAL:
				case Weapon.CLASS_DAGGER:
					break;
				default:
					if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
					{
						myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().displayName()+".");
						return false;
					}
					break;
				}
			}
		}
		else
		if(affect.amITarget(myChar))
		{
			if((affect.tool()!=null)
			   &&(affect.tool() instanceof Ability)
			   &&((((Ability)affect.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
			   &&((((Ability)affect.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_DIVINATION)
			   &&(Dice.roll(1,100,0)<(myChar.charStats().getClassLevel(this)*4)))
			{
				myChar.location().show(affect.source(),myChar,Affect.MSG_OK_ACTION,"<T-NAME> fool(s) <S-NAMESELF>, causing <S-HIM-HER> to fizzle "+affect.tool().displayName()+".");
				return false;
			}
		}
		
		return super.okAffect(myChar,affect);
	}
	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		
		// if he already has one, don't give another!
		if(!mob.isMonster())
		{
			int classLevel=mob.baseCharStats().getClassLevel(this);
			if(classLevel<2) return;
			if((classLevel%2)!=0) return;
			
			for(int a=0;a<mob.numAbilities();a++)
			{
				Ability A=mob.fetchAbility(a);
				if((CMAble.getQualifyingLevel(ID(),A.ID())<0)
				&&((CMAble.lowestQualifyingLevel(A.ID())==classLevel)||(CMAble.lowestQualifyingLevel(A.ID())==classLevel-1)))
					return;
			}
			// now only give one, for current level, respecting alignment!
			Vector choices=new Vector();
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if((CMAble.getQualifyingLevel(ID(),A.ID())<0)
				&&((CMAble.lowestQualifyingLevel(A.ID())==classLevel)||(CMAble.lowestQualifyingLevel(A.ID())==classLevel-1)))
					choices.addElement(A);
			}
			if(choices.size()==0) return;
			Ability A=(Ability)choices.elementAt(Dice.roll(1,choices.size(),-1));
			if(A!=null)	giveMobAbility(mob,A,0,"",isBorrowedClass);
		}
	}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+(2*affectableStats.getClassLevel(this)));
	}
}
