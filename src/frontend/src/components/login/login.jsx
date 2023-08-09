import React from "react";
import { connect } from "react-redux";
import IconSymbols from "../icon/icon-symbols";
import Header from "../header/header";
import Logo from "../logo/logo";
import Footer from "../footer/footer";
import './login.less'
import { apiServer } from "../../env";
import { fetchCurrentUser } from "../../actions";


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

    login(e){
        e.preventDefault();
		const options = { method: 'POST', credentials: 'same-origin',
        body: JSON.stringify({username: this.state.username, password: this.state.password}) }
		const requestURL = `${apiServer}/login`
		fetch(requestURL, options)
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
                    this.props.history.push('/')
                }
			})
			.catch(err => console.log(err.message))
	}

    render(){
        return (
            <div>
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
	}
}
export default connect(mapStateToProps,{
    fetchCurrentUser
})(Login)