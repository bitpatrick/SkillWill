import React from 'react'
import Navigation from '../navigation/navigation'
import NavigationItem from '../navigation/navigation-item'
import NavigationLink from '../navigation/navigation-link'
import NavigationList from '../navigation/navigation-list'
import NavigationListItem from '../navigation/navigation-list-item'
import Logo from '../logo/logo'
import Icon from '../icon/icon.jsx'
import config from '../../config.json'
import { connect } from 'react-redux'

class Header extends React.Component {
	constructor(props){
		super(props)
	}

	render() {
		return (
			<header className="header">
				{!this.props.login && <Navigation>
					<NavigationItem>
						{/* <NavigationLink target={'/'} location={this.props.location}>
							<Icon name="s2-logo" width={131} height={30} />
						</NavigationLink> */}
					</NavigationItem>
					<NavigationItem>
						<NavigationLink target={'/'} location={this.props.location}
						refreshReloaded={this.props.refreshReloaded}>
							<Logo small />
						</NavigationLink>
					</NavigationItem>
					<NavigationItem>
						<NavigationList>
							{
								this.props.currentUser &&
								this.props.currentUser.loaded &&
								<NavigationListItem target={'/login'} location={this.props.location}
								logout={this.props.logout}>
									<Icon name="logout" width={20} height={25} />
								</NavigationListItem>
							}
							{/* <NavigationListItem target={config.slackChannelUrl} location={this.props.location}>
								<Icon name="slack" width={20} height={20} />
							</NavigationListItem> */}
							{
								this.props.currentUser &&
								this.props.currentUser.loaded &&
								this.props.currentUser.authorities.some(x=>x=='ADMIN') &&
								<NavigationListItem
									target={'/new-skill'} location={this.props.location}>
									<Icon name="admin" width={20} height={20} />
								</NavigationListItem>
							}
							<NavigationListItem
								target={'/my-profile'} location={this.props.location}>
								<Icon name="user" width={20} height={20} />
							</NavigationListItem>
						</NavigationList>
					</NavigationItem>
				</Navigation>}
			</header>
		)
	}

}

function mapStateToProps(state) {
	return {
		currentUser: state.currentUser,
	}
}
export default connect(mapStateToProps, {})(Header)