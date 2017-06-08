import React from 'react'
import ReactDOM from 'react-dom'
import config from '../../config.json'
import getStateObjectFromURL from '../../utils/getStateObjectFromURL'
import User from '../user/user'
import { browserHistory } from 'react-router'
import { connect } from 'react-redux'

class Results extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			lastSortedBy: 'fitness'
		}
		this.scrollToResults = this.scrollToResults.bind(this)
		this.sortResults = this.sortResults.bind(this)
		this.filterUserByLocation = this.filterUserByLocation.bind(this)
		this.removeAnimationClass = this.removeAnimationClass.bind(this)
	}

	componentDidMount() {
		ReactDOM.findDOMNode(this).addEventListener('animationend', this.removeAnimationClass)
	}

	removeAnimationClass() {
		ReactDOM.findDOMNode(this).classList.remove('animateable')
		ReactDOM.findDOMNode(this).removeEventListener('animationend', this.removeAnimationClass)
	}

	scrollToResults() {
		const searchbarRect = document.querySelector('.searchbar').getBoundingClientRect()
		window.scrollBy({ top: `${searchbarRect.top - 10}`, behavior: "smooth" })
	}

	sortResults(criterion) {
		const { results: { user } } = this.props
		if (this.state.lastSortedBy === criterion) {
			user.reverse()
		} else if (criterion === 'fitness') {
			user.sort((a, b) => {
				return a[criterion] > b[criterion] ? -1 : 1
			})
		} else {
			user.sort((a, b) => {
				return a[criterion] < b[criterion] ? -1 : 1
			})
		}

		this.setState({
			lastSortedBy: criterion,
		})
	}

	filterUserByLocation(user) {
		const { locationFilter } = this.props
		if (locationFilter === 'all') {
			return true
		} else {
			return user.location === locationFilter
		}
	}

	render() {
		const { locationFilter, results: { user, searched } } = this.props
		if (user && user.length > 0) {
			const filteredUser = user.filter(this.filterUserByLocation)
			return (
				<div class="results-container animateable">
					<a class="counter" onClick={this.scrollToResults}>
						<span>{filteredUser.length} Ergebnisse</span>
					</a>
					<ul class="results">
						<ul class="sort-buttons">
							<li class="sort-button sort-button-name" onClick={() => this.sortResults('lastName')}>
								<span class="sort-button-label">Sort by Name</span>
							</li>
							<li class="sort-button sort-button-location" onClick={() => this.sortResults('location')}>
								<span class="sort-button-label">Sort by Location</span>
							</li>
							<li class="sort-button sort-button-fitness" onClick={() => this.sortResults('fitness')}>
								<span class="sort-button-label">Sort by Match</span>
							</li>
						</ul>
						{filteredUser.map((user, i) => {
							return (
								<li class="result-item" key={i}>
									<User user={user} searchTerms={searched} />
								</li>
							)
						})}
					</ul>
				</div>
			)
		}
		else {
			return (
				<div class="results-container" data-isEmptyLabel={this.props.noResultsLabel}></div>
			)
		}
	}
}

function mapStateToProps(state) {
	return {
		results: state.results,
		searchTerms: state.searchTerms,
		locationFilter: state.locationFilter
	}
}
export default connect(mapStateToProps)(Results)