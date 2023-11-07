import React from 'react'
import SkillItem from '../skill-item/skill-item'

const TopSkills = props => {
	return (
		<li className="top-wills skill-listing">
			<div className="listing-header">Skill Migliori</div>
			<ul className="skills-list">
				{props.wills.map((skill, i) => {
					if (i < 5 && skill['skillLevel'] > 1) {
						return <SkillItem skill={skill} key={skill.name} />
					}
				})}
			</ul>
		</li>
	)
}

export default TopSkills
