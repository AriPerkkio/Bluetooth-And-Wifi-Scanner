var React     = require('react');
var PropTypes = require('prop-types');

var Table     = require('./Table');
var Content   = require('./Content');
var Loading   = require('./Loading');
var ArrayUtil = require('../Utils/ArrayUtil');

class BluetoothResultsContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      bluetoothResults: null
    };
  }

  componentDidMount() {
    fetch('http://localhost:8081/getAllBt')
      .then((response) => response.json())
      .then((responseJson) => this.setState({ 'bluetoothResults' : responseJson.sort(ArrayUtil.sortByName) }))
      .catch((error) => console.log(error));
  }

  render() {
    return (
      <Content>
        { this.state.bluetoothResults ?
          <Table
            onResultClick={this.props.onResultClick}
            data={this.state.bluetoothResults}
            columns={Object.keys(this.state.bluetoothResults[0])}
            title="Bluetooth Results" /> :
            <Loading text="Loading Bluetooth results..."/>
        }
      </Content>
    )
  }
}

BluetoothResultsContainer.propTypes = {
  onResultClick: PropTypes.func.isRequired
};

module.exports = BluetoothResultsContainer;