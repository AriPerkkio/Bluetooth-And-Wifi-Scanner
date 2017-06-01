var React       = require('react');
var Route       = require('react-router-dom').Route;

var ResultsContainer = require('./ResultContainer');
var MapContainer     = require('./MapContainer');
var MainContainer    = require('./MainContainer');
var ComponentTester  = require('./ComponentTester');

class Container extends React.Component {
  render() {
    return (
      <main>
        <Route exact path='/' component={MainContainer} />
        <Route path="/results/wifi"      render={() => <ResultsContainer path='/getAllWifi' resultType='Wifi'      />} />
        <Route path="/results/bt"        render={() => <ResultsContainer path='/getAllBt'   resultType='Bluetooth' />} />
        <Route path="/results/"          render={() => <MapContainer />} />
        <Route path="/component-tester/" render={() => <ComponentTester /> } />
      </main>
    )
  }
}

module.exports = Container;