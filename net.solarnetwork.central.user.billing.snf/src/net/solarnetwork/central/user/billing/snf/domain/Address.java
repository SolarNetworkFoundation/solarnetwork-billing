/* ==================================================================
 * Address.java - 20/07/2020 11:14:28 AM
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

package net.solarnetwork.central.user.billing.snf.domain;

import java.time.Instant;
import net.solarnetwork.dao.BasicLongEntity;

/**
 * An address for billing.
 * 
 * @author matt
 * @version 1.0
 */
public class Address extends BasicLongEntity {

	private String name;
	private String email;
	private String country;
	private String timeZoneId;
	private String region;
	private String stateOrProvince;
	private String locality;
	private String postalCode;
	private String street[];

	/**
	 * Default constructor.
	 */
	public Address() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the creation date
	 */
	public Address(Long id, Instant created) {
		super(id, created);
	}

	/**
	 * Get the display name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the display name.
	 * 
	 * @param name
	 *        the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the email.
	 * 
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set the email.
	 * 
	 * @param email
	 *        the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Get the country.
	 * 
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Set the country.
	 * 
	 * @param country
	 *        the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * Get the time zone ID.
	 * 
	 * @return the timeZoneId
	 */
	public String getTimeZoneId() {
		return timeZoneId;
	}

	/**
	 * Set the time zone ID.
	 * 
	 * @param timeZoneId
	 *        the timeZoneId to set
	 */
	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

	/**
	 * Get the region.
	 * 
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * Set the region.
	 * 
	 * @param region
	 *        the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * Set the state/province.
	 * 
	 * @return the stateOrProvince
	 */
	public String getStateOrProvince() {
		return stateOrProvince;
	}

	/**
	 * Get the state/province.
	 * 
	 * @param stateOrProvince
	 *        the stateOrProvince to set
	 */
	public void setStateOrProvince(String stateOrProvince) {
		this.stateOrProvince = stateOrProvince;
	}

	/**
	 * Get the locality (city).
	 * 
	 * @return the locality
	 */
	public String getLocality() {
		return locality;
	}

	/**
	 * Set the locality (city).
	 * 
	 * @param locality
	 *        the locality to set
	 */
	public void setLocality(String locality) {
		this.locality = locality;
	}

	/**
	 * Get the postal code.
	 * 
	 * @return the postalCode
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * Set the postal code.
	 * 
	 * @param postalCode
	 *        the postalCode to set
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * Get the street list.
	 * 
	 * @return the street
	 */
	public String[] getStreet() {
		return street;
	}

	/**
	 * Set the street list.
	 * 
	 * @param street
	 *        the street to set
	 */
	public void setStreet(String[] street) {
		this.street = street;
	}

}
