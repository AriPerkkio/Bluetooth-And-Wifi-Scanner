var TempUtil = require('../Utils/TempUtil');

module.exports = {
  sortByName(a, b) {
    var nameA = a.name.toUpperCase().trim();
    var nameB = b.name.toUpperCase().trim();
    return nameA === '' || nameA === ' ' ? 1 :
           nameB === '' || nameB === ' ' ? -1 :
           nameA < nameB ? -1 :
           nameA > nameB ? 1 :
           0;
  },

  // Matching all keys is enough
  includesResult(array, result) {
    return !array.every( arrayResult =>
      !Object.keys(result)
        .every( key =>
          arrayResult[key] === result[key]))
  }
}