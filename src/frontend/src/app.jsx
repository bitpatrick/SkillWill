import styles from './styles.less'
import React from 'react'
import Header from './components/header/header.jsx'
import Footer from './components/footer/footer.jsx'
import UserSearch from './components/search/user-search.jsx'
import Results from './components/results/results.jsx'

export default class App extends React.Component {
	render() {
		return (
			<div>
				<Header />
				<div class="background-layer"></div>
				<div class="content">
					<h1 class="title">SkillWill</h1>
					<h3 class="subtitle">Wir haben Talent</h3>
					<UserSearch location={this.props.location} />
					<Results />
					{this.props.children}
				</div>
				<Footer />
			</div>
		)
	}
}
