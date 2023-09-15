import React from "react";
import { connect } from "react-redux";
import IconSymbols from "../icon/icon-symbols";
import Header from "../header/header";
import Logo from "../logo/logo";
import Footer from "../footer/footer";
import './login.less'
import { apiServer } from "../../env";
import { fetchCurrentUser, startLoading, stopLoading, errorAlertManage } from "../../actions";
import Spinner from "../common/spinner";
import ErrorAlert from "../common/error-alert";


class Login extends React.Component{
    constructor(props){
        super(props)

        this.state = {
            username: '',
            password: ''
        };

        this.handleUsername = this.handleUsername.bind(this);
        this.handlePassword = this.handlePassword.bind(this);
        this.login = this.login.bind(this);

        if(this.props.currentUser.loaded){
            // add logout api
        }

        console.log(this.props)
    }

    handleUsername(e){
        e.preventDefault()
        this.setState({username: e.target.value});
    }

    handlePassword(e){
        e.preventDefault()
        this.setState({password: e.target.value});
    }

    async login(e){
        e.preventDefault();
        var formBody = [];
        let details={username: this.state.username, password: this.state.password};
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
		const requestURL = `${apiServer}/login`
        this.props.startLoading();
		await fetch(requestURL, options)
			.then(res => {
				if (res.status === 403) {
                    alert('session invalid') // eslint-disable-line
					this.setState({
						editLayerOpen: false,
					})
					this.props.fetchCurrentUser()
				}

				if (res.status !== 200) {
					throw Error('error while logging')
				} else {
					this.props.fetchCurrentUser()
                    this.props.history.push('/my-profile')
                }
			})
			.catch(err =>{
                console.log(err.message)
                this.props.errorAlertManage(err.message);
            })
        this.props.stopLoading();
	}

    render(){
		const { isLoading, errorAlertAction } = this.props
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
                <IconSymbols />
                <Header login/>
                <div className="search">
                    <Logo/>
                    <div className="container">
                        <div className="searchbar login">
                            {/* <p className="item1">Username</p>
                            <div className="input-container">
                                <input type="text" onChange={this.handleUsername} 
                                value={this.state.username} id="username"
                                className="" placeholder="Username"/>
                            </div>
                            <p>Password</p>
                            <div className="input-container">
                                <input type="password" onChange={this.handlePassword} 
                                value={this.state.password} id="password"
                                placeholder="Password"/>
                            </div> */}
                            
                            <div className="input-container">
                                <input type="text" onChange={this.handleUsername} 
                                value={this.state.username} id="username"
                                className="" placeholder="Username"/>
                            </div>
                            
                            <div className="input-container">
                                <input type="password" onChange={this.handlePassword} 
                                value={this.state.password} id="password"
                                placeholder="Password"/>
                            </div>
                        </div>
                        <div className="center">
                            <button className="btn" onClick={this.login}>Login</button>
                        </div>
                    </div>
                </div>
                <Footer />
                <div className="layer-overlay" />
            </div>
        )
    }
}

function mapStateToProps(state) {
	return {
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
})(Login)