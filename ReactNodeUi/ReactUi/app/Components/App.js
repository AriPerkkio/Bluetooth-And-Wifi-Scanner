var React          = require('react');
var ReactRouter    = require('react-router-dom');
var Router         = ReactRouter.BrowserRouter;
var browserHistory = ReactRouter.browserHistory;

var Header    = require('./Header');
var Container = require('./Container');
var Footer    = require('./Footer');

class App extends React.Component {
  render() {
    return (
      <Router history={browserHistory}>
        <div id='app-wrapper'>
          <Header />
          <Container />
          <Footer />
        </div>
      </Router>
    )
  }
}

module.exports = App;