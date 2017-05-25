var React       = require('react');
var ReactRouter = require('react-router-dom');
var Router      = ReactRouter.BrowserRouter;
var Route       = ReactRouter.Route;
var Switch      = ReactRouter.Switch;
var PropTypes   = require('prop-types');


var WifiResultsContainer      = require('./WifiResultsContainer');
var BluetoothResultsContainer = require('./BluetoothResultsContainer');
var MapComponent              = require('./Map');
var MainContainer             = require('./MainContainer');

class Container extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      selectedResult: null
    }
  }

  onResultClick(result) {
    this.setState({
      selectedResult: result
    })
  }

  render() {
    return (
      <main>
        <Route exact path='/' component={MainContainer} />
        <Route path="/results/wifi" render={() => <WifiResultsContainer      onResultClick={this.onResultClick.bind(this)} />} />
        <Route path="/results/bt"   render={() => <BluetoothResultsContainer onResultClick={this.onResultClick.bind(this)} />} />
        <Route path="/results/"     render={() => <MapComponent markedObj={this.state.selectedResult} />} />
      </main>
    )
  }
}

module.exports = Container;