import React from 'react'
import { Link } from 'react-router'
import SkillItem from '../skill-item/skill-item.jsx'
import TopSkills from '../profile/top-wills'
import Icon from '../icon/icon.jsx'
import { SkillLegend, SkillLegendItem } from '../skill-legend/skill-legend'
import { connect } from 'react-redux'
import { clearUserData } from '../../actions'
import md5 from 'md5'
import config from '../../config.json'

class BasicProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			shouldShowAllSkills: this.props.shouldShowAllSkills,
			showMoreLabel: 'More',
			editLayerAt: null,
			numberOfSkillsToShow: 10,
			isSkillEditActive: false,
			avatarUrl: null
		}
		this.showAllSkills = this.showAllSkills.bind(this)
		this.sortSkills = this.sortSkills.bind(this)
		this.removeAnimationClass = this.removeAnimationClass.bind(this)
		this.renderSearchedSkills = this.renderSearchedSkills.bind(this)
		this.createAvatarUrl = this.createAvatarUrl.bind(this)
	}

	componentWillMount() {
		this.state.shouldShowAllSkills &&
			this.setState({ numberOfSkillsToShow: Infinity })

		this.setState({
			topWills: this.sortSkills('willLevel', 'desc'),
			sortedSkills: this.sortSkills('skillLevel', 'desc'),
			avatarUrl: this.createAvatarUrl()
		})
	}

	componentDidMount() {
		this.node.addEventListener(
			'animationend',
			this.removeAnimationClass
		)
	}

	componentWillReceiveProps() {
		this.state.shouldShowAllSkills &&
			this.setState({ numberOfSkillsToShow: Infinity })
	}

	removeAnimationClass() {
		this.node.classList.remove('animateable')
		this.node.removeEventListener(
			'animationend',
			this.removeAnimationClass
		)
	}

	createAvatarUrl() {
		const mailHash = md5(this.props.user.mail);
		return config.avatarUrl + mailHash + '?s=' + config.avatarSize;
	}

	showAllSkills(e) {
		e.preventDefault()
		const { shouldShowAllSkills, numberOfSkillsToShow } = this.state
		this.setState({
			shouldShowAllSkills: !shouldShowAllSkills,
			numberOfSkillsToShow: numberOfSkillsToShow === 10 ? Infinity : 10,
		})
		e.target.classList.toggle('open')
	}

	sortSkills(criterion, order = 'asc') {
		let skills = {}
		if (this.state.lastSortedBy === criterion) {
			skills = [...this.state.sortedSkills]
			skills.reverse()
		} else {
			skills = [...this.props.user.skills]
			skills.sort((a, b) => {
				if (order === 'asc') {
					return a[criterion].toString().toUpperCase() <
						b[criterion].toString().toUpperCase()
						? -1
						: 1
				} else {
					return a[criterion].toString().toUpperCase() <
						b[criterion].toString().toUpperCase()
						? 1
						: -1
				}
			})
			this.setState({
				lastSortedBy: criterion,
			})
		}

		return skills
	}

	sortAscending(a, b) {
		return a - b
	}
	sortDescending(a, b) {
		return b - a
	}

	renderSearchedSkills() {
		const { skills } = this.props.user
		const { searchedSkills } = this.props
		if (!searchedSkills || searchedSkills.length <= 0) {
			return
		}
		return (
			<li className="searched-skills skill-listing">
				<div className="listing-header">Skills you searched for</div>
				<ul className="skills-list">
					{skills
						.filter(skill => searchedSkills.indexOf(skill.name) !== -1)
						.map((skill, i) => {
							return <SkillItem key={i} skill={skill} />
						})}
				</ul>
			</li>
		)
	}

	render() {
		const {
			user: { id, firstName, lastName, title, location, mail, phone, company, authorities },
		} = this.props

		const {
			numberOfSkillsToShow,
			sortedSkills,
			topWills,
			shouldShowAllSkills,
		} = this.state

		const regex = /.*(?=@)/i // matches everything from the email address before the @
		const slackName = mail.match(regex)

		return (
			<ul
				ref={(ref) => { this.node = ref }}
				className={`basic-profile ${this.props.shouldSkillsAnimate
					? 'animateable'
					: ''}`}>
				<li className="info">
					{/* <div className="avatar" style={{backgroundImage: `url('${this.state.avatarUrl}')`}}></div> */}
					<p className="name">
						{firstName} {lastName}
					</p>
					<p className="id">{id}</p>
					<p className="department">{title}</p>
					<p className="roles">
						{
							authorities.length>0 ? 
							'Ruol'+(authorities.length>1 ? 'i' : 'o')+': ' :
							'Nessun ruolo'
						} 
						{authorities.join(", ")}
					</p>
					<p className="location phone">
						{company} - {location} / TEL. {phone}
					</p>
					<div className="social">
						<Link className="mail" href={`mailto:${mail}`} target="_blank">
							<Icon name="mail" size={30} />
						</Link>
						<Link
							className="slack"
							href={`https://sinnerschrader.slack.com/messages/@${slackName}`}
							target="_blank">
							<Icon name="slack" size={30} />
						</Link>
						<Link
							className="move"
							href={`https://move.sinnerschrader.com/?id=${id}`}
							target="_blank">
							<Icon name="location" size={30} />
						</Link>
					</div>
				</li>

				{this.renderSearchedSkills()}

				<TopSkills wills={sortedSkills} />

				<li className="all-skills skill-listing">
					<div className="listing-header">
						Tutte le skill
						<SkillLegend>
							<SkillLegendItem
								title="Nome"
								wide
								handleClickEvent={() =>
									this.setState({
										sortedSkills: this.sortSkills('name', 'asc'),
									})}
							/>
							<div className="skill-legend__item--skills">
								<SkillLegendItem
									title="Livello Skill"
									withTooltip="skill"
									handleClickEvent={() =>
										this.setState({
											sortedSkills: this.sortSkills('skillLevel', 'desc'),
										})}
								/>
								<SkillLegendItem
									title="Livello Will"
									withTooltip="will"
									handleClickEvent={() =>
										this.setState({
											sortedSkills: this.sortSkills('willLevel', 'desc'),
										})}
								/>
							</div>
						</SkillLegend>
					</div>

					<ul className="skills-list">
						{sortedSkills.map((skill, i) => {
							if (i < numberOfSkillsToShow) {
								return (
									<SkillItem
										editSkill={this.props.editSkill}
										deleteSkill={this.props.deleteSkill}
										skill={skill}
										key={skill.name}
									/>
								)
							}
						})}
					</ul>

					{!shouldShowAllSkills && (
						<a className="show-more-link" onClick={this.showAllSkills}>
							Altre Skill
							<Icon className="show-more-link-icon" name="chevron" size={20} />
						</a>
					)}
				</li>
			</ul>
		)
	}
}

function mapStateToProps(state) {
	const { shouldSkillsAnimate, searchedSkills } = state
	return {
		shouldSkillsAnimate,
		searchedSkills
	}
}
export default connect(mapStateToProps, { clearUserData })(BasicProfile)
