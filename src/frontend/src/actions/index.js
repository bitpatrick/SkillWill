import { apiServer } from '../env.js'

export const ADD_SEARCH_TERMS = 'ADD_SEARCH_TERMS'
export function addSearchTerms(searchTerms) {
	return {
		type: ADD_SEARCH_TERMS,
		payload: searchTerms,
	}
}

export const DELETE_SEARCH_TERM = 'DELETE_SEARCH_TERM'
export function deleteSearchTerms(searchTerm) {
	return {
		type: DELETE_SEARCH_TERM,
		payload: searchTerm,
	}
}

export const SET_LOCATION_FILTER = 'SET_LOCATION_FILTER'
export function setLocationFilter(locationFilter) {
	return {
		type: SET_LOCATION_FILTER,
		payload: locationFilter,
	}
}

export const SET_DIRECTION_FILTER = 'SET_DIRECTION_FILTER'
export function setDirectionFilter(directionFilter) {
	return {
		type: SET_DIRECTION_FILTER,
		payload: directionFilter,
	}
}

export const SET_LAST_SORTED_BY = 'SET_LAST_SORTED_BY'
export function setLastSortedBy(sortFilter, lastSortedBy) {
	return {
		type: SET_LAST_SORTED_BY,
		sortFilter,
		lastSortedBy,
	}
}
export function setSortFilter(criterion) {
	return function(dispatch, getState) {
		let { sortFilter, lastSortedBy } = getState().lastSortedBy
		sortFilter = sortFilter || lastSortedBy
		dispatch(setLastSortedBy(criterion, sortFilter))
	}
}

export const FETCH_RESULTS = 'FETCH_RESULTS'
export async function fetchResults(searchTerms,dispatch) {
	const requestURL = `${apiServer}/users?skills=${encodeURIComponent(
		searchTerms
	)}`
	const options = {
		credentials: 'include',
	}
	const request = await fetch(requestURL, options).then(response => response.json())
	.catch(err=>{
		console.log(err.message)
		dispatch(errorAlertManage(err.message))
	})
	return {
		type: FETCH_RESULTS,
		payload: request,
	}
}

export function getUserBySearchTerms(term, method) {
	return function(dispatch, getState) {
		if (method === 'delete') {
			dispatch(deleteSearchTerms(term))
		} else {
			dispatch(addSearchTerms(term))
		}
		const { searchTerms, lastSortedBy: { lastSortedBy } } = getState()
		dispatch(startLoading())
		dispatch(fetchResults(searchTerms))
		dispatch(stopLoading())
		dispatch(setSortFilter(lastSortedBy))
	}
}

export const ADD_SKILL_SEARCH = 'ADD_SKILL_SEARCH'
export function addSkillSearch(searchTerm) {
	return {
		type: ADD_SKILL_SEARCH,
		payload: searchTerm,
	}
}

export const DELETE_SKILL_SEARCH = 'DELETE_SKILL_SEARCH'
export function deleteSkillSearch(searchTerm) {
	return {
		type: DELETE_SKILL_SEARCH,
		payload: searchTerm,
	}
}

export const FETCH_SKILLS = 'FETCH_SKILLS'
export async function fetchSkills(searchTerm) {
	const requestURL = `${apiServer}/skills?search=${encodeURIComponent(
		searchTerm
	)}`
	const options = {
		credentials: 'include',
	}
	const request = await fetch(requestURL, options).then(response => response.json())
	.catch(err=>{
		console.log(err.message)
		dispatch(errorAlertManage(err.message))
	})
	return {
		type: FETCH_SKILLS,
		payload: request,
	}
}

export function getSkillsBySearchTerm(term, method) {
	return function(dispatch, getState) {
		if (method === 'delete') {
			dispatch(deleteSkillSearch(term))
		} else {
			dispatch(addSkillSearch(term))
		}
		const { skillSearchTerms } = getState()
		dispatch(startLoading())
		dispatch(fetchSkills(skillSearchTerms))
		dispatch(stopLoading())
	}
}

export const REQUEST_PROFILE_DATA = 'REQUEST_PROFILE_DATA'
export const requestProfileData = () => ({
	type: REQUEST_PROFILE_DATA
})

export const RECEIVE_PROFILE_DATA = 'RECEIVE_PROFILE_DATA'
export const receiveProfileData = (payload) => ({
	type: RECEIVE_PROFILE_DATA,
	payload
})

export function getUserProfileData(profile) {
	return async (dispatch) => {
		dispatch(requestProfileData())
		const requestURL = `${apiServer}/users/${profile}`
		const options = {
			credentials: 'include',
		}
		dispatch(startLoading())
		await fetch(requestURL, options)
		.then(response => response.json())
		.then(json => {
			dispatch(receiveProfileData(json))
		})
		.catch(err=>{
			console.log(err.message)
			dispatch(errorAlertManage(err.message))
		})
		dispatch(stopLoading())
	}
}

export const SORT_USER_SKILLS_DESC = 'SORT_USER_SKILLS_DESC'
export function sortUserSkillsDesc(user) {
	return {
		type: SORT_USER_SKILLS_DESC,
		payload: user
	}
}

export const SORT_USER_WILLS_DESC = 'SORT_USER_WILLS_DESC'
export function sortUserWillsDesc(user) {
	return {
		type: SORT_USER_WILLS_DESC,
		payload: user
	}
}

export const SORT_USER_SKILLS_BY_NAME = 'SORT_USER_SKILLS_BY_NAME'
export function sortUserSkillsByName(user) {
	return {
		type: SORT_USER_SKILLS_BY_NAME,
		payload: user
	}
}

export const CLEAR_USER_DATA = 'CLEAR_USER_DATA'
export function clearUserData() {
	return {
		type: CLEAR_USER_DATA,
		payload: {},
	}
}

export const TOGGLE_SKILLS_EDIT_MODE = 'TOGGLE_SKILLS_EDIT_MODE'
export function toggleSkillsEditMode() {
	return {
		type: TOGGLE_SKILLS_EDIT_MODE,
	}
}
export const EXIT_SKILLS_EDIT_MODE = 'EXIT_SKILLS_EDIT_MODE'
export function exitSkillsEditMode() {
	return {
		type: EXIT_SKILLS_EDIT_MODE,
	}
}

export const EDIT_SKILL = 'EDIT_SKILL'
export async function editSkill(requestURL, options) {
	const request = await fetch(requestURL, options).then(response => response.json())
	return {
		type: EDIT_SKILL,
		payload: request,
	}
}

export function updateUserSkills(options, user) {
	const requestURL = `${apiServer}/users/${user}/skills`
	return function(dispatch) {
		dispatch(startLoading())
		dispatch(editSkill(requestURL, options)).then(() =>{
			dispatch(getUserProfileData(user))
			dispatch(stopLoading())
		})
		.catch(err=>{
			console.log(err.message)
			dispatch(errorAlertManage(err.message))
			dispatch(stopLoading())
		})
	}
}

export const START_ANIMATING = 'START_ANIMATING'
export function startAnimating() {
	return {
		type: START_ANIMATING,
	}
}

export const STOP_ANIMATING = 'STOP_ANIMATING'
export function stopAnimating() {
	return {
		type: STOP_ANIMATING,
	}
}

export const START_LOADING = 'START_LOADING'
export function startLoading() {
	return {
		type: START_LOADING,
	}
}

export const STOP_LOADING = 'STOP_LOADING'
export function stopLoading() {
	return {
		type: STOP_LOADING,
	}
}

export const ERROR_ALERT = 'ERROR_ALERT'
export function errorAlert(message){
	return {
		type: ERROR_ALERT,
		message: message
	}
}

export const ERROR_HIDE = 'ERROR_HIDE'
export function errorHide(){
	return {
		type: ERROR_HIDE
	}
}

export function errorAlertManage(message){
	return function(dispatch){
		dispatch(errorAlert(message));
		setTimeout(()=>{
			dispatch(errorHide());
		},3000);
	}
}

export const REQUEST_CURRENT_USER = 'REQUEST_CURRENT_USER'
export function requestCurrentUser() {
	return {
		type: REQUEST_CURRENT_USER
	}
}

export const RECEIVE_CURRENT_USER = 'RECEIVE_CURRENT_USER'
export function receiveCurrentUser(payload) {
	return {
		type: RECEIVE_CURRENT_USER,
		payload
	}
}

export const SET_COMPANY_FILTER = 'SET_COMPANY_FILTER'
export function setCompanyFilter(filter) {
	return {
		type: SET_COMPANY_FILTER,
		filter
	}
}

export const REDIRECT_LOGIN = 'REDIRECT_LOGIN'
export function redirectLogin(check) {
	return {
		type: REDIRECT_LOGIN,
		check: check
	}
}

export function fetchCurrentUser() {
	return async function(dispatch){
		dispatch(requestCurrentUser())
		dispatch(startLoading())
		const requestURL = `${apiServer}/session/user`
		await fetch(requestURL, {credentials: 'include', redirect: 'manual'})
		.then(res => {
			return res.json();
		})
		.then(user => {
			console.log(user)
			user['id']=user['username']
			if(!user['skills']) user['skills']=[]
			if(!user['mail']) user['mail']=''
			dispatch(receiveCurrentUser(user))
		})
		.catch(err=>{
			console.log(err.message)
			dispatch(errorAlertManage(err.message))
		})
		dispatch(stopLoading())
	}
}
