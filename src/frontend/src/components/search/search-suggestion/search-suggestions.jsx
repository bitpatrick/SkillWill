import React from 'react'
import { apiServer } from '../../../env.js'
import config from '../../../config.json'
import SuggestionItem from './suggestion-item'
import {
	stopLoading,
	startLoading,
	errorAlertManage
} from '../../../actions'
import { connect } from 'react-redux'

class SearchSuggestions extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			results: [],
			doAutoComplete: true,
			autoCompleteTimeOut: null,
			hint: '',
		}
		this.getAutoCompletion = this.getAutoCompletion.bind(this)
		this.getRecommendations = this.getRecommendations.bind(this)
		this.getResults = this.getResults.bind(this)
		this.resetTimeOut = this.resetTimeOut.bind(this)
		this.getHint = this.getHint.bind(this)
	}

	componentDidMount() {
		this.getResults()
	}
	componentDidUpdate(props) {
		this.getResults(props)
	}

	getResults(props) {
		if (this.props === props) {
			return
		}

		this.resetTimeOut()
		const isCurrentValueSet = this.props.currentValue.length > 0

		if (this.props.variant === 'skill') {
			this.setState({ doAutoComplete: true })

			isCurrentValueSet
				? this.getAutoCompletion(this.props.currentValue)
				: this.setState({ results: [] })

			return
		}

		this.setState({ doAutoComplete: isCurrentValueSet })
		isCurrentValueSet
			? this.getAutoCompletion(this.props.currentValue)
			: this.getRecommendations(this.props.searchTerms)
	}

	getAutoCompletion(searchTerm) {
		this.setState({
			autoCompleteTimeOut: setTimeout(async () => {
				this.props.startLoading();
				await fetch(
					`${apiServer}/skills?count=${config.suggestions
						.count}&search=${encodeURIComponent(searchTerm)}`,
					{
						credentials: 'include',
					}
				)
					.then(res => (res.status === 200 ? res.json() : []))
					.then(data =>
						this.setState({
							results: data,
							autoCompleteTimeOut: null,
							hint: this.getHint(),
						})
					)
					.catch(err => {
						console.log(err.message)
						this.props.errorAlertManage(err.message);
					})
				this.props.stopLoading();
			}, config.suggestions.autoCompleteDelay),
		})
	}

	resetTimeOut() {
		if (this.state.autoCompleteTimeOut !== null) {
			clearTimeout(this.state.autoCompleteTimeOut)
		}
	}

	async getRecommendations(searchTerms) {
		const searchString = searchTerms.map(t => t.trim()).join(',')
		this.props.startLoading();
		await fetch(
			`${apiServer}/skills/next?count=${config.suggestions
				.count}&search=${encodeURIComponent(searchString)}`,
			{
				credentials: 'include',
			}
		)
			.then(res => (res.status === 200 ? res.json() : []))
			.then(data => {
				this.setState({ results: data, hint: this.getHint() });
			})
			.catch(err =>{
				console.log(err.message);
                this.props.errorAlertManage(err.message);
				if(err.status==302){
					let el=document.getElementById('anchor');
					if(el){
						el.click();
					}
				}
			});
		this.props.stopLoading();
	}

	getHint() {
		if (this.state.doAutoComplete) {
			return 'Skill che corrispondono:'
		} else if (this.props.searchTerms.length > 0) {
			return 'Skill Rilevanti:'
		}
		return 'Skill Popolari:'
	}

	render() {
		const { handleSuggestionSelected, variant } = this.props
		const { results, hint } = this.state
		let elem=<a href='http://localhost:1337' id='anchor' className='hidden'>anchor</a>;
		return (
			<div
				className={
					results.length > 0
						? `search-suggestions search-suggestions--${variant}`
						: `search-suggestions search-suggestions--${variant} hidden`
				}>
				<span className="search-suggestions__info">{hint}</span>
				<ul className="search-suggestions-list">
					{results.map((suggestion, i) => (
						<SuggestionItem
							name={suggestion.name}
							handleSuggestionSelected={handleSuggestionSelected}
							key={i}
						/>
					))}
				</ul>
				{elem}
			</div>
		)
	}
}

function mapStateToProps(state) {
	return {}
}

export default connect(mapStateToProps, {
	startLoading,
	stopLoading,
	errorAlertManage
})(SearchSuggestions)
