import React from 'react'
import BasicProfile from './basic-profile.jsx'
import SkillSearch from '../search/skill-search.jsx'
import Icon from '../icon/icon.jsx'
import Layer from "../layer/layer"
import { apiServer } from '../../env.js'
import {
	toggleSkillsEditMode,
	exitSkillsEditMode,
	editSkill,
	setLastSortedBy,
	updateUserSkills,
	fetchCurrentUser,
	startLoading,
	stopLoading,
	errorAlertManage,
	redirectLogin
} from '../../actions'
import { connect } from 'react-redux'

class MyProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			data: null,
			dataLoaded: false,
			editLayerOpen: false,
			openLayerAt: -1,
			shouldShowAllSkills: true,
			skillSearchOpen: false,
			skillEditOpen: false,
		}
		this.toggleSkillsSearch = this.toggleSkillsSearch.bind(this)
		this.toggleSkillsEdit = this.toggleSkillsEdit.bind(this)
		this.editSkill = this.editSkill.bind(this)
		this.deleteSkill = this.deleteSkill.bind(this)
	}

	async componentWillMount() {
		document.body.classList.add('my-profile-open')
		await this.props.fetchCurrentUser();
		if(this.props.redirLogin){
			this.props.history.push('/login')
		}
	}

	componentWillUnmount() {
		this.props.exitSkillsEditMode()
		document.body.classList.remove('my-profile-open')
	}

	toggleSkillsSearch() {
		this.props.fetchCurrentUser()
		this.setState({
			skillSearchOpen: !this.state.skillSearchOpen,
		})
	}

	toggleSkillsEdit() {
		this.props.fetchCurrentUser()
		this.props.toggleSkillsEditMode()
		this.setState({
			skillEditOpen: !this.state.skillEditOpen,
		})
		document.body.classList.toggle('is-edit-mode')
	}

	getCurrentUserId() {
		const { currentUser } = this.props
		return currentUser.id
	}

	editSkill(skill, skillLevel, willLevel, isMentor = false) {
		if (skillLevel === '0' && willLevel === '0') {
      alert('Please select a value greater than 0') // eslint-disable-line
			return
		}
        var formBody = [];
        let details={skill: skill, skill_level: skillLevel, will_level: willLevel, mentor: isMentor};
        for (var property in details) {
          var encodedKey = encodeURIComponent(property);
          var encodedValue = encodeURIComponent(details[property]);
          formBody.push(encodedKey + "=" + encodedValue);
        }
        formBody = formBody.join("&");
		const options = {
			method: 'PATCH',
			body: formBody,
			credentials: 'include',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
		}
		this.props.updateUserSkills(options, this.getCurrentUserId())
	}

	async deleteSkill(skill) {
		const options = { method: 'DELETE', credentials: 'include' }
		const requestURL = `${apiServer}/users/${this.getCurrentUserId()}/skills?skill=${encodeURIComponent(
			skill
		).toUpperCase()}`
		this.props.startLoading();
		await fetch(requestURL, options)
			.then(async res => {
				if (res.status === 403) {
          			alert('session invalid') // eslint-disable-line
					this.setState({
						editLayerOpen: false,
					})
					await this.props.fetchCurrentUser()
				}

				if (res.status !== 200) {
					throw Error('error while deleting skills')
				} else {
					await this.props.fetchCurrentUser()
				}
			})
			.catch(err => {
				console.log(err)
                this.props.errorAlertManage(err.message);
			})
		this.props.stopLoading();
	}

	render() {
		const {
			skillSearchOpen,
			openLayerAt,
			shouldShowAllSkills,
			skillEditOpen,
			userId,
		} = this.state
		const {
			currentUser: {
				loaded
			}
		}= this.props
		return (
			<Layer>
				{ loaded ? (
					skillSearchOpen ? (
						<div className="profile">
							<SkillSearch
								handleEdit={this.editSkill}
								handleDelete={this.deleteSkill}
								userId={userId}
							/>
							<div className="profile-actions" data-skillsearch={skillSearchOpen}>
								<button
									className="edit-skill-btn"
									onClick={this.toggleSkillsEdit}
									disabled={skillSearchOpen}>
									<Icon name="edit" size={19} />
									Modifica skill
								</button>
								<button className="add-skill-btn" onClick={this.toggleSkillsSearch}>
									<Icon name="checkmark" size={19} />
									Done
								</button>
							</div>
						</div>
					) : (
						<div className="profile">
							<BasicProfile
								openLayerAt={openLayerAt}
								shouldShowAllSkills={shouldShowAllSkills}
								editSkill={this.editSkill}
								deleteSkill={this.deleteSkill}
								setLastSortedBy={this.props.setLastSortedBy}
								lastSortedBy={this.props.lastSortedBy}
								getUserProfileData={this.props.getUserProfileData}
								user={this.props.currentUser}
							/>
							<div className="profile-actions" data-skilledit={skillEditOpen}>
								<button className="edit-skill-btn" onClick={this.toggleSkillsEdit}>
									{skillEditOpen ? (
										<Icon name="checkmark" size={18} />
									) : (
										<Icon name="edit" size={18} />
									)}
									{skillEditOpen ? 'Fatto' : 'Modifica skill'}
								</button>
								<button
									className="add-skill-btn"
									onClick={this.toggleSkillsSearch}
									disabled={skillEditOpen}>
									<Icon name="plus" size={18} />
									Aggiungi nuova skill
								</button>
							</div>
						</div>
					)
				) : null }
			</Layer>)
	}
}
function mapStateToProps(state) {
	return {
		currentUser: state.currentUser,
		lastSortedBy: state.lastSortedBy,
		redirLogin: state.redirLogin
	}
}
export default connect(mapStateToProps, {
	toggleSkillsEditMode,
	exitSkillsEditMode,
	editSkill,
	setLastSortedBy,
	updateUserSkills,
	fetchCurrentUser,
	startLoading,
	stopLoading,
	errorAlertManage,
	redirectLogin
})(MyProfile)
