// eslint-disable-next-line no-unused-vars
import React from 'react'
import IconSymbols from './components/icon/icon-symbols.jsx'
import Header from './components/header/header.jsx'
import Logo from './components/logo/logo.jsx'
import Footer from './components/footer/footer.jsx'
import UserSearch from './components/search/user-search.jsx'
import Results from './components/results/results.jsx'
import { connect } from 'react-redux'
import { fetchCurrentUser,
    startLoading,
    stopLoading,
    errorAlertManage,
	isResultsLoaded } from './actions/index.js'
import Spinner from './components/common/spinner.js'
import ErrorAlert from './components/common/error-alert.js'
import { apiServer } from "./env.js";

class App extends React.Component {

	constructor(props){
		super(props)

		console.log(this.props)
		this.checkUser = this.checkUser.bind(this);
		this.logout = this.logout.bind(this);
		this.refreshReloaded = this.refreshReloaded.bind(this);
	}

	refreshReloaded(){
		// this.props.isResultsLoaded(false);
	}

	componentDidMount(){
		// this.checkUser();
	}

	async checkUser(){
		await this.props.fetchCurrentUser();
	}

	async logout(e){
        e.preventDefault();
        var formBody = [];
        let details={username: this.props.currentUser.username, password: this.props.currentUser.password};
        for (var property in details) {
          var encodedKey = encodeURIComponent(property);
          var encodedValue = encodeURIComponent(details[property]);
          formBody.push(encodedKey + "=" + encodedValue);
        }
        formBody = formBody.join("&");
		const options = { 
            method: 'POST', 
            credentials: 'include',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: formBody
        };
		const requestURL = `${apiServer}/logout`
		//add logout api
        this.props.startLoading();
		await fetch(requestURL, options)
			.then( async res => {
				await this.props.fetchCurrentUser(true)
				this.props.history.push('/')
			})
			.catch(err =>{
                console.log(err.message)
                this.props.errorAlertManage(err.message);
            })
        this.props.stopLoading();
	}

	render() {
		const { isResultsLoaded, isSkillAnimated, isLoading, errorAlertAction,
		currentUser } = this.props

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
					<Header location={this.props.location} logout={this.logout}
					refreshReloaded={this.refreshReloaded}/>
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
	fetchCurrentUser,
    startLoading,
    stopLoading,
    errorAlertManage
})(App)
