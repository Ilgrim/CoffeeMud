package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Alarm extends Spell
{
	public String ID() { return "Spell_Alarm"; }
	public String name(){return "Alarm";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	Room myRoomContainer=null;

	boolean waitingForLook=false;

	public Environmental newInstance(){	return new Spell_Alarm();}
	public int classificationCode(){	return Ability.SPELL | Ability.DOMAIN_ENCHANTMENT;}


	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);

		if((affected==null)||(invoker==null))
		{
			unInvoke();
			return;
		}

		if(affect.source()!=null)
		{
			myRoomContainer=affect.source().location();
			if(affect.source()==invoker) return;
		}

		if(affect.amITarget(affected))
		{
			myRoomContainer.showHappens(Affect.MSG_NOISE,"A HORRENDOUS ALARM GOES OFF, WHICH SEEMS TO BE COMING FROM "+affected.displayName().toUpperCase()+"!!!");
			invoker.tell("The alarm on your "+affected.displayName()+" has gone off.");
			unInvoke();
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> glow(s) faintly for a short time.":"^S<S-NAME> touch(es) <T-NAMESELF> very lightly.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				myRoomContainer=mob.location();
				beneficialAffect(mob,target,0);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) and touch(es) <T-NAMESELF> very lightly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}