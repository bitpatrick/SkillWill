// eslint-disable-next-line no-unused-vars
import React from 'react'
import IconSymbols from './components/icon/icon-symbols.jsx'
import Header from './components/header/header.jsx'
import Logo from './components/logo/logo.jsx'
import Footer from './components/footer/footer.jsx'
import UserSearch from './components/search/user-search.jsx'
import Results from './components/results/results.jsx'
import { connect } from 'react-redux'
import { fetchCurrentUser } from './actions/index.js'
import Spinner from './components/common/spinner.js'
import ErrorAlert from './components/common/error-alert.js'

class App extends React.Component {

	constructor(props){
		super(props)

		console.log(this.props)
		// this.checkUser = this.checkUser.bind(this);
		// this.checkUser();

	}

	// async checkUser(){
	// 	await this.props.fetchCurrentUser();
	// 	if(!this.props.currentUser.loaded){
	// 		this.props.history.push('/login')
	// 	}
	// }

	render() {
		const { isResultsLoaded, isSkillAnimated, isLoading, errorAlertAction } = this.props

		return (
			<div>
				{ isLoading.loading ? 
					<div className='spinner-div'>
						<Spinner/>
					</div> 
				: null }
                { errorAlertAction.visible ? 
                    <ErrorAlert message={errorAlertAction.message}/>
                : null }
				<div className={isResultsLoaded ? 'results-loaded' : ''}>
					<IconSymbols />
					<Header />
					<div className="search">
						<Logo/>
						<div className="container">
							<UserSearch location={this.props.location} />
						</div>
					</div>
					<div className="content">
						<Results animated={isSkillAnimated} />
						{this.props.children}
					</div>
					<Footer />
					<div className="layer-overlay" />
				</div>
			</div>
		)
	}
	
}

function mapStateToProps(state) {
	return {
		isResultsLoaded: state.isResultsLoaded,
		isSkillAnimated: state.isSkillAnimated,
		currentUser: state.currentUser,
		isLoading: state.isLoading,
        errorAlertAction: state.errorAlertAction
	}
}
export default connect(mapStateToProps,{
	// fetchCurrentUser
})(App)
