import './tooltip.less'
import React from 'react'
import Icon from '../icon/icon.jsx'

const className = 'tooltip'

class Tooltip extends React.Component {
	constructor(props) {
		super(props)
	}
	render() {
		const { variant } = this.props
		return (
			<div className={`${className} ${className}--${variant}`}>
				<Icon name={`${variant}-tooltip`} size={19} />
				<div className={`${className}-content`}>
					{variant === 'skill' && (
						<div className={`${className}-legend-wrapper`}>
							<div className={`${className}-legend-item`}>
								<div className="level">
									<div className={`skillBar levelBar levelBar--zero`} />
								</div>
								<span className={`${className}-legend-name`}>N/A</span>
							</div>
							<div className={`${className}-legend-item`}>
								<div className="level">
									<div className={`skillBar levelBar levelBar--1`} />
								</div>
								<span className={`${className}-legend-name`}>Basico</span>
							</div>
							<div className={`${className}-legend-item`}>
								<div className="level">
									<div className={`skillBar levelBar levelBar--2`} />
								</div>
								<span className={`${className}-legend-name`}>Intermedio</span>
							</div>
							<div className={`${className}-legend-item`}>
								<div className="level">
									<div className={`skillBar levelBar levelBar--3`} />
								</div>
								<span className={`${className}-legend-name`}>Esperto</span>
							</div>
						</div>
					)}

					{variant === 'will' && (
						<div className={`${className}-legend-wrapper`}>
							<div className={`${className}-legend-item`}>
								<span className={`${className}-legend-name`}>Super Alta</span>
								<div className="level">
									<div className={`willBar levelBar levelBar--3`} />
								</div>
							</div>
							<div className={`${className}-legend-item`}>
								<span className={`${className}-legend-name`}>Alta</span>
								<div className="level">
									<div className={`willBar levelBar levelBar--2`} />
								</div>
							</div>
							<div className={`${className}-legend-item`}>
								<span className={`${className}-legend-name`}>Ok</span>
								<div className="level">
									<div className={`willBar levelBar levelBar--1`} />
								</div>
							</div>
							<div className={`${className}-legend-item`}>
								<span className={`${className}-legend-name`}>Nessuna</span>
								<div className="level">
									<div className={`willBar levelBar levelBar--zero`} />
								</div>
							</div>
						</div>
					)}
				</div>
			</div>
		)
	}
}

export default Tooltip
