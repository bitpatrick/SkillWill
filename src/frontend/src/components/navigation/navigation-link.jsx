import React from 'react'
import { Link } from 'react-router'
import './navigation.less'

const NavigationLink = props => {
	return (
		<div>
			{
				props.target=='/login' ?
				<Link
					onClick={props.logout}
					className="navigation__link"
					activeClassName={`${props.location.pathname.includes(props.target) ? 'navigation__link--active' : ''}`}
					to={props.target}>
					{props.children}
				</Link> :
				(
					props.target=='/' ?
					<Link
						onClick={props.refreshReloaded}
						className="navigation__link"
						activeClassName={`${props.location.pathname.includes(props.target) ? 'navigation__link--active' : ''}`}
						to={props.target}>
						{props.children}
					</Link> :
					<Link
						className="navigation__link"
						activeClassName={`${props.location.pathname.includes(props.target) ? 'navigation__link--active' : ''}`}
						to={props.target}>
						{props.children}
					</Link>
				)
			}
		</div>
	)
}

export default NavigationLink
