/* ==================================================================
 * NodeUsageTiersTests.java - 22/07/2020 2:50:25 PM
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.central.user.billing.snf.domain.test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsageCost;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsageTier;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsageTiers;

/**
 * Test cases for the {@link NodeUsageTiers} class.
 * 
 * @author matt
 * @version 1.0
 */
public class NodeUsageTiersTests {

	@Test
	public void asString() {
		// GIVEN
		// @formatter:off
		NodeUsageTiers tiers = new NodeUsageTiers(asList(
				new NodeUsageTier(0, new NodeUsageCost("0.000009", "0.000002", "0.0000004")),
				new NodeUsageTier(50000, new NodeUsageCost("0.000006", "0.000001", "0.0000002")),
				new NodeUsageTier(400000, new NodeUsageCost("0.000004", "0.0000005", "0.00000005")),
				new NodeUsageTier(1000000, new NodeUsageCost("0.000002", "0.0000002", "0.000000006"))
				));

		// WHEN
		String s = tiers.toString();

		// THEN
		assertThat("String output", s,
				  equalTo("|  Quantity | PropIn       | DatumOut     | DatumStored  |\n"
						+ "|-----------|--------------|--------------|--------------|\n"
						+ "|         0 | 0.0000090000 | 0.0000020000 | 0.0000004000 |\n"
						+ "|    50,000 | 0.0000060000 | 0.0000010000 | 0.0000002000 |\n"
						+ "|   400,000 | 0.0000040000 | 0.0000005000 | 0.0000000500 |\n"
						+ "| 1,000,000 | 0.0000020000 | 0.0000002000 | 0.0000000060 |"));
		// @formatter:on
	}

}
