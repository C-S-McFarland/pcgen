/*
 * Created on Sep 2, 2005
 *
 */
package plugin.lsttokens;

import pcgen.core.Campaign;
import pcgen.core.PObject;
import pcgen.core.SettingsHandler;
import pcgen.core.TimeUnit;
import pcgen.core.prereq.Prerequisite;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.GlobalLstToken;
import pcgen.persistence.lst.prereq.PreParserFactory;
import pcgen.util.Logging;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import pcgen.core.PCSpell;

/**
 * @author djones4
 *
 */
public class SpellsLst implements GlobalLstToken
{

	/*
	 * FIXME Template's LevelToken needs adjustment before this can be converted
	 * to the new syntax, since this is level-dependent
	 */

	public String getTokenName()
	{
		return "SPELLS";
	}

	/* (non-Javadoc)
	 * @see pcgen.persistence.lst.GlobalLstToken#parse(pcgen.core.PObject, java.lang.String, int)
	 */
	public boolean parse(PObject obj, String value, int anInt)
	{
		if (!(obj instanceof Campaign))
		{
			obj.getSpellSupport().addSpells(anInt, createSpellsList(value, obj));
			return true;
		}
		return false;
	}

	/**
	 * SPELLS:<spellbook name>|[<optional parameters, pipe deliminated>]
	 * |<spell name>[,<formula for DC>]
	 * |<spell name2>[,<formula2 for DC>]
	 * |PRExxx
	 * |PRExxx
	 *
	 * CASTERLEVEL=<formula> Casterlevel of spells
	 * TIMES=<formula> Cast Times per day, -1=At Will
	 * @param sourceLine Line from the LST file without the SPELLS:
	 * @param obj The object the line is being added to. Used for error reporting.
	 * @return spells list
	 */
	private List<PCSpell> createSpellsList(final String sourceLine, PObject obj)
	{
		List<PCSpell> spellList = new ArrayList<PCSpell>();
		StringTokenizer tok = new StringTokenizer(sourceLine, "|");
		boolean isPre = false;
		
		if (tok.countTokens() > 1)
		{
			String spellBook = tok.nextToken();
			String casterLevel = null;
			String times = "1";
			TimeUnit timeUnit = SettingsHandler.getGame().getDefaultTimeUnit();
			List<String> preParseSpellList = new ArrayList<String>();
			List<Prerequisite> preList = new ArrayList<Prerequisite>();
			while (tok.hasMoreTokens())
			{
				String token = tok.nextToken();
				if (token.startsWith("CASTERLEVEL="))
				{
					if (isPre)
					{
						Logging.errorPrint("Invalid " + getTokenName() + ": " + sourceLine);
						Logging.errorPrint("  PRExxx must be at the END of the Token");
						Logging.errorPrint("Please change: " + sourceLine
							+ " in " + obj.getSourceURI());
						isPre = false;
					}
					casterLevel = token.substring(12);
				}
				else if (token.startsWith("TIMES="))
				{
					if (isPre)
					{
						Logging.errorPrint("Invalid " + getTokenName() + ": " + sourceLine);
						Logging.errorPrint("  PRExxx must be at the END of the Token");
						Logging.errorPrint("Please change: " + sourceLine
							+ " in " + obj.getSourceURI());
						isPre = false;
					}
					times = token.substring(6);
					if ("ATWILL".equals(times))
					{
						times = "-1";
					}
					else if ("-1".equals(times))
					{
						Logging.deprecationPrint("TIMES=-1 in "
							+ getTokenName() + " is deprecated. "
							+ "Assuming you meant TIMES=ATWILL. ");
						Logging.deprecationPrint("Please change: " + sourceLine
							+ " in " + obj.getSourceURI());
						times = "-1";
					}
				}
				else if (token.startsWith("TIMEUNIT="))
				{
					if (isPre)
					{
						Logging.errorPrint("Invalid " + getTokenName() + ": " + sourceLine);
						Logging.errorPrint("  PRExxx must be at the END of the Token");
						Logging.errorPrint("Please change: " + sourceLine
							+ " in " + obj.getSourceURI());
						isPre = false;
					}
					String timeUnitKey = token.substring(9);
					// Retrieve the time unit by key
					timeUnit = SettingsHandler.getGame().getTimeUnit(timeUnitKey); 
					if (timeUnit == null)
					{
						// For now we create a new one if it isn't already present
						timeUnit = new TimeUnit(timeUnitKey);
						SettingsHandler.getGame().addTimeUnit(timeUnit);
					}
				}
				else if (PreParserFactory.isPreReqString(token))
				{
					isPre = true;
					try
					{
						PreParserFactory factory =
								PreParserFactory.getInstance();
						preList.add(factory.parse(token));
					}
					catch (PersistenceLayerException ple)
					{
						Logging.errorPrint(ple.getMessage(), ple);
					}
				}
				else
				{
					if (isPre)
					{
						Logging.errorPrint("Invalid " + getTokenName() + ": " + sourceLine);
						Logging.errorPrint("  PRExxx must be at the END of the Token");
						Logging.errorPrint("Please change: " + sourceLine
							+ " in " + obj.getSourceURI());
						isPre = false;
					}
					preParseSpellList.add(token);
				}
			}
			for (int i = 0; i < preParseSpellList.size(); i++)
			{
				StringTokenizer spellTok =
						new StringTokenizer(preParseSpellList.get(i), ",");
				String name = spellTok.nextToken();
				String dcFormula = null;
				if (spellTok.hasMoreTokens())
				{
					dcFormula = spellTok.nextToken();
				}
				PCSpell spell = new PCSpell();
				spell.setName(name);
				spell.setKeyName(spell.getKeyName());
				spell.setSpellbook(spellBook);
				spell.setCasterLevelFormula(casterLevel);
				spell.setTimesPerDay(times);
				spell.setTimeUnit(timeUnit);
				spell.setDcFormula(dcFormula);
				for (Prerequisite prereq : preList)
				{
					spell.addPreReq(prereq);
				}
				spellList.add(spell);
			}
		}
		else
		{
			Logging
				.errorPrint("SPELLS: line minimally requires SPELLS:<spellbook name>|<spell name>");
			Logging.errorPrint("Please change: " + sourceLine
				+ " in " + obj.getSourceURI());
		}
		return spellList;
	}
}
