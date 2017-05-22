var React = require('react');
var ReactDOM = require('react-dom');
require('./index.css');

var Header    = require('./Components/Header');
var Container = require('./Components/Container');
var Footer    = require('./Components/Footer');

class App extends React.Component {
  render() {
    return (
      <div id='app-wrapper'>
        <Header />
        <Container />
        <Footer />
      </div>
    )
  }
}

ReactDOM.render(
  <App />,
  document.getElementById('app')
);