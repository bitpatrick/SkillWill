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
	redirectLogin,
	updateSkill,
	createSkill,
	deleteSkill
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
			editMode: false,
			selectedSkill: null,
			deleteConfirm: false,
		}
		this.toggleSkillsSearch = this.toggleSkillsSearch.bind(this)
		this.toggleSkillsEdit = this.toggleSkillsEdit.bind(this)
		this.editSkill = this.editSkill.bind(this)
		this.deleteSkill = this.deleteSkill.bind(this)
		this.handleSkillName = this.handleSkillName.bind(this)
		this.handleSkillDescription = this.handleSkillDescription.bind(this)
		this.saveSkill = this.saveSkill.bind(this)
		this.resetSkill = this.resetSkill.bind(this)
		this.handleDeleteSkill = this.handleDeleteSkill.bind(this)
	}

	async componentWillMount() {
		document.body.classList.add('my-profile-open')
		await this.props.fetchCurrentUser();
		if(!this.props.currentUser ||
		!this.props.currentUser.loaded ||
		!this.props.currentUser.authorities.some(x=>x=='ADMIN')){
			this.props.history.push('/')
		}
		else this.getSkills();
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

	editSkill(skill) {
		console.log(skill)
		this.setState({
			editMode: true,
			skillName: skill.name,
			skillDescription: skill.description,
			skillHidden: skill.hidden,
			selectedSkill: skill,
		});
	}

	handleDeleteSkill(skill){
		this.setState({
			deleteConfirm: true,
			selectedSkill: skill,
			skillName: skill.name,
		});
	}

	async deleteSkill() {
		const options = {
			method: 'DELETE',
			credentials: 'include',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
		};
		await this.props.deleteSkill(options, this.state.selectedSkill.nameStem);
		this.resetSkill();
	}

	resetSkill(){
		this.setState({
			editMode: false,
			skillName: '',
			skillDescription: '',
			skillHidden: false,
			selectedSkill: null,
			deleteConfirm: false,
		},()=>this.getSkills());
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

	async saveSkill(){
		let skill=this.state.editMode ? this.state.selectedSkill : {subSkillNames: []};
        var formBody = [];
        let details={name: this.state.skillName, description: this.state.skillDescription, 
			hidden: this.state.skillHidden, subSkills: skill.subSkillNames};
        for (var property in details) {
          var encodedKey = encodeURIComponent(property);
          var encodedValue = encodeURIComponent(details[property]);
          formBody.push(encodedKey + "=" + encodedValue);
        }
        formBody = formBody.join("&");
		console.log(details,formBody)
		const options = {
			method: this.state.editMode ? 'PUT' : 'POST',
			body: formBody,
			credentials: 'include',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
		}
		this.state.editMode ?
			await this.props.updateSkill(options,skill.nameStem) :
			await this.props.createSkill(options,skill.nameStem) 
		this.resetSkill();
	}

	render() {
		const {
			skillSearchOpen,
			openLayerAt,
			shouldShowAllSkills,
			skillEditOpen,
			userId,
			skillName,
			skillsList,
			skillHidden,
			editMode,
			deleteConfirm
		} = this.state
		const {
			currentUser: {
				loaded
			}
		}= this.props
		return (
			<Layer>
				<div className="profile">

					{
						!deleteConfirm ?
						<div>
							{
								editMode ? 
								'Modifica la skill selezionata:' : 
								'Cerca una skill esistente oppure creane una nuova...'
							}
							<br/>
							<br/>
							<div className="input-container">
								<input type="text" onChange={this.handleSkillName} 
								value={this.state.skillName} id="skillname"
								className="skill-input" placeholder="Nome Skill"/>
							</div>
									
							{
							skillsList.length>0 && !editMode ? 
							<div className="skill-editor">
								<div className="skill-listing">
									{skillsList.length > 0 && (
										<div className="listing-header">
											<SkillLegend>
												<SkillLegendItem title="Nome" wide />
												<div className="skill-legend__item--skills">
													<SkillLegendItem title="Livello Skill" withTooltip="skill" />
													<SkillLegendItem title="Livello Will" withTooltip="will" />
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
													handleDelete={this.handleDeleteSkill}
													key={i}
													isCreateSkillPage={true}
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
									className="skill-input" placeholder="Descrizione Skill"/>
								</div>
								<br/><br/>
								Scegliere se la skill sarà visibile nella lista della ricerca:
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
										<span>Nascosta</span>
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
										<span>Visibile</span>
									</div>
								</div>
								<br/>
								<button className="btn" onClick={this.saveSkill}>
									{editMode ? 'Modifica' : 'Crea'} Skill
								</button>
								<button className="btn btn-annulla" onClick={this.resetSkill}>
									Annulla
								</button>
							</div>
							}
						</div> :
							
						<div className='center confirm-container'>
							Confermi di voler eliminare la skill {skillName}?
							<br/>
							<br/>
							<button className="btn" onClick={this.deleteSkill}>
								Conferma
							</button>
							<button className="btn btn-annulla" onClick={this.resetSkill}>
								Annulla
							</button>
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
	redirectLogin,
	updateSkill,
	createSkill,
	deleteSkill,
})(SkillCreate)
