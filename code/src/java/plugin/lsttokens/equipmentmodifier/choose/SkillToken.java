/*
 * Copyright 2007 (C) Thomas Parker <thpr@users.sourceforge.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package plugin.lsttokens.equipmentmodifier.choose;

import pcgen.cdom.base.Constants;
import pcgen.cdom.enumeration.StringKey;
import pcgen.core.EquipmentModifier;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.CDOMSecondaryToken;
import pcgen.rules.persistence.token.ParseResult;

public class SkillToken implements CDOMSecondaryToken<EquipmentModifier>
{

	@Override
	public String getTokenName()
	{
		return "SKILL";
	}

	@Override
	public String getParentToken()
	{
		return "CHOOSE";
	}

	@Override
	public ParseResult parseToken(LoadContext context, EquipmentModifier obj,
		String value)
	{
		if (value == null)
		{
			// Very strange, but legal
			value = Constants.LST_ANY;
		}
		if (value.indexOf('[') != -1)
		{
			return new ParseResult.Fail("CHOOSE:" + getTokenName()
					+ " arguments may not contain [] : " + value, context);
		}
		if (value.charAt(0) == '|')
		{
			return new ParseResult.Fail("CHOOSE:" + getTokenName()
					+ " arguments may not start with | : " + value, context);
		}
		if (value.charAt(value.length() - 1) == '|')
		{
			return new ParseResult.Fail("CHOOSE:" + getTokenName()
					+ " arguments may not end with | : " + value, context);
		}
		if (value.indexOf("||") != -1)
		{
			return new ParseResult.Fail("CHOOSE:" + getTokenName()
					+ " arguments uses double separator || : " + value, context);
		}
		// StringTokenizer tok = new StringTokenizer(value, Constants.PIPE);
		// while (tok.hasMoreTokens())
		// {
		// String tokString = tok.nextToken();
		// if (tokString.startsWith("TITLE="))
		// {
		// //OK
		// }
		// else
		// {
		// //Assume it's a primitive skill??
		// }
		// }
		StringBuilder sb = new StringBuilder(value.length() + 20);
		sb.append(getTokenName()).append('|').append(value);
		context.getObjectContext().put(obj, StringKey.CHOICE_STRING, sb.toString());
		return ParseResult.SUCCESS;
	}

	@Override
	public String[] unparse(LoadContext context, EquipmentModifier eqMod)
	{
		String chooseString = context.getObjectContext().getString(eqMod,
				StringKey.CHOICE_STRING);
		if (chooseString == null
				|| chooseString.indexOf(getTokenName() + '|') == -1)
		{
			return null;
		}
		return new String[] { chooseString
				.substring(getTokenName().length() + 1) };
	}

	@Override
	public Class<EquipmentModifier> getTokenClass()
	{
		return EquipmentModifier.class;
	}
}
