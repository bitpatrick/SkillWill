import React from 'react'
import BasicProfile from '../profile/basic-profile'
import SkillSearch from './skill-search.jsx'
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
import { SkillLegendItem, SkillLegend } from '../skill-legend/skill-legend'
import SkillItem from '../skill-item/skill-item'

class SkillCreate extends React.Component {
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
			skillsList: [],
			skillName: '',
			skillDescription: '',
			skillHidden: false,
		}
		this.toggleSkillsSearch = this.toggleSkillsSearch.bind(this)
		this.toggleSkillsEdit = this.toggleSkillsEdit.bind(this)
		this.editSkill = this.editSkill.bind(this)
		this.deleteSkill = this.deleteSkill.bind(this)
		this.handleSkillName = this.handleSkillName.bind(this)
		this.handleSkillDescription = this.handleSkillDescription.bind(this)
		this.saveSkill = this.saveSkill.bind(this)
	}

	async componentWillMount() {
		document.body.classList.add('my-profile-open')
		await this.props.fetchCurrentUser();
		this.getSkills();
	}

	async getSkills(){
		const options = {
            credentials: 'include',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
        };
		let params = 'exclude_hidden=false'
		if(this.state.skillName.length>0) params+='&search='+encodeURIComponent(this.state.skillName)
		const requestURL = `${apiServer}/skills?`+params
	
        this.props.startLoading();
		await fetch(requestURL, options)
		.then(response => response.json())
		.then(res => {
				res.sort((a, b) => {
					return a.name.toString().toUpperCase() <
						b.name.toString().toUpperCase()
						? -1
						: 1
				})
				this.setState({skillsList: res});
			})
			.catch(err =>{
                console.log(err.message)
                this.props.errorAlertManage(err.message);
            })
		this.props.stopLoading();
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
		)}`
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

    handleSkillName(e){
        e.preventDefault()
        this.setState({skillName: e.target.value},()=>{
			this.getSkills()
		});
    }

	handleSkillDescription(e){
        e.preventDefault()
        this.setState({skillDescription: e.target.value});
	}

	saveSkill(){
		console.log(this.state)
	}

	render() {
		const {
			skillSearchOpen,
			openLayerAt,
			shouldShowAllSkills,
			skillEditOpen,
			userId,
			skillsList,
			skillHidden
		} = this.state
		const {
			currentUser: {
				loaded
			}
		}= this.props
		return (
			<Layer>
				<div className="profile">

					<div className="input-container">
						<input type="text" onChange={this.handleSkillName} 
						value={this.state.skillName} id="skillname"
						className="" placeholder="Skill Name"/>
					</div>
                            
					{
					skillsList.length>0 ? 
					<div className="skill-editor">
						<div className="skill-listing">
							{skillsList.length > 0 && (
								<div className="listing-header">
									<SkillLegend>
										<SkillLegendItem title="Name" wide />
										<div className="skill-legend__item--skills">
											<SkillLegendItem title="Skill level" withTooltip="skill" />
											<SkillLegendItem title="Will level" withTooltip="will" />
										</div>
									</SkillLegend>
								</div>
							)}
							<ul className="skills-list">
								{skillsList.map((skill, i) => {
									return (
										<SkillItem
											skill={skill}
											handleEdit={this.editSkill}
											handleDelete={this.deleteSkill}
											key={i}
											hasZeroLevel={true}
										/>
									)
								})}
							</ul>
						</div>
					</div> :
					<div className="center">
						<br/>
						<div className="input-container">
							<input type="text" onChange={this.handleSkillDescription} 
							value={this.state.skillDescription} id="skilldescription"
							className="" placeholder="Skill Description"/>
						</div>
						<br/><br/>
						<div className='radio-container'>
							<div>
								<input
									type="radio"
									name="hidden"
									value={true}
									checked={skillHidden}
									onClick={e => this.setState({
										skillHidden: true
									})}
								/>
								<span>Hidden</span>
							</div>
							<div>
								<input
									type="radio"
									name="hidden"
									value={false}
									checked={!skillHidden}
									onClick={e => this.setState({
										skillHidden: false
									})}
								/>
								<span>Not hidden</span>
							</div>
						</div>
						<br/>
						<button className="btn" onClick={this.saveSkill}>Save Skill</button>
					</div>
					}
				</div>
			</Layer>)
	}
}
function mapStateToProps(state) {
	return {
		currentUser: state.currentUser,
		lastSortedBy: state.lastSortedBy,
		redirLogin: state.redirLogin,
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
})(SkillCreate)
