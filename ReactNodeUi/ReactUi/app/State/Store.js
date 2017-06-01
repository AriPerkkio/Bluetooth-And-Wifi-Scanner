var { createStore } = require('redux');

var { resultReducer } = require('./Reducers');

const Store = createStore(resultReducer);

module.exports = Store;