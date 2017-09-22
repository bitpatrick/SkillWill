import React from 'react'
import SearchBar from './search-bar.jsx'
import Dropdown from '../dropdown/dropdown.jsx'
import config from '../../config.json'
import { getUserBySearchTerms, setLocationFilter } from '../../actions'
import { connect } from 'react-redux'

class UserSearch extends React.Component {
	constructor(props) {
		super(props)

		this.handleDropdownSelect = this.handleDropdownSelect.bind(this)
		this.handleSearchBarInput = this.handleSearchBarInput.bind(this)
		this.handleSearchBarDelete = this.handleSearchBarDelete.bind(this)
	}

	handleSearchBarInput(newSearchTerms) {
		this.props.getUserBySearchTerms(newSearchTerms)
	}

	handleSearchBarDelete(deleteItem) {
		this.props.getUserBySearchTerms(deleteItem, 'delete')
	}

	handleDropdownSelect(location) {
		this.props.setLocationFilter(location)
	}

	render() {
		const { locationFilterOptions } = config
		const { searchTerms, locationFilter, setLocationFilter } = this.props
		return (
			<div className="searchbar">
				<Dropdown
					onDropdownSelect={setLocationFilter}
					dropdownLabel={locationFilter}
					options={locationFilterOptions}
				/>
				<SearchBar
					onInputChange={this.handleSearchBarInput}
					onInputDelete={this.handleSearchBarDelete}
					searchTerms={searchTerms}>
					{/*<SearchSuggestions
						searchTerms={searchItems}
						noResults={results.length === 0} />*/}
				</SearchBar>
			</div>
		)
	}
}
function mapStateToProps(state) {
	return {
		searchTerms: state.searchTerms,
		locationFilter: state.locationFilter,
	}
}

export default connect(mapStateToProps, {
	getUserBySearchTerms,
	setLocationFilter,
})(UserSearch)
