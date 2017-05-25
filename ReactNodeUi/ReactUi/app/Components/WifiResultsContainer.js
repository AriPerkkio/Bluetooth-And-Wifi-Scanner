var React     = require('react');
var PropTypes = require('prop-types');

var Table     = require('./Table');
var Content   = require('./Content');
var Loading   = require('./Loading');
var ArrayUtil = require('../Utils/ArrayUtil');

class WifiResultsContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      wifiResults: null
    };
  }

  componentDidMount() {
    fetch('http://localhost:8081/getAllWifi')
      .then((response) => response.json())
      .then((responseJson) => this.setState({ 'wifiResults' : responseJson.sort(ArrayUtil.sortByName) }))
      .catch((error) => console.log(error));
  }

  render() {
    return (
      <Content>
        { this.state.wifiResults ?
          <Table
            onResultClick={this.props.onResultClick}
            data={this.state.wifiResults}
            columns={Object.keys(this.state.wifiResults[0])}
            title="Wifi Results" /> :
          <Loading text="Loading Wifi results..."/>
        }
      </Content>
    )
  }
}



WifiResultsContainer.propTypes = {
  onResultClick: PropTypes.func.isRequired
};

module.exports = WifiResultsContainer;