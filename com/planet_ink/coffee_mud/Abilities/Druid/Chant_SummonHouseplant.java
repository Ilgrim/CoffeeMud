package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SummonHouseplant extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonHouseplant"; }
	public String name(){ return "Summon Houseplant";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonHouseplant();}
	private boolean processing=false;
	
	public void affect(Environmental myHost, Affect affect)
	{
		if((affect.amITarget(littlePlants))
		&&(!processing)
		&&(affect.targetMinor()==Affect.TYP_GET))
		{
			processing=true;
			Ability A=littlePlants.fetchAffect(ID());
			if(A!=null)
			{
				ExternalPlay.deleteTick(A,-1);
				littlePlants.delAffect(A);
				littlePlants.setSecretIdentity("");
			}
			if(littlePlants.fetchBehavior("Decay")==null)
			{
				Behavior B=CMClass.getBehavior("Decay");
				B.setParms("min="+Host.TICKS_PER_DAY+" max="+Host.TICKS_PER_DAY+" chance=100");
				littlePlants.addBehavior(B);
				B.affect(myHost,affect);
			}
			processing=false;
		}
	}
	public boolean rightPlace(MOB mob,boolean auto)
	{
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_STONE)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WOOD))
		{
			mob.tell("This is not the place for a houseplant.");
			return false;
		}
		return true;
	}
	
	public static Item buildHouseplant(MOB mob, Room room)
	{
		Item newItem=(Item)CMClass.getItem("GenItem");
		newItem.setMaterial(EnvResource.RESOURCE_GREENS);
		switch(Dice.roll(1,7,0))
		{
		case 1:
			newItem.setName("a potted rose");
			newItem.setDisplayText("a potted rose is here.");
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName("a potted daisy");
			newItem.setDisplayText("a potted daisy is here.");
			newItem.setDescription("");
			break;
		case 3:
			newItem.setName("a potted carnation");
			newItem.setDisplayText("a potted white carnation is here");
			newItem.setDescription("");
			break;
		case 4:
			newItem.setName("a potted sunflower");
			newItem.setDisplayText("a potted sunflowers is here.");
			newItem.setDescription("Happy flowers have little yellow blooms.");
			break;
		case 5:
			newItem.setName("a potted gladiola");
			newItem.setDisplayText("a potted gladiola is here.");
			newItem.setDescription("");
			break;
		case 6:
			newItem.setName("a potted fern");
			newItem.setDisplayText("a potted fern is here.");
			newItem.setDescription("Like a tiny bush, this dark green plant is lovely.");
			break;
		case 7:
			newItem.setName("a potted patch of bluebonnets");
			newItem.setDisplayText("a potted patch of bluebonnets is here.");
			newItem.setDescription("Happy flowers with little blue and purple blooms.");
			break;
		}
		newItem.setSecretIdentity(mob.name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.baseEnvStats().setWeight(1);
		newItem.setDispossessionTime(0);
		room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.displayName()+" appears here.");
		Chant_SummonFlower newChant=new Chant_SummonFlower();
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		newChant.beneficialAffect(mob,newItem,(newChant.adjustedLevel(mob)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public Item buildMyPlant(MOB mob, Room room)
	{
		return buildHouseplant(mob,room);
	}
}
