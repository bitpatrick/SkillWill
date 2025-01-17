import React from 'react'
import NavigationLink from '../navigation/navigation-link'
import './navigation.less'

const NavigationListItem = props => {
	return (
		<li className="navigation__list-item">
			<NavigationLink target={props.target} location={props.location}
			logout={props.logout}>
				{props.title ? props.title : props.children}
			</NavigationLink>
		</li>
	)
}

export default NavigationListItem
