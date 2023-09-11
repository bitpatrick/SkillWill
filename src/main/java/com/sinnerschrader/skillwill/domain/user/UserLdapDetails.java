package com.sinnerschrader.skillwill.domain.user;

import com.sinnerschrader.skillwill.dto.UserLdapDetailsDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Data Structure for Details from LDAP.
 *
 * @author torree
 */
@AllArgsConstructor
@EqualsAndHashCode
@Data
@Builder

public class UserLdapDetails {

  private final String firstName;
  private final String lastName;
  private final String mail;
  private final String phone;
  private final String location;
  private final String title;
  private final String company;
  private final Role role;
  

  public static UserLdapDetails fromDto(UserLdapDetailsDto ldapDetailsDto) {
	  
	  if ( ldapDetailsDto == null ) {
		  return null;
	  }
	
	 Role convertedRole = Role.valueOf(ldapDetailsDto.role().toUpperCase());

	    return UserLdapDetails.builder()
	            .firstName(ldapDetailsDto.firstName())
	            .lastName(ldapDetailsDto.lastName())
	            .mail(ldapDetailsDto.mail())
	            .phone(ldapDetailsDto.phone())
	            .location(ldapDetailsDto.location())
	            .title(ldapDetailsDto.title())
	            .company(ldapDetailsDto.company())
	            .role(convertedRole)
	            .build();
  }
	 
}
