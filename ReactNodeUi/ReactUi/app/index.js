var React = require('react');
var ReactDOM = require('react-dom');
var { Provider } = require('react-redux');

var App = require('./Components/App');
var Store = require('./State/Store');
require('./index.css');

ReactDOM.render(
  <Provider store={Store}>
    <App/>
  </Provider>,
  document.getElementById('app')
);