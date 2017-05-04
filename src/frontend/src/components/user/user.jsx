import React from 'react'
import { Router, Route, Link, browserHistory } from 'react-router'
import Levels from '../level/level.jsx'

export default class User extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			skillsToShow: []
		}

		const {skills} = this.props.data
		skills.map((skill) => {
			const indexOfSearchedSkill = this.props.searchTerms.indexOf(skill.name)

			if (indexOfSearchedSkill > -1) {
				this.setState({
					skillsToShow: this.state.skillsToShow.concat([skill])
				})
			}
		})
		this.handleClick = this.handleClick.bind(this)
	}

	handleClick() {
		document.body.classList.add('layer-open')
	}

	render() {
		return (
			<ul class="user">
				<li class="info">
					<Link
						class="name"
						to={`/profile/${this.props.data.id}`}
						activeClassName="active"
						id={`${this.props.data.id}`}
						onClick={this.handleClick}>
						{`${this.props.data.firstName} ${this.props.data.lastName}`}
					</Link>
					<span class="id">{this.props.data.id}</span>
					<span class="department">{this.props.data.title}</span>
				</li>
				<li class="location">{this.props.data.location}</li>
				<li class="skills">
					<ul class="skills-list">
						{this.state.skillsToShow.map((data, i) => {
							return (
								<li key={i} class="skill-item">
									<Levels skill={data} />
								</li>
							)
						})}
					</ul>
				</li>
			</ul>
		)
	}
}
