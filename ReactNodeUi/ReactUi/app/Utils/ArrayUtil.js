module.exports = {
  sortByName(a, b) {
    var nameA = a.name.toUpperCase().trim();
    var nameB = b.name.toUpperCase().trim();
    return nameA === '' || nameA === ' ' ? 1 :
           nameB === '' || nameB === ' ' ? -1 :
           nameA < nameB ? -1 :
           nameA > nameB ? 1 :
           0;
  }
}