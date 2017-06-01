var TempUtil  = require('../Utils/TempUtil');
var ArrayUtil = require('../Utils/ArrayUtil');

const initialResultstate = {
  selectedResults: [],
  counts: {
    wifi: null,
    bluetooth: null
  }
};

module.exports = {
  resultReducer: (state = initialResultstate, action) => {
    switch(action.type) {
      case 'SELECT_RESULT':
        var result = Object.assign({}, action.result, { tech: TempUtil.getResultType(action.result) });
        var newResults = ArrayUtil.includesResult(state.selectedResults, result) ?
                         state.selectedResults :
                         state.selectedResults.concat(result);
        return Object.assign({}, state, {selectedResults: newResults});
      case 'SET_COUNT':
        var newCount = Object.assign({}, state.counts, action.resultCount);
        return Object.assign({}, state, {counts: newCount});
      default:
        return state;
    }
  }
}