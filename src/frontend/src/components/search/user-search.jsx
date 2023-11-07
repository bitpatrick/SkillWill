import React from 'react'
import SearchBar from './search-bar.jsx'
import Dropdown from '../dropdown/dropdown.jsx'
import config from '../../config.json'
import { getUserBySearchTerms, setLocationFilter, setCompanyFilter,
	fetchCurrentUser, startLoading, stopLoading, errorAlertManage } from '../../actions'
import { connect } from 'react-redux'
import { companiesFilterOptions, locationOptionsForCompany } from './filter-options'
import { option } from './filter-options.js'
import { apiServer } from "../../env";
class UserSearch extends React.Component {
	constructor(props) {
		super(props)

		this.state = {
			locationsFilter: [],
			companiesFilter: [],
		}

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

	async componentDidMount(){
		const options = { 
            method: 'GET', 
            credentials: 'include',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
        };
		const requestURL = `${apiServer}/locations`
        this.props.startLoading();
		await fetch(requestURL,options)
			.then(response => response.json()).then(json=>{
				this.setState({
					locationsFilter: json
				})
			})
			.catch(err =>{
                console.log(err.message)
                this.props.errorAlertManage(err.message);
            })
        this.props.stopLoading();
		const requestURL2 = `${apiServer}/companies`
        this.props.startLoading();
		await fetch(requestURL2,options)
			.then(response => response.json()).then(json=>{
				this.setState({
					companiesFilter: json
				}, ()=>{
					console.log(this.state)
				})
			})
			.catch(err =>{
                console.log(err.message)
                this.props.errorAlertManage(err.message);
            })
        this.props.stopLoading();
	}

	render() {
		// const { locationFilterOptions } = config
		const { locationsFilter, companiesFilter } = this.state
		const { searchTerms, locationFilter, companyFilter, setLocationFilter, setCompanyFilter } = this.props
		return (
			<div className="searchbar">
				<Dropdown
					onDropdownSelect={setCompanyFilter}
					dropdownLabel={companyFilter}
					options={companiesFilterOptions(companiesFilter)}
				/>
				<Dropdown
					onDropdownSelect={setLocationFilter}
					dropdownLabel={locationFilter}
					options={locationOptionsForCompany(locationsFilter)}
				/>
				<SearchBar
					variant="user"
					onInputChange={this.handleSearchBarInput}
					onInputDelete={this.handleSearchBarDelete}
					searchTerms={searchTerms}
				/>
			</div>
		)
	}
}
function mapStateToProps(state) {
	return {
		searchTerms: state.searchTerms,
		locationFilter: state.locationFilter,
		companyFilter: state.companyFilter
	}
}

export default connect(mapStateToProps, {
	getUserBySearchTerms,
	setLocationFilter,
	setCompanyFilter,
    fetchCurrentUser,
    startLoading,
    stopLoading,
    errorAlertManage
})(UserSearch)
