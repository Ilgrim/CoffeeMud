package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Follow extends StdCommand
{
	public Follow(){}

	private String[] access={getScr("Follow","cmd1"),getScr("Follow","cmd2"),getScr("Follow","cmd3"),getScr("Follow","cmd4")};
	public String[] getAccessWords(){return access;}


	public boolean nofollow(MOB mob, boolean errorsOk, boolean quiet)
	{
		if(mob==null) return false;
		Room R=mob.location();
		if(R==null) return false;
		if(mob.amFollowing()!=null)
		{
			CMMsg msg=CMClass.getMsg(mob,mob.amFollowing(),null,CMMsg.MSG_NOFOLLOW,quiet?null:"<S-NAME> stop(s) following <T-NAMESELF>.");
			// no room OKaffects, since the damn leader may not be here.
			if(mob.okMessage(mob,msg))
				R.send(mob,msg);
			else
				return false;
		}
		else
		if(errorsOk)
			mob.tell(getScr("Follow","nofolany"));
		return true;
	}

	public void unfollow(MOB mob, boolean quiet)
	{
		nofollow(mob,false,quiet);
		Vector V=new Vector();
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB F=mob.fetchFollower(f);
			if(F!=null) V.addElement(F);
		}
		for(int v=0;v<V.size();v++)
		{
			MOB F=(MOB)V.elementAt(v);
			nofollow(F,false,quiet);
		}
	}


	public boolean processFollow(MOB mob, MOB tofollow, boolean quiet)
	{
		if(mob==null) return false;
		Room R=mob.location();
		if(R==null) return false;
		if(tofollow!=null)
		{
			if(tofollow==mob)
			{
				return nofollow(mob,true,false);
			}
			if(mob.getGroupMembers(new HashSet()).contains(tofollow))
			{
				if(!quiet)
					mob.tell(getScr("Follow","alreadyfol",tofollow.name()));
				return false;
			}
			if(nofollow(mob,false,false))
			{
				CMMsg msg=CMClass.getMsg(mob,tofollow,null,CMMsg.MSG_FOLLOW,quiet?null:"<S-NAME> follow(s) <T-NAMESELF>.");
				if(R.okMessage(mob,msg))
					R.send(mob,msg);
				else
					return false;
			}
			else
				return false;
		}
		else
			return nofollow(mob,!quiet,quiet);
		return true;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean quiet=false;

		if(mob==null) return false;
		Room R=mob.location();
		if(R==null) return false;
		if((commands.size()>2)
		&&(commands.lastElement() instanceof String)
		&&(((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			commands.removeElementAt(commands.size()-1);
			quiet=true;
		}
		if((commands.size()>1)&&(commands.elementAt(1) instanceof MOB))
			return processFollow(mob,(MOB)commands.elementAt(1),quiet);

		if(commands.size()<2)
		{
			mob.tell(getScr("Follow","whom"));
			return false;
		}

		String whomToFollow=CMParms.combine(commands,1);
		if((whomToFollow.equalsIgnoreCase(getScr("Follow","self"))||whomToFollow.equalsIgnoreCase(getScr("Follow","me")))
		   ||(mob.name().toUpperCase().startsWith(whomToFollow)))
		{
			nofollow(mob,true,quiet);
			return false;
		}
		MOB target=R.fetchInhabitant(whomToFollow);
		if((target==null)||((target!=null)&&(!CMLib.flags().canBeSeenBy(target,mob))))
		{
			mob.tell(getScr("Follow","nosee"));
			return false;
		}
		if((target.isMonster())&&(!mob.isMonster()))
		{
			mob.tell(getScr("Follow","nofollow")+target.name()+"'.");
			return false;
		}
		if(CMath.bset(target.getBitmap(),MOB.ATT_NOFOLLOW))
		{
			mob.tell(target.name()+getScr("Follow","noaccept"));
			return false;
		}
        MOB ultiTarget=target.amUltimatelyFollowing();
        if((ultiTarget!=null)&&(CMath.bset(ultiTarget.getBitmap(),MOB.ATT_NOFOLLOW)))
        {
            mob.tell(ultiTarget.name()+getScr("Follow","noaccept"));
            return false;
        }
		processFollow(mob,target,quiet);
		return false;
	}
    public double combatActionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
