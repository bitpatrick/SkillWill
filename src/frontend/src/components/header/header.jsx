import React from 'react'
import Cookies from 'react-cookie'
import { Link } from 'react-router'
import Icon from '../icon/icon.jsx'

export default class Header extends React.Component {
	constructor(props) {
		super(props)
		this.state = {}
		this.checkUserIdCookie = this.checkUserIdCookie.bind(this)
		this.handleClick = this.handleClick.bind(this)
	}
	componentDidMount() {
		this.checkUserIdCookie()
	}
	checkUserIdCookie() {
		const user = Cookies.load('user')
		if (user !== this.state.userId) {
			this.setState({ userId: user })
		}
		return !!user
	}

	handleClick() {
		this.checkUserIdCookie()
	}

	renderLogOut() {
		const user = Cookies.load('user')
		if (user) {
			return (
				<li className="nav-item">
					<Link
						onClick={this.handleClick}
						className="nav-link"
						to={`/my-profile/logout`}>
						Logout
					</Link>
				</li>
			)
		}
	}

	returnMyProfileLink() {
		if (typeof this.state.userId !== 'undefined') {
			return this.state.userId
		} else {
			return 'login'
		}
	}

	render() {
		return (
			<header className="mod-navigation-container">
				<div className="mod-navigation">
					<Link className="s2-logo" to="/">
						<Icon name="s2-logo" width={131} height={30} />
					</Link>
					<Link className="sw-title" to="/">
						skill/will
					</Link>
					<nav className="nav">
						<ul className="nav-list">
							<li className="nav-item">
								<Link
									className="nav-link nav-link--profile"
									to={`/my-profile/${this.returnMyProfileLink()}`}>
									Your profile
								</Link>
							</li>
							{this.renderLogOut()}
						</ul>
					</nav>
				</div>
			</header>
		)
	}
}
