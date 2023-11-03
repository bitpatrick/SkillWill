package com.sinnerschrader.skillwill.domain.user;

import com.sinnerschrader.skillwill.dto.UserLdapDetailsDto;
import lombok.*;

/**
 * Data Structure for Details from LDAP.
 *
 * @author torree
 */
@AllArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@NoArgsConstructor
public class UserLdapDetails {

  private String firstName;
  private String lastName;
  private String mail;
  private String phone;
  private String location;
  private String title;
  private String company;
  private Role role;
  
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
