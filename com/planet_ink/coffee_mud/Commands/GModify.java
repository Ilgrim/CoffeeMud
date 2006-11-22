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
import java.util.regex.*;

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
public class GModify extends StdCommand
{
    public GModify(){}

    private String[] access={getScr("GModify","cmd1")};
    public String[] getAccessWords(){return access;}
    
    private static final int FLAG_CASESENSITIVE=1;
    private static final int FLAG_SUBSTRING=2;
    private static final int FLAG_OR=4;
    private static final int FLAG_AND=8;

    public static String getStat(Environmental E, String stat)
    {
        if((stat!=null)&&(stat.length()>0)&&(stat.equalsIgnoreCase("REJUV")))
        {
            if(E.baseEnvStats().rejuv()==Integer.MAX_VALUE)
                return "0";
            return ""+E.baseEnvStats().rejuv();
        }
        return E.getStat(stat);
    }

    public static void setStat(Environmental E, String stat, String value)
    {
        if((stat!=null)&&(stat.length()>0)&&(stat.equalsIgnoreCase("REJUV")))
            E.baseEnvStats().setRejuv(CMath.s_int(value));
        else
            E.setStat(stat,value);
    }

    public static void gmodifydebugtell(MOB mob, String msg)
    {
        if(mob!=null) mob.tell(msg);
        Log.sysOut(getScr("GModify","cmd1"),msg);
    }

    private static boolean tryModfy(MOB mob,
                                    Room room,
                                    Environmental E,
                                    DVector changes,
                                    DVector onfields,
                                    boolean noisy)
    {
        if((mob.session()==null)||(mob.session().killFlag()))
        	return false;
        boolean didAnything=false;
        if(noisy) gmodifydebugtell(mob,E.name()+"/"+CMClass.classID(E));
        String field=null;
        String value=null;
        String equator=null;
        String stat=null;
        int codes=-1;
        Pattern pattern=null;
        boolean checkedOut=true;
        Matcher M=null;
        DVector matches=new DVector(3);
        int lastCode=FLAG_AND;
        for(int i=0;i<onfields.size();i++)
        {
            field=(String)onfields.elementAt(i,1);
            equator=(String)onfields.elementAt(i,2);
            value=(String)onfields.elementAt(i,3);
            codes=((Integer)onfields.elementAt(i,4)).intValue();
            pattern=(Pattern)onfields.elementAt(i,5);
            if(noisy) gmodifydebugtell(mob,field+"/"+getStat(E,field)+"/"+value+"/"+getStat(E,field).equals(value));
            int matchStart=-1;
            int matchEnd=-1;
            stat=getStat(E,field);
            if(equator.equals("$")&&(pattern!=null))
            {
                if(!CMath.bset(codes,FLAG_SUBSTRING))
                {
                    if(stat.matches(value))
                    {
                        matchStart=0;
                        matchEnd=stat.length();
                    }
                }
                else
                {
                    M=pattern.matcher(stat);
                    M.reset();
                    if(M.find())
                    {
                        matchStart=M.start();
                        matchEnd=M.end();
                    }
                }
            }
            else
            if(equator.equals("="))
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.bset(codes,FLAG_SUBSTRING))
                {
                    matchStart=stat.indexOf(value);
                    matchEnd=matchStart+value.length();
                }
                else
                if(stat.equals(value))
                {
                    matchStart=0;
                    matchEnd=stat.length();
                }
            }
            else
            if(equator.equals("!="))
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.bset(codes,FLAG_SUBSTRING))
                {
                    if(stat.indexOf(value)<0)
                    {
                        matchStart=0;
                        matchEnd=stat.length();
                    }
                }
                else
                if(!stat.equals(value))
                {
                    matchStart=0;
                    matchEnd=stat.length();
                }
            }
            else
            if(equator.equals(">"))
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.isNumber(stat)&&CMath.isNumber(value))
                    matchStart=(CMath.s_long(stat)>CMath.s_long(value))?0:-1;
                else
                    matchStart=(stat.compareTo(value)>0)?0:-1;
            }
            else
            if(equator.equals("<"))
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.isNumber(stat)&&CMath.isNumber(value))
                    matchStart=(CMath.s_long(stat)<CMath.s_long(value))?0:-1;
                else
                    matchStart=(stat.compareTo(value)<0)?0:-1;
            }
            else
            if(equator.equals("<="))
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.isNumber(stat)&&CMath.isNumber(value))
                    matchStart=(CMath.s_long(stat)<=CMath.s_long(value))?0:-1;
                else
                    matchStart=(stat.compareTo(value)<=0)?0:-1;
            }
            else
            if(equator.equals(">="))
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.isNumber(stat)&&CMath.isNumber(value))
                    matchStart=(CMath.s_long(stat)>=CMath.s_long(value))?0:-1;
                else
                    matchStart=(stat.compareTo(value)>=0)?0:-1;
            }
            if(matchStart>=0)
                matches.addElement(field,new Integer(matchStart),new Integer(matchEnd));
            if(CMath.bset(lastCode,FLAG_AND))
                checkedOut=checkedOut&&(matchStart>=0);
            else
            if(CMath.bset(lastCode,FLAG_OR))
                checkedOut=checkedOut||(matchStart>=0);
            else
                checkedOut=(matchStart>=0);
            lastCode=codes;
        }
        if(checkedOut)
        {
            if(changes.size()==0)
                mob.tell(getScr("GModify","matchedon",E.name())+CMLib.map().getExtendedRoomID(room)+".");
            else
            for(int i=0;i<changes.size();i++)
            {
                field=(String)changes.elementAt(i,1);
                value=(String)changes.elementAt(i,3);
                codes=((Integer)changes.elementAt(i,4)).intValue();
                if(noisy) gmodifydebugtell(mob,E.name()+getScr("GModify","wantschange",field,getStat(E,field))+value+"/"+(!getStat(E,field).equals(value)));
                if(CMath.bset(codes,FLAG_SUBSTRING))
                {
                    int matchStart=-1;
                    int matchEnd=-1;
                    for(int m=0;m<matches.size();m++)
                        if(((String)matches.elementAt(m,1)).equals(field))
                        {
                            matchStart=((Integer)matches.elementAt(m,2)).intValue();
                            matchEnd=((Integer)matches.elementAt(m,3)).intValue();
                        }
                    if(matchStart>=0)
                    {
                        stat=getStat(E,field);
                        value=stat.substring(0,matchStart)+value+stat.substring(matchEnd);
                    }
                }
                if(!getStat(E,field).equals(value))
                {
                    Log.sysOut(getScr("GModify","cmd1"),getScr("GModify","changed",CMStrings.capitalizeAndLower(field),E.Name(),room.roomID(),getStat(E,field))+value+".");
                    setStat(E,field,value);
                    didAnything=true;
                }
            }
        }
        if(didAnything)
        {
            E.recoverEnvStats();
            if(E instanceof MOB)
            {
                ((MOB)E).recoverCharStats();
                ((MOB)E).recoverMaxState();
            }
            E.text();
        }
        return didAnything;
    }
    
    public void sortEnumeratedList(Enumeration e, Vector allKnownFields, StringBuffer allFieldsMsg)
    {
        for(;e.hasMoreElements();)
        {
            Environmental E=(Environmental)e.nextElement();
            String[] fields=E.getStatCodes();
            for(int x=0;x<fields.length;x++)
                if(!allKnownFields.contains(fields[x]))
                {
                    allKnownFields.addElement(fields[x]);
                    allFieldsMsg.append(fields[x]+" ");
                }
        }
    }

    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        boolean noisy=CMSecurity.isDebugging(getScr("GModify","cmd1"));
        Vector placesToDo=new Vector();
        String whole=CMParms.combine(commands,0);
        commands.removeElementAt(0);
        if(commands.size()==0)
        {
            mob.tell(getScr("GModify","what"));
            return false;
        }
        if(mob.isMonster())
        {
            mob.tell(getScr("GModify","no"));
            return false;
        }
        if((commands.size()>0)&&
           ((String)commands.elementAt(0)).equalsIgnoreCase("?"))
        {
            StringBuffer allFieldsMsg=new StringBuffer("");
            Vector allKnownFields=new Vector();
            sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.basicItems(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.miscTech(),allKnownFields,allFieldsMsg);
            mob.tell(getScr("GModify","validfields")+allFieldsMsg.toString());
            return false;
        }
        if((commands.size()>0)&&
           ((String)commands.elementAt(0)).equalsIgnoreCase(getScr("GModify","cmdroom")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"GMODIFY"))
            {
                mob.tell(getScr("GModify","noallowed"));
                return false;
            }
            commands.removeElementAt(0);
            placesToDo.addElement(mob.location());
        }
        if((commands.size()>0)&&
           ((String)commands.elementAt(0)).equalsIgnoreCase(getScr("GModify","cmdarea")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"GMODIFY"))
            {
                mob.tell(getScr("GModify","noallowed"));
                return false;
            }
            commands.removeElementAt(0);
            placesToDo.addElement(mob.location().getArea());
        }
        if((commands.size()>0)&&
           ((String)commands.elementAt(0)).equalsIgnoreCase(getScr("GModify","cmdworld")))
        {
            if(!CMSecurity.isAllowedEverywhere(mob,"GMODIFY"))
            {
                mob.tell(getScr("GModify","nohere"));
                return false;
            }
            commands.removeElementAt(0);
            placesToDo=new Vector();
        }
        DVector changes=new DVector(5);
        DVector onfields=new DVector(5);
        DVector use=null;
        Vector allKnownFields=new Vector();
        StringBuffer allFieldsMsg=new StringBuffer("");
        sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.basicItems(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.miscTech(),allKnownFields,allFieldsMsg);

        allKnownFields.addElement("REJUV");
        allFieldsMsg.append("REJUV ");
        use=onfields;
        Vector newSet=new Vector();
        StringBuffer s=new StringBuffer("");
        for(int i=0;i<commands.size();i++)
        {
            String str=((String)commands.elementAt(i));
            if((str.toUpperCase().startsWith(getScr("GModify","cmdchange")))
            ||(str.toUpperCase().startsWith(getScr("GModify","cmdwhen"))))
            {
                if(s.length()>0)
                    newSet.addElement(s.toString());
                s=new StringBuffer("");
            }
            if(str.indexOf(" ")>=0)
                str="\""+str+"\"";
            if(s.length()>0)
                s.append(" "+str);
            else
                s.append(str);
        }
        if(s.length()>0)
            newSet.addElement(s.toString());
        for(int i=0;i<newSet.size();i++)
        {
            String str=((String)newSet.elementAt(i));
            if(str.toUpperCase().startsWith(getScr("GModify","cmdchange")))
            {
                use=changes;
                str=str.substring(7).trim();
            }
            if(str.toUpperCase().startsWith(getScr("GModify","cmdwhen")))
            {
                str=str.substring(5).trim();
                use=onfields;
            }
            while(str.trim().length()>0)
            {
                int eq=-1;
                int divLen=0;
                Integer code=new Integer(0);
                while((divLen==0)&&((++eq)<str.length()))
                    switch(str.charAt(eq))
                    {
                    case '!':
                        if((eq<(str.length()-1))&&(str.charAt(eq+1)=='='))
                        {
                            divLen=2;
                            break;
                        }
                        break;
                    case '=':
                    case '$':
                        divLen=1;
                        break;
                    case '<':
                    case '>':
                        divLen=1;
                        if((eq<(str.length()-1))&&(str.charAt(eq+1)=='='))
                        {
                            divLen=2;
                            break;
                        }
                        break;
                    }
                if(divLen==0)
                {
                    mob.tell(getScr("GModify","nodivid",str));
                    return false;
                }
                String equator=str.substring(eq,eq+divLen);
                String val=str.substring(eq+divLen);
                String key=str.substring(0,eq).trim();
                
                int divBackLen=0;
                eq=-1;
                while((divBackLen==0)&&((++eq)<val.length()))
                    switch(val.charAt(eq))
                    {
                    case '&':
                        if((eq<(val.length()-1))&&(val.charAt(eq+1)=='&'))
                        {
                            divBackLen=2;
                            break;
                        }
                        break;
                    case '|':
                        if((eq<(val.length()-1))&&(val.charAt(eq+1)=='&'))
                        {
                            divBackLen=2;
                            break;
                        }
                        break;
                    }
                if(divBackLen==0)
                    str="";
                else
                {
                    String attach=val.substring(eq,eq+divBackLen).trim();
                    if(attach.equals("&&"))
                        code=new Integer(code.intValue()|FLAG_AND);
                    else
                    if(attach.equals("||"))
                        code=new Integer(code.intValue()|FLAG_OR);
                    str=val.substring(eq+divBackLen);
                    val=val.substring(0,eq);
                }
                Pattern P=null;
                if(use==null)
                {
                    mob.tell("'"+((String)commands.elementAt(i))+getScr("GModify","unknownparm"));
                    return false;
                }
                while(val.trim().startsWith("["))
                {
                    int x2=val.indexOf("]");
                    if(x2<0) break;
                    String cd=val.trim().substring(1,x2);
                    if(cd.length()!=2)
                        break;
                    if(cd.equalsIgnoreCase("ss"))
                        code=new Integer(code.intValue()|FLAG_SUBSTRING);
                    if(cd.equalsIgnoreCase("cs"))
                        code=new Integer(code.intValue()|FLAG_CASESENSITIVE);
                    val=val.substring(x2+1);
                }
                if(equator.equals("$"))
                {
                    int patCodes=Pattern.DOTALL;
                    if(!CMath.bset(code.intValue(),FLAG_CASESENSITIVE))
                        patCodes=patCodes|Pattern.CASE_INSENSITIVE;
                    P=Pattern.compile(val,patCodes);
                }
                key=key.toUpperCase().trim();
                if(allKnownFields.contains(key))
                    use.addElement(key,equator,val,code,P);
                else
                {
                    mob.tell("'"+key+getScr("GModify","nofield")+allFieldsMsg.toString());
                    return false;
                }
            }
        }
        if((onfields.size()==0)&&(changes.size()==0))
        {
            mob.tell(getScr("GModify","nowhen"));
            return false;
        }
        if(placesToDo.size()==0)
        for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
        {
            Area A=(Area)a.nextElement();
            if(A.getCompleteMap().hasMoreElements()
            &&CMSecurity.isAllowed(mob,((Room)A.getCompleteMap().nextElement()),"GMODIFY"))
                placesToDo.addElement(A);
        }
        if(placesToDo.size()==0)
        {
            mob.tell(getScr("GModify","norooms"));
            return false;
        }
        for(int i=placesToDo.size()-1;i>=0;i--)
        {
            if(placesToDo.elementAt(i) instanceof Area)
            {
                Area A=(Area)placesToDo.elementAt(i);
                placesToDo.removeElement(A);
                for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
                {
                    Room R=(Room)r.nextElement();
                    if(CMSecurity.isAllowed(mob,R,"GMODIFY"))
                        placesToDo.addElement(R);
                }
            }
            else
            if(placesToDo.elementAt(i) instanceof Room)
                if(mob.session()!=null) mob.session().rawPrint(".");
            else
                return false;
        }
        // now do the modification...
        if(mob.session()!=null)
        {
            if(changes.size()==0)
                mob.session().rawPrintln(getScr("GModify","search"));
            else
                mob.session().rawPrint(getScr("GModify","modify"));
        }
        if(noisy) gmodifydebugtell(mob,getScr("GModify","roomstodo")+placesToDo.size());
        if(noisy) gmodifydebugtell(mob,getScr("GModify","whenfields")+CMParms.toStringList(onfields.getDimensionVector(1)));
        if(noisy) gmodifydebugtell(mob,getScr("GModify","changefields")+CMParms.toStringList(changes.getDimensionVector(1)));
        Log.sysOut("GModify",mob.Name()+" "+whole+".");
        for(int r=0;r<placesToDo.size();r++)
        {
            Room R=(Room)placesToDo.elementAt(r);
            if(!CMSecurity.isAllowed(mob,R,"GMODIFY"))
                continue;
            if((R==null)||(R.roomID()==null)||(R.roomID().length()==0)) continue;
	    	synchronized(("SYNC"+R.roomID()).intern())
	    	{
	    		R=CMLib.map().getRoom(R);
	            if((mob.session()==null)||(mob.session().killFlag()))
	            	return false;
	            boolean oldMobility=R.getArea().getMobility();
	            if(changes.size()==0)
	            {
	                R=CMLib.coffeeMaker().makeNewRoomContent(R);
		            if(R!=null) R.getArea().toggleMobility(false);
	            }
	            else
	            {
		            R.getArea().toggleMobility(false);
	                CMLib.map().resetRoom(R);
	            }
	            if(R==null) continue;
	            boolean savemobs=false;
	            boolean saveitems=false;
	            for(int i=0;i<R.numItems();i++)
	            {
	                Item I=R.fetchItem(i);
	                if((I!=null)&&(tryModfy(mob,R,I,changes,onfields,noisy)))
	                    saveitems=true;
	            }
	            for(int m=0;m<R.numInhabitants();m++)
	            {
	                MOB M=R.fetchInhabitant(m);
	                if((M!=null)&&(M.savable()))
	                    if(tryModfy(mob,R,M,changes,onfields,noisy))
	                        savemobs=true;
	                for(int i=0;i<M.inventorySize();i++)
	                {
	                    Item I=M.fetchInventory(i);
	                    if((I!=null)&&(tryModfy(mob,R,I,changes,onfields,noisy)))
	                        savemobs=true;
	                }
	                if(CMLib.coffeeShops().getShopKeeper(M)!=null)
	                {
	                    Vector V=CMLib.coffeeShops().getShopKeeper(M).getShop().getStoreInventory();
	                    for(int i=0;i<V.size();i++)
	                    {
	                        if(V.elementAt(i) instanceof Item)
	                        {
	                            Item I=(Item)V.elementAt(i);
	                            if((I!=null)&&(tryModfy(mob,R,I,changes,onfields,noisy)))
	                                savemobs=true;
	                        }
	                    }
	                }
	            }
	            if(saveitems) CMLib.database().DBUpdateItems(R);
	            if(savemobs) CMLib.database().DBUpdateMOBs(R);
	            if((mob.session()!=null)&&(changes.size()>0)) 
	                mob.session().rawPrint(".");
	            R.getArea().toggleMobility(oldMobility);
	            if(changes.size()==0) R.destroy();
	    	}
        }

        if(mob.session()!=null) mob.session().rawPrintln(getScr("GModify","done"));
        for(int i=0;i<placesToDo.size();i++)
            ((Room)placesToDo.elementAt(i)).getArea().toggleMobility(true);
        return false;
    }
    
    public boolean canBeOrdered(){return true;}
    public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"GMODIFY");}

    
}
