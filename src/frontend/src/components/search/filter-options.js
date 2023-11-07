function option(value, display) {
  display = display == undefined ? value : display
  return {
    value,
    display
  }
}

const allLocations = option('all', 'Tutte le località')

const allCompanies = option('all', 'Tutte le aziende')

const hamburg = option('Hamburg')

const frankfurt = option('Frankfurt')

const munich = option('München')

const berlin = option('Berlin')

const prague = option('Prag')

export const companiesFilterOptions = (list = []) =>{
  let vet=[];
  vet.push(allCompanies)
  for(let l of list){
    vet.push(option(l))
  }
  return vet
//   [
//   allCompanies,
//   option('S2 Germany'),
//   option('S2 Swipe'),
//   option('S2 Commerce'),
//   option('S2 Content'),
//   option('S2 AG')
// ]
}

export const locationOptionsForCompany = (list = []) => {
  let vet=[];
  vet.push(allLocations)
  for(let l of list){
    vet.push(option(l))
  }
  return vet
  // switch(company) {
  //   case 'S2 Germany':
  //     return [allLocations, hamburg, frankfurt, munich, prague, berlin]
  //   case 'S2 Swipe':
  //     return [allLocations, hamburg, berlin]
  //   case 'S2 Commerce':
  //     return [allLocations, hamburg, prague]
  //   case 'S2 Content':
  //     return [allLocations, hamburg]
  //   case 'S2 AG':
  //     return [allLocations, hamburg]
  //   default:
  //     return [allLocations, hamburg, frankfurt, munich, prague, berlin]
  // }
}
